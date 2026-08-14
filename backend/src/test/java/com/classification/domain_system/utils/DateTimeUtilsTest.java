package com.classification.domain_system.utils;

import com.classification.domain_system.context.UserTimeZoneContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DateTimeUtilsTest {

    @AfterEach
    void tearDown() {
        UserTimeZoneContextHolder.clear();
    }

    @Test
    @DisplayName("ISO 8601 오프셋 포함 문자열(UTC) 파싱 검증")
    void parseDateTime_isoWithOffset_success() {
        String isoUtc = "2026-08-14T00:00:00Z";
        LocalDateTime parsed = DateTimeUtils.parseDateTime(isoUtc);

        assertThat(parsed).isNotNull();
        assertThat(parsed.getYear()).isEqualTo(2026);
        assertThat(parsed.getMonthValue()).isEqualTo(8);
        assertThat(parsed.getDayOfMonth()).isEqualTo(14);
    }

    @Test
    @DisplayName("ISO 8601 KST 오프셋(+09:00) 포함 문자열 파싱 검증")
    void parseDateTime_isoWithKstOffset_success() {
        String isoKst = "2026-08-14T09:30:00+09:00";
        LocalDateTime parsed = DateTimeUtils.parseDateTime(isoKst);

        assertThat(parsed).isNotNull();
        assertThat(parsed.getHour()).isEqualTo(9);
        assertThat(parsed.getMinute()).isEqualTo(30);
    }

    @Test
    @DisplayName("표준 LocalDateTime(오프셋 없음) 문자열 파싱 검증")
    void parseDateTime_localDateTimeWithoutOffset_success() {
        String localStr = "2026-08-14T15:45:30";
        LocalDateTime parsed = DateTimeUtils.parseDateTime(localStr);

        assertThat(parsed).isNotNull();
        assertThat(parsed.getHour()).isEqualTo(15);
        assertThat(parsed.getMinute()).isEqualTo(45);
        assertThat(parsed.getSecond()).isEqualTo(30);
    }

    @Test
    @DisplayName("yyyy-MM-dd HH:mm:ss 공백 구분자 패턴 파싱 검증")
    void parseDateTime_spacePattern_success() {
        String spaceStr = "2026-08-14 18:20:10";
        LocalDateTime parsed = DateTimeUtils.parseDateTime(spaceStr);

        assertThat(parsed).isNotNull();
        assertThat(parsed.getHour()).isEqualTo(18);
        assertThat(parsed.getMinute()).isEqualTo(20);
    }

    @Test
    @DisplayName("yyyy-MM-dd 날짜 전용 문자열 파싱 시 당일 00:00:00 반환")
    void parseDateTime_dateOnly_success() {
        String dateOnly = "2026-08-14";
        LocalDateTime parsed = DateTimeUtils.parseDateTime(dateOnly);

        assertThat(parsed).isNotNull();
        assertThat(parsed.getHour()).isEqualTo(0);
        assertThat(parsed.getMinute()).isEqualTo(0);
    }

    @Test
    @DisplayName("null 또는 빈 문자열 입력 시 null 반환")
    void parseDateTime_nullOrBlank_returnsNull() {
        assertThat(DateTimeUtils.parseDateTime(null)).isNull();
        assertThat(DateTimeUtils.parseDateTime("")).isNull();
        assertThat(DateTimeUtils.parseDateTime("   ")).isNull();
    }

    @Test
    @DisplayName("사용자 개인화 타임존(ContextHolder) 적용 포맷팅 검증")
    void formatWithUserTimezone_success() {
        UserTimeZoneContextHolder.setZoneId(ZoneId.of("America/New_York"));
        ZonedDateTime utcTime = ZonedDateTime.of(2026, 8, 14, 12, 0, 0, 0, ZoneId.of("UTC"));

        String formatted = DateTimeUtils.formatWithUserTimezone(utcTime.toInstant(), "yyyy-MM-dd HH:mm:ss");
        // UTC 12:00 -> NY (EDT, UTC-4) -> 08:00
        assertThat(formatted).isEqualTo("2026-08-14 08:00:00");
    }
}
