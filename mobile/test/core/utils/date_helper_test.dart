import 'package:flutter_test/flutter_test.dart';
import 'package:mplatform_mobile/core/utils/date_helper.dart';

void main() {
  group('DateHelper Tests', () {
    test('parseDate handles null and empty string gracefully', () {
      expect(DateHelper.parseDate(null), isNull);
      expect(DateHelper.parseDate(''), isNull);
      expect(DateHelper.parseDate('   '), isNull);
    });

    test('parseDate appends Z when offset is missing from Spring Boot LocalDateTime', () {
      // Spring Boot default LocalDateTime serialization without UTC offset
      const input = '2026-08-06T14:30:00';
      final parsed = DateHelper.parseDate(input);
      expect(parsed, isNotNull);
      expect(parsed!.isUtc, isTrue);
      expect(parsed.year, equals(2026));
      expect(parsed.hour, equals(14));
      expect(parsed.minute, equals(30));
    });

    test('parseDate preserves explicit UTC offset when present', () {
      const input = '2026-08-06T14:30:00Z';
      final parsed = DateHelper.parseDate(input);
      expect(parsed, isNotNull);
      expect(parsed!.isUtc, isTrue);
    });

    test('formatDateWithOffset adjusts time by hours difference from UTC', () {
      const input = '2026-08-06T14:30:00'; // assume UTC 14:30
      // KST is UTC+9 -> should become 23:30
      final formatted = DateHelper.formatWithOffset(input, 9, pattern: 'yyyy-MM-dd HH:mm');
      expect(formatted, equals('2026-08-06 23:30'));
    });

    test('formatWithOffset handles negative offset correctly (e.g., EST UTC-5)', () {
      const input = '2026-08-06T14:30:00'; 
      final formatted = DateHelper.formatWithOffset(input, -5, pattern: 'yyyy-MM-dd HH:mm');
      expect(formatted, equals('2026-08-06 09:30'));
    });
  });
}
