package com.classification.domain_system.service;

import com.classification.domain_system.repository.LoginLogRepository;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceRefreshTokenTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private LoginLogRepository loginLogRepository;

    @Mock
    private com.classification.domain_system.websocket.WebSocketPublisher webSocketPublisher;

    @Mock
    private SseNotificationService sseNotificationService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "keycloakTokenUri", "http://keycloak:8080/auth/realms/mplatform/protocol/openid-connect/token");
        ReflectionTestUtils.setField(authService, "keycloakClientId", "mdm-frontend");
        ReflectionTestUtils.setField(authService, "keycloakClientSecret", "secret");
        ReflectionTestUtils.setField(authService, "restTemplate", restTemplate);
    }

    private String createMockJwtWithIssuer(String issuer) {
        String header = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"RS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(("{\"iss\":\"" + issuer + "\",\"sub\":\"user-123\",\"preferred_username\":\"superadmin\"}").getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + ".mockSignature";
    }

    @Test
    @DisplayName("성공 - Keycloak 토큰의 iss로부터 X-Forwarded-Host 및 X-Forwarded-Proto를 추출하고 client_secret을 포함하여 갱신 요청을 전송한다")
    void refreshTokens_withKeycloakIss_forwardsHostAndSecret() {
        // given
        String refreshToken = createMockJwtWithIssuer("http://mplatform.local/auth/realms/mplatform");

        Map<String, Object> kcResponseBody = new HashMap<>();
        kcResponseBody.put("access_token", "new-access-token-xyz");
        kcResponseBody.put("refresh_token", "new-refresh-token-xyz");
        kcResponseBody.put("session_state", "sid-123");

        given(restTemplate.postForEntity(eq("http://keycloak:8080/auth/realms/mplatform/protocol/openid-connect/token"), any(HttpEntity.class), eq(Map.class)))
                .willReturn(ResponseEntity.ok(kcResponseBody));

        // when
        Map<String, String> result = authService.refreshTokens(refreshToken);

        // then
        assertThat(result).isNotNull();
        assertThat(result.get("token")).isEqualTo("new-access-token-xyz");
        assertThat(result.get("refreshToken")).isEqualTo("new-refresh-token-xyz");

        // 요청 캡처 및 헤더/바디 검증
        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(eq("http://keycloak:8080/auth/realms/mplatform/protocol/openid-connect/token"), captor.capture(), eq(Map.class));

        HttpEntity<MultiValueMap<String, String>> capturedRequest = captor.getValue();
        assertThat(capturedRequest.getHeaders().getFirst("X-Forwarded-Host")).isEqualTo("mplatform.local");
        assertThat(capturedRequest.getHeaders().getFirst("X-Forwarded-Proto")).isEqualTo("http");

        MultiValueMap<String, String> body = capturedRequest.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getFirst("client_id")).isEqualTo("mdm-frontend");
        assertThat(body.getFirst("client_secret")).isEqualTo("secret");
        assertThat(body.getFirst("grant_type")).isEqualTo("refresh_token");
        assertThat(body.getFirst("refresh_token")).isEqualTo(refreshToken);
    }

    @Test
    @DisplayName("성공 - HTTPS 포트가 포함된 커스텀 도메인 토큰도 X-Forwarded-Host와 Proto를 정확히 추출하여 전송한다")
    void refreshTokens_withHttpsCustomDomain_forwardsExactHostAndHttps() {
        // given
        String refreshToken = createMockJwtWithIssuer("https://mdm.mplat.store:8443/auth/realms/mplatform");

        Map<String, Object> kcResponseBody = new HashMap<>();
        kcResponseBody.put("access_token", "new-access-token-https");
        kcResponseBody.put("refresh_token", "new-refresh-token-https");

        given(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Map.class)))
                .willReturn(ResponseEntity.ok(kcResponseBody));

        // when
        Map<String, String> result = authService.refreshTokens(refreshToken);

        // then
        assertThat(result.get("token")).isEqualTo("new-access-token-https");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(any(String.class), captor.capture(), eq(Map.class));

        HttpEntity<MultiValueMap<String, String>> capturedRequest = captor.getValue();
        assertThat(capturedRequest.getHeaders().getFirst("X-Forwarded-Host")).isEqualTo("mdm.mplat.store:8443");
        assertThat(capturedRequest.getHeaders().getFirst("X-Forwarded-Proto")).isEqualTo("https");
    }
}
