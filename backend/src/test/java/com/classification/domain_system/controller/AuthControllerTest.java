package com.classification.domain_system.controller;

import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.security.JwtUtil;
import com.classification.domain_system.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private com.classification.domain_system.service.PermissionService permissionService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        lenient().when(permissionService.getAuthoritiesForUser(any(), any())).thenReturn(List.of());
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    // ─────────────────────────────────────────────────────────────────
    // login 테스트
    // ─────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("정상 로그인 시 200 OK + 토큰 및 리프레시 토큰 반환")
        void success_Returns200WithToken() throws Exception {
            User user = new User();
            user.setId(UUID.randomUUID().toString());
            user.setUsername("testuser");
            user.setRole("USER");

            Map<String, String> tokens = new HashMap<>();
            tokens.put("token", "access-token-123");
            tokens.put("refreshToken", "refresh-token-456");

            when(authService.loginWithTokens(eq("testuser"), eq("password"), any(), any())).thenReturn(tokens);
            when(authService.findByUsername("testuser")).thenReturn(user);

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"testuser\",\"password\":\"password\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("access-token-123"))
                    .andExpect(jsonPath("$.refreshToken").value("refresh-token-456"))
                    .andExpect(jsonPath("$.username").value("testuser"))
                    .andExpect(jsonPath("$.role").value("USER"));
        }

        @Test
        @DisplayName("비밀번호 불일치 시 401 Unauthorized 반환")
        void wrongPassword_Returns401() throws Exception {
            when(authService.loginWithTokens(eq("testuser"), eq("wrong"), any(), any()))
                    .thenThrow(new RuntimeException("Invalid credentials"));

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"testuser\",\"password\":\"wrong\"}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Invalid credentials"));
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // refreshToken 테스트
    // ─────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("refreshToken")
    class RefreshToken {

        @Test
        @DisplayName("유효한 리프레시 토큰 전달 시 200 OK + 새 토큰 세트 반환")
        void success_ReturnsNewTokens() throws Exception {
            Map<String, String> tokens = new HashMap<>();
            tokens.put("token", "new-access-token");
            tokens.put("refreshToken", "new-refresh-token");

            when(authService.refreshTokens("valid-refresh-token")).thenReturn(tokens);

            mockMvc.perform(post("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"refreshToken\":\"valid-refresh-token\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("new-access-token"))
                    .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
        }

        @Test
        @DisplayName("유효하지 않은 리프레시 토큰 전달 시 401 Unauthorized 반환")
        void invalidRefreshToken_Returns401() throws Exception {
            when(authService.refreshTokens("invalid-token"))
                    .thenThrow(new RuntimeException("Invalid refresh token"));

            mockMvc.perform(post("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"refreshToken\":\"invalid-token\"}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Invalid refresh token"));
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // register 테스트
    // ─────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("정상 회원가입 시 200 OK 반환")
        void success_Returns200() throws Exception {
            doNothing().when(authService).register(any(), any(), any(), any());

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"newuser\",\"password\":\"password\",\"role\":\"USER\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("User registered successfully"));
        }

        @Test
        @DisplayName("이미 존재하는 사용자면 400 Bad Request 반환")
        void duplicateUser_Returns400() throws Exception {
            doThrow(new RuntimeException("Username already exists"))
                    .when(authService).register(any(), any(), any(), any());

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"exists\",\"password\":\"pass\",\"role\":\"USER\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Username already exists"));
        }

        @Test
        @DisplayName("아이디 중복 검사 - 사용 가능한 아이디면 available: true 반환")
        void checkUsername_Available_Returns200() throws Exception {
            org.mockito.BDDMockito.given(authService.existsByUsername("newbie")).willReturn(false);

            mockMvc.perform(get("/api/auth/check-username").param("username", "newbie"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.exists").value(false))
                    .andExpect(jsonPath("$.available").value(true));
        }

        @Test
        @DisplayName("아이디 중복 검사 - 이미 존재하는 아이디면 available: false 반환")
        void checkUsername_Exists_Returns200() throws Exception {
            org.mockito.BDDMockito.given(authService.existsByUsername("existing")).willReturn(true);

            mockMvc.perform(get("/api/auth/check-username").param("username", "existing"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.exists").value(true))
                    .andExpect(jsonPath("$.available").value(false));
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // getAllUsers 테스트
    // ─────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsers {

        @Test
        @DisplayName("사용자 목록 반환")
        void success_ReturnsUserList() throws Exception {
            User user = new User();
            user.setId(UUID.randomUUID().toString());
            user.setUsername("testuser");
            user.setRole("USER");

            when(userRepository.findAll()).thenReturn(List.of(user));

            mockMvc.perform(get("/api/auth/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].username").value("testuser"))
                    .andExpect(jsonPath("$[0].role").value("USER"));
        }

        @Test
        @DisplayName("사용자 없으면 빈 배열 반환")
        void noUsers_ReturnsEmptyList() throws Exception {
            when(userRepository.findAll()).thenReturn(List.of());

            mockMvc.perform(get("/api/auth/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }
}
