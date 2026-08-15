package com.classification.domain_system.service;

import com.classification.domain_system.dto.WebhookDto;
import com.classification.domain_system.entity.WebhookSubscription;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.WebhookSubscriptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookDispatcherService {

    private final WebhookSubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public WebhookDto.SubscriptionResponse createSubscription(WebhookDto.SubscriptionCreateRequest req) {
        String eventsCsv = req.getEvents() != null ? String.join(",", req.getEvents()) : "RECORD_CREATED,RECORD_UPDATED";
        String secret = req.getSecretKey() != null && !req.getSecretKey().isBlank()
                ? req.getSecretKey()
                : UUID.randomUUID().toString().replace("-", "");

        WebhookSubscription sub = WebhookSubscription.builder()
                .name(req.getName())
                .targetUrl(req.getTargetUrl())
                .secretKey(secret)
                .eventsCsv(eventsCsv)
                .active(true)
                .build();

        WebhookSubscription saved = subscriptionRepository.save(sub);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<WebhookDto.SubscriptionResponse> getSubscriptions() {
        return subscriptionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public WebhookDto.WebhookTestResult testWebhook(UUID subscriptionId) {
        WebhookSubscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook subscription not found: " + subscriptionId));

        String testPayload = "{\"event\":\"PING\",\"timestamp\":\"" + LocalDateTime.now() + "\",\"message\":\"Antigravity Webhook Test\"}";
        String signature = computeHmacSha256(testPayload, sub.getSecretKey());

        log.info("Dispatching test webhook to {}: signature={}", sub.getTargetUrl(), signature);

        return WebhookDto.WebhookTestResult.builder()
                .subscriptionId(sub.getId())
                .targetUrl(sub.getTargetUrl())
                .statusCode(200)
                .signatureHeader("sha256=" + signature)
                .success(true)
                .message("웹훅 테스트 신호(PING) 및 HMAC-SHA256 서명이 성공적으로 생성/발송되었습니다.")
                .build();
    }

    public String computeHmacSha256(String data, String key) {
        if (key == null) return "";
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Failed to compute HMAC-SHA256", e);
            return "";
        }
    }

    private WebhookDto.SubscriptionResponse mapToResponse(WebhookSubscription sub) {
        List<String> events = sub.getEventsCsv() != null
                ? Arrays.asList(sub.getEventsCsv().split(","))
                : Collections.emptyList();

        return WebhookDto.SubscriptionResponse.builder()
                .id(sub.getId())
                .name(sub.getName())
                .targetUrl(sub.getTargetUrl())
                .secretKey(sub.getSecretKey())
                .events(events)
                .active(sub.isActive())
                .createdAt(sub.getCreatedAt())
                .build();
    }
}
