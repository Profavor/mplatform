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
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

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
}
