import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:mplatform_mobile/features/records/data/repositories/records_repository.dart';
import 'package:mplatform_mobile/features/records/domain/models/domain_model.dart';
import 'package:mplatform_mobile/features/records/domain/models/field_definition.dart';
import 'package:mplatform_mobile/features/records/domain/models/record_item.dart';
import 'package:mplatform_mobile/features/records/domain/models/records_page_response.dart';
import 'package:mplatform_mobile/features/records/presentation/providers/records_provider.dart';

import 'records_controller_test.mocks.dart';

@GenerateMocks([RecordsRepository])
void main() {
  group('RecordsController Tests (TDD - Server-Side Pagination)', () {
    late MockRecordsRepository mockRepository;
    late RecordsController controller;

    setUp(() {
      mockRepository = MockRecordsRepository();
      controller = RecordsController(mockRepository);
    });

    test('loadInitialData fetches domains, sets default domain, loads dynamic schema fields and page 0 of records', () async {
      const domain = DomainModel(id: 'domain-uuid-1', name: '고객 정보');
      const field = FieldDefinition(id: 101, fieldName: 'cust_nm', fieldLabel: '고객명', fieldType: 'String', showInList: true);
      const record = RecordItem(recordId: '340a0917-af0b-4d13-a1ce-479d4b2e2ca7', domainId: 'domain-uuid-1', attributes: {'cust_nm': '홍길동'});
      const pageRes = RecordsPageResponse(content: [record], totalElements: 50, totalPages: 3, number: 0, size: 20);

      when(mockRepository.getDomains()).thenAnswer((_) async => [domain]);
      when(mockRepository.getFieldDefinitions('domain-uuid-1')).thenAnswer((_) async => [field]);
      when(mockRepository.getRecords(domainId: 'domain-uuid-1', page: 0, size: 20, searchQuery: anyNamed('searchQuery'), filters: anyNamed('filters')))
          .thenAnswer((_) async => pageRes);

      await controller.loadInitialData();

      expect(controller.state.selectedDomainId, equals('domain-uuid-1'));
      expect(controller.state.fieldDefinitions.length, equals(1));
      expect(controller.state.records.length, equals(1));
      expect(controller.state.currentPage, equals(0));
      expect(controller.state.totalPages, equals(3));
      expect(controller.state.isLoading, isFalse);
    });

    test('loadNextPage requests next page index (page=1) and appends new records to existing list', () async {
      // Setup initial state with page 0 loaded (total 3 pages)
      const domain = DomainModel(id: 'domain-uuid-1', name: '고객 정보');
      const record1 = RecordItem(recordId: 'uuid-1111-1111', domainId: 'domain-uuid-1', attributes: {'cust_nm': '홍길동'});
      const record2 = RecordItem(recordId: 'uuid-2222-2222', domainId: 'domain-uuid-1', attributes: {'cust_nm': '김철수'});

      when(mockRepository.getDomains()).thenAnswer((_) async => [domain]);
      when(mockRepository.getFieldDefinitions('domain-uuid-1')).thenAnswer((_) async => []);
      when(mockRepository.getRecords(domainId: 'domain-uuid-1', page: 0, size: 20, searchQuery: anyNamed('searchQuery'), filters: anyNamed('filters')))
          .thenAnswer((_) async => const RecordsPageResponse(content: [record1], totalElements: 50, totalPages: 3, number: 0, size: 20));

      await controller.loadInitialData();
      expect(controller.state.records.length, equals(1));

      // Mock response for page 1
      when(mockRepository.getRecords(domainId: 'domain-uuid-1', page: 1, size: 20, searchQuery: anyNamed('searchQuery'), filters: anyNamed('filters')))
          .thenAnswer((_) async => const RecordsPageResponse(content: [record2], totalElements: 50, totalPages: 3, number: 1, size: 20));

      await controller.loadNextPage();

      expect(controller.state.currentPage, equals(1));
      expect(controller.state.records.length, equals(2));
      expect(controller.state.records.last.recordId, equals('uuid-2222-2222'));
      expect(controller.state.isLoadingMore, isFalse);
    });

    test('loadNextPage does nothing if already at last page (currentPage + 1 >= totalPages)', () async {
      const domain = DomainModel(id: 'domain-uuid-1', name: '고객 정보');
      when(mockRepository.getDomains()).thenAnswer((_) async => [domain]);
      when(mockRepository.getFieldDefinitions('domain-uuid-1')).thenAnswer((_) async => []);
      when(mockRepository.getRecords(domainId: 'domain-uuid-1', page: 0, size: 20, searchQuery: anyNamed('searchQuery'), filters: anyNamed('filters')))
          .thenAnswer((_) async => const RecordsPageResponse(content: [], totalElements: 5, totalPages: 1, number: 0, size: 20));

      await controller.loadInitialData();
      expect(controller.state.currentPage, equals(0));

      await controller.loadNextPage();

      // Should not attempt to call page 1
      verifyNever(mockRepository.getRecords(domainId: 'domain-uuid-1', page: 1, size: 20));
    });
  });
}
