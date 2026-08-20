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
    @DisplayName("CORS 설정 검증: 모바일 웹(localhost:9090) Origin 및 공백 포함 목록이 정상적으로 허용되어야 한다.")
    void corsConfigurationShouldAllowMobileWebOrigin() {
        ReflectionTestUtils.setField(securityConfig, "allowedOrigins", "http://localhost:3000, http://localhost:8080, http://localhost:9090, http://127.0.0.1:9090");

        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/domains");
        CorsConfiguration config = source.getCorsConfiguration(request);

        assertThat(config).isNotNull();
        List<String> allowedOrigins = config.getAllowedOriginPatterns();
        assertThat(allowedOrigins).containsExactly("http://localhost:3000", "http://localhost:8080", "http://localhost:9090", "http://127.0.0.1:9090", "http://localhost:*", "http://127.0.0.1:*");
        assertThat(config.getAllowCredentials()).isTrue();
    }
}
