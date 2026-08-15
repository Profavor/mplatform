package com.classification.domain_system.controller;

import com.classification.domain_system.dto.ApiKeyDto;
import com.classification.domain_system.service.ApiKeyManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/integration/api-keys")
@RequiredArgsConstructor
public class ApiKeyManagementController {

    private final ApiKeyManagementService apiKeyManagementService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'integration:read')")
    public ResponseEntity<List<ApiKeyDto.ApiKeyItem>> getApiKeys() {
        return ResponseEntity.ok(apiKeyManagementService.getApiKeys());
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'integration:write')")
    public ResponseEntity<ApiKeyDto.CreateApiKeyResponse> createApiKey(
            @RequestBody ApiKeyDto.CreateApiKeyRequest request) {
        return ResponseEntity.ok(apiKeyManagementService.createApiKey(request));
    }

    @DeleteMapping("/{keyId}")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'integration:write')")
    public ResponseEntity<Boolean> revokeApiKey(@PathVariable String keyId) {
        return ResponseEntity.ok(apiKeyManagementService.revokeApiKey(keyId));
    }
}
