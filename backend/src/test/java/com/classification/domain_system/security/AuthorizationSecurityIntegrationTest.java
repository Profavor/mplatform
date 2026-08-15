package com.classification.domain_system.security;

import com.classification.domain_system.context.AuthContext;
import com.classification.domain_system.dto.AdminUserUpdateDto;
import com.classification.domain_system.dto.SelfUserUpdateDto;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.entity.Role;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.repository.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthorizationSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RoleRepository roleRepository;

    @MockitoBean
    private AuthContext authContext;

    @MockitoBean
    private SecurityUtils securityUtils;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        adminRole.setPermissions(Set.of("*"));

        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        userRole.setPermissions(Set.of("user:read"));

        given(roleRepository.findAll()).willReturn(List.of(adminRole, userRole));

        userToken = jwtUtil.generateToken("regular_user", "ROLE_USER", "user-1");
        adminToken = jwtUtil.generateToken("admin_user", "ROLE_ADMIN", "admin-1");
    }

    @Test
    @DisplayName("P0 - 일반 사용자(ROLE_USER)가 PUT /api/users/{id} 로 role: ADMIN 변경 시도 시 403 Forbidden")
    void regularUser_CannotUpdateUserAdminFields() throws Exception {
        AdminUserUpdateDto updateDto = new AdminUserUpdateDto();
        updateDto.setRole("ADMIN");

        mockMvc.perform(put("/api/users/target-user-1")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("P0 - 일반 사용자(ROLE_USER)가 PUT /api/users/me 호출 시 본인 정보(timezone)만 수정 가능")
    void regularUser_CanUpdateSelfProfile() throws Exception {
        User user = new User();
        user.setId("user-1");
        user.setUsername("regular_user");
        user.setRole("ROLE_USER");
        user.setTimezone("Asia/Seoul");

        given(authContext.getUserId()).willReturn("user-1");
        given(securityUtils.getCurrentUserIdOrThrow()).willReturn("regular_user");
        given(userRepository.findById("user-1")).willReturn(Optional.of(user));
        given(userRepository.findByUsername("regular_user")).willReturn(Optional.of(user));
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

        SelfUserUpdateDto selfDto = new SelfUserUpdateDto();
        selfDto.setTimezone("UTC");

        mockMvc.perform(put("/api/users/me")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(selfDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("P0 - 관리자 사용자(ROLE_ADMIN)는 PUT /api/users/{id} 로 타인 사용자 역할 수정 성공")
    void adminUser_CanUpdateUserAdminFields() throws Exception {
        User targetUser = new User();
        targetUser.setId("target-user-1");
        targetUser.setUsername("target_user");
        targetUser.setRole("ROLE_USER");

        given(authContext.getUserId()).willReturn("admin-1");
        given(userRepository.findById("target-user-1")).willReturn(Optional.of(targetUser));
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

        AdminUserUpdateDto updateDto = new AdminUserUpdateDto();
        updateDto.setRole("ADMIN");

        mockMvc.perform(put("/api/users/target-user-1")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("P1 - 일반 사용자(ROLE_USER)가 Role C/U/D 엔드포인트 접근 시 403 Forbidden")
    void regularUser_CannotAccessRoleManagementEndpoints() throws Exception {
        mockMvc.perform(get("/api/roles")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/roles")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"NEW_ROLE\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/roles/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("P1 - 일반 사용자(ROLE_USER)가 Organization C/U/D 엔드포인트 접근 시 403 Forbidden")
    void regularUser_CannotAccessOrganizationEndpoints() throws Exception {
        mockMvc.perform(post("/api/organizations")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"NEW_ORG\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("P1 - 일반 사용자(ROLE_USER)가 WorkflowConfig C/U/D 엔드포인트 접근 시 403 Forbidden")
    void regularUser_CannotAccessWorkflowConfigEndpoints() throws Exception {
        mockMvc.perform(post("/api/workflow-configs/domain/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
                .andExpect(status().isForbidden());
    }
}
