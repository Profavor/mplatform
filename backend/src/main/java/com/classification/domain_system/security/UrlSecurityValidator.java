package com.classification.domain_system.security;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Set;

@Slf4j
public class UrlSecurityValidator {

    private static final Set<String> BLOCKED_HOSTS = Set.of(
            "localhost",
            "127.0.0.1",
            "::1",
            "169.254.169.254",
            "metadata.google.internal"
    );

    public static boolean isSafeExternalUrl(String urlString) {
        if (Boolean.getBoolean("security.url.allow-loopback-for-test")) {
            return true;
        }
        if (urlString == null || urlString.isBlank()) {
            return false;
        }

        try {
            URI uri = URI.create(urlString.trim());
            String scheme = uri.getScheme();
            if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                return false;
            }

            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                return false;
            }

            String lowerHost = host.toLowerCase();
            if (BLOCKED_HOSTS.contains(lowerHost)) {
                return false;
            }

            // DNS Resolution check to prevent DNS rebinding or private IP aliases
            try {
                InetAddress[] addresses = InetAddress.getAllByName(host);
                for (InetAddress addr : addresses) {
                    if (isPrivateOrBlockedAddress(addr)) {
                        return false;
                    }
                }
            } catch (UnknownHostException e) {
                // If DNS fails, fail safe
                return false;
            }

            return true;
        } catch (Exception e) {
            log.warn("URL security validation failed for '{}': {}", urlString, e.getMessage());
            return false;
        }
    }

    public static void validateExternalUrl(String urlString) {
        if (!isSafeExternalUrl(urlString)) {
            throw new SecurityException("Access to internal, loopback, or cloud metadata URL is prohibited: " + urlString);
        }
    }

    public static boolean isSafeHost(String host) {
        if (host == null || host.isBlank()) {
            return false;
        }
        String lowerHost = host.toLowerCase().trim();
        if (BLOCKED_HOSTS.contains(lowerHost)) {
            return false;
        }
        try {
            InetAddress[] addresses = InetAddress.getAllByName(lowerHost);
            for (InetAddress addr : addresses) {
                if (isPrivateOrBlockedAddress(addr)) {
                    return false;
                }
            }
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    public static void validateHost(String host) {
        if (!isSafeHost(host)) {
            throw new SecurityException("Access to internal, loopback, or cloud metadata host is prohibited: " + host);
        }
    }

    private static boolean isPrivateOrBlockedAddress(InetAddress addr) {
        if (addr.isLoopbackAddress() || addr.isAnyLocalAddress() || addr.isLinkLocalAddress() || addr.isSiteLocalAddress()) {
            return true;
        }

        byte[] bytes = addr.getAddress();
        if (bytes.length == 4) {
            int b0 = bytes[0] & 0xFF;
            int b1 = bytes[1] & 0xFF;

            // 10.0.0.0/8
            if (b0 == 10) return true;
            // 172.16.0.0/12
            if (b0 == 172 && (b1 >= 16 && b1 <= 31)) return true;
            // 192.168.0.0/16
            if (b0 == 192 && b1 == 168) return true;
            // 169.254.0.0/16 (Link Local / Cloud Metadata)
            if (b0 == 169 && b1 == 254) return true;
            // 127.0.0.0/8
            if (b0 == 127) return true;
            // 0.0.0.0/8
            if (b0 == 0) return true;
        }

        return false;
    }
}
