package com.classification.domain_system.websocket;

import com.classification.domain_system.security.JwtUtil;
import com.classification.domain_system.service.PermissionService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompJwtInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final PermissionService permissionService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            if (token == null || token.isBlank()) {
                token = accessor.getFirstNativeHeader("token");
            }
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            if (token == null || !jwtUtil.isTokenValid(token)) {
                log.warn("[STOMP] Rejected connection due to invalid or missing JWT token.");
                throw new AccessDeniedException("Invalid or missing JWT token for STOMP connection");
            }

            try {
                String username = jwtUtil.extractUsername(token);
                Claims claims = jwtUtil.extractAllClaims(token);
                String roleStr = claims.get("role", String.class);

                Collection<GrantedAuthority> authorities = permissionService.getAuthoritiesForUser(username, roleStr);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
                
                StompHeaderAccessor mutableAccessor = accessor.isMutable() ? accessor : (StompHeaderAccessor) MessageHeaderAccessor.getMutableAccessor(message);
                mutableAccessor.setUser(auth);
                
                return MessageBuilder.createMessage(message.getPayload(), mutableAccessor.getMessageHeaders());
            } catch (Exception e) {
                log.warn("[STOMP] Exception while evaluating JWT during CONNECT: {}", e.getMessage());
                throw new AccessDeniedException("Invalid or missing JWT token for STOMP connection", e);
            }
        }
        return message;
    }
}
