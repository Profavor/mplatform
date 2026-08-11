package com.classification.domain_system.websocket;

import com.classification.domain_system.security.JwtUtil;
import com.classification.domain_system.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompJwtInterceptor implements ChannelInterceptor {

    @Autowired(required = false)
    private JwtDecoder jwtDecoder;

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

            if (token == null) {
                log.warn("[STOMP] Rejected connection due to invalid or missing JWT token.");
                throw new AccessDeniedException("Invalid or missing JWT token for STOMP connection");
            }

            try {
                String username = null;
                String roleStr = "USER"; // Default fallback
                if (jwtDecoder != null) {
                    try {
                        Jwt jwt = jwtDecoder.decode(token);
                        username = jwt.getClaimAsString("preferred_username");
                        if (username == null) username = jwt.getSubject();
                    } catch (Exception e) {
                        // If JwtDecoder fails, maybe it's a local token. Fallback to JwtUtil.
                        if (jwtUtil.isTokenValid(token)) {
                            username = jwtUtil.extractUsername(token);
                            io.jsonwebtoken.Claims claims = jwtUtil.extractAllClaims(token);
                            roleStr = claims.get("role", String.class);
                        } else {
                            throw e;
                        }
                    }
                } else if (jwtUtil.isTokenValid(token)) {
                    username = jwtUtil.extractUsername(token);
                    io.jsonwebtoken.Claims claims = jwtUtil.extractAllClaims(token);
                    roleStr = claims.get("role", String.class);
                } else {
                    throw new IllegalArgumentException("Token validation failed");
                }

                Collection<GrantedAuthority> authorities = permissionService.getAuthoritiesForUser(username, roleStr);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
                
                java.util.Map<String, String> details = new java.util.HashMap<>();
                details.put("username", username);
                auth.setDetails(details);
                
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
