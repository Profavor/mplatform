package com.classification.domain_system.controller;

import com.classification.domain_system.entity.Role;
import com.classification.domain_system.repository.RoleRepository;
import com.classification.domain_system.repository.UserRoleRepository;
import com.classification.domain_system.security.JwtUtil;
import com.classification.domain_system.service.PermissionService;
import com.classification.domain_system.service.RoleInitializer;
import com.classification.domain_system.service.SystemSeedDumpService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RoleController.class)
@org.springframework.context.annotation.Import({com.classification.domain_system.config.SecurityConfig.class, com.classification.domain_system.config.TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private com.classification.domain_system.service.RoleService roleService;

    @MockitoBean
    private RoleInitializer roleInitializer;

    @MockitoBean
    private SystemSeedDumpService systemSeedDumpService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PermissionService permissionService;

    @MockitoBean
    private com.classification.domain_system.context.AuthContext authContext;

    @Test
    @DisplayName("deleteRole - 역할 삭제 성공 시 200 OK")
    void deleteRole_Success() throws Exception {
        UUID roleId = UUID.randomUUID();

        when(roleService.deleteRole(roleId)).thenReturn(true);

        mockMvc.perform(delete("/api/roles/" + roleId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(roleService).deleteRole(roleId);
    }

    @Test
    @DisplayName("deleteRole - 존재하지 않는 역할 삭제 시 404 Not Found")
    void deleteRole_NotFound() throws Exception {
        UUID roleId = UUID.randomUUID();

        when(roleService.deleteRole(roleId)).thenReturn(false);

        mockMvc.perform(delete("/api/roles/" + roleId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(roleService).deleteRole(roleId);
    }

    @Test
    @DisplayName("syncDefaultRolesForAllOrganizations - 전체 조직 대상 기본 역할 및 퍼미션 동기화 성공")
    void syncDefaultRolesForAllOrganizations_Success() throws Exception {
        mockMvc.perform(post("/api/roles/sync-defaults")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(roleInitializer).syncDefaultRolesForAllOrganizations();
    }

    @Test
    @DisplayName("syncDefaultRolesForOrg - 특정 조직 대상 기본 역할 및 퍼미션 동기화 성공")
    void syncDefaultRolesForOrg_Success() throws Exception {
        UUID orgId = UUID.randomUUID();

        mockMvc.perform(post("/api/roles/org/" + orgId + "/sync-defaults")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(roleInitializer).createDefaultRolesForOrg(orgId);
    }
}

