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
    private com.classification.domain_system.websocket.WebSocketPublisher webSocketPublisher;

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
}
