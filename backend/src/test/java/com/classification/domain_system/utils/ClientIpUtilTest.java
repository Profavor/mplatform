package com.classification.domain_system.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientIpUtilTest {

    @Mock
    private HttpServletRequest request;

    @Test
    @DisplayName("X-Forwarded-For 헤더에 다중 IP가 존재할 경우 첫 번째 클라이언트 IP만 추출하고 공백을 제거해야 한다 (Red)")
    void testXForwardedForMultipleIps() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.195, 70.41.3.18, 150.172.238.178");
        String ip = ClientIpUtil.getClientIp(request);
        assertThat(ip).isEqualTo("203.0.113.195");
    }

    @Test
    @DisplayName("X-Forwarded-For가 null이거나 unknown인 경우 대체 프록시 헤더(Proxy-Client-IP 등)를 조회해야 한다")
    void testFallbackToProxyClientIp() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("unknown");
        when(request.getHeader("Proxy-Client-IP")).thenReturn("203.0.113.196");
        String ip = ClientIpUtil.getClientIp(request);
        assertThat(ip).isEqualTo("203.0.113.196");
    }

    @Test
    @DisplayName("프록시 관련 헤더가 전혀 없는 경우 request.getRemoteAddr()의 값을 반환해야 한다")
    void testFallbackToRemoteAddr() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("HTTP_CLIENT_IP")).thenReturn(null);
        when(request.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.0.5");
        
        String ip = ClientIpUtil.getClientIp(request);
        assertThat(ip).isEqualTo("192.168.0.5");
    }

    @Test
    @DisplayName("스푸핑 시도로 인해 추출된 IP 문자열 길이가 45자를 초과하는 경우 45자로 절사(Truncation)해야 한다")
    void testLongIpStringTruncation() {
        String spoofedLongIp = "2001:0db8:85a3:0000:0000:8a2e:0370:7334:extra:illegal:spoofed:data:overflowing:length:limit";
        when(request.getHeader("X-Forwarded-For")).thenReturn(spoofedLongIp);
        
        String ip = ClientIpUtil.getClientIp(request);
        assertThat(ip).isNotNull();
        assertThat(ip.length()).isLessThanOrEqualTo(45);
        assertThat(ip).isEqualTo(spoofedLongIp.substring(0, 45));
    }

    @Test
    @DisplayName("IPv6 로컬호스트(0:0:0:0:0:0:0:1 또는 ::1)는 127.0.0.1로 정규화되어야 한다")
    void testIpv6LocalhostNormalization() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("HTTP_CLIENT_IP")).thenReturn(null);
        when(request.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("0:0:0:0:0:0:0:1");

        String ip = ClientIpUtil.getClientIp(request);
        assertThat(ip).isEqualTo("127.0.0.1");
    }
}
