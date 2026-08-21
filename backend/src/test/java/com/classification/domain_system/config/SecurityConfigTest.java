package com.classification.domain_system.config;

import com.classification.domain_system.security.CustomPermissionEvaluator;
import com.classification.domain_system.security.JwtFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtFilter jwtFilter;

    @Mock
    private CustomPermissionEvaluator permissionEvaluator;

    @InjectMocks
    private SecurityConfig securityConfig;

    @Test
    @DisplayName("CORS 설정 검증: 프로퍼티로 지정된 Origin 목록만 정확히 허용되어야 하며 임의의 하드코딩된 패턴이 추가되지 않아야 한다.")
    void corsConfigurationShouldOnlyIncludeConfiguredOrigins() {
        ReflectionTestUtils.setField(securityConfig, "allowedOrigins", "http://localhost:3000, http://localhost:8080, http://localhost:9090, http://127.0.0.1:9090");

        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/domains");
        CorsConfiguration config = source.getCorsConfiguration(request);

        assertThat(config).isNotNull();
        List<String> allowedOrigins = config.getAllowedOriginPatterns();
        // 하드코딩된 localhost:*, 127.0.0.1:* 패턴 없이 설정된 항목만 정확히 일치해야 함
        assertThat(allowedOrigins).containsExactly("http://localhost:3000", "http://localhost:8080", "http://localhost:9090", "http://127.0.0.1:9090");
        assertThat(config.getAllowCredentials()).isTrue();
    }

    @Test
    @DisplayName("CORS 설정 검증: 운영 환경(Prod) 단일 도메인 설정 시 해당 도메인만 허용되어야 한다.")
    void corsConfigurationForProductionDomain() {
        ReflectionTestUtils.setField(securityConfig, "allowedOrigins", "https://mplatform.mycompany.com");

        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/records");
        CorsConfiguration config = source.getCorsConfiguration(request);

        assertThat(config).isNotNull();
        List<String> allowedOrigins = config.getAllowedOriginPatterns();
        assertThat(allowedOrigins).containsExactly("https://mplatform.mycompany.com");
        assertThat(config.getAllowCredentials()).isTrue();
    }

    @Test
    @DisplayName("CORS 설정 검증: 인바운드 웹훅 경로는 모든 Origin(*)을 허용하고 credentials=false이어야 한다.")
    void corsConfigurationForInboundWebhook() {
        ReflectionTestUtils.setField(securityConfig, "allowedOrigins", "http://localhost:3000");

        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/integration/inbound/channel-123");
        CorsConfiguration config = source.getCorsConfiguration(request);

        assertThat(config).isNotNull();
        assertThat(config.getAllowedOriginPatterns()).containsExactly("*");
        assertThat(config.getAllowCredentials()).isFalse();
    }

    @Test
    @DisplayName("CORS 설정 검증: Flutter 웹 개발 포트(http://localhost:5000)가 패턴에 의해 정상 허용되어야 한다.")
    void corsConfigurationShouldAllowFlutterWebOrigin() {
        ReflectionTestUtils.setField(securityConfig, "allowedOrigins", "http://localhost:3000, http://localhost:8080, http://localhost:*");

        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth/me");
        request.addHeader("Origin", "http://localhost:5000");
        request.addHeader("Access-Control-Request-Method", "GET");

        CorsConfiguration config = source.getCorsConfiguration(request);

        assertThat(config).isNotNull();
        assertThat(config.checkOrigin("http://localhost:5000")).isEqualTo("http://localhost:5000");
        assertThat(config.getAllowCredentials()).isTrue();
    }
}
