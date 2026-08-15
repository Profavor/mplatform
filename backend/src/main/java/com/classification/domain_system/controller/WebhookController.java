package com.classification.domain_system.controller;

import com.classification.domain_system.dto.WebhookDto;
import com.classification.domain_system.service.WebhookDispatcherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/integration/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookDispatcherService webhookDispatcherService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'integration:read') or hasPermission(null, 'domain:read')")
    public ResponseEntity<List<WebhookDto.SubscriptionResponse>> getSubscriptions() {
        return ResponseEntity.ok(webhookDispatcherService.getSubscriptions());
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'integration:write')")
    public ResponseEntity<WebhookDto.SubscriptionResponse> createSubscription(
            @RequestBody WebhookDto.SubscriptionCreateRequest request) {
        return ResponseEntity.ok(webhookDispatcherService.createSubscription(request));
    }

    @PostMapping("/{id}/test")
    @PreAuthorize("hasPermission(null, 'integration:write')")
    public ResponseEntity<WebhookDto.WebhookTestResult> testWebhook(
            @PathVariable UUID id) {
        return ResponseEntity.ok(webhookDispatcherService.testWebhook(id));
    }
}
