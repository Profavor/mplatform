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
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/admin/integration/channels")
@RequiredArgsConstructor
public class IntegrationChannelController {

    private final IntegrationChannelService service;
    private final IntegrationTestService testService;

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
}
