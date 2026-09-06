package com.classification.domain_system.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class WebServiceDynamicExecutionServiceTest {

    private WebServiceDynamicExecutionService service;

    @BeforeEach
    void setUp() {
        service = new WebServiceDynamicExecutionService();
        ReflectionTestUtils.setField(service, "connectTimeoutMs", 100);
        ReflectionTestUtils.setField(service, "readTimeoutMs", 100);
        service.init();
    }

    @Test
    void executeWebService_WithUnresponsiveServer_ThrowsTimeoutException() throws Exception {
        System.setProperty("security.url.allow-loopback-for-test", "true");
        try {
            // Dummy server that accepts connection but never responds
            try (ServerSocket serverSocket = new ServerSocket(0)) {
                int port = serverSocket.getLocalPort();
                String configJson = String.format("{\"url\":\"http://localhost:%d/dummy\",\"method\":\"POST\"}", port);
                
                long startTime = System.currentTimeMillis();
                
                Exception exception = assertThrows(RuntimeException.class, () -> {
                    service.executeWebService(configJson, "{}");
                });
                
                long duration = System.currentTimeMillis() - startTime;
                
                // Should fail due to timeout, usually within 1000ms given our 100ms timeout
                assertTrue(duration < 2000, "Should timeout quickly, took " + duration + "ms");
                assertTrue(exception.getMessage().contains("WEB_SERVICE execution failed"));
            }
        } finally {
            System.clearProperty("security.url.allow-loopback-for-test");
        }
    }

    @Test
    void executeWebService_DecryptsEncryptedHeaders() throws Exception {
        com.classification.domain_system.service.FieldEncryptionService encryptionService = org.mockito.Mockito.mock(com.classification.domain_system.service.FieldEncryptionService.class);
        org.springframework.web.client.RestTemplate restTemplate = org.mockito.Mockito.mock(org.springframework.web.client.RestTemplate.class);

        when(encryptionService.isEncrypted("ENC(secret123)")).thenReturn(true);
        when(encryptionService.decrypt("ENC(secret123)")).thenReturn("decryptedSecret");

        service.setEncryptionService(encryptionService);
        service.setRestTemplate(restTemplate);

        String configJson = "{\"url\":\"https://example.com/api/ingest\",\"method\":\"POST\",\"headers\":[{\"key\":\"x-cartbom-source-token\",\"value\":\"ENC(secret123)\"}]}";

        org.mockito.ArgumentCaptor<org.springframework.http.HttpEntity> captor = org.mockito.ArgumentCaptor.forClass(org.springframework.http.HttpEntity.class);
        when(restTemplate.exchange(
                org.mockito.ArgumentMatchers.eq("https://example.com/api/ingest"),
                org.mockito.ArgumentMatchers.eq(org.springframework.http.HttpMethod.POST),
                captor.capture(),
                org.mockito.ArgumentMatchers.eq(String.class)
        )).thenReturn(org.springframework.http.ResponseEntity.ok("ok"));

        service.executeWebService(configJson, "{\"data\":1}");

        org.springframework.http.HttpEntity entity = captor.getValue();
        org.springframework.http.HttpHeaders headers = entity.getHeaders();
        assertEquals("decryptedSecret", headers.getFirst("x-cartbom-source-token"));
    }

    @Test
    void executeWebService_AutoInjectsIdempotencyKeyHeaderFromPayload() throws Exception {
        org.springframework.web.client.RestTemplate restTemplate = org.mockito.Mockito.mock(org.springframework.web.client.RestTemplate.class);
        service.setRestTemplate(restTemplate);

        String configJson = "{\"url\":\"https://cartbom.com/api/partners/ingest\",\"method\":\"POST\",\"headers\":[{\"key\":\"x-cartbom-source-id\",\"value\":\"mdm-mplat\"}]}";
        String payloadJson = "{\"idempotencyKey\":\"CBP-000503:2026-08-29:11480\",\"externalId\":\"8763386823\",\"product\":{\"name\":\"토스트 소스\"}}";

        org.mockito.ArgumentCaptor<org.springframework.http.HttpEntity> captor = org.mockito.ArgumentCaptor.forClass(org.springframework.http.HttpEntity.class);
        when(restTemplate.exchange(
                org.mockito.ArgumentMatchers.eq("https://cartbom.com/api/partners/ingest"),
                org.mockito.ArgumentMatchers.eq(org.springframework.http.HttpMethod.POST),
                captor.capture(),
                org.mockito.ArgumentMatchers.eq(String.class)
        )).thenReturn(org.springframework.http.ResponseEntity.ok("ok"));

        service.executeWebService(configJson, payloadJson);

        org.springframework.http.HttpEntity entity = captor.getValue();
        org.springframework.http.HttpHeaders headers = entity.getHeaders();
        assertEquals("CBP-000503:2026-08-29:11480", headers.getFirst("Idempotency-Key"));
        assertEquals("mdm-mplat", headers.getFirst("x-cartbom-source-id"));
    }
}
