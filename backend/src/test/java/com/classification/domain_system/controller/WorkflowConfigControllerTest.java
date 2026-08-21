package com.classification.domain_system.controller;

import com.classification.domain_system.entity.WorkflowConfig;
import com.classification.domain_system.repository.WorkflowConfigRepository;
import com.classification.domain_system.security.JwtUtil;
import com.classification.domain_system.service.PermissionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WorkflowConfigController.class)
@org.springframework.context.annotation.Import({com.classification.domain_system.config.SecurityConfig.class, com.classification.domain_system.config.TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class WorkflowConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private com.classification.domain_system.service.WorkflowConfigService workflowConfigService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PermissionService permissionService;

    @MockitoBean
    private com.classification.domain_system.context.AuthContext authContext;

    @Test
    @DisplayName("saveForDomain - stepOrder가 1부터 시작하지 않으면 BAD_REQUEST(400) 던짐")
    void saveForDomain_NonOneStart_ThrowsBadRequest() throws Exception {
        UUID domainId = UUID.randomUUID();
        String invalidJson = "[{\"actionType\":\"CREATE\",\"stepsConfig\":\"{\\\"steps\\\":[{\\\"stepOrder\\\":2}]}\"}]";

        org.mockito.Mockito.doThrow(new com.classification.domain_system.exception.BusinessException(
                com.classification.domain_system.exception.ErrorCode.INVALID_WORKFLOW_CONFIG,
                "Step orders must start at 1"))
                .when(workflowConfigService).saveForDomain(org.mockito.ArgumentMatchers.eq(domainId), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(post("/api/workflow-configs/domain/{domainId}", domainId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("saveForDomain - stepOrder에 갭이 존재하면 BAD_REQUEST(400) 던짐")
    void saveForDomain_GapInSteps_ThrowsBadRequest() throws Exception {
        UUID domainId = UUID.randomUUID();
        String invalidJson = "[{\"actionType\":\"CREATE\",\"stepsConfig\":\"{\\\"steps\\\":[{\\\"stepOrder\\\":1},{\\\"stepOrder\\\":3}]}\"}]";

        org.mockito.Mockito.doThrow(new com.classification.domain_system.exception.BusinessException(
                com.classification.domain_system.exception.ErrorCode.INVALID_WORKFLOW_CONFIG,
                "Step orders must be contiguous with no gaps"))
                .when(workflowConfigService).saveForDomain(org.mockito.ArgumentMatchers.eq(domainId), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(post("/api/workflow-configs/domain/{domainId}", domainId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
