package com.classification.domain_system.controller;

import com.classification.domain_system.controller.UserController.UserDto;
import com.classification.domain_system.service.UserService;
import com.classification.domain_system.service.PermissionService;
import com.classification.domain_system.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@org.springframework.context.annotation.Import({com.classification.domain_system.config.SecurityConfig.class, com.classification.domain_system.config.TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private com.classification.domain_system.security.SecurityUtils securityUtils;

    @MockitoBean
    private com.classification.domain_system.repository.UserRepository userRepository;

    @MockitoBean
    private com.classification.domain_system.repository.UserRoleRepository userRoleRepository;

    @MockitoBean
    private com.classification.domain_system.repository.UserOrgHistoryRepository userOrgHistoryRepository;

    @MockitoBean
    private com.classification.domain_system.repository.DepartmentRepository departmentRepository;

    @MockitoBean
    private com.classification.domain_system.repository.TeamRepository teamRepository;

    @MockitoBean
    private com.classification.domain_system.repository.OrganizationRepository organizationRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PermissionService permissionService;

    @MockitoBean
    private com.classification.domain_system.service.FieldEncryptionService fieldEncryptionService;

    @MockitoBean
    private com.classification.domain_system.context.AuthContext authContext;

    @Test
    @DisplayName("getAllUsers - 전체 유저를 UserDto로 반환하는 서비스 호출")
    void getAllUsers_ReturnsMappedUserDtos() throws Exception {
        UserDto u1 = new UserDto("user-1-id", "alice", "ADMIN");
        UserDto u2 = new UserDto("user-2-id", "bob", "USER");

        when(userService.getAllUsers()).thenReturn(List.of(u1, u2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("user-1-id"))
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[0].role").value("ADMIN"))
                .andExpect(jsonPath("$[1].id").value("user-2-id"))
                .andExpect(jsonPath("$[1].username").value("bob"))
                .andExpect(jsonPath("$[1].role").value("USER"));
    }

    @Test
    @DisplayName("updateUser - 사용자 정보 및 이메일 수정 성공")
    void updateUser_Success() throws Exception {
        com.classification.domain_system.entity.User user = new com.classification.domain_system.entity.User();
        user.setId("user-1-id");
        user.setUsername("alice");
        user.setEmail("alice_new@example.com");
        user.setRole("ROLE_ADMIN");

        when(userService.updateAdminUserInfo(org.mockito.ArgumentMatchers.eq("user-1-id"), org.mockito.ArgumentMatchers.any(com.classification.domain_system.dto.AdminUserUpdateDto.class)))
                .thenReturn(user);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/users/user-1-id")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"email\":\"alice_new@example.com\",\"role\":\"ROLE_ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user-1-id"))
                .andExpect(jsonPath("$.email").value("alice_new@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("updateUser - 이메일 중복 시 400 Bad Request 반환")
    void updateUser_DuplicateEmail_ReturnsBadRequest() throws Exception {
        when(userService.updateAdminUserInfo(org.mockito.ArgumentMatchers.eq("user-1-id"), org.mockito.ArgumentMatchers.any(com.classification.domain_system.dto.AdminUserUpdateDto.class)))
                .thenThrow(new IllegalArgumentException("이미 다른 사용자가 사용 중인 이메일 주소입니다: duplicate@example.com"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/users/user-1-id")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"email\":\"duplicate@example.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 다른 사용자가 사용 중인 이메일 주소입니다: duplicate@example.com"));
    }
}
