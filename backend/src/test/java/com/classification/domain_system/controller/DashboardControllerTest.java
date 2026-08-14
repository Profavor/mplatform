package com.classification.domain_system.controller;

import com.classification.domain_system.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    @Test
    @DisplayName("대시보드 KPI 통계 조회 성공")
    void testGetStats_Success() {
        Map<String, Object> stats = Map.of("totalDomains", 10, "totalRecords", 5000);
        when(dashboardService.getStats()).thenReturn(stats);

        ResponseEntity<Map<String, Object>> response = dashboardController.getStats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10, response.getBody().get("totalDomains"));
        verify(dashboardService).getStats();
    }

    @Test
    @DisplayName("대시보드 결재 추이 트렌드 조회 성공")
    void testGetApprovalTrends_Success() {
        List<Map<String, Object>> trends = List.of(Map.of("date", "2026-08-01", "count", 15));
        when(dashboardService.getApprovalTrends()).thenReturn(trends);

        ResponseEntity<List<Map<String, Object>>> response = dashboardController.getApprovalTrends();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(dashboardService).getApprovalTrends();
    }

    @Test
    @DisplayName("대시보드 도메인별 데이터 분포 조회 성공")
    void testGetDomainDistribution_Success() {
        List<Map<String, Object>> dist = List.of(Map.of("domainName", "금융", "recordCount", 1200));
        when(dashboardService.getDomainDistribution()).thenReturn(dist);

        ResponseEntity<List<Map<String, Object>>> response = dashboardController.getDomainDistribution();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(dashboardService).getDomainDistribution();
    }

    @Test
    @DisplayName("대시보드 데이터 품질(DQ) 검증 추이 조회 성공")
    void testGetDqTrends_Success() {
        List<Map<String, Object>> dqTrends = List.of(Map.of("date", "2026-08-01", "errorCount", 3));
        when(dashboardService.getDqTrends()).thenReturn(dqTrends);

        ResponseEntity<List<Map<String, Object>>> response = dashboardController.getDqTrends();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(dashboardService).getDqTrends();
    }

    @Test
    @DisplayName("대시보드 DQ 심각도 분포 조회 성공")
    void testGetDqSeverityDistribution_Success() {
        List<Map<String, Object>> severityDist = List.of(Map.of("severity", "CRITICAL", "count", 5));
        when(dashboardService.getDqSeverityDistribution()).thenReturn(severityDist);

        ResponseEntity<List<Map<String, Object>>> response = dashboardController.getDqSeverityDistribution();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(dashboardService).getDqSeverityDistribution();
    }
}
