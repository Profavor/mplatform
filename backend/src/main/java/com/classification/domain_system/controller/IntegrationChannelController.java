package com.classification.domain_system.controller;

import com.classification.domain_system.dto.IntegrationChannelResponse;
import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.service.IntegrationChannelService;
import com.classification.domain_system.dto.ConnectionTestRequest;
import com.classification.domain_system.dto.ConnectionTestResponse;
import com.classification.domain_system.service.IntegrationTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/admin/integration/channels")
@RequiredArgsConstructor
public class IntegrationChannelController {

    private final IntegrationChannelService service;
    private final IntegrationTestService testService;
    private final com.classification.domain_system.batch.stock.scheduler.StockBatchJobScheduler stockBatchJobScheduler;

    @PostMapping("/test-connection")
    @PreAuthorize("hasPermission(null, 'integration:write')")
    public ResponseEntity<ConnectionTestResponse> testConnection(@RequestBody ConnectionTestRequest request) {
        return ResponseEntity.ok(testService.testConnection(request));
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, 'integration:read')")
    public List<IntegrationChannelResponse> getAll() {
        return service.getAllChannels();
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'integration:write')")
    public IntegrationChannelResponse create(@RequestBody IntegrationChannel channel) {
        return service.createChannel(channel);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'integration:write')")
    public ResponseEntity<IntegrationChannelResponse> update(@PathVariable UUID id, @RequestBody IntegrationChannel updated) {
        return service.updateChannel(id, updated)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'integration:write')")
    public ResponseEntity<Void> deleteChannel(@PathVariable UUID id) {
        if (service.deleteChannel(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/metrics")
    @PreAuthorize("hasPermission(null, 'integration:read')")
    public ResponseEntity<com.classification.domain_system.dto.IntegrationMetricsDto> getChannelMetrics(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getChannelMetrics(id));
    }

    @PostMapping("/{id}/ping")
    @PreAuthorize("hasPermission(null, 'integration:read') or hasPermission(null, 'integration:write')")
    public ResponseEntity<com.classification.domain_system.dto.IntegrationMetricsDto> pingChannel(@PathVariable UUID id) {
        return ResponseEntity.ok(service.pingChannel(id));
    }

    @PostMapping("/smart-mapping-recommend")
    @PreAuthorize("hasPermission(null, 'integration:read') or hasPermission(null, 'integration:write')")
    public ResponseEntity<List<com.classification.domain_system.dto.SmartMappingRecommendationDto>> getSmartMappingRecommendations(
            @RequestBody Map<String, Object> requestBody,
            @org.springframework.beans.factory.annotation.Autowired com.classification.domain_system.service.SmartMappingService smartMappingService) {
        UUID domainId = UUID.fromString(String.valueOf(requestBody.get("domainId")));
        String samplePayload = String.valueOf(requestBody.get("samplePayload"));
        return ResponseEntity.ok(smartMappingService.recommendMappings(domainId, samplePayload));
    }

    @PostMapping("/{id}/trigger-batch")
    @PreAuthorize("hasPermission(null, 'integration:write')")
    public ResponseEntity<Map<String, Object>> triggerBatchJob(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, Object> requestParams,
            java.security.Principal principal) {
        String username = principal != null ? principal.getName() : "ADMIN";
        Boolean clearExisting = requestParams != null && Boolean.TRUE.equals(requestParams.get("clearExisting"));
        String markets = requestParams != null && requestParams.get("markets") != null ? String.valueOf(requestParams.get("markets")) : null;

        try {
            var execution = stockBatchJobScheduler.runBatchJob(clearExisting, markets, username);
            return ResponseEntity.ok(Map.of(
                    "jobExecutionId", execution.getId(),
                    "status", execution.getStatus().name(),
                    "message", "Spring Batch Job has been successfully triggered."
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
}
