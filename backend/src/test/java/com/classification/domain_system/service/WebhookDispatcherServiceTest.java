package com.classification.domain_system.service;

import com.classification.domain_system.dto.WebhookDto;
import com.classification.domain_system.entity.WebhookSubscription;
import com.classification.domain_system.repository.WebhookSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WebhookDispatcherServiceTest {

    @Mock private WebhookSubscriptionRepository subscriptionRepository;

    @InjectMocks
    private WebhookDispatcherService webhookDispatcherService;

    private UUID subscriptionId;
    private WebhookSubscription subscription;

    @BeforeEach
    void setUp() {
        subscriptionId = UUID.randomUUID();
        subscription = WebhookSubscription.builder()
                .id(subscriptionId)
                .name("Slack Alert Hook")
                .targetUrl("https://hooks.slack.com/services/xxx/yyy")
                .secretKey("super-secret-key-123")
                .eventsCsv("RECORD_CREATED,RECORD_APPROVED")
                .active(true)
                .build();
    }

    @Test
    @DisplayName("createSubscription: 웹훅 구독 등록 검증")
    void testCreateSubscription() {
        when(subscriptionRepository.save(any(WebhookSubscription.class))).thenReturn(subscription);

        WebhookDto.SubscriptionCreateRequest req = WebhookDto.SubscriptionCreateRequest.builder()
                .name("Slack Alert Hook")
                .targetUrl("https://hooks.slack.com/services/xxx/yyy")
                .events(List.of("RECORD_CREATED", "RECORD_APPROVED"))
                .build();

        WebhookDto.SubscriptionResponse res = webhookDispatcherService.createSubscription(req);

        assertThat(res).isNotNull();
        assertThat(res.getName()).isEqualTo("Slack Alert Hook");
        assertThat(res.getEvents()).contains("RECORD_CREATED");
    }

    @Test
    @DisplayName("testWebhook: HMAC-SHA256 서명 헤더 생성 및 PING 테스트 검증")
    void testTestWebhook() {
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));

        WebhookDto.WebhookTestResult result = webhookDispatcherService.testWebhook(subscriptionId);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getStatusCode()).isEqualTo(200);
        assertThat(result.getSignatureHeader()).startsWith("sha256=");
    }
}
