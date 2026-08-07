import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:mplatform_mobile/features/records/data/repositories/records_repository.dart';

import 'records_repository_test.mocks.dart';

@GenerateMocks([Dio])
void main() {
  group('RecordsRepository Tests (TDD - Server-Side Pagination & Dynamic Schema)', () {
    late MockDio mockDio;
    late RecordsRepository repository;

    setUp(() {
      mockDio = MockDio();
      repository = RecordsRepository(mockDio);
    });

    test('getDomains fetches domain list from /api/domains and parses DomainModel array', () async {
      final mockData = [
        {'id': 'domain-uuid-1', 'name': '고객 정보 도메인', 'description': '고객 관련 마스터 데이터', 'active': true},
        {'id': 'domain-uuid-2', 'name': '상품 정보 도메인', 'description': '상품 분류 및 가격', 'active': true},
      ];

      when(mockDio.get('/api/domains', queryParameters: anyNamed('queryParameters'), options: anyNamed('options'), cancelToken: anyNamed('cancelToken'), onReceiveProgress: anyNamed('onReceiveProgress')))
          .thenAnswer((_) async => Response(requestOptions: RequestOptions(path: '/api/domains'), statusCode: 200, data: mockData));

      final domains = await repository.getDomains();

      expect(domains.length, equals(2));
      expect(domains.first.name, equals('고객 정보 도메인'));
      expect(domains.first.id, equals('domain-uuid-1'));
    });

    test('getRecords sends server-side pagination parameters (page, size) to GET /api/records/domain/{id}', () async {
      final mockPageData = {
        'content': [
          {
            'recordId': '340a0917-af0b-4d13-a1ce-479d4b2e2ca7',
            'domainId': 'domain-uuid-1',
            'data': {'cust_nm': '홍길동', 'cust_grade': 'VIP', 'created_dt': '2026-08-06T12:00:00'},
            'createdBy': 'admin',
            'createdAt': '2026-08-06T12:00:00',
            'updatedAt': '2026-08-06T12:00:00',
          }
        ],
        'totalElements': 150,
        'totalPages': 8,
        'number': 0,
        'size': 20,
        'first': true,
        'last': false,
      };

      when(mockDio.get(
        '/api/records/domain/domain-uuid-1',
        queryParameters: argThat(
          equals({'page': 0, 'size': 20, 'search_name': '홍길동', 'search_q': '홍길동'}),
          named: 'queryParameters',
        ),
        options: anyNamed('options'),
        cancelToken: anyNamed('cancelToken'),
        onReceiveProgress: anyNamed('onReceiveProgress'),
      )).thenAnswer((_) async => Response(
            requestOptions: RequestOptions(path: '/api/records/domain/domain-uuid-1'),
            statusCode: 200,
            data: mockPageData,
          ));

      final result = await repository.getRecords(domainId: 'domain-uuid-1', page: 0, size: 20, searchQuery: '홍길동');

      expect(result.content.length, equals(1));
      expect(result.totalElements, equals(150));
      expect(result.totalPages, equals(8));
      expect(result.content.first.attributes['cust_nm'], equals('홍길동'));
    });

    test('getFieldDefinitions fetches dynamic schema definition from DB/API without hardcoded field names', () async {
      final mockFields = [
        {'id': '101', 'fieldName': 'cust_nm', 'fieldLabel': '고객명', 'fieldType': 'String', 'required': true, 'showInList': true, 'displayOrder': 1},
        {'id': '102', 'fieldName': 'cust_grade', 'fieldLabel': '회원등급', 'fieldType': 'Enum', 'required': true, 'showInList': true, 'options': ['GENERAL', 'VIP', 'VVIP'], 'displayOrder': 2},
      ];

      when(mockDio.get('/api/domains/domain-uuid-1/fields', queryParameters: anyNamed('queryParameters'), options: anyNamed('options'), cancelToken: anyNamed('cancelToken'), onReceiveProgress: anyNamed('onReceiveProgress')))
          .thenAnswer((_) async => Response(requestOptions: RequestOptions(path: '/api/domains/domain-uuid-1/fields'), statusCode: 200, data: mockFields));

      final fields = await repository.getFieldDefinitions('domain-uuid-1');

      expect(fields.length, equals(2));
      expect(fields[1].options, equals(['GENERAL', 'VIP', 'VVIP']));
    });

    test('decryptRecordFields calls /api/sensitive-data/record/{id}/decrypt and returns decrypted values', () async {
      final mockResponse = {
        'C_EMAIL': 'profavor@naver.com',
      };

      when(mockDio.post(
        '/api/sensitive-data/record/rec-123/decrypt',
        data: {
          'fieldKeys': ['C_EMAIL'],
          'accessReason': '본인 확인',
        },
        queryParameters: anyNamed('queryParameters'),
        options: anyNamed('options'),
        cancelToken: anyNamed('cancelToken'),
        onReceiveProgress: anyNamed('onReceiveProgress'),
      )).thenAnswer((_) async => Response(
            requestOptions: RequestOptions(path: '/api/sensitive-data/record/rec-123/decrypt'),
            statusCode: 200,
            data: mockResponse,
          ));

      final result = await repository.decryptRecordFields(
        recordId: 'rec-123',
        fieldKeys: ['C_EMAIL'],
        accessReason: '본인 확인',
      );

      expect(result.length, equals(1));
      expect(result['C_EMAIL'], equals('profavor@naver.com'));
    });
  });
}
