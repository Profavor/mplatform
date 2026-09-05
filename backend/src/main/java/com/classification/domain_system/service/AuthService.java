package com.classification.domain_system.service;

import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final com.classification.domain_system.repository.LoginLogRepository loginLogRepository;
    private final com.classification.domain_system.websocket.WebSocketPublisher webSocketPublisher;
    private final SseNotificationService sseNotificationService;
    private final org.springframework.beans.factory.ObjectProvider<org.springframework.security.oauth2.jwt.JwtDecoder> jwtDecoderProvider;

    @org.springframework.beans.factory.annotation.Value("${keycloak.token-uri:}")
    private String keycloakTokenUri;

    @org.springframework.beans.factory.annotation.Value("${keycloak.client-id:}")
    private String keycloakClientId;

    @org.springframework.beans.factory.annotation.Value("${keycloak.client-secret:secret}")
    private String keycloakClientSecret;

    @lombok.Setter
    private org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();

    public void register(String username, String password, String role) {
        register(username, password, role, "Asia/Seoul");
    }

    public void register(String username, String password, String role, String timezone) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new com.classification.domain_system.exception.BusinessException(
                    com.classification.domain_system.exception.ErrorCode.USERNAME_ALREADY_EXISTS,
                    "Username already exists"
            );
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        // 회원가입 시 무분별한 관리자 생성 방지: 일반 회원가입은 무조건 ROLE_USER 지정
        user.setRole("ROLE_USER");
        user.setTimezone(timezone != null && !timezone.trim().isEmpty() ? timezone : "Asia/Seoul");
        
        userRepository.save(user);
    }

    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return userRepository.findByUsername(username.trim()).isPresent();
    }

    private void sendForceLogout(User user) {
        if (user == null) return;
        Map<String, Object> logoutEvent = Map.of(
                "eventType", "FORCE_LOGOUT",
                "title", "세션 종료",
                "message", "다른 기기/브라우저에서 로그인되어 현재 세션이 종료되었습니다."
        );
        if (user.getId() != null) {
            if (sseNotificationService != null) {
                try {
                    sseNotificationService.sendNotification(user.getId(), logoutEvent);
                } catch (Exception ignored) {}
            }
            if (webSocketPublisher != null) {
                try {
                    webSocketPublisher.publishNotification(user.getId(), logoutEvent);
                } catch (Exception ignored) {}
            }
        }
        if (user.getUsername() != null && !user.getUsername().equals(user.getId())) {
            if (sseNotificationService != null) {
                try {
                    sseNotificationService.sendNotification(user.getUsername(), logoutEvent);
                } catch (Exception ignored) {}
            }
            if (webSocketPublisher != null) {
                try {
                    webSocketPublisher.publishNotification(user.getUsername(), logoutEvent);
                } catch (Exception ignored) {}
            }
        }
    }

    public String login(String username, String password, String ipAddress) {
        return login(username, password, ipAddress, null);
    }

    @org.springframework.transaction.annotation.Transactional
    public String login(String username, String password, String ipAddress, String userAgent) {
        User user = validateAndProcessLogin(username, password);

        // 1세션 생성을 위한 newSessionId
        String newSessionId = java.util.UUID.randomUUID().toString();
        sendForceLogout(user);
        user.setActiveSessionId(newSessionId);
        userRepository.saveAndFlush(user);

        // 로그인 이력 기록
        com.classification.domain_system.entity.LoginLog log = com.classification.domain_system.entity.LoginLog.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .userAgent(userAgent)
                .clientIp(ipAddress)
                .build();
        loginLogRepository.save(log);

        return jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getId(), newSessionId);
    }

    @org.springframework.transaction.annotation.Transactional
    public Map<String, String> loginWithTokens(String username, String password, String ipAddress, String userAgent) {
        String accessToken = null;
        String refreshToken = null;
        User user = null;

        if (keycloakTokenUri != null && !keycloakTokenUri.trim().isEmpty()) {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

            org.springframework.util.MultiValueMap<String, String> mapConfig = new org.springframework.util.LinkedMultiValueMap<>();
            mapConfig.add("client_id", keycloakClientId != null ? keycloakClientId : "mdm-frontend");
            mapConfig.add("grant_type", "password");
            mapConfig.add("username", username);
            mapConfig.add("password", password);

            org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, String>> kcRequest = new org.springframework.http.HttpEntity<>(mapConfig, headers);

            boolean kcSuccess = false;
            try {
                org.springframework.http.ResponseEntity<Map> kcResponse = restTemplate.postForEntity(keycloakTokenUri, kcRequest, Map.class);
                Map body = kcResponse.getBody();
                accessToken = (String) body.get("access_token");
                refreshToken = (String) body.get("refresh_token");
                kcSuccess = true;
                
                user = userRepository.findByUsername(username).orElseThrow(() -> 
                    new com.classification.domain_system.exception.BusinessException(com.classification.domain_system.exception.ErrorCode.INVALID_CREDENTIALS, "User authenticated by Keycloak but not found in local DB")
                );

                String sid = (String) body.get("session_state");
                if (sid == null || sid.isBlank()) {
                    sid = java.util.UUID.randomUUID().toString();
                }
                sendForceLogout(user);
                user.setActiveSessionId(sid);
            } catch (Exception e) {
                System.err.println("Keycloak login failed for user '" + username + "', falling back to local DB. Reason: " + e.getMessage());
            }

            if (!kcSuccess) {
                user = validateAndProcessLogin(username, password);
                String userIdStr = user.getId() != null ? user.getId().toString() : null;
                String newSessionId = java.util.UUID.randomUUID().toString();
                accessToken = jwtUtil.generateToken(user.getUsername(), user.getRole(), userIdStr, newSessionId);
                refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getRole(), userIdStr, newSessionId);
                sendForceLogout(user);
                user.setActiveSessionId(newSessionId);
            }
        } else {
            user = validateAndProcessLogin(username, password);
            String userIdStr = user.getId() != null ? user.getId().toString() : null;
            String newSessionId = java.util.UUID.randomUUID().toString();
            accessToken = jwtUtil.generateToken(user.getUsername(), user.getRole(), userIdStr, newSessionId);
            refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getRole(), userIdStr, newSessionId);
            sendForceLogout(user);
            user.setActiveSessionId(newSessionId);
        }

        userRepository.saveAndFlush(user);

        com.classification.domain_system.entity.LoginLog log = com.classification.domain_system.entity.LoginLog.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .userAgent(userAgent)
                .clientIp(ipAddress)
                .build();
        loginLogRepository.save(log);

        Map<String, String> map = new HashMap<>();
        map.put("token", accessToken);
        map.put("refreshToken", refreshToken);
        return map;
    }

    public Map<String, String> refreshTokens(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new com.classification.domain_system.exception.BusinessException(
                    com.classification.domain_system.exception.ErrorCode.INVALID_CREDENTIALS,
                    "Invalid refresh token"
            );
        }

        // 1. Keycloak OIDC refresh 시도
        if (keycloakTokenUri != null && !keycloakTokenUri.trim().isEmpty()) {
            try {
                org.springframework.web.client.RestTemplate rt = this.restTemplate != null ? this.restTemplate : new org.springframework.web.client.RestTemplate();
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

                // Extract issuer host & proto from refreshToken if available to prevent Invalid token issuer
                String forwardedHost = "mplatform.local";
                String forwardedProto = "http";
                try {
                    String[] parts = refreshToken.split("\\.");
                    if (parts.length > 1) {
                        String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]), java.nio.charset.StandardCharsets.UTF_8);
                        com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(payloadJson);
                        if (node.has("iss")) {
                            java.net.URI issUri = java.net.URI.create(node.get("iss").asText());
                            if (issUri.getHost() != null) {
                                String host = issUri.getHost();
                                if (issUri.getPort() > 0 && issUri.getPort() != 80 && issUri.getPort() != 443) {
                                    host += ":" + issUri.getPort();
                                }
                                forwardedHost = host;
                                forwardedProto = issUri.getScheme() != null ? issUri.getScheme() : "http";
                            }
                        }
                    }
                } catch (Exception ignored) {}

                headers.set("X-Forwarded-Host", forwardedHost);
                headers.set("X-Forwarded-Proto", forwardedProto);

                org.springframework.util.MultiValueMap<String, String> mapConfig = new org.springframework.util.LinkedMultiValueMap<>();
                mapConfig.add("client_id", keycloakClientId != null && !keycloakClientId.trim().isEmpty() ? keycloakClientId : "mdm-frontend");
                if (keycloakClientSecret != null && !keycloakClientSecret.trim().isEmpty()) {
                    mapConfig.add("client_secret", keycloakClientSecret);
                }
                mapConfig.add("grant_type", "refresh_token");
                mapConfig.add("refresh_token", refreshToken);

                org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, String>> kcRequest = new org.springframework.http.HttpEntity<>(mapConfig, headers);
                org.springframework.http.ResponseEntity<Map> kcResponse = rt.postForEntity(keycloakTokenUri, kcRequest, Map.class);
                Map body = kcResponse.getBody();
                if (body != null && body.containsKey("access_token")) {
                    String refreshedAccessToken = (String) body.get("access_token");
                    String sid = (String) body.get("session_state");

                    if (jwtDecoderProvider != null && jwtDecoderProvider.getIfAvailable() != null) {
                        try {
                            org.springframework.security.oauth2.jwt.Jwt decoded = jwtDecoderProvider.getIfAvailable().decode(refreshedAccessToken);
                            String preferredUsername = decoded.getClaimAsString("preferred_username");
                            if (sid == null) {
                                sid = decoded.getClaimAsString("sid");
                                if (sid == null) sid = decoded.getClaimAsString("session_state");
                            }
                            if (preferredUsername != null) {
                                User u = findByUsername(preferredUsername);
                                if (u != null && u.getActiveSessionId() != null && sid != null) {
                                    if (!u.getActiveSessionId().equals(sid)) {
                                        throw new com.classification.domain_system.exception.BusinessException(
                                                com.classification.domain_system.exception.ErrorCode.INVALID_CREDENTIALS,
                                                "Session expired due to login from another device."
                                        );
                                    }
                                }
                            }
                        } catch (com.classification.domain_system.exception.BusinessException be) {
                            throw be;
                        } catch (Exception ignored) {}
                    }

                    Map<String, String> map = new HashMap<>();
                    map.put("token", refreshedAccessToken);
                    map.put("refreshToken", (String) body.getOrDefault("refresh_token", refreshToken));
                    return map;
                }
            } catch (com.classification.domain_system.exception.BusinessException be) {
                throw be;
            } catch (Exception e) {
                System.err.println("Keycloak token refresh failed, falling back to local DB JWT check: " + e.getMessage());
            }
        }

        // 2. Local JWT refresh
        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new com.classification.domain_system.exception.BusinessException(
                    com.classification.domain_system.exception.ErrorCode.INVALID_CREDENTIALS,
                    "Invalid refresh token"
            );
        }

        String username = jwtUtil.extractUsername(refreshToken);
        User user = findByUsername(username);
        if (user == null) {
            throw new com.classification.domain_system.exception.BusinessException(
                    com.classification.domain_system.exception.ErrorCode.INVALID_CREDENTIALS,
                    "User not found for refresh token"
            );
        }

        String refreshSessionId = jwtUtil.extractSessionId(refreshToken);
        String currentActiveSessionId = user.getActiveSessionId();
        if (currentActiveSessionId != null && refreshSessionId != null && !currentActiveSessionId.equals(refreshSessionId)) {
            throw new com.classification.domain_system.exception.BusinessException(
                    com.classification.domain_system.exception.ErrorCode.INVALID_CREDENTIALS,
                    "Session expired due to login from another device."
            );
        }

        String userIdStr = user.getId() != null ? user.getId().toString() : null;
        String newAccessToken = jwtUtil.generateToken(user.getUsername(), user.getRole(), userIdStr, currentActiveSessionId);
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getRole(), userIdStr, currentActiveSessionId);

        Map<String, String> map = new HashMap<>();
        map.put("token", newAccessToken);
        map.put("refreshToken", newRefreshToken);
        return map;
    }
    
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @org.springframework.transaction.annotation.Transactional
    public User autoProvisionUser(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        String username = authentication.getName();
        User existing = userRepository.findByUsername(username).orElse(null);
        if (existing != null) {
            return existing;
        }

        String dynamicRole = "";
        if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            dynamicRole = authentication.getAuthorities().stream()
                    .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                    .filter(a -> a != null && !a.isBlank())
                    .collect(java.util.stream.Collectors.joining(","));
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
        newUser.setRole(dynamicRole);
        newUser.setTimezone("Asia/Seoul");
        return userRepository.save(newUser);
    }

    public org.springframework.data.domain.Page<com.classification.domain_system.entity.LoginLog> getLoginLogs(org.springframework.data.domain.Pageable pageable) {
        return loginLogRepository.findAll(pageable);
    }

    private User validateAndProcessLogin(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new com.classification.domain_system.exception.BusinessException(
                    com.classification.domain_system.exception.ErrorCode.INVALID_CREDENTIALS,
                    "Invalid credentials"
            );
        }

        User user = userOpt.get();
        if (user.getLockedUntil() != null && java.time.LocalDateTime.now().isBefore(user.getLockedUntil())) {
            throw new com.classification.domain_system.exception.BusinessException(
                    com.classification.domain_system.exception.ErrorCode.ACCOUNT_LOCKED,
                    "Account is temporarily locked due to consecutive login failures."
            );
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            int currentFails = (user.getFailedLoginCount() == null ? 0 : user.getFailedLoginCount()) + 1;
            user.setFailedLoginCount(currentFails);
            if (currentFails >= 5) {
                user.setLockedUntil(java.time.LocalDateTime.now().plusMinutes(15));
            }
            userRepository.saveAndFlush(user);
            throw new com.classification.domain_system.exception.BusinessException(
                    com.classification.domain_system.exception.ErrorCode.INVALID_CREDENTIALS,
                    "Invalid credentials"
            );
        }

        if (user.getFailedLoginCount() != null && user.getFailedLoginCount() > 0) {
            user.setFailedLoginCount(0);
            user.setLockedUntil(null);
        }
        return user;
    }
}
