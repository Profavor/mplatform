import 'package:intl/intl.dart';

class DateHelper {
  /// Spring Boot의 LocalDateTime 직렬화 시 UTC 오프셋('Z' 또는 '+HH:mm') 누락 건에 대비한 방어적 날짜 파싱
  static DateTime? parseDate(String? dateStr) {
    if (dateStr == null || dateStr.trim().isEmpty) {
      return null;
    }
    String str = dateStr.trim();
    // 만약 타임존 표시(Z, +, -)가 없다면 기본적으로 서버 시간(KST, +09:00)으로 간주하고 보정
    // 예: "2026-08-06T14:30:00" -> "2026-08-06T14:30:00+09:00"
    if (str.contains('T') && !str.endsWith('Z') && !RegExp(r'[+-]\d{2}:\d{2}$').hasMatch(str)) {
      str = "$str+09:00";
    }
    try {
      return DateTime.parse(str);
    } catch (e) {
      return null;
    }
  }

  /// 사용자가 정의한 개인화 타임존(Timezone) 시차(offset in hours)를 적용하여 날짜 포매팅
  static String formatWithOffset(String? dateStr, int offsetHours, {String pattern = 'yyyy-MM-dd HH:mm:ss'}) {
    final DateTime? parsed = parseDate(dateStr);
    if (parsed == null) return '';
    
    // UTC 기준으로 파싱된 시간에 개인화 타임존 오프셋(시간)을 합산하여 현지 시간 표현
    final DateTime targetTime = parsed.toUtc().add(Duration(hours: offsetHours));
    return DateFormat(pattern).format(targetTime);
  }
  /// 사용자가 설정한 타임존(SharedPreferences) 문자열을 기반으로 시차(offset in hours)를 반환
  static int getTimezoneOffset(String tzString) {
    if (tzString == 'Asia/Seoul') return 9;
    if (tzString.startsWith('GMT') || tzString.startsWith('UTC')) {
      final match = RegExp(r'[+-]\d+').firstMatch(tzString);
      if (match != null) {
        return int.tryParse(match.group(0)!) ?? 0;
      }
      return 0; // Exactly 'UTC' or 'GMT'
    }
    return DateTime.now().timeZoneOffset.inHours;
  }
}
