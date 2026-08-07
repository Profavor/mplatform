import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:mplatform_mobile/features/approvals/data/repositories/approvals_repository.dart';

import 'approvals_repository_test.mocks.dart';

@GenerateMocks([Dio])
void main() {
  group('ApprovalsRepository Tests (TDD - Workflow API Integration & PageResponse Parsing)', () {
    late MockDio mockDio;
    late ApprovalsRepository repository;

    setUp(() {
      mockDio = MockDio();
      repository = ApprovalsRepository(mockDio);
    });

    test('getPendingApprovals fetches workflow list from /api/approval-requests/todos and parses raw list', () async {
      final mockData = [
        {
          'approvalId': 'app-uuid-1111-2222',
          'targetType': 'RECORD_UPDATE',
          'targetId': 'rec-340a0917-af0b',
          'requester': 'user01',
          'status': 'PENDING',
          'requestDate': '2026-08-06T15:00:00Z'
        }
      ];

      when(mockDio.get('/api/approval-requests/todos', queryParameters: anyNamed('queryParameters'), options: anyNamed('options'), cancelToken: anyNamed('cancelToken'), onReceiveProgress: anyNamed('onReceiveProgress')))
          .thenAnswer((_) async => Response(requestOptions: RequestOptions(path: '/api/approval-requests/todos'), statusCode: 200, data: mockData));

      final items = await repository.getPendingApprovals();

      expect(items.length, equals(1));
      expect(items.first.approvalId, equals('app-uuid-1111-2222'));
      expect(items.first.requester, equals('user01'));
      expect(items.first.status, equals('PENDING'));
    });

    test('getPendingApprovals extracts content when server returns Spring Boot PageResponse map structure', () async {
      final pageResponseMap = {
        'content': [
          {
            'id': 'step-uuid-9999',
            'status': 'PENDING',
            'approvalRequest': {
              'id': 'req-uuid-8888',
              'targetType': 'SCHEMA',
              'targetId': 'dom-uuid-7777',
              'requesterUsername': 'admin',
              'status': 'PENDING',
              'createdAt': '2026-08-06T20:00:00Z'
            }
          }
        ],
        'totalElements': 1,
        'totalPages': 1,
        'size': 20,
        'number': 0
      };

      when(mockDio.get('/api/approval-requests/todos', queryParameters: anyNamed('queryParameters'), options: anyNamed('options'), cancelToken: anyNamed('cancelToken'), onReceiveProgress: anyNamed('onReceiveProgress')))
          .thenAnswer((_) async => Response(requestOptions: RequestOptions(path: '/api/approval-requests/todos'), statusCode: 200, data: pageResponseMap));

      final items = await repository.getPendingApprovals();

      expect(items.length, equals(1));
      expect(items.first.approvalId, equals('step-uuid-9999'));
      expect(items.first.targetType, equals('SCHEMA'));
      expect(items.first.requester, equals('admin'));
    });

    test('getMySubmittedApprovals fetches from /api/approval-requests/my-requests and handles PageResponse map', () async {
      final pageResponseMap = {
        'content': [
          {
            'id': 'my-req-uuid-1234',
            'targetType': 'RECORD',
            'targetId': 'rec-uuid-5678',
            'requesterName': 'kim_submitter',
            'status': 'APPROVED'
          }
        ],
        'totalElements': 1
      };

      when(mockDio.get('/api/approval-requests/my-requests', queryParameters: anyNamed('queryParameters'), options: anyNamed('options'), cancelToken: anyNamed('cancelToken'), onReceiveProgress: anyNamed('onReceiveProgress')))
          .thenAnswer((_) async => Response(requestOptions: RequestOptions(path: '/api/approval-requests/my-requests'), statusCode: 200, data: pageResponseMap));

      final items = await repository.getMySubmittedApprovals();

      expect(items.length, equals(1));
      expect(items.first.approvalId, equals('my-req-uuid-1234'));
      expect(items.first.requester, equals('kim_submitter'));
      expect(items.first.status, equals('APPROVED'));
    });

    test('approveRequest calls POST /api/approval-requests/steps/{id}/approve and returns success status', () async {
      when(mockDio.post(
        '/api/approval-requests/steps/app-uuid-1111-2222/approve',
        data: argThat(equals({'comment': '승인합니다.'}), named: 'data'),
        options: anyNamed('options'),
        cancelToken: anyNamed('cancelToken'),
        onSendProgress: anyNamed('onSendProgress'),
        onReceiveProgress: anyNamed('onReceiveProgress'),
      )).thenAnswer((_) async => Response(
            requestOptions: RequestOptions(path: '/api/approval-requests/steps/app-uuid-1111-2222/approve'),
            statusCode: 200,
          ));

      final success = await repository.approveRequest('app-uuid-1111-2222', comment: '승인합니다.');

      expect(success, isTrue);
      verify(mockDio.post('/api/approval-requests/steps/app-uuid-1111-2222/approve', data: anyNamed('data'), options: anyNamed('options'), cancelToken: anyNamed('cancelToken'), onSendProgress: anyNamed('onSendProgress'), onReceiveProgress: anyNamed('onReceiveProgress'))).called(1);
    });

    test('rejectRequest calls POST /api/approval-requests/steps/{id}/reject and returns success status', () async {
      when(mockDio.post(
        '/api/approval-requests/steps/app-uuid-1111-2222/reject',
        data: argThat(equals({'comment': '반려 사유'}), named: 'data'),
        options: anyNamed('options'),
        cancelToken: anyNamed('cancelToken'),
        onSendProgress: anyNamed('onSendProgress'),
        onReceiveProgress: anyNamed('onReceiveProgress'),
      )).thenAnswer((_) async => Response(
            requestOptions: RequestOptions(path: '/api/approval-requests/steps/app-uuid-1111-2222/reject'),
            statusCode: 200,
          ));

      final success = await repository.rejectRequest('app-uuid-1111-2222', reason: '반려 사유');

      expect(success, isTrue);
      verify(mockDio.post('/api/approval-requests/steps/app-uuid-1111-2222/reject', data: anyNamed('data'), options: anyNamed('options'), cancelToken: anyNamed('cancelToken'), onSendProgress: anyNamed('onSendProgress'), onReceiveProgress: anyNamed('onReceiveProgress'))).called(1);
    });
  });
}
