package com.classification.domain_system.context;

import com.classification.domain_system.config.TimezoneInterceptor;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class TimezoneInterceptorTest {

    private final TimezoneInterceptor interceptor = new TimezoneInterceptor();

    @AfterEach
    void tearDown() {
        UserTimeZoneContextHolder.clear();
    }

    @Test
    @DisplayName("timezone 쿠키가 존재하는 경우 해당 ZoneId로 ContextHolder가 설정된다")
    void preHandle_withTimezoneCookie_setsContextHolder() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("timezone", "Asia/Tokyo"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        assertThat(UserTimeZoneContextHolder.getZoneId()).isEqualTo(ZoneId.of("Asia/Tokyo"));
    }

    @Test
    @DisplayName("USER_TIMEZONE 쿠키가 존재하는 경우 해당 ZoneId로 ContextHolder가 설정된다")
    void preHandle_withUserTimezoneCookie_setsContextHolder() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("USER_TIMEZONE", "UTC"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        assertThat(UserTimeZoneContextHolder.getZoneId()).isEqualTo(ZoneId.of("UTC"));
    }

    @Test
    @DisplayName("X-Timezone 헤더가 존재하는 경우 해당 ZoneId로 ContextHolder가 설정된다")
    void preHandle_withTimezoneHeader_setsContextHolder() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Timezone", "America/New_York");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        assertThat(UserTimeZoneContextHolder.getZoneId()).isEqualTo(ZoneId.of("America/New_York"));
    }

    @Test
    @DisplayName("타임존 정보가 없거나 잘못된 타임존 문자열인 경우 기본 타임존(Asia/Seoul)으로 설정된다")
    void preHandle_withInvalidTimezone_defaultsToAsiaSeoul() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("timezone", "Invalid/Timezone"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        assertThat(UserTimeZoneContextHolder.getZoneId()).isEqualTo(ZoneId.of("Asia/Seoul"));
    }

    @Test
    @DisplayName("afterCompletion 호출 시 ContextHolder가 초기화된다")
    void afterCompletion_clearsContextHolder() {
        UserTimeZoneContextHolder.setZoneId(ZoneId.of("UTC"));

        interceptor.afterCompletion(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object(), null);

        assertThat(UserTimeZoneContextHolder.getZoneId()).isEqualTo(ZoneId.of("Asia/Seoul"));
    }
}
