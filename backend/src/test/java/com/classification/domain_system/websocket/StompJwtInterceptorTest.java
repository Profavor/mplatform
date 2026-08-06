package com.classification.domain_system.websocket;

import com.classification.domain_system.security.JwtUtil;
import com.classification.domain_system.service.PermissionService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class StompJwtInterceptorTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private StompJwtInterceptor interceptor;

    private MessageChannel mockChannel;

    @BeforeEach
    void setUp() {
        mockChannel = mock(MessageChannel.class);
    }

    @Test
    @DisplayName("CONNECT 프레임에 JWT 토큰이 없으면 AccessDeniedException 예외 발생 (Red)")
    void connect_WithoutToken_ThrowsException() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThatThrownBy(() -> interceptor.preSend(message, mockChannel))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Invalid or missing JWT token for STOMP connection");
    }

    @Test
    @DisplayName("CONNECT 프레임에 유효하지 않은 JWT 토큰 전달 시 AccessDeniedException 예외 발생 (Red)")
    void connect_WithInvalidToken_ThrowsException() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer invalid-token-123");
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        given(jwtUtil.isTokenValid("invalid-token-123")).willReturn(false);

        assertThatThrownBy(() -> interceptor.preSend(message, mockChannel))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Invalid or missing JWT token for STOMP connection");
    }

    @Test
    @DisplayName("CONNECT 프레임에 정상 JWT 토큰 전달 시 Authentication 설정 통과 (Green)")
    void connect_WithValidToken_SetsUserAuthentication() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer valid.jwt.token");
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Claims mockClaims = mock(Claims.class);
        given(jwtUtil.isTokenValid("valid.jwt.token")).willReturn(true);
        given(jwtUtil.extractUsername("valid.jwt.token")).willReturn("stomp_user");
        given(jwtUtil.extractAllClaims("valid.jwt.token")).willReturn(mockClaims);
        given(mockClaims.get("role", String.class)).willReturn("ROLE_USER");
        given(permissionService.getAuthoritiesForUser("stomp_user", "ROLE_USER"))
                .willReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));

        Message<?> resultMessage = interceptor.preSend(message, mockChannel);
        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(resultMessage);

        Authentication auth = (Authentication) resultAccessor.getUser();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("stomp_user");
        assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
    }
}
