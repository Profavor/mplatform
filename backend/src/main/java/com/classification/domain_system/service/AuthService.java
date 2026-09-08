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
    private final com.classification.domain_system.repository.OrganizationRepository organizationRepository;
    private final com.classification.domain_system.repository.DomainPermissionRepository domainPermissionRepository;
    private final SpecializedDomainTemplateService specializedDomainTemplateService;
    private final TwoFactorAuthService twoFactorAuthService;

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

    @org.springframework.transaction.annotation.Transactional
    public Map<String, String> selfRegister(com.classification.domain_system.dto.SelfRegisterRequest request, String ipAddress, String userAgent) {
        if (request == null) {
            throw new com.classification.domain_system.exception.BusinessException(
                    com.classification.domain_system.exception.ErrorCode.INVALID_INPUT,
                    "Request body is required"
            );
        }

        String username = request.getUsername() != null ? request.getUsername().trim() : "";
        String email = request.getEmail() != null ? request.getEmail().trim() : "";
        String password = request.getPassword() != null ? request.getPassword().trim() : "";
        String companyName = request.getCompanyName() != null ? request.getCompanyName().trim() : "";
        Boolean termsAgreed = request.getTermsAgreed();

        if (username.isEmpty() || password.isEmpty() || companyName.isEmpty()) {
            throw new com.classification.domain_system.exception.BusinessException(
                    com.classification.domain_system.exception.ErrorCode.INVALID_INPUT,
                    "Username, password, and company name are required"
            );
        }

        if (termsAgreed == null || !termsAgreed) {
            throw new com.classification.domain_system.exception.BusinessException(
                    com.classification.domain_system.exception.ErrorCode.INVALID_INPUT,
                    "Terms of service and privacy policy agreement is required"
            );
        }

        if (userRepository.findByUsername(username).isPresent()) {
            throw new com.classification.domain_system.exception.BusinessException(
                    com.classification.domain_system.exception.ErrorCode.USERNAME_ALREADY_EXISTS,
                    "Username already exists"
            );
        }

        // 1. Create or Find Organization
        com.classification.domain_system.entity.Organization org = new com.classification.domain_system.entity.Organization();
        org.setName(companyName);
        org.setDisplayName(companyName);
        org.setIsActive(true);
        try {
            org = organizationRepository.save(org);
        } catch (Exception e) {
            // In case of duplicate name collision, append UUID suffix or handle gracefully
            org.setName(companyName + " (" + java.util.UUID.randomUUID().toString().substring(0, 8) + ")");
            org = organizationRepository.save(org);
        }

        // 2. Create User
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ROLE_USER,ORG_ADMIN");
        user.setOrganizationId(org.getId());
        user.setTimezone(request.getTimezone() != null && !request.getTimezone().trim().isEmpty() ? request.getTimezone() : "Asia/Seoul");
        user.setIsActive(true);
        user = userRepository.save(user);

        // 3. Provision Trial Domain (Custom template if requested, otherwise default to CUSTOMER)
        try {
            if (specializedDomainTemplateService != null) {
                String requestedCategory = request.getTemplateCategory() != null && !request.getTemplateCategory().trim().isEmpty()
                        ? request.getTemplateCategory().trim().toUpperCase()
                        : "CUSTOMER";

                String koSuffix = "CUSTOMER".equals(requestedCategory) ? " 고객 마스터" : " 마스터";
                String enSuffix = "CUSTOMER".equals(requestedCategory) ? " Customer Master" : " Master";
                try {
                    var templateDto = specializedDomainTemplateService.getTemplate(requestedCategory);
                    if (templateDto != null && templateDto.getName() != null) {
                        if (templateDto.getName().get("ko") != null) koSuffix = " " + templateDto.getName().get("ko");
                        if (templateDto.getName().get("en") != null) enSuffix = " " + templateDto.getName().get("en");
                    }
                } catch (Exception ignored) {
                }

                com.classification.domain_system.dto.SpecializedDomainProvisionRequest provReq =
                        com.classification.domain_system.dto.SpecializedDomainProvisionRequest.builder()
                                .category(requestedCategory)
                                .name(Map.of(
                                        "ko", companyName + koSuffix,
                                        "en", companyName + enSuffix
                                ))
                                .build();
                var domainResp = specializedDomainTemplateService.provisionDomainForOrganization(provReq, org.getId());
                if (domainResp != null && domainResp.getId() != null && domainPermissionRepository != null) {
                    com.classification.domain_system.entity.DomainPermission perm = new com.classification.domain_system.entity.DomainPermission();
                    perm.setUser(user);
                    com.classification.domain_system.entity.Domain dom = new com.classification.domain_system.entity.Domain();
                    dom.setId(domainResp.getId());
                    perm.setDomain(dom);
                    domainPermissionRepository.save(perm);
                }
            }
        } catch (Exception e) {
            System.err.println("Trial domain provisioning error for org " + org.getId() + ": " + e.getMessage());
        }

        // 4. Issue Login Tokens for seamless onboarding
        String userIdStr = user.getId() != null ? user.getId().toString() : null;
        String newSessionId = java.util.UUID.randomUUID().toString();
        user.setActiveSessionId(newSessionId);
        userRepository.saveAndFlush(user);

        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getRole(), userIdStr, newSessionId);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getRole(), userIdStr, newSessionId);

        com.classification.domain_system.entity.LoginLog log = com.classification.domain_system.entity.LoginLog.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .userAgent(userAgent)
                .clientIp(ipAddress)
                .build();
        loginLogRepository.save(log);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("token", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
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

    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class TempTokenInfo {
        private String username;
        private String ipAddress;
        private String userAgent;
        private long createdAt;
    }

    private final Map<String, TempTokenInfo> tempTokenCache = new java.util.concurrent.ConcurrentHashMap<>();

    public String generateTempToken(String username, String ipAddress, String userAgent) {
        cleanExpiredTempTokens();
        String tempToken = java.util.UUID.randomUUID().toString();
        tempTokenCache.put(tempToken, new TempTokenInfo(username, ipAddress, userAgent, System.currentTimeMillis()));
        return tempToken;
    }

    public boolean validateTempToken(String tempToken, String username) {
        if (tempToken == null || tempToken.isBlank() || username == null) {
            return false;
        }
        TempTokenInfo info = tempTokenCache.get(tempToken);
        if (info == null) {
            return false;
        }
        if (System.currentTimeMillis() - info.getCreatedAt() > 5 * 60 * 1000) {
            tempTokenCache.remove(tempToken);
            return false;
        }
        return username.equals(info.getUsername());
    }

    public TempTokenInfo getTempTokenInfo(String tempToken) {
        if (tempToken == null || tempToken.isBlank()) {
            return null;
        }
        TempTokenInfo info = tempTokenCache.get(tempToken);
        if (info == null) {
            return null;
        }
        if (System.currentTimeMillis() - info.getCreatedAt() > 5 * 60 * 1000) {
            tempTokenCache.remove(tempToken);
            return null;
        }
        return info;
    }

    public void consumeTempToken(String tempToken) {
        if (tempToken != null) {
            tempTokenCache.remove(tempToken);
        }
    }

    private void cleanExpiredTempTokens() {
        long now = System.currentTimeMillis();
        tempTokenCache.entrySet().removeIf(entry -> now - entry.getValue().getCreatedAt() > 10 * 60 * 1000);
    }

    public String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email != null ? email : "";
        }
        int atIndex = email.indexOf('@');
        String name = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (name.length() <= 2) {
            return name.charAt(0) + "*".repeat(Math.max(1, name.length() - 1)) + domain;
        }
        return name.substring(0, 2) + "*".repeat(Math.max(1, name.length() - 2)) + domain;
    }

    private Map<String, String> buildTwoFactorRequiredResponse(User user, String ipAddress, String userAgent) {
        String tempToken = generateTempToken(user.getUsername(), ipAddress, userAgent);
        Map<String, String> map = new HashMap<>();
        map.put("twoFactorRequired", "true");
        map.put("tempToken", tempToken);
        map.put("twoFactorType", user.getTwoFactorType() != null ? user.getTwoFactorType() : (Boolean.TRUE.equals(user.getTwoFactorEnabled()) ? "TOTP" : "NONE"));
        map.put("role", user.getRole());
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            map.put("maskedEmail", maskEmail(user.getEmail()));
        }
        if (user.getTwoFactorGraceUntil() != null && java.time.LocalDateTime.now().isBefore(user.getTwoFactorGraceUntil())) {
            long days = java.time.Duration.between(java.time.LocalDateTime.now(), user.getTwoFactorGraceUntil()).toDays();
            map.put("gracePeriodRemainingDays", String.valueOf(Math.max(1, days)));
        }
        return map;
    }

    @org.springframework.transaction.annotation.Transactional
    public Map<String, String> issueFinalTokensAfter2Fa(User user, String ipAddress, String userAgent) {
        return issueFinalTokensAfter2Fa(user, ipAddress, userAgent, user.getTwoFactorType() != null ? user.getTwoFactorType() : "TOTP");
    }

    @org.springframework.transaction.annotation.Transactional
    public Map<String, String> issueFinalTokensAfter2Fa(User user, String ipAddress, String userAgent, String authType) {
        if (user == null) {
            throw new com.classification.domain_system.exception.BusinessException(
                    com.classification.domain_system.exception.ErrorCode.INVALID_CREDENTIALS,
                    "User not found"
            );
        }

        String userIdStr = user.getId() != null ? user.getId().toString() : null;
        String newSessionId = java.util.UUID.randomUUID().toString();
        sendForceLogout(user);
        user.setActiveSessionId(newSessionId);
        userRepository.saveAndFlush(user);

        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getRole(), userIdStr, newSessionId);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getRole(), userIdStr, newSessionId);

        com.classification.domain_system.entity.LoginLog log = com.classification.domain_system.entity.LoginLog.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .userAgent(userAgent)
                .clientIp(ipAddress)
                .twoFactorStatus("SUCCESS")
                .twoFactorType(authType)
                .build();
        loginLogRepository.save(log);

        Map<String, String> map = new HashMap<>();
        map.put("token", accessToken);
        map.put("refreshToken", refreshToken);
        return map;
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

                if (twoFactorAuthService != null && twoFactorAuthService.isTwoFactorRequired(user)) {
                    return buildTwoFactorRequiredResponse(user, ipAddress, userAgent);
                }

                String sid = (String) body.get("session_state");
                if (sid == null || sid.isBlank()) {
                    sid = java.util.UUID.randomUUID().toString();
                }
                sendForceLogout(user);
                user.setActiveSessionId(sid);
            } catch (com.classification.domain_system.exception.BusinessException be) {
                throw be;
            } catch (Exception e) {
                System.err.println("Keycloak login failed for user '" + username + "', falling back to local DB. Reason: " + e.getMessage());
            }

            if (!kcSuccess) {
                user = validateAndProcessLogin(username, password);
                if (twoFactorAuthService != null && twoFactorAuthService.isTwoFactorRequired(user)) {
                    return buildTwoFactorRequiredResponse(user, ipAddress, userAgent);
                }
                String userIdStr = user.getId() != null ? user.getId().toString() : null;
                String newSessionId = java.util.UUID.randomUUID().toString();
                accessToken = jwtUtil.generateToken(user.getUsername(), user.getRole(), userIdStr, newSessionId);
                refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getRole(), userIdStr, newSessionId);
                sendForceLogout(user);
                user.setActiveSessionId(newSessionId);
            }
        } else {
            user = validateAndProcessLogin(username, password);
            if (twoFactorAuthService != null && twoFactorAuthService.isTwoFactorRequired(user)) {
                return buildTwoFactorRequiredResponse(user, ipAddress, userAgent);
            }
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

    @org.springframework.transaction.annotation.Transactional
    public void recordLoginLog(String username, String clientIp, String userAgent) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }
        String cleanUsername = username.trim();

        // 30초 이내 동일 사용자의 중복 로그인 요청은 중복 저장 방지 (Deduplication)
        java.time.LocalDateTime deduplicationWindow = java.time.LocalDateTime.now().minusSeconds(30);
        if (loginLogRepository.existsByUsernameAndLoginAtAfter(cleanUsername, deduplicationWindow)) {
            return;
        }

        User user = userRepository.findByUsername(cleanUsername).orElse(null);
        String userId = user != null ? user.getId() : null;

        String safeIp = clientIp;
        if (safeIp != null && safeIp.length() > 45) {
            safeIp = safeIp.substring(0, 45);
        }

        String safeUserAgent = userAgent;
        if (safeUserAgent != null && safeUserAgent.length() > 500) {
            safeUserAgent = safeUserAgent.substring(0, 500);
        }

        com.classification.domain_system.entity.LoginLog log = com.classification.domain_system.entity.LoginLog.builder()
                .userId(userId)
                .username(cleanUsername)
                .clientIp(safeIp)
                .userAgent(safeUserAgent)
                .build();
        loginLogRepository.save(log);
    }

    /**
     * OIDC 인가 코드를 Keycloak 토큰 엔드포인트와 직접 통신하여 토큰으로 교환하고
     * 백엔드 트랜잭션 내에서 login_log를 단 1회 정확하게 적재합니다.
     */
    @org.springframework.transaction.annotation.Transactional
    public Map<String, Object> exchangeOidcCode(String code, String redirectUri, String clientIp, String userAgent) {
        if (code == null || code.trim().isEmpty()) {
            throw new com.classification.domain_system.exception.BusinessException(
                    com.classification.domain_system.exception.ErrorCode.INVALID_INPUT,
                    "Authorization code is required"
            );
        }
        if (keycloakTokenUri == null || keycloakTokenUri.trim().isEmpty()) {
            throw new com.classification.domain_system.exception.BusinessException(
                    com.classification.domain_system.exception.ErrorCode.INTERNAL_SERVER_ERROR,
                    "Keycloak token URI is not configured"
            );
        }

        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

        org.springframework.util.MultiValueMap<String, String> mapConfig = new org.springframework.util.LinkedMultiValueMap<>();
        mapConfig.add("grant_type", "authorization_code");
        mapConfig.add("code", code.trim());
        mapConfig.add("redirect_uri", redirectUri != null ? redirectUri.trim() : "");
        mapConfig.add("client_id", keycloakClientId != null && !keycloakClientId.trim().isEmpty() ? keycloakClientId : "mdm-frontend");
        if (keycloakClientSecret != null && !keycloakClientSecret.trim().isEmpty()) {
            mapConfig.add("client_secret", keycloakClientSecret.trim());
        }

        org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, String>> kcRequest =
                new org.springframework.http.HttpEntity<>(mapConfig, headers);

        Map body;
        try {
            org.springframework.http.ResponseEntity<Map> kcResponse = restTemplate.postForEntity(keycloakTokenUri, kcRequest, Map.class);
            body = kcResponse.getBody();
            if (body == null) {
                throw new com.classification.domain_system.exception.BusinessException(
                        com.classification.domain_system.exception.ErrorCode.INVALID_CREDENTIALS,
                        "Keycloak token response is empty"
                );
            }
        } catch (com.classification.domain_system.exception.BusinessException be) {
            throw be;
        } catch (Exception e) {
            throw new com.classification.domain_system.exception.BusinessException(
                    com.classification.domain_system.exception.ErrorCode.INVALID_CREDENTIALS,
                    "Failed to exchange authorization code with Keycloak: " + e.getMessage()
            );
        }

        String accessToken = (String) body.get("access_token");
        String refreshToken = (String) body.get("refresh_token");
        Number expiresIn = (Number) body.get("expires_in");

        String username = extractUsernameFromJwt(accessToken);
        User user = null;
        if (username != null && !username.trim().isEmpty()) {
            user = userRepository.findByUsername(username.trim()).orElse(null);
            if (user != null) {
                String sid = (String) body.get("session_state");
                if (sid == null || sid.isBlank()) {
                    sid = java.util.UUID.randomUUID().toString();
                }
                sendForceLogout(user);
                user.setActiveSessionId(sid);
                user.setLastLoginEpochSec(System.currentTimeMillis() / 1000L);
                userRepository.saveAndFlush(user);
            }
            recordLoginLog(username.trim(), clientIp, userAgent);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        result.put("expiresIn", expiresIn != null ? expiresIn.intValue() : 1800);
        result.put("username", username);
        if (user != null) {
            result.put("user", user);
        }
        return result;
    }

    public String extractUsernameFromJwt(String token) {
        if (token == null || token.trim().isEmpty()) return null;
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            byte[] payloadBytes = java.util.Base64.getUrlDecoder().decode(parts[1]);
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map claims = mapper.readValue(payloadBytes, Map.class);
            String username = (String) claims.get("preferred_username");
            if (username == null || username.isBlank()) username = (String) claims.get("clientId");
            if (username == null || username.isBlank()) username = (String) claims.get("client_id");
            if (username == null || username.isBlank()) username = (String) claims.get("azp");
            if (username == null || username.isBlank()) username = (String) claims.get("sub");
            return username;
        } catch (Exception e) {
            return null;
        }
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
