package com.classification.domain_system.service;

import com.classification.domain_system.dto.ApiKeyDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiKeyManagementServiceTest {

    private ApiKeyManagementService apiKeyService;

    @BeforeEach
    void setUp() {
        apiKeyService = new ApiKeyManagementService();
    }

    @Test
    @DisplayName("createApiKey: 세부 권한 스코프 기반 연계 API Key 발급")
    void testCreateApiKey() {
        ApiKeyDto.CreateApiKeyRequest req = ApiKeyDto.CreateApiKeyRequest.builder()
                .name("모바일 앱 연계 Key")
                .scopes(List.of("customer:read", "product:read"))
                .allowedIpsCsv("192.168.0.1/24")
                .validDays(180)
                .build();

        ApiKeyDto.CreateApiKeyResponse res = apiKeyService.createApiKey(req);

        assertThat(res).isNotNull();
        assertThat(res.getKeyId()).startsWith("KEY-");
        assertThat(res.getRawApiKey()).startsWith("ak_live_");
        assertThat(res.getMaskedKey()).contains("...");

        List<ApiKeyDto.ApiKeyItem> list = apiKeyService.getApiKeys();
        assertThat(list).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("revokeApiKey: API Key 즉시 폐기")
    void testRevokeApiKey() {
        boolean revoked = apiKeyService.revokeApiKey("KEY-001");
        assertThat(revoked).isTrue();

        boolean revokedAgain = apiKeyService.revokeApiKey("KEY-001");
        assertThat(revokedAgain).isFalse();
    }
}
