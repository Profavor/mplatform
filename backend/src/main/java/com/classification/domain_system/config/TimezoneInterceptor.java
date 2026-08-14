package com.classification.domain_system.config;

import com.classification.domain_system.context.UserTimeZoneContextHolder;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;

@Slf4j
@Component
public class TimezoneInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String timezoneStr = extractTimezone(request);
        if (timezoneStr != null && !timezoneStr.trim().isEmpty()) {
            try {
                String decoded = URLDecoder.decode(timezoneStr.trim(), StandardCharsets.UTF_8);
                ZoneId zoneId = ZoneId.of(decoded);
                UserTimeZoneContextHolder.setZoneId(zoneId);
            } catch (Exception e) {
                log.warn("유효하지 않은 타임존 정보: {}, 기본 타임존 적용", timezoneStr);
                UserTimeZoneContextHolder.setZoneId(ZoneId.of("Asia/Seoul"));
            }
        } else {
            UserTimeZoneContextHolder.setZoneId(ZoneId.of("Asia/Seoul"));
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserTimeZoneContextHolder.clear();
    }

    private String extractTimezone(HttpServletRequest request) {
        // 1. 헤더 체크 (X-Timezone)
        String headerTimezone = request.getHeader("X-Timezone");
        if (headerTimezone != null && !headerTimezone.trim().isEmpty()) {
            return headerTimezone;
        }

        // 2. 쿠키 체크 (timezone 또는 USER_TIMEZONE)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("timezone".equalsIgnoreCase(cookie.getName()) || "USER_TIMEZONE".equalsIgnoreCase(cookie.getName())) {
                    if (cookie.getValue() != null && !cookie.getValue().trim().isEmpty()) {
                        return cookie.getValue();
                    }
                }
            }
        }
        return null;
    }
}
