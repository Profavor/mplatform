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

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:#{null}}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/system/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                // Inbound Webhook: 외부 시스템이 자체 채널 시크릿 토큰으로 호출하므로 JWT 인증 제외
                .requestMatchers(HttpMethod.POST, "/api/integration/inbound/**").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication token expired or invalid\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"Access denied\"}");
                })
            );

        if (jwkSetUri != null && !jwkSetUri.isEmpty()) {
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(org.springframework.security.config.Customizer.withDefaults()));
        }

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

        // 일반 API: 허용된 프론트엔드 Origin만 허용
        CorsConfiguration config = new CorsConfiguration();
        List<String> originPatterns = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .collect(java.util.stream.Collectors.toList());
        // allowedOriginPatterns로 통일 (allowedOrigins + allowCredentials 동시 사용 시 충돌 방지)
        for (String origin : originPatterns) {
            config.addAllowedOriginPattern(origin);
        }
        config.addAllowedOriginPattern("http://127.0.0.1:*");
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setExposedHeaders(Arrays.asList("Content-Disposition", "Content-Length", "Content-Range", "Accept-Ranges"));
        config.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
