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
        CustomPermissionEvaluator evaluator = Mockito.mock(CustomPermissionEvaluator.class);
        Mockito.when(evaluator.hasPermission(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(evaluator.hasPermission(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        return evaluator;
    }

    @Bean
    @Primary
    public jakarta.persistence.EntityManagerFactory entityManagerFactory() {
        return Mockito.mock(jakarta.persistence.EntityManagerFactory.class);
    }
    
    @Bean
    @Primary
    public org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder() {
        return Mockito.mock(org.springframework.security.oauth2.jwt.JwtDecoder.class);
    }

    @Bean
    public org.springframework.web.servlet.config.annotation.WebMvcConfigurer testSecurityInterceptor() {
        return new org.springframework.web.servlet.config.annotation.WebMvcConfigurer() {
            @Override
            public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
                registry.addInterceptor(new org.springframework.web.servlet.HandlerInterceptor() {
                    @Override
                    public boolean preHandle(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, Object handler) {
                        org.springframework.security.core.context.SecurityContext context = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
                        java.util.List<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities = java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"), new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"));
                        org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("admin", "password", authorities);
                        context.setAuthentication(auth);
                        org.springframework.security.core.context.SecurityContextHolder.setContext(context);
                        return true;
                    }

                    @Override
                    public void afterCompletion(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, Object handler, Exception ex) {
                        org.springframework.security.core.context.SecurityContextHolder.clearContext();
                    }
                });
            }
        };
    }
}
