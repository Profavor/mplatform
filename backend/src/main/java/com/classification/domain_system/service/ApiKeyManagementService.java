package com.classification.domain_system.service;

import com.classification.domain_system.dto.ApiKeyDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiKeyManagementService {

    private final Map<String, ApiKeyDto.ApiKeyItem> keyStorage = new ConcurrentHashMap<>();

    {
        String keyId = "KEY-001";
        keyStorage.put(keyId, ApiKeyDto.ApiKeyItem.builder()
                .keyId(keyId)
                .name("ERP 연계 시스템 전용 Key")
                .maskedKey("ak_live_a1b2...9f8e")
                .scopes(List.of("customer:read", "order:write"))
                .allowedIpsCsv("10.0.0.0/8, 192.168.1.100")
                .active(true)
                .createdAt(LocalDateTime.now().minusMonths(1))
                .expiresAt(LocalDateTime.now().plusMonths(11))
                .build());
    }

    public List<ApiKeyDto.ApiKeyItem> getApiKeys() {
        return new ArrayList<>(keyStorage.values());
    }

    public ApiKeyDto.CreateApiKeyResponse createApiKey(ApiKeyDto.CreateApiKeyRequest req) {
        String keyId = "KEY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String rawKey = "ak_live_" + UUID.randomUUID().toString().replace("-", "");
        String maskedKey = "ak_live_" + rawKey.substring(8, 12) + "..." + rawKey.substring(rawKey.length() - 4);

        int days = req.getValidDays() > 0 ? req.getValidDays() : 365;
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(days);

        ApiKeyDto.ApiKeyItem item = ApiKeyDto.ApiKeyItem.builder()
                .keyId(keyId)
                .name(req.getName() != null ? req.getName() : "신규 API Key")
                .maskedKey(maskedKey)
                .scopes(req.getScopes() != null ? req.getScopes() : List.of("record:read"))
                .allowedIpsCsv(req.getAllowedIpsCsv())
                .active(true)
                .createdAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .build();

        keyStorage.put(keyId, item);

        return ApiKeyDto.CreateApiKeyResponse.builder()
                .keyId(keyId)
                .rawApiKey(rawKey)
                .maskedKey(maskedKey)
                .scopes(item.getScopes())
                .expiresAt(expiresAt)
                .message("API Key가 발급되었습니다. 원본 키는 지금 1회만 표시되므로 안전한 곳에 보관하세요.")
                .build();
    }

    public boolean revokeApiKey(String keyId) {
        if (keyStorage.containsKey(keyId)) {
            keyStorage.remove(keyId);
            return true;
        }
        return false;
    }
}
