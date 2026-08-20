import 'package:flutter_test/flutter_test.dart';
import 'package:mplatform_mobile/core/utils/date_helper.dart';

void main() {
  group('DateHelper Tests', () {
    test('parseDate handles null and empty string gracefully', () {
      expect(DateHelper.parseDate(null), isNull);
      expect(DateHelper.parseDate(''), isNull);
      expect(DateHelper.parseDate('   '), isNull);
    });

    test('parseDate treats Spring Boot LocalDateTime (no Z) as local time', () {
      const input = '2026-08-06T14:30:00';
      final parsed = DateHelper.parseDate(input);
      expect(parsed, isNotNull);
      expect(parsed!.isUtc, isFalse); // local time, NOT UTC
      expect(parsed.hour, equals(14));
      expect(parsed.minute, equals(30));
    });

    test('parseDate preserves explicit UTC offset when present', () {
      const input = '2026-08-06T14:30:00Z';
      final parsed = DateHelper.parseDate(input);
      expect(parsed, isNotNull);
      expect(parsed!.isUtc, isTrue);
    });

    test('formatWithOffset displays local time as-is without adding offset', () {
      // Backend LocalDateTime without Z: already local time
      const input = '2026-08-06T11:42:38';
      // Even with KST offset +9, should NOT add 9 hours (would be 20:42 which is wrong)
      final formatted = DateHelper.formatWithOffset(input, 9, pattern: 'yyyy-MM-dd HH:mm:ss');
      expect(formatted, equals('2026-08-06 11:42:38'));
    });

    test('formatWithOffset applies offset to explicit UTC time', () {
      // Explicit UTC time with Z: need to add offset
      const input = '2026-08-06T02:42:38Z'; // UTC 02:42
      // KST +9 → should become 11:42
      final formatted = DateHelper.formatWithOffset(input, 9, pattern: 'yyyy-MM-dd HH:mm:ss');
      expect(formatted, equals('2026-08-06 11:42:38'));
    });

    test('formatWithOffset handles negative offset for UTC time', () {
      const input = '2026-08-06T14:30:00Z'; // UTC 14:30
      final formatted = DateHelper.formatWithOffset(input, -5, pattern: 'yyyy-MM-dd HH:mm');
      expect(formatted, equals('2026-08-06 09:30'));
    });
  });
}
