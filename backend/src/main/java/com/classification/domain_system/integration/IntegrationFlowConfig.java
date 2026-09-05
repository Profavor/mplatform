package com.classification.domain_system.integration;

import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.event.MasterDataChangedEvent;
import com.classification.domain_system.repository.IntegrationChannelRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpMethod;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.event.inbound.ApplicationEventListeningMessageProducer;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;
import org.springframework.integration.http.outbound.HttpRequestExecutingMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class IntegrationFlowConfig {

    private final IntegrationChannelRepository channelRepository;
    private final DataMappingTransformer mappingTransformer;
    private final IntegrationLogService logService;
    private final JdbcDynamicExecutionService jdbcService;
    private final WebServiceDynamicExecutionService wsService;
    private final MessageQueueDynamicExecutionService mqService;
    private final ObjectMapper mapper = new ObjectMapper();

    @Bean
    public MessageChannel masterDataChangedChannel() {
        return new org.springframework.integration.channel.ExecutorChannel(integrationTaskExecutor());
    }

    @Bean
    public java.util.concurrent.Executor integrationTaskExecutor() {
        org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor executor = new org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("mdm-integration-");
        executor.initialize();
        return executor;
    }

    @Bean
    public MessageChannel integrationProcessingChannel() {
        return new DirectChannel();
    }

    @Bean
    public ApplicationEventListeningMessageProducer eventProducer() {
        ApplicationEventListeningMessageProducer producer = new ApplicationEventListeningMessageProducer();
        producer.setEventTypes(MasterDataChangedEvent.class);
        producer.setOutputChannel(masterDataChangedChannel());
        return producer;
    }

    @Bean
    public IntegrationFlow mainIntegrationFlow() {
        return IntegrationFlow.from(masterDataChangedChannel())
                // 1. Splitter: Active 채널 중 현재 이벤트의 nodeId와 일치하는(혹은 널인) 채널로 복제
                .split(Message.class, message -> {
                    MasterDataChangedEvent event = (MasterDataChangedEvent) message.getPayload();
                    List<IntegrationChannel> activeChannels = channelRepository.findByIsActiveTrue();
                    
                    return activeChannels.stream()
                        .filter(channel -> channel.getDirection() == null || "OUTBOUND".equalsIgnoreCase(channel.getDirection()))
                        .filter(channel -> channel.getNodeId() == null || channel.getNodeId().equals(event.getNodeId()))
                        .map(channel -> 
                            MessageBuilder.withPayload(event.getPayloadJson())
                            .copyHeaders(message.getHeaders())
                            .setHeader("RECORD_ID", event.getRecordId())
                            .setHeader("EVENT_TYPE", event.getChangeType())
                            .setHeader("CHANNEL_ID", channel.getId())
                            .setHeader("CHANNEL_TYPE", channel.getType())
                            .setHeader("MAPPING_CONFIG", channel.getMappingConfigJson())
                            .setHeader("CHANNEL_CONFIG", channel.getConfigJson())
                            .setHeader("ORIGINAL_PAYLOAD", event.getPayloadJson())
                            .build()
                    ).collect(Collectors.toList());
                })
                .channel(integrationProcessingChannel())
                .get();
    }

    @Bean
    public IntegrationFlow processingFlow() {
        return IntegrationFlow.from(integrationProcessingChannel())
                // 2. Transformer: SpEL 기반 맵핑 변환
                .transform(Message.class, mappingTransformer::transform)
                // 3. Router: 채널 타입(WEB_SERVICE, JDBC, MESSAGE_QUEUE)에 따라 분기
                .route("headers['CHANNEL_TYPE']",
                    mapping -> mapping
                        .subFlowMapping("WEB_SERVICE", sf -> sf.handle(webServiceOutboundHandler(), e -> e.advice(retryAdvice())))
                        .subFlowMapping("JDBC", sf -> sf.handle(jdbcOutboundHandler(), e -> e.advice(retryAdvice())))
                        .subFlowMapping("MESSAGE_QUEUE", sf -> sf.handle(mqOutboundHandler(), e -> e.advice(retryAdvice())))
                        .defaultOutputToParentFlow() // 처리 불가 타입은 무시
                )
                // 4. 성공 로깅 (Error가 발생하지 않고 끝까지 온 경우)
                .handle(message -> {
                    UUID channelId = message.getHeaders().get("CHANNEL_ID", UUID.class);
                    UUID recordId = message.getHeaders().get("RECORD_ID", UUID.class);
                    String eventType = message.getHeaders().get("EVENT_TYPE", String.class);
                    String origPayload = message.getHeaders().get("ORIGINAL_PAYLOAD", String.class);
                    String mappedPayload = (String) message.getPayload(); // HTTP gateway returns response payload, but actually we want to log the mapped payload. 
                    // Let's rely on the Request payload header if we want to log what was sent.
                    // For now, let's assume the outbound adapter returns the response or we just log success.
                    // Actually HttpRequestExecutingMessageHandler returns the ResponseEntity.
                    // To get the mappedPayload that was sent, we can look at headers if we saved it, or just log success.
                    logService.logSuccess(channelId, recordId, eventType, origPayload, "Success");
                })
                .get();
    }

    @Bean
    public org.springframework.integration.core.GenericHandler<String> webServiceOutboundHandler() {
        return (payload, headers) -> {
            String configJson = headers.get("CHANNEL_CONFIG", String.class);
            try {
                wsService.executeWebService(configJson, payload);
            } catch (Exception e) {
                throw new RuntimeException("WEB_SERVICE Execution failed: " + e.getMessage(), e);
            }
            return payload; // return payload so logging can log it
        };
    }

    @Bean
    public org.springframework.integration.core.GenericHandler<String> jdbcOutboundHandler() {
        return (payload, headers) -> {
            String configJson = headers.get("CHANNEL_CONFIG", String.class);
            try {
                jdbcService.executeUpsert(configJson, payload);
            } catch (Exception e) {
                throw new RuntimeException("JDBC Execution failed: " + e.getMessage(), e);
            }
            return payload;
        };
    }

    @Bean
    public org.springframework.integration.core.GenericHandler<String> mqOutboundHandler() {
        return (payload, headers) -> {
            String configJson = headers.get("CHANNEL_CONFIG", String.class);
            try {
                mqService.executeMq(configJson, payload);
            } catch (Exception e) {
                throw new RuntimeException("MESSAGE_QUEUE Execution failed: " + e.getMessage(), e);
            }
            return payload;
        };
    }

    @Bean
    public MessageChannel integrationErrorChannel() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow integrationErrorFlow() {
        return IntegrationFlow.from(integrationErrorChannel())
                .handle(message -> {
                    if (message.getPayload() instanceof org.springframework.messaging.MessagingException) {
                        org.springframework.messaging.MessagingException exception = (org.springframework.messaging.MessagingException) message.getPayload();
                        Message<?> failedMessage = exception.getFailedMessage();
                        if (failedMessage != null) {
                            UUID channelId = failedMessage.getHeaders().get("CHANNEL_ID", UUID.class);
                            UUID recordId = failedMessage.getHeaders().get("RECORD_ID", UUID.class);
                            String eventType = failedMessage.getHeaders().get("EVENT_TYPE", String.class);
                            String origPayload = failedMessage.getHeaders().get("ORIGINAL_PAYLOAD", String.class);
                            String mappedPayload = (String) failedMessage.getPayload();
                            
                            Throwable rootCause = exception;
                            while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                                rootCause = rootCause.getCause();
                            }
                            String errorMessage = rootCause.toString();

                            StringBuilder sbTrace = new StringBuilder();
                            for (StackTraceElement element : exception.getStackTrace()) {
                                sbTrace.append(element.toString()).append("\n");
                            }
                            String stackTrace = sbTrace.toString();

                            // Retry count is harder to extract here, let's just use 3
                            logService.logError(channelId, recordId, eventType, origPayload, mappedPayload, errorMessage, stackTrace, 3);
                        }
                    }
                })
                .get();
    }

    @Bean
    public RequestHandlerRetryAdvice retryAdvice() {
        RequestHandlerRetryAdvice advice = new RequestHandlerRetryAdvice();
        RetryTemplate retryTemplate = new RetryTemplate();
        
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(2000); // 2 seconds
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        advice.setRecoveryCallback(new org.springframework.integration.handler.advice.ErrorMessageSendingRecoverer(integrationErrorChannel()));
        
        return advice;
    }
}
