package com.classification.domain_system.config;

import com.classification.domain_system.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

import com.classification.domain_system.security.CustomPermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomPermissionEvaluator permissionEvaluator;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        return expressionHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 인증 및 계정 관련 공개 API (로그인, 토큰 갱신, 아이디 중복확인, B2B 셀프 회원가입, 2FA 단계별 인증 공개)
                .requestMatchers("/api/auth/login", "/api/auth/refresh", "/api/auth/record-login", "/api/auth/check-username", "/api/auth/self-register", "/api/auth/2fa/**").permitAll()
                .requestMatchers("/api/roi/**").permitAll()
                .requestMatchers("/api/auth/me").authenticated()
                // 시스템 최초 설치 엔드포인트 (최초 환경 구성 및 설치 상태 조회용)
                .requestMatchers("/api/system/install", "/api/system/install-status").permitAll()
                // 브라우저 SSE EventSource 알림 구독 (커스텀 헤더 미지원 대응 및 익명/토큰 연결 지원)
                .requestMatchers("/api/notifications/subscribe").permitAll()
                // 이메일 수신 확인용 투명 1x1 픽셀 이미지 트래킹 (외부 메일 클라이언트 요청)
                .requestMatchers("/api/inbox/track/open/**").permitAll()
                // 시스템 모니터링 공개 엔드포인트: 헬스체크 및 기본 정보만 노출 (프로메테우스 등 민감 지표 제외)
                .requestMatchers(
                    "/actuator/health", "/actuator/health/**",
                    "/api/actuator/health", "/api/actuator/health/**",
                    "/actuator/info", "/api/actuator/info"
                ).permitAll()
                // Inbound Webhook: 외부 연계 시스템이 자체 채널 시크릿 토큰으로 호출하므로 JWT 인증 제외
                .requestMatchers(HttpMethod.POST, "/api/integration/inbound/**").permitAll()
                // 메뉴 접근 로그 수집 (모든 클라이언트 비차단 비동기 로깅 지원)
                .requestMatchers(HttpMethod.POST, "/api/menus/access").permitAll()
                // 파일 다운로드/조회를 포함한 모든 /api/** 엔드포인트는 인증 필수
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    var mapper = new ObjectMapper();
                    var body = java.util.Map.of(
                        "status", 401,
                        "error", "Unauthorized",
                        "message", authException.getMessage() != null ? authException.getMessage() : "Authentication token expired or invalid"
                    );
                    response.getWriter().write(mapper.writeValueAsString(body));
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    var mapper = new ObjectMapper();
                    var body = java.util.Map.of(
                        "status", 403,
                        "error", "Forbidden",
                        "message", accessDeniedException.getMessage() != null ? accessDeniedException.getMessage() : "Access denied"
                    );
                    response.getWriter().write(mapper.writeValueAsString(body));
                })
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // Inbound Webhook: 외부 시스템에서 호출하므로 모든 Origin 허용
        CorsConfiguration inboundConfig = new CorsConfiguration();
        inboundConfig.addAllowedOriginPattern("*");
        inboundConfig.setAllowedMethods(Arrays.asList("POST", "OPTIONS"));
        inboundConfig.setAllowedHeaders(Arrays.asList("*"));
        inboundConfig.setAllowCredentials(false);
        source.registerCorsConfiguration("/api/integration/inbound/**", inboundConfig);

        // 일반 API: 신뢰할 수 있는 Origin 패턴만 허용 (설정 파일 기반 화이트리스트)
        CorsConfiguration config = new CorsConfiguration();
        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            List<String> originPatterns = Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .collect(java.util.stream.Collectors.toList());
            for (String origin : originPatterns) {
                if (!origin.isBlank()) {
                    config.addAllowedOriginPattern(origin);
                }
            }
        } else {
            config.addAllowedOriginPattern("http://localhost:3000");
            config.addAllowedOriginPattern("http://localhost:8080");
        }
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setExposedHeaders(Arrays.asList("Content-Disposition", "Content-Length", "Content-Range", "Accept-Ranges"));
        config.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
