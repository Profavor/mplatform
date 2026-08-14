package com.classification.domain_system.controller;

import com.classification.domain_system.entity.Organization;
import com.classification.domain_system.security.JwtUtil;
import com.classification.domain_system.service.OrganizationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrganizationController.class)
@org.springframework.context.annotation.Import({com.classification.domain_system.config.SecurityConfig.class, com.classification.domain_system.config.TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrganizationService organizationService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private com.classification.domain_system.service.PermissionService permissionService;

    @MockitoBean
    private com.classification.domain_system.context.AuthContext authContext;

    @Test
    @DisplayName("조직 전체 목록 조회 API 테스트")
    void getAllOrganizations_success() throws Exception {
        UUID orgId = UUID.randomUUID();
        Organization org = new Organization();
        org.setId(orgId);
        org.setName("test-org");
        org.setDisplayName("테스트 조직");

        when(organizationService.getAllOrganizations()).thenReturn(List.of(org));

        mockMvc.perform(get("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("test-org"))
                .andExpect(jsonPath("$[0].displayName").value("테스트 조직"));
    }

    @Test
    @DisplayName("조직 단건 조회 API - 존재할 때 200 반환")
    void getOrganization_found() throws Exception {
        UUID orgId = UUID.randomUUID();
        Organization org = new Organization();
        org.setId(orgId);
        org.setName("test-org");
        org.setDisplayName("테스트 조직");

        when(organizationService.getOrganization(orgId)).thenReturn(Optional.of(org));

        mockMvc.perform(get("/api/organizations/" + orgId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test-org"));
    }
}
