package com.classification.domain_system.controller;

import com.classification.domain_system.dto.SystemInstallRequest;
import com.classification.domain_system.dto.SystemInstallStatusResponse;
import com.classification.domain_system.security.JwtUtil;
import com.classification.domain_system.service.AuthService;
import com.classification.domain_system.service.PermissionService;
import com.classification.domain_system.service.SystemInstallService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SystemInstallController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.context.annotation.Import(com.classification.domain_system.exception.GlobalExceptionHandler.class)
class SystemInstallControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SystemInstallService installService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private PermissionService permissionService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private com.classification.domain_system.context.AuthContext authContext;

    @Test
    @DisplayName("설치 상태 조회 정상 응답")
    void getInstallStatus_Success() throws Exception {
        when(installService.getInstallStatus()).thenReturn(new SystemInstallStatusResponse(false, false));

        mockMvc.perform(get("/api/system/install-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isInstalled").value(false))
                .andExpect(jsonPath("$.hasAdminAccount").value(false));
    }

    @Test
    @DisplayName("이미 설치된 시스템에 대해 설치 시도 시 표준 ErrorResponse JSON 반환 (400 Bad Request)")
    void installSystem_AlreadyInstalled_ReturnsStandardErrorJson() throws Exception {
        when(installService.installSystem(any(SystemInstallRequest.class)))
                .thenThrow(new IllegalStateException("이미 설치가 완료된 시스템입니다."));

        mockMvc.perform(post("/api/system/install")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"adminUsername\":\"admin\",\"adminPassword\":\"password123\",\"organizationName\":\"Company\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("이미 설치가 완료된 시스템입니다."));
    }
}
