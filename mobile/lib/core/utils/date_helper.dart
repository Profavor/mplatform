import 'package:intl/intl.dart';

class DateHelper {
  /// Spring Boot LocalDateTime 직렬화 시 UTC 오프셋 누락 건에 대비한 방어적 날짜 파싱.
  /// - 백엔드 LocalDateTime ('2026-08-17T11:42:38'): Z 없음 → isUtc=false (로컬 시간)
  /// - 명시적 UTC ('2026-08-17T02:42:38Z'): Z 있음 → isUtc=true
  static DateTime? parseDate(dynamic dateInput) {
    if (dateInput == null) return null;
    if (dateInput is DateTime) return dateInput;

    final str = dateInput.toString().trim();
    if (str.isEmpty || str == '-') return null;

    try {
      // Normalize space-separated format to ISO T format
      final normalized = str.contains('T') ? str : str.replaceFirst(' ', 'T');
      // Parse as-is: without Z → local time (isUtc=false), with Z → UTC (isUtc=true)
      return DateTime.parse(normalized);
    } catch (_) {
      return null;
    }
  }

  /// 사용자의 개인화 타임존(offset in hours)을 적용하여 날짜 포매팅.
  /// - isUtc=true (명시적 UTC): offsetHours를 더해 현지 시각으로 변환 후 포맷
  /// - isUtc=false (백엔드 LocalDateTime): 이미 현지 시각이므로 그대로 포맷 (이중 가산 방지)
  static String formatWithOffset(dynamic dateInput, int offsetHours, {String pattern = 'yyyy. MM. dd. HH:mm:ss'}) {
    final DateTime? parsed = parseDate(dateInput);
    if (parsed == null) return '';

    DateTime targetTime;
    if (parsed.isUtc) {
      // UTC 기준 → 사용자 타임존 오프셋 적용
      targetTime = parsed.add(Duration(hours: offsetHours));
    } else {
      // 이미 로컬 시간 (백엔드 LocalDateTime) → 그대로 사용
      targetTime = parsed;
    }
    return DateFormat(pattern).format(targetTime);
  }

  /// 기본 날짜/시간 포매팅 편의 함수
  static String formatDateTime(dynamic dateInput, {String pattern = 'yyyy-MM-dd HH:mm'}) {
    final parsed = parseDate(dateInput);
    if (parsed == null) return '';
    return DateFormat(pattern).format(parsed);
  }

  /// 사용자가 설정한 타임존(SharedPreferences) 문자열을 기반으로 시차(offset in hours)를 반환
  static int getTimezoneOffset(String tzString) {
    if (tzString == 'Asia/Seoul' || tzString == 'KST') return 9;
    if (tzString.startsWith('GMT') || tzString.startsWith('UTC')) {
      final match = RegExp(r'[+-]\d+').firstMatch(tzString);
      if (match != null) {
        return int.tryParse(match.group(0)!) ?? 0;
      }
      return 0;
    }
    return DateTime.now().timeZoneOffset.inHours;
  }
}
