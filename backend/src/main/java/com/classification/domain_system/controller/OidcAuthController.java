package com.classification.domain_system.controller;

import com.classification.domain_system.entity.User;
import com.classification.domain_system.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Keycloak OIDC 통합 인증 컨트롤러
 * <p>
 * 웹(Nuxt), 네이티브 모바일(Flutter), 모바일 웹 등 모든 클라이언트의 OIDC SSO 로그인을
 * 백엔드 콜백 엔드포인트(/api/auth/oidc/callback)로 일원화하고,
 * 백엔드에서 인가코드 검증, 토큰 교환 및 login_log DB 1회 정확 적재를 수행한 후
 * 요청 클라이언트에 맞는 리다이렉트(웹 쿠키 설정 + 302, 모바일 딥링크 등)를 처리합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/oidc")
@RequiredArgsConstructor
public class OidcAuthController {

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Value("${spring.keycloak.auth-uri:${keycloak.auth-uri:}}")
    private String keycloakAuthUri;

    @Value("${spring.keycloak.client-id:${keycloak.client-id:mdm-frontend}}")
    private String keycloakClientId;

    @Value("${spring.keycloak.realm:${keycloak.realm:mplatform}}")
    private String keycloakRealm;

    /**
     * OIDC 로그인 시작 엔드포인트
     * 클라이언트별 state를 생성하고 Keycloak 로그인 페이지로 302 리다이렉트합니다.
     */
    @GetMapping("/login")
    public ResponseEntity<Void> login(
            @RequestParam(value = "client", defaultValue = "web") String client,
            @RequestParam(value = "redirect", defaultValue = "/dashboard") String redirect,
            HttpServletRequest request) {

        String callbackUri = buildCallbackUri(request);
        String state = buildState(client, redirect);

        String authEndpoint = resolveAuthEndpoint(request);
        String redirectUrl = UriComponentsBuilder.fromUriString(authEndpoint)
                .queryParam("client_id", StringUtils.hasText(keycloakClientId) ? keycloakClientId : "mdm-frontend")
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", callbackUri)
                .queryParam("state", state)
                .queryParam("scope", "openid profile email")
                .build()
                .encode()
                .toUriString();

        log.info("[OIDC Login] Initiating login for client: {}, target: {}, redirect_uri: {}", client, redirect, callbackUri);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
    }

    /**
     * Keycloak 인증 완료 후 복귀하는 단일 백엔드 콜백 엔드포인트
     * 1) 인가 코드 검증 및 토큰 교환
     * 2) 백엔드에서 login_log 1회 적재
     * 3) 클라이언트 타입별 맞춤 리다이렉트 (Web 쿠키 세팅, Mobile 딥링크 등)
     */
    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription,
            HttpServletRequest request,
            HttpServletResponse response) {

        Map<String, String> stateMap = parseState(state);
        String client = stateMap.getOrDefault("client", "web");
        String finalRedirect = stateMap.getOrDefault("redirect", "/dashboard");

        // 1. Keycloak 오류 처리 (사용자 취소 등)
        if (StringUtils.hasText(error)) {
            log.warn("[OIDC Callback] Error from Keycloak: {} - {}", error, errorDescription);
            String encodedError = URLEncoder.encode(error, StandardCharsets.UTF_8);
            if ("mobile".equalsIgnoreCase(client)) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create("mplatform://oauth2redirect?error=" + encodedError))
                        .build();
            } else if ("mobile_web".equalsIgnoreCase(client)) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create("/mobile/?error=" + encodedError))
                        .build();
            }
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/login?error=" + encodedError))
                    .build();
        }

        // 2. 인가 코드 누락 처리
        if (!StringUtils.hasText(code)) {
            log.warn("[OIDC Callback] Missing code parameter");
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/login?error=missing_code"))
                    .build();
        }

        // 3. 인가 코드 교환 및 백엔드 로그인 로깅 (AuthService)
        String callbackUri = buildCallbackUri(request);
        String clientIp = extractClientIp(request);
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);

        Map<String, Object> exchangeResult = authService.exchangeOidcCode(code, callbackUri, clientIp, userAgent);
        String accessToken = (String) exchangeResult.get("accessToken");
        String refreshToken = (String) exchangeResult.get("refreshToken");
        int expiresIn = exchangeResult.get("expiresIn") instanceof Number num ? num.intValue() : 1800;
        User user = (User) exchangeResult.get("user");

        boolean isHttps = "https".equalsIgnoreCase(resolveScheme(request));

        // 4. 클라이언트별 리다이렉트 분기
        if ("mobile".equalsIgnoreCase(client)) {
            String target = "mplatform://oauth2redirect?access_token=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
                    + "&refresh_token=" + URLEncoder.encode(refreshToken != null ? refreshToken : "", StandardCharsets.UTF_8);
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(target)).build();
        }

        if ("mobile_web".equalsIgnoreCase(client)) {
            String target = "/mobile/?access_token=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
                    + "&refresh_token=" + URLEncoder.encode(refreshToken != null ? refreshToken : "", StandardCharsets.UTF_8);
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(target)).build();
        }

        // 기본 웹(Nuxt): 브라우저에 쿠키를 주입하고 대시보드(또는 요청 경로)로 302 리다이렉트
        ResponseCookie authCookie = ResponseCookie.from("auth_token", accessToken)
                .path("/")
                .maxAge(expiresIn)
                .sameSite("Lax")
                .secure(isHttps)
                .httpOnly(false)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, authCookie.toString());

        if (StringUtils.hasText(refreshToken)) {
            ResponseCookie refCookie = ResponseCookie.from("refresh_token", refreshToken)
                    .path("/")
                    .maxAge(86400)
                    .sameSite("Lax")
                    .secure(isHttps)
                    .httpOnly(false)
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, refCookie.toString());
        }

        if (user != null) {
            try {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("username", user.getUsername());
                userData.put("role", user.getRole());
                userData.put("organizationId", user.getOrganizationId());
                userData.put("departmentId", user.getDepartmentId());
                userData.put("teamId", user.getTeamId());
                userData.put("timezone", user.getTimezone() != null ? user.getTimezone() : "Asia/Seoul");
                userData.put("mustChangePassword", Boolean.TRUE.equals(user.getMustChangePassword()));

                String userJson = objectMapper.writeValueAsString(userData);
                ResponseCookie userCookie = ResponseCookie.from("user_data", URLEncoder.encode(userJson, StandardCharsets.UTF_8))
                        .path("/")
                        .maxAge(expiresIn)
                        .sameSite("Lax")
                        .secure(isHttps)
                        .httpOnly(false)
                        .build();
                response.addHeader(HttpHeaders.SET_COOKIE, userCookie.toString());
            } catch (Exception e) {
                log.warn("[OIDC Callback] Failed to serialize user_data cookie", e);
            }
        }

        String destination = StringUtils.hasText(finalRedirect) && finalRedirect.startsWith("/") && !finalRedirect.startsWith("//")
                ? finalRedirect
                : "/dashboard";

        log.info("[OIDC Callback] Login successful for user: {}, redirecting to: {}", exchangeResult.get("username"), destination);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(destination)).build();
    }

    private String buildState(String client, String redirect) {
        try {
            Map<String, String> stateMap = Map.of(
                    "client", StringUtils.hasText(client) ? client.trim() : "web",
                    "redirect", StringUtils.hasText(redirect) ? redirect.trim() : "/dashboard",
                    "nonce", UUID.randomUUID().toString()
            );
            return Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(stateMap));
        } catch (Exception e) {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(client.getBytes(StandardCharsets.UTF_8));
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> parseState(String state) {
        if (!StringUtils.hasText(state)) {
            return Map.of("client", "web", "redirect", "/dashboard");
        }
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(state.trim());
            return objectMapper.readValue(bytes, Map.class);
        } catch (Exception e) {
            return Map.of("client", "web", "redirect", "/dashboard");
        }
    }

    private String resolveScheme(HttpServletRequest request) {
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        if (StringUtils.hasText(forwardedProto)) {
            return forwardedProto.split(",")[0].trim();
        }
        return request.isSecure() ? "https" : "http";
    }

    private String resolveHost(HttpServletRequest request) {
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        if (StringUtils.hasText(forwardedHost)) {
            return forwardedHost.split(",")[0].trim();
        }
        String hostHeader = request.getHeader(HttpHeaders.HOST);
        if (StringUtils.hasText(hostHeader)) {
            return hostHeader.split(",")[0].trim();
        }
        int port = request.getServerPort();
        if (port == 80 || port == 443 || port <= 0) {
            return request.getServerName();
        }
        return request.getServerName() + ":" + port;
    }

    private String buildCallbackUri(HttpServletRequest request) {
        String scheme = resolveScheme(request);
        String host = resolveHost(request);
        return scheme + "://" + host + "/api/auth/oidc/callback";
    }

    private String resolveAuthEndpoint(HttpServletRequest request) {
        if (StringUtils.hasText(keycloakAuthUri)) {
            if (keycloakAuthUri.startsWith("http://") || keycloakAuthUri.startsWith("https://")) {
                return keycloakAuthUri;
            }
            String scheme = resolveScheme(request);
            String host = resolveHost(request);
            return scheme + "://" + host + (keycloakAuthUri.startsWith("/") ? keycloakAuthUri : "/" + keycloakAuthUri);
        }
        String scheme = resolveScheme(request);
        String host = resolveHost(request);
        String realm = StringUtils.hasText(keycloakRealm) ? keycloakRealm : "mplatform";
        return scheme + "://" + host + "/auth/realms/" + realm + "/protocol/openid-connect/auth";
    }

    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(ip)) {
            return ip.trim();
        }
        return request.getRemoteAddr();
    }
}
