package com.classification.domain_system.config;

import com.classification.domain_system.context.AuthContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitInterceptorTest {

    @Mock
    private AuthContext authContext;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private RateLimitInterceptor rateLimitInterceptor;

    @BeforeEach
    void setUp() {
        rateLimitInterceptor = new RateLimitInterceptor(authContext);
    }

    @Test
    void testRateLimit_UnderLimit_ShouldAllow() throws Exception {
        when(authContext.getUserId()).thenReturn("user-1");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        boolean result = rateLimitInterceptor.preHandle(request, response, null);

        assertTrue(result);
    }

    @Test
    void testRateLimit_OverLimit_ShouldBlock() throws Exception {
        when(authContext.getUserId()).thenReturn("user-2");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // Exhaust the bucket (default max capacity is 120)
        for (int i = 0; i < 120; i++) {
            assertTrue(rateLimitInterceptor.preHandle(request, response, null));
        }

        // 121st request should be blocked
        boolean result = rateLimitInterceptor.preHandle(request, response, null);
        assertFalse(result);
        
        verify(response, times(1)).setStatus(429);
    }

    @Test
    void testRateLimit_AnonymousUser_ShouldUseIp() throws Exception {
        when(authContext.getUserId()).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100");

        boolean result = rateLimitInterceptor.preHandle(request, response, null);

        assertTrue(result);
    }
}
