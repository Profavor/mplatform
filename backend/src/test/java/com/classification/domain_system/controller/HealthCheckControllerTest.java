package com.classification.domain_system.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthCheckControllerTest {

    @Test
    @DisplayName("루트 GET / 및 /health 헬스체크 요청 시 OK 응답 200 반환")
    void healthCheckTest() {
        HealthCheckController controller = new HealthCheckController();
        ResponseEntity<String> response = controller.healthCheck();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OK", response.getBody());
    }
}
