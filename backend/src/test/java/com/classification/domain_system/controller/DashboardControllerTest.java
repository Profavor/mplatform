package com.classification.domain_system.controller;

import com.classification.domain_system.security.JwtUtil;
import com.classification.domain_system.service.DashboardService;
import com.classification.domain_system.service.PermissionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DashboardController.class)
@org.springframework.context.annotation.Import({com.classification.domain_system.config.SecurityConfig.class, com.classification.domain_system.config.TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PermissionService permissionService;

    @MockitoBean
    private com.classification.domain_system.context.AuthContext authContext;

    @Test
    @DisplayName("getStats - DashboardService에서 반환된 통계 데이터 응답")
    void getStats_ReturnsStatsMap() throws Exception {
        Map<String, Object> stats = Map.of(
                "totalDomains", 5L,
                "pendingApprovals", 2L,
                "activeRecords", 100L,
                "pendingMatches", 3L,
                "openDqViolations", 1L
        );
        when(dashboardService.getStats()).thenReturn(stats);

        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDomains").value(5))
                .andExpect(jsonPath("$.pendingApprovals").value(2))
                .andExpect(jsonPath("$.activeRecords").value(100))
                .andExpect(jsonPath("$.pendingMatches").value(3))
                .andExpect(jsonPath("$.openDqViolations").value(1));
    }

    @Test
    @DisplayName("getApprovalTrends - 최근 7일 결재 추이 데이터 응답 검증")
    void getApprovalTrends_ReturnsList() throws Exception {
        java.util.List<Map<String, Object>> trends = java.util.List.of(
                Map.of("date", "2026-07-24", "count", 5L),
                Map.of("date", "2026-07-25", "count", 10L)
        );
        when(dashboardService.getApprovalTrends()).thenReturn(trends);

        mockMvc.perform(get("/api/dashboard/trends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].count").value(5));
    }

    @Test
    @DisplayName("getDomainDistribution - 도메인별 레코드 분포 응답 검증")
    void getDomainDistribution_ReturnsList() throws Exception {
        java.util.List<Map<String, Object>> list = java.util.List.of(
                Map.of("domainName", "고객", "recordCount", 50L)
        );
        when(dashboardService.getDomainDistribution()).thenReturn(list);

        mockMvc.perform(get("/api/dashboard/domain-distribution"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].domainName").value("고객"));
    }
}
