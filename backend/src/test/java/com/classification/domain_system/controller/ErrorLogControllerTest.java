package com.classification.domain_system.controller;

import com.classification.domain_system.entity.ErrorLog;
import com.classification.domain_system.service.ErrorLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ErrorLogControllerTest {

    @Mock
    private ErrorLogService errorLogService;

    @InjectMocks
    private ErrorLogController errorLogController;

    @Test
    @DisplayName("에러 로그 페이징 조회 성공")
    void testGetErrorLogs_Success() {
        ErrorLog log = ErrorLog.builder()
                .id(1L)
                .userId("admin")
                .requestUri("/api/test")
                .errorMessage("NullPointerException")
                .loggedAt(LocalDateTime.now())
                .build();

        Page<ErrorLog> page = new PageImpl<>(List.of(log));
        when(errorLogService.getErrorLogs(any(Pageable.class))).thenReturn(page);

        ResponseEntity<?> response = errorLogController.getErrorLogs(0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Page);
        Page<?> resultPage = (Page<?>) response.getBody();
        assertEquals(1, resultPage.getTotalElements());
        verify(errorLogService).getErrorLogs(any(Pageable.class));
    }
}
