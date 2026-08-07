import 'package:flutter_test/flutter_test.dart';
import 'package:mplatform_mobile/core/utils/uuid_formatter.dart';

void main() {
  group('UuidFormatter Tests (TDD)', () {
    test('formats raw standard UUID into short identification code with custom prefix', () {
      const rawUuid = '340a0917-af0b-4d13-a1ce-479d4b2e2ca7';
      final formatted = UuidFormatter.format(rawUuid, prefix: 'REC');
      expect(formatted, equals('REC-340a0917'));
    });

    test('uses default ID prefix when prefix argument is omitted', () {
      const rawUuid = '340a0917-af0b-4d13-a1ce-479d4b2e2ca7';
      final formatted = UuidFormatter.format(rawUuid);
      expect(formatted, equals('ID-340a0917'));
    });

    test('handles null, empty string, and whitespace gracefully', () {
      expect(UuidFormatter.format(null), equals(''));
      expect(UuidFormatter.format('   '), equals(''));
    });

    test('preserves strings that are not raw UUIDs (such as user names or already formatted codes)', () {
      expect(UuidFormatter.format('SYS-ADMIN', prefix: 'REC'), equals('SYS-ADMIN'));
      expect(UuidFormatter.format('REC-340a0917', prefix: 'REC'), equals('REC-340a0917'));
    });
  });
}
