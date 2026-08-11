package com.classification.domain_system.config;

import com.classification.domain_system.security.JwtFilter;
import com.classification.domain_system.security.CustomPermissionEvaluator;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestSecurityConfig {
    @Bean
    @Primary
    public JwtFilter jwtFilter() {
        return Mockito.mock(JwtFilter.class);
    }
    
    @Bean
    @Primary
    public CustomPermissionEvaluator customPermissionEvaluator() {
        return Mockito.mock(CustomPermissionEvaluator.class);
    }
}
