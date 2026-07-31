package com.classification.domain_system.controller;

import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.repository.IntegrationChannelRepository;
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

    private final IntegrationChannelRepository repository;
    private final IntegrationTestService testService;

    @PostMapping("/test-connection")
    @PreAuthorize("hasPermission(null, 'integration:write')")
    public ResponseEntity<ConnectionTestResponse> testConnection(@RequestBody ConnectionTestRequest request) {
        return ResponseEntity.ok(testService.testConnection(request));
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, 'integration:read')")
    public List<IntegrationChannel> getAll() {
        return repository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'integration:write')")
    public IntegrationChannel create(@RequestBody IntegrationChannel channel) {
        if (channel.getDirection() == null || channel.getDirection().isBlank()) {
            channel.setDirection("OUTBOUND");
        }
        return repository.save(channel);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'integration:write')")
    public ResponseEntity<IntegrationChannel> update(@PathVariable UUID id, @RequestBody IntegrationChannel updated) {
        return repository.findById(id).map(channel -> {
            channel.setName(updated.getName());
            channel.setType(updated.getType());
            if (updated.getDirection() != null && !updated.getDirection().isBlank()) {
                channel.setDirection(updated.getDirection());
            }
            channel.setNodeId(updated.getNodeId());
            channel.setConfigJson(updated.getConfigJson());
            channel.setMappingConfigJson(updated.getMappingConfigJson());
            channel.setActive(updated.isActive());
            return ResponseEntity.ok(repository.save(channel));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'integration:write')")
    public ResponseEntity<Void> deleteChannel(@PathVariable UUID id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
