package com.classification.domain_system.utils;

import jakarta.servlet.http.HttpServletRequest;

public final class ClientIpUtil {

    private static final String[] PROXY_HEADERS = {
        "X-Forwarded-For",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_CLIENT_IP",
        "HTTP_X_FORWARDED_FOR"
    };

    private ClientIpUtil() {
        // Utility class
    }

    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String ip = null;
        for (String header : PROXY_HEADERS) {
            ip = request.getHeader(header);
            if (isValidIp(ip)) {
                break;
            }
        }

        if (!isValidIp(ip)) {
            ip = request.getRemoteAddr();
        }

        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            ip = "127.0.0.1";
        }

        if (ip != null && ip.length() > 45) {
            ip = ip.substring(0, 45);
        }

        return ip;
    }

    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip.trim());
    }
}
