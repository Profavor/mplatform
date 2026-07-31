package com.classification.domain_system.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = Mockito.mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    @DisplayName("BusinessException 발생 시 해당 에러코드와 메시지로 응답")
    void handleBusinessException() {
        BusinessException ex = new BusinessException(ErrorCode.DOMAIN_MISSING_FIELD_MAPPING, "Missing mapping");
        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("DOMAIN_MISSING_FIELD_MAPPING", response.getBody().getErrorCode());
        assertEquals("Missing mapping", response.getBody().getMessage());
    }

    @Test
    @DisplayName("ResourceNotFoundException 발생 시 404 상태 코드 반환")
    void handleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().getErrorCode());
    }

    @Test
    @DisplayName("알 수 없는 Exception 발생 시 500 에러 및 내부 메시지 마스킹")
    void handleUncaughtException() {
        Exception ex = new NullPointerException("Secret internal database connection error trace");
        ResponseEntity<ErrorResponse> response = handler.handleAllExceptions(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getErrorCode());
        assertEquals("내부 오류가 발생했습니다.", response.getBody().getMessage());
        assertFalse(response.getBody().getMessage().contains("Secret internal database"));
    }

    @Test
    @DisplayName("P3: OptimisticLockException 발생 시 409 Conflict 및 안내 메시지 반환")
    void handleOptimisticLockException() {
        jakarta.persistence.OptimisticLockException ex = new jakarta.persistence.OptimisticLockException("Row was updated or deleted by another transaction");
        ResponseEntity<ErrorResponse> response = handler.handleOptimisticLockException(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("OPTIMISTIC_LOCK_ERROR", response.getBody().getErrorCode());
        assertTrue(response.getBody().getMessage().contains("다른 사용자가 이미 이 레코드를 수정했습니다"));
    }
}
