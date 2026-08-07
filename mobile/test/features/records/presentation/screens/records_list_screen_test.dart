import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/features/records/data/repositories/records_repository.dart';
import 'package:mplatform_mobile/features/records/domain/models/domain_model.dart';
import 'package:mplatform_mobile/features/records/domain/models/field_definition.dart';
import 'package:mplatform_mobile/features/records/domain/models/record_item.dart';
import 'package:mplatform_mobile/features/records/domain/models/records_page_response.dart';
import 'package:mplatform_mobile/features/records/presentation/providers/records_provider.dart';
import 'package:mplatform_mobile/features/records/presentation/screens/records_list_screen.dart';

import 'records_list_screen_test.mocks.dart';

@GenerateMocks([RecordsRepository])
void main() {
  group('RecordsListScreen & DetailScreen Widget Tests (TDD - Zero Hardcoding & UUID Formatting)', () {
    late MockRecordsRepository mockRepository;

    setUp(() {
      mockRepository = MockRecordsRepository();
    });

    Widget createTestWidget() {
      return ProviderScope(
        overrides: [
          recordsRepositoryProvider.overrideWithValue(mockRepository),
        ],
        child: const MaterialApp(
          localizationsDelegates: [
            AppLocalizations.delegate,
            GlobalMaterialLocalizations.delegate,
            GlobalWidgetsLocalizations.delegate,
            GlobalCupertinoLocalizations.delegate,
          ],
          supportedLocales: [Locale('ko'), Locale('en')],
          locale: Locale('ko'),
          home: RecordsListScreen(),
        ),
      );
    }

    testWidgets('displays domain name, formatted UUID (never raw UUID), and dynamically rendered fields without hardcoding', (WidgetTester tester) async {
      const domain = DomainModel(id: 'domain-uuid-1', name: '고객 정보');
      const field1 = FieldDefinition(id: 101, fieldName: 'cust_nm', fieldLabel: '고객명', fieldType: 'String', showInList: true, displayOrder: 1);
      const field2 = FieldDefinition(id: 102, fieldName: 'cust_grade', fieldLabel: '회원등급', fieldType: 'String', showInList: true, displayOrder: 2);
      
      // Raw UUID generated from server
      const rawUuid = '340a0917-af0b-4d13-a1ce-479d4b2e2ca7';
      const record = RecordItem(
        recordId: rawUuid,
        domainId: 'domain-uuid-1',
        attributes: {'cust_nm': '홍길동', 'cust_grade': 'VIP'},
        createdAt: '2026-08-06T12:00:00Z',
      );
      const pageRes = RecordsPageResponse(content: [record], totalElements: 1, totalPages: 1, number: 0, size: 20);

      when(mockRepository.getDomains()).thenAnswer((_) async => [domain]);
      when(mockRepository.getFieldDefinitions('domain-uuid-1')).thenAnswer((_) async => [field1, field2]);
      when(mockRepository.getRecords(domainId: 'domain-uuid-1', page: 0, size: 20, searchQuery: anyNamed('searchQuery'), filters: anyNamed('filters')))
          .thenAnswer((_) async => pageRes);

      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();

      // 1. Check localization titles
      expect(find.text('마스터 데이터 레코드 목록'), findsOneWidget);
      expect(find.text('총 데이터 건수: 1건'), findsOneWidget);
      expect(find.text('고객 정보'), findsOneWidget);

      // 2. Critical Rule Check: Raw UUID (340a0917...) should NOT exist anywhere!
      expect(find.text(rawUuid), findsNothing);
      // Formatted UUID (REC-340a0917) MUST exist!
      expect(find.text('REC-340a0917'), findsOneWidget);

      // 3. Dynamic Field Check: labels & values rendered directly from FieldDefinition
      expect(find.text('고객명'), findsOneWidget);
      expect(find.text('홍길동'), findsOneWidget);
      expect(find.text('회원등급'), findsOneWidget);
      expect(find.text('VIP'), findsOneWidget);
    });
  });
}
