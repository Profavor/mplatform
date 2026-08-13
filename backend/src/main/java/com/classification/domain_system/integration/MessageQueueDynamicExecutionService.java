package com.classification.domain_system.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Service;
import jakarta.annotation.PreDestroy;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 동적으로 Kafka 또는 RabbitMQ 프로듀서를 생성하여 메시지를 전송하는 서비스.
 * 채널 설정의 broker URL 스키마(kafka://, amqp://)에 따라 자동으로 분기합니다.
 */
@Slf4j
@Service
public class MessageQueueDynamicExecutionService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 브로커별 KafkaTemplate 캐시 (동일 브로커에 대한 중복 생성 방지)
    private final Map<String, KafkaTemplate<String, String>> kafkaTemplateCache = new ConcurrentHashMap<>();

    // 브로커별 RabbitTemplate 캐시
    private final Map<String, AmqpTemplate> rabbitTemplateCache = new ConcurrentHashMap<>();

    /**
     * 채널 설정 JSON과 변환된 페이로드를 받아 적절한 MQ 브로커로 메시지를 발송합니다.
     *
     * @param configJson 채널의 configJson (broker, topic 포함)
     * @param payload    SpEL 변환이 완료된 JSON 페이로드
     */
    public void executeMq(String configJson, String payload) {
        try {
            JsonNode config = objectMapper.readTree(configJson);
            String broker = config.path("broker").asText();
            String topic = config.path("topic").asText();

            if (broker == null || broker.isBlank()) {
                throw new IllegalArgumentException("Broker URL is not configured.");
            }
            if (topic == null || topic.isBlank()) {
                throw new IllegalArgumentException("Topic/Queue name is not configured.");
            }

            if (broker.startsWith("kafka://")) {
                sendToKafka(broker, topic, payload);
            } else if (broker.startsWith("amqp://")) {
                sendToRabbit(broker, topic, payload);
            } else {
                throw new IllegalArgumentException("Unsupported MQ protocol: " + broker
                        + " (Please use kafka:// or amqp:// scheme)");
            }
        } catch (Exception e) {
            log.error("Failed to send MQ: {}", e.getMessage(), e);
            throw new RuntimeException("MESSAGE_QUEUE Execution failed: " + e.getMessage(), e);
        }
    }

    // ──────────────────────────────────────────────
    // Kafka
    // ──────────────────────────────────────────────

    private void sendToKafka(String brokerUrl, String topic, String payload) {
        // kafka://localhost:9092 → localhost:9092
        String bootstrapServers = brokerUrl.replaceFirst("^kafka://", "");
        log.info("[Kafka] Sending to broker={}, topic={}", bootstrapServers, topic);

        KafkaTemplate<String, String> template = kafkaTemplateCache.computeIfAbsent(bootstrapServers, this::createKafkaTemplate);
        template.send(topic, payload);
        log.info("[Kafka] Message sent successfully to topic={}", topic);
    }

    private KafkaTemplate<String, String> createKafkaTemplate(String bootstrapServers) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // 전송 실패 시 빠르게 알 수 있도록 타임아웃 설정
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5000);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 5000);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 10000);

        ProducerFactory<String, String> factory = new DefaultKafkaProducerFactory<>(props);
        return new KafkaTemplate<>(factory);
    }

    // ──────────────────────────────────────────────
    // RabbitMQ
    // ──────────────────────────────────────────────

    private void sendToRabbit(String brokerUrl, String routingKey, String payload) {
        log.info("[RabbitMQ] Sending to broker={}, routingKey={}", brokerUrl, routingKey);

        AmqpTemplate template = rabbitTemplateCache.computeIfAbsent(brokerUrl, this::createRabbitTemplate);
        // 기본 exchange("")에 routingKey(=큐 이름)으로 전송
        template.convertAndSend(routingKey, payload);
        log.info("[RabbitMQ] Message sent successfully to routingKey={}", routingKey);
    }

    private AmqpTemplate createRabbitTemplate(String brokerUrl) {
        try {
            // amqp://localhost:5672 → host=localhost, port=5672
            URI uri = new URI(brokerUrl);
            String host = uri.getHost() != null ? uri.getHost() : "localhost";
            int port = uri.getPort() > 0 ? uri.getPort() : 5672;

            CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, port);

            // URI에 userinfo가 포함된 경우 (amqp://guest:guest@localhost:5672)
            String userInfo = uri.getUserInfo();
            if (userInfo != null && userInfo.contains(":")) {
                String[] parts = userInfo.split(":", 2);
                connectionFactory.setUsername(parts[0]);
                connectionFactory.setPassword(parts[1]);
            }

            connectionFactory.getRabbitConnectionFactory().setConnectionTimeout(5000);

            RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
            rabbitTemplate.setReplyTimeout(5000);
            return rabbitTemplate;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create RabbitMQ ConnectionFactory: " + e.getMessage(), e);
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Closing all cached MessageQueue templates...");
        kafkaTemplateCache.values().forEach(t -> {
            try { 
                ProducerFactory<String, String> factory = t.getProducerFactory();
                if (factory instanceof org.springframework.beans.factory.DisposableBean disposableFactory) {
                    disposableFactory.destroy();
                }
            } catch (Exception e) { 
                log.warn("Failed to destroy KafkaTemplate: {}", e.getMessage()); 
            }
        });
        kafkaTemplateCache.clear();

        rabbitTemplateCache.values().forEach(t -> {
            if (t instanceof RabbitTemplate rt && rt.getConnectionFactory() instanceof CachingConnectionFactory ccf) {
                try { 
                    ccf.destroy(); 
                } catch (Exception e) { 
                    log.warn("Failed to destroy RabbitMQ ConnectionFactory: {}", e.getMessage()); 
                }
            }
        });
        rabbitTemplateCache.clear();
    }
}
