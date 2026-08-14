package com.classification.domain_system.controller;

import com.classification.domain_system.dto.SensitiveDataAccessLogDto;
import com.classification.domain_system.dto.SensitiveDataStatsDto;
import com.classification.domain_system.service.SensitiveDataService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensitiveDataControllerTest {

    @Mock
    private SensitiveDataService sensitiveDataService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private SensitiveDataController sensitiveDataController;

    @Test
    @DisplayName("결재 민감 데이터 필드 복호화 성공")
    void testDecryptApprovalFields_Success() {
        UUID approvalId = UUID.randomUUID();
        SensitiveDataController.DecryptRequest request = new SensitiveDataController.DecryptRequest();
        request.setFieldKeys(List.of("residentNumber"));
        request.setAccessReason("고객 업무 처리");

        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(sensitiveDataService.decryptApprovalFields(eq(approvalId), eq(List.of("residentNumber")), eq("고객 업무 처리"), any()))
                .thenReturn(Map.of("residentNumber", "900101-1234567"));

        ResponseEntity<Map<String, String>> response = sensitiveDataController.decryptApprovalFields(approvalId, request, httpRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("900101-1234567", response.getBody().get("residentNumber"));
    }

    @Test
    @DisplayName("레코드 민감 데이터 필드 복호화 성공")
    void testDecryptRecordFields_Success() {
        UUID recordId = UUID.randomUUID();
        SensitiveDataController.DecryptRequest request = new SensitiveDataController.DecryptRequest();
        request.setFieldKeys(List.of("creditCardNumber"));
        request.setAccessReason("정산 업무");

        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(sensitiveDataService.decryptRecordFields(eq(recordId), eq(List.of("creditCardNumber")), eq("정산 업무"), any()))
                .thenReturn(Map.of("creditCardNumber", "1234-5678-9012-3456"));

        ResponseEntity<Map<String, String>> response = sensitiveDataController.decryptRecordFields(recordId, request, httpRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("1234-5678-9012-3456", response.getBody().get("creditCardNumber"));
    }

    @Test
    @DisplayName("변경 이력 민감 데이터 필드 복호화 성공")
    void testDecryptHistoryFields_Success() {
        UUID historyId = UUID.randomUUID();
        SensitiveDataController.DecryptRequest request = new SensitiveDataController.DecryptRequest();
        request.setFieldKeys(List.of("phone"));
        request.setAccessReason("이력 감사");

        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(sensitiveDataService.decryptHistoryFields(eq(historyId), eq(List.of("phone")), eq("이력 감사"), any()))
                .thenReturn(Map.of("phone", "010-1234-5678"));

        ResponseEntity<Map<String, String>> response = sensitiveDataController.decryptHistoryFields(historyId, request, httpRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("010-1234-5678", response.getBody().get("phone"));
    }

    @Test
    @DisplayName("민감 데이터 접근 감사 로그 페이징 조회 성공")
    void testGetAccessLogs_Success() {
        SensitiveDataAccessLogDto logDto = new SensitiveDataAccessLogDto();
        logDto.setUserId("admin");
        logDto.setAccessReason("정기 감사");

        Page<SensitiveDataAccessLogDto> page = new PageImpl<>(List.of(logDto), PageRequest.of(0, 10), 1);
        when(sensitiveDataService.getAccessLogs(eq("admin"), any(), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<SensitiveDataAccessLogDto>> response = sensitiveDataController.getAccessLogs("admin", null, PageRequest.of(0, 10));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    @DisplayName("민감 데이터 통계 조회 성공")
    void testGetStatistics_Success() {
        SensitiveDataStatsDto stats = new SensitiveDataStatsDto();
        when(sensitiveDataService.getStatistics()).thenReturn(stats);

        ResponseEntity<SensitiveDataStatsDto> response = sensitiveDataController.getStatistics();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
