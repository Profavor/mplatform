package com.classification.domain_system.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    }
}
