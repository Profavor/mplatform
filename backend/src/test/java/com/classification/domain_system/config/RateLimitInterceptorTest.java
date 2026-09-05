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

        // Exhaust the bucket with constructor-configured capacity (e.g. 5 requests for fast test)
        RateLimitInterceptor customInterceptor = new RateLimitInterceptor(authContext, 5, 5);
        for (int i = 0; i < 5; i++) {
            assertTrue(customInterceptor.preHandle(request, response, null));
        }

        // 6th request should be blocked with 429 and Retry-After header
        boolean result = customInterceptor.preHandle(request, response, null);
        assertFalse(result);
        
        verify(response, times(1)).setStatus(429);
        verify(response, times(1)).setHeader("Retry-After", "1");
    }

    @Test
    void testRateLimit_AnonymousUser_ShouldUseIp() throws Exception {
        when(authContext.getUserId()).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100");

        boolean result = rateLimitInterceptor.preHandle(request, response, null);

        assertTrue(result);
    }
}
