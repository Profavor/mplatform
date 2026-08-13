package com.classification.domain_system.controller;

import com.classification.domain_system.context.AuthContext;
import com.classification.domain_system.entity.BatchJob;
import com.classification.domain_system.service.BatchImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.classification.domain_system.security.JwtUtil;
import com.classification.domain_system.service.PermissionService;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BatchController.class)
@org.springframework.context.annotation.Import({com.classification.domain_system.config.SecurityConfig.class, com.classification.domain_system.config.TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class BatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BatchImportService batchImportService;

    @MockitoBean
    private AuthContext authContext;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PermissionService permissionService;

    @BeforeEach
    void setUp() {
        Mockito.when(authContext.getUserId()).thenReturn("test-user");
        Mockito.when(authContext.hasPermission("record:write")).thenReturn(true);
    }

    @Test
    void createBatch_Success_WithPermission() throws Exception {
        UUID domainId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();

        BatchJob mockJob = new BatchJob();
        mockJob.setId(UUID.randomUUID());
        Mockito.when(batchImportService.createBatch(eq(domainId), eq(nodeId), any(), eq("TEST"), eq("test-user")))
               .thenReturn(mockJob);

        mockMvc.perform(post("/api/batch/import")
                .param("domainId", domainId.toString())
                .param("nodeId", nodeId.toString())
                .param("sourceSystem", "TEST")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{}]"))
               // Will return 403 if MethodSecurity evaluates @PreAuthorize, 
               // but wait, @WebMvcTest usually needs extra config to enable MethodSecurity.
               // For this unit test, it will return 200 if the controller runs.
               .andExpect(status().isOk());
    }
}
