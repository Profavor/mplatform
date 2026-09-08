package com.classification.domain_system.service;

import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.repository.LoginLogRepository;
import com.classification.domain_system.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private LoginLogRepository loginLogRepository;

    @Mock
    private com.classification.domain_system.repository.OrganizationRepository organizationRepository;

    @Mock
    private com.classification.domain_system.repository.DomainPermissionRepository domainPermissionRepository;

    @Mock
    private SpecializedDomainTemplateService specializedDomainTemplateService;

    @Mock
    private com.classification.domain_system.websocket.WebSocketPublisher webSocketPublisher;

    @Mock
    private TwoFactorAuthService twoFactorAuthService;

    @InjectMocks
    private AuthService authService;

    private User createTestUser(String id, String username, String password, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        return user;
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("성공 - 신규 사용자를 등록한다")
        void success() {
            // given
            given(userRepository.findByUsername("newuser")).willReturn(Optional.empty());
            given(passwordEncoder.encode("password123")).willReturn("encoded_password");

            // when
            authService.register("newuser", "password123", "USER");

            // then
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("성공 - 신규 사용자 등록 시 지정된 타임존을 설정하여 저장한다")
        void registerWithTimezoneSuccess() {
            // given
            given(userRepository.findByUsername("tzuser")).willReturn(Optional.empty());
            given(passwordEncoder.encode("password123")).willReturn("encoded_password");

            // when
            authService.register("tzuser", "password123", "ROLE_USER", "America/New_York");

            // then
            org.mockito.ArgumentCaptor<User> userCaptor = org.mockito.ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getTimezone()).isEqualTo("America/New_York");
            assertThat(userCaptor.getValue().getRole()).isEqualTo("ROLE_USER");
        }

        @Test
        @DisplayName("실패 - 중복된 username으로 등록 시 예외 발생")
        void failDuplicateUsername() {
            // given
            User existing = createTestUser("id-1", "existinguser", "encoded", "USER");
            given(userRepository.findByUsername("existinguser")).willReturn(Optional.of(existing));

            // when & then
            assertThatThrownBy(() -> authService.register("existinguser", "password", "USER"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("성공 - username 존재 여부를 확인한다")
        void existsByUsernameTest() {
            // given
            given(userRepository.findByUsername("existing")).willReturn(Optional.of(new User()));
            given(userRepository.findByUsername("not_existing")).willReturn(Optional.empty());

            // when & then
            assertThat(authService.existsByUsername("existing")).isTrue();
            assertThat(authService.existsByUsername("not_existing")).isFalse();
        }
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("성공 - 올바른 자격증명으로 JWT 토큰을 반환한다")
        void success() {
            // given
            User user = createTestUser("user-id-1", "admin", "encoded_pw", "ADMIN");
            given(userRepository.findByUsername("admin")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("rawpassword", "encoded_pw")).willReturn(true);
            given(jwtUtil.generateToken(anyString(), anyString(), anyString(), anyString())).willReturn("jwt.token.here");

            // when
            String token = authService.login("admin", "rawpassword", "127.0.0.1");

            // then
            assertThat(token).isEqualTo("jwt.token.here");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 username으로 로그인 시 예외 발생")
        void failUserNotFound() {
            // given
            given(userRepository.findByUsername("nonexistent")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.login("nonexistent", "password", "127.0.0.1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid credentials");
        }

        @Test
        @DisplayName("실패 - 잘못된 패스워드로 로그인 시 예외 발생")
        void failWrongPassword() {
            // given
            User user = createTestUser("user-id-1", "admin", "encoded_pw", "ADMIN");
            given(userRepository.findByUsername("admin")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("wrongpassword", "encoded_pw")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login("admin", "wrongpassword", "127.0.0.1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid credentials");
        }

        @Test
        @DisplayName("실패 - 계정이 잠긴 상태에서 로그인 시도 시 예외 발생")
        void failAccountLocked() {
            // given
            User user = createTestUser("user-id-1", "admin", "encoded_pw", "ADMIN");
            user.setFailedLoginCount(5);
            user.setLockedUntil(java.time.LocalDateTime.now().plusMinutes(15));
            given(userRepository.findByUsername("admin")).willReturn(Optional.of(user));

            // when & then
            assertThatThrownBy(() -> authService.login("admin", "any_password", "127.0.0.1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Account is temporarily locked");
            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("실패 - 4회 실패 후 5번째 로그인 실패 시 계정이 15분 간 잠긴다")
        void lockoutAfter5thFailure() {
            // given
            User user = createTestUser("user-id-1", "admin", "encoded_pw", "ADMIN");
            user.setFailedLoginCount(4);
            given(userRepository.findByUsername("admin")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("wrongpassword", "encoded_pw")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login("admin", "wrongpassword", "127.0.0.1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid credentials");

            assertThat(user.getFailedLoginCount()).isEqualTo(5);
            assertThat(user.getLockedUntil()).isNotNull();
            verify(userRepository).saveAndFlush(user);
        }
    }

    @Nested
    @DisplayName("findByUsername")
    class FindByUsername {

        @Test
        @DisplayName("성공 - 사용자를 찾아 반환한다")
        void success() {
            // given
            User user = createTestUser("user-id-1", "admin", "pw", "ADMIN");
            given(userRepository.findByUsername("admin")).willReturn(Optional.of(user));

            // when
            User result = authService.findByUsername("admin");

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("admin");
        }

        @Test
        @DisplayName("실패 - 사용자를 찾지 못하면 null을 반환한다")
        void returnsNull() {
            // given
            given(userRepository.findByUsername("unknown")).willReturn(Optional.empty());

            // when
            User result = authService.findByUsername("unknown");

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("TwoFactorAuth")
    class TwoFactorAuth {

        @Test
        @DisplayName("2FA 필수 사용자인 경우 loginWithTokens는 tempToken과 twoFactorRequired=true를 반환한다")
        void loginWithTokens_twoFactorRequired() {
            // given
            User user = createTestUser("user-id-1", "admin", "encoded_pw", "ROLE_ADMIN");
            user.setEmail("admin@example.com");
            user.setTwoFactorType("TOTP");
            given(userRepository.findByUsername("admin")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("rawpassword", "encoded_pw")).willReturn(true);
            given(twoFactorAuthService.isTwoFactorRequired(user)).willReturn(true);

            // when
            java.util.Map<String, String> response = authService.loginWithTokens("admin", "rawpassword", "127.0.0.1", "Mozilla/5.0");

            // then
            assertThat(response).containsEntry("twoFactorRequired", "true");
            assertThat(response).containsKey("tempToken");
            assertThat(response.get("tempToken")).isNotBlank();
            assertThat(response).containsEntry("twoFactorType", "TOTP");
            assertThat(response).containsEntry("maskedEmail", "ad***@example.com");
            assertThat(response).doesNotContainKey("token");
        }

        @Test
        @DisplayName("2FA 불필요 사용자인 경우 loginWithTokens는 정식 JWT 토큰을 즉시 반환한다")
        void loginWithTokens_normalUser() {
            // given
            User user = createTestUser("user-id-2", "viewer", "encoded_pw", "ROLE_VIEWER");
            given(userRepository.findByUsername("viewer")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("rawpassword", "encoded_pw")).willReturn(true);
            given(twoFactorAuthService.isTwoFactorRequired(user)).willReturn(false);
            given(jwtUtil.generateToken(anyString(), anyString(), anyString(), anyString())).willReturn("access.token.jwt");
            given(jwtUtil.generateRefreshToken(anyString(), anyString(), anyString(), anyString())).willReturn("refresh.token.jwt");

            // when
            java.util.Map<String, String> response = authService.loginWithTokens("viewer", "rawpassword", "127.0.0.1", "Mozilla/5.0");

            // then
            assertThat(response).doesNotContainKey("twoFactorRequired");
            assertThat(response).containsEntry("token", "access.token.jwt");
            assertThat(response).containsEntry("refreshToken", "refresh.token.jwt");
        }

        @Test
        @DisplayName("임시 2FA 토큰 검증 및 최종 토큰 발급 테스트")
        void tempTokenValidationAndFinalTokenIssue() {
            // given
            String tempToken = authService.generateTempToken("admin", "127.0.0.1", "Mozilla/5.0");
            assertThat(authService.validateTempToken(tempToken, "admin")).isTrue();
            assertThat(authService.validateTempToken(tempToken, "otherUser")).isFalse();
            assertThat(authService.validateTempToken("invalid-token", "admin")).isFalse();

            User user = createTestUser("user-id-1", "admin", "encoded_pw", "ROLE_ADMIN");
            given(jwtUtil.generateToken(anyString(), anyString(), anyString(), anyString())).willReturn("final.access.token");
            given(jwtUtil.generateRefreshToken(anyString(), anyString(), anyString(), anyString())).willReturn("final.refresh.token");

            // when
            java.util.Map<String, String> finalTokens = authService.issueFinalTokensAfter2Fa(user, "127.0.0.1", "Mozilla/5.0", "TOTP");

            // then
            assertThat(finalTokens).containsEntry("token", "final.access.token");
            assertThat(finalTokens).containsEntry("refreshToken", "final.refresh.token");
            verify(loginLogRepository).save(any());

            // consume tempToken
            authService.consumeTempToken(tempToken);
            assertThat(authService.validateTempToken(tempToken, "admin")).isFalse();
        }
    }

    @Nested
    @DisplayName("recordLoginLog")
    class RecordLoginLog {

        @Test
        @DisplayName("정상 로그인 이력 적재 시 LoginLog가 성공적으로 저장된다")
        void recordLoginLog_Success() {
            // given
            User user = createTestUser("user-uuid-1", "testuser", "pw", "ROLE_USER");
            given(loginLogRepository.existsByUsernameAndLoginAtAfter(eq("testuser"), any())).willReturn(false);
            given(userRepository.findByUsername("testuser")).willReturn(Optional.of(user));

            // when
            authService.recordLoginLog("testuser", "192.168.1.100", "Mozilla/5.0 Chrome");

            // then
            org.mockito.ArgumentCaptor<com.classification.domain_system.entity.LoginLog> captor =
                    org.mockito.ArgumentCaptor.forClass(com.classification.domain_system.entity.LoginLog.class);
            verify(loginLogRepository).save(captor.capture());
            com.classification.domain_system.entity.LoginLog saved = captor.getValue();
            assertThat(saved.getUsername()).isEqualTo("testuser");
            assertThat(saved.getUserId()).isEqualTo("user-uuid-1");
            assertThat(saved.getClientIp()).isEqualTo("192.168.1.100");
            assertThat(saved.getUserAgent()).isEqualTo("Mozilla/5.0 Chrome");
        }

        @Test
        @DisplayName("30초 이내 동일 사용자의 중복 로그인 요청은 저장을 스킵한다")
        void recordLoginLog_Duplicate_Skipped() {
            // given
            given(loginLogRepository.existsByUsernameAndLoginAtAfter(eq("testuser"), any())).willReturn(true);

            // when
            authService.recordLoginLog("testuser", "192.168.1.100", "Mozilla/5.0 Chrome");

            // then
            verify(loginLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("UserAgent가 500자를 초과하는 경우 안전하게 500자로 잘라내어 저장한다")
        void recordLoginLog_TruncatesLongUserAgent() {
            // given
            String longUserAgent = "A".repeat(600);
            given(loginLogRepository.existsByUsernameAndLoginAtAfter(eq("testuser"), any())).willReturn(false);
            given(userRepository.findByUsername("testuser")).willReturn(Optional.empty());

            // when
            authService.recordLoginLog("testuser", "127.0.0.1", longUserAgent);

            // then
            org.mockito.ArgumentCaptor<com.classification.domain_system.entity.LoginLog> captor =
                    org.mockito.ArgumentCaptor.forClass(com.classification.domain_system.entity.LoginLog.class);
            verify(loginLogRepository).save(captor.capture());
            com.classification.domain_system.entity.LoginLog saved = captor.getValue();
            assertThat(saved.getUserAgent()).hasSize(500);
        }
    }

    @Nested
    @DisplayName("OIDC & JWT Token Helper Tests")
    class OidcHelperTests {

        @Test
        @DisplayName("extractUsernameFromJwt - 유효한 JWT 페이로드에서 preferred_username을 정상 추출한다")
        void extractUsernameFromJwt_ExtractsPreferredUsername() {
            // given: header.payload.signature
            // payload: {"preferred_username":"superadmin","sub":"user-123"}
            String payload = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("{\"preferred_username\":\"superadmin\",\"sub\":\"user-123\"}".getBytes());
            String dummyJwt = "eyJhbGciOiJSUzI1NiJ9." + payload + ".dummySignature";

            // when
            String username = authService.extractUsernameFromJwt(dummyJwt);

            // then
            assertThat(username).isEqualTo("superadmin");
        }

        @Test
        @DisplayName("extractUsernameFromJwt - preferred_username이 없으면 sub를 폴백으로 추출한다")
        void extractUsernameFromJwt_FallsBackToSub() {
            String payload = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("{\"sub\":\"user-uuid-999\"}".getBytes());
            String dummyJwt = "eyJhbGciOiJSUzI1NiJ9." + payload + ".dummySignature";

            // when
            String username = authService.extractUsernameFromJwt(dummyJwt);

            // then
            assertThat(username).isEqualTo("user-uuid-999");
        }

        @Test
        @DisplayName("extractUsernameFromJwt - 잘못된 토큰 형식인 경우 null을 반환한다")
        void extractUsernameFromJwt_MalformedReturnsNull() {
            assertThat(authService.extractUsernameFromJwt(null)).isNull();
            assertThat(authService.extractUsernameFromJwt("invalid.token")).isNull();
            assertThat(authService.extractUsernameFromJwt("not-a-jwt")).isNull();
        }

        @Test
        @DisplayName("exchangeOidcCode - 인가 코드가 빈 값인 경우 INVALID_REQUEST 예외 발생")
        void exchangeOidcCode_EmptyCode_ThrowsException() {
            assertThatThrownBy(() -> authService.exchangeOidcCode("", "http://localhost/callback", "127.0.0.1", "agent"))
                    .isInstanceOf(com.classification.domain_system.exception.BusinessException.class);
        }
    }
}
