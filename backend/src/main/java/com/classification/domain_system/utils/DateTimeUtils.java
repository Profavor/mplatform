package com.classification.domain_system.utils;

import com.classification.domain_system.context.UserTimeZoneContextHolder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateTimeUtils {

    private static final DateTimeFormatter SPACE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DateTimeUtils() {
    }

    /**
     * 다양한 날짜/시간 문자열(ISO-8601, 오프셋 포함/미포함, 날짜 전용 등)을 방어적으로 파싱하여 LocalDateTime으로 반환합니다.
     */
    public static LocalDateTime parseDateTime(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String trimmed = text.trim();

        // 1. ZonedDateTime / OffsetDateTime (예: "2026-08-14T09:30:00+09:00", "2026-08-14T00:00:00Z")
        try {
            return ZonedDateTime.parse(trimmed).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
        }

        try {
            return OffsetDateTime.parse(trimmed).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
        }

        // 2. ISO Local Date Time (예: "2026-08-14T15:45:30")
        try {
            return LocalDateTime.parse(trimmed);
        } catch (DateTimeParseException ignored) {
        }

        // 3. Space separated pattern (예: "2026-08-14 18:20:10")
        try {
            return LocalDateTime.parse(trimmed, SPACE_FORMATTER);
        } catch (DateTimeParseException ignored) {
        }

        // 4. Date only pattern (예: "2026-08-14") -> 00:00:00
        try {
            LocalDate localDate = LocalDate.parse(trimmed, DATE_ONLY_FORMATTER);
            return localDate.atStartOfDay();
        } catch (DateTimeParseException ignored) {
        }

        // 5. Epoch timestamp (ms 또는 sec)
        try {
            long epoch = Long.parseLong(trimmed);
            Instant instant = (epoch > 100000000000L) ? Instant.ofEpochMilli(epoch) : Instant.ofEpochSecond(epoch);
            return LocalDateTime.ofInstant(instant, UserTimeZoneContextHolder.getZoneId());
        } catch (NumberFormatException ignored) {
        }

        throw new IllegalArgumentException("지원하지 않는 날짜/시간 포맷입니다: " + text);
    }

    /**
     * Instant를 사용자의 개인화 Timezone에 맞추어 포맷팅합니다.
     */
    public static String formatWithUserTimezone(Instant instant, String pattern) {
        if (instant == null) {
            return null;
        }
        ZoneId zoneId = UserTimeZoneContextHolder.getZoneId();
        DateTimeFormatter formatter = (pattern == null || pattern.trim().isEmpty())
                ? SPACE_FORMATTER
                : DateTimeFormatter.ofPattern(pattern);
        return ZonedDateTime.ofInstant(instant, zoneId).format(formatter);
    }

    /**
     * LocalDateTime을 사용자의 개인화 Timezone을 고려하여 문자열로 포맷팅합니다.
     */
    public static String formatWithUserTimezone(LocalDateTime ldt, String pattern) {
        if (ldt == null) {
            return null;
        }
        DateTimeFormatter formatter = (pattern == null || pattern.trim().isEmpty())
                ? SPACE_FORMATTER
                : DateTimeFormatter.ofPattern(pattern);
        return ldt.format(formatter);
    }
}
