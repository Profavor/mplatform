package com.classification.domain_system.controller;

import com.classification.domain_system.entity.User;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Base64;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OidcAuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @InjectMocks
    private OidcAuthController oidcAuthController;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(oidcAuthController, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(oidcAuthController, "keycloakAuthUri", "http://keycloak:8080/auth/realms/mplatform/protocol/openid-connect/auth");
        ReflectionTestUtils.setField(oidcAuthController, "keycloakClientId", "mdm-frontend");
        ReflectionTestUtils.setField(oidcAuthController, "keycloakRealm", "mplatform");

        mockMvc = MockMvcBuilders.standaloneSetup(oidcAuthController)
                .setControllerAdvice(new com.classification.domain_system.exception.GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("GET /api/auth/oidc/login")
    class Login {

        @Test
        @DisplayName("웹 클라이언트 요청 시 Keycloak Auth URL로 302 리다이렉트한다")
        void login_WebClient_RedirectsToKeycloak() throws Exception {
            mockMvc.perform(get("/api/auth/oidc/login")
                            .param("client", "web")
                            .param("redirect", "/dashboard")
                            .header("Host", "mplatform.local")
                            .header("X-Forwarded-Proto", "https"))
                    .andExpect(status().isFound())
                    .andExpect(header().string("Location", containsString("http://keycloak:8080/auth/realms/mplatform/protocol/openid-connect/auth")))
                    .andExpect(header().string("Location", containsString("client_id=mdm-frontend")))
                    .andExpect(header().string("Location", containsString("redirect_uri=")))
                    .andExpect(header().string("Location", containsString("state=")));
        }

        @Test
        @DisplayName("모바일 클라이언트 요청 시 state에 mobile 정보가 포함되어 리다이렉트된다")
        void login_MobileClient_IncludesMobileState() throws Exception {
            mockMvc.perform(get("/api/auth/oidc/login")
                            .param("client", "mobile")
                            .param("redirect", "mplatform://oauth2redirect")
                            .header("Host", "mplatform.local"))
                    .andExpect(status().isFound())
                    .andExpect(header().string("Location", containsString("client_id=mdm-frontend")));
        }
    }

    @Nested
    @DisplayName("GET /api/auth/oidc/callback")
    class Callback {

        @Test
        @DisplayName("Keycloak 인증 오류(error 파라미터) 발생 시 웹 클라이언트는 로그인 오류 화면으로 리다이렉트된다")
        void callback_WithError_WebRedirectsToLogin() throws Exception {
            Map<String, String> stateMap = Map.of("client", "web", "redirect", "/dashboard");
            String state = Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(stateMap));

            mockMvc.perform(get("/api/auth/oidc/callback")
                            .param("error", "access_denied")
                            .param("state", state))
                    .andExpect(status().isFound())
                    .andExpect(header().string("Location", containsString("/login?error=access_denied")));
        }

        @Test
        @DisplayName("Keycloak 인증 오류 발생 시 모바일 클라이언트는 딥링크 오류로 리다이렉트된다")
        void callback_WithError_MobileRedirectsToDeepLink() throws Exception {
            Map<String, String> stateMap = Map.of("client", "mobile", "redirect", "mplatform://oauth2redirect");
            String state = Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(stateMap));

            mockMvc.perform(get("/api/auth/oidc/callback")
                            .param("error", "access_denied")
                            .param("state", state))
                    .andExpect(status().isFound())
                    .andExpect(header().string("Location", containsString("mplatform://oauth2redirect?error=access_denied")));
        }

        @Test
        @DisplayName("웹 클라이언트 콜백 성공 시 토큰 교환 후 쿠키를 설정하고 대상 경로로 리다이렉트한다")
        void callback_WebClient_Success() throws Exception {
            // given
            Map<String, String> stateMap = Map.of("client", "web", "redirect", "/records");
            String state = Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(stateMap));

            User user = new User();
            user.setId("user-uuid-1");
            user.setUsername("superadmin");
            user.setRole("ROLE_ADMIN");

            Map<String, Object> exchangeResult = Map.of(
                    "accessToken", "mock-access-token",
                    "refreshToken", "mock-refresh-token",
                    "expiresIn", 1800,
                    "username", "superadmin",
                    "user", user
            );

            given(authService.exchangeOidcCode(eq("valid-code"), any(), any(), any()))
                    .willReturn(exchangeResult);

            // when & then
            mockMvc.perform(get("/api/auth/oidc/callback")
                            .param("code", "valid-code")
                            .param("state", state)
                            .header("Host", "mplatform.local")
                            .header("X-Forwarded-Proto", "https"))
                    .andExpect(status().isFound())
                    .andExpect(header().string("Location", "/records"))
                    .andExpect(cookie().value("auth_token", "mock-access-token"))
                    .andExpect(cookie().value("refresh_token", "mock-refresh-token"))
                    .andExpect(cookie().exists("user_data"));

            verify(authService).exchangeOidcCode(eq("valid-code"), any(), any(), any());
        }

        @Test
        @DisplayName("모바일 클라이언트 콜백 성공 시 토큰을 포함한 딥링크로 리다이렉트한다")
        void callback_MobileClient_Success() throws Exception {
            // given
            Map<String, String> stateMap = Map.of("client", "mobile", "redirect", "mplatform://oauth2redirect");
            String state = Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(stateMap));

            Map<String, Object> exchangeResult = Map.of(
                    "accessToken", "mock-access-token",
                    "refreshToken", "mock-refresh-token",
                    "expiresIn", 1800,
                    "username", "mobileuser"
            );

            given(authService.exchangeOidcCode(eq("mobile-code"), any(), any(), any()))
                    .willReturn(exchangeResult);

            // when & then
            mockMvc.perform(get("/api/auth/oidc/callback")
                            .param("code", "mobile-code")
                            .param("state", state))
                    .andExpect(status().isFound())
                    .andExpect(header().string("Location", containsString("mplatform://oauth2redirect?access_token=mock-access-token&refresh_token=mock-refresh-token")));
        }

        @Test
        @DisplayName("모바일 웹 클라이언트 콜백 성공 시 /mobile/ 경로로 토큰과 함께 리다이렉트한다")
        void callback_MobileWebClient_Success() throws Exception {
            // given
            Map<String, String> stateMap = Map.of("client", "mobile_web", "redirect", "/mobile/");
            String state = Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(stateMap));

            Map<String, Object> exchangeResult = Map.of(
                    "accessToken", "mock-access-token",
                    "refreshToken", "mock-refresh-token",
                    "expiresIn", 1800,
                    "username", "mobilewebuser"
            );

            given(authService.exchangeOidcCode(eq("mobile-web-code"), any(), any(), any()))
                    .willReturn(exchangeResult);

            // when & then
            mockMvc.perform(get("/api/auth/oidc/callback")
                            .param("code", "mobile-web-code")
                            .param("state", state))
                    .andExpect(status().isFound())
                    .andExpect(header().string("Location", containsString("/mobile/?access_token=mock-access-token&refresh_token=mock-refresh-token")));
        }
    }
}
