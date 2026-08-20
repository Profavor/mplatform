import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:mplatform_mobile/features/inbox/data/repositories/inbox_repository.dart';

import 'inbox_repository_test.mocks.dart';

@GenerateMocks([Dio])
void main() {
  group('InboxRepository Tests (TDD - Zero Hardcoding & Folder Navigation)', () {
    late MockDio mockDio;
    late InboxRepository repository;

    setUp(() {
      mockDio = MockDio();
      repository = InboxRepositoryImpl(mockDio);
    });

    test('getMessages fetches message list from /api/inbox/messages and parses properly', () async {
      final mockData = {
        'content': [
          {
            'id': 'inb-uuid-1111-2222',
            'senderId': 'user01',
            'senderName': '김철수',
            'subject': '신규 스키마 승인 안내',
            'body': '요청하신 마스터 데이터 스키마가 승인되었습니다.',
            'importance': 'HIGH',
            'isRead': false,
            'isStarred': true,
            'folder': 'INBOX',
            'createdAt': '2026-08-20T10:00:00Z',
          }
        ],
        'totalElements': 1,
      };

      when(mockDio.get('/api/inbox/messages', queryParameters: anyNamed('queryParameters'), options: anyNamed('options'), cancelToken: anyNamed('cancelToken'), onReceiveProgress: anyNamed('onReceiveProgress')))
          .thenAnswer((_) async => Response(requestOptions: RequestOptions(path: '/api/inbox/messages'), statusCode: 200, data: mockData));

      final items = await repository.getMessages(folder: 'INBOX');

      expect(items.length, equals(1));
      expect(items.first.id, equals('inb-uuid-1111-2222'));
      expect(items.first.senderName, equals('김철수'));
      expect(items.first.subject, equals('신규 스키마 승인 안내'));
      expect(items.first.isStarred, isTrue);
      expect(items.first.isRead, isFalse);
    });

    test('getMessage fetches single message detail from /api/inbox/messages/{id}', () async {
      final mockData = {
        'id': 'inb-uuid-1111-2222',
        'senderId': 'user01',
        'senderName': '김철수',
        'subject': '신규 스키마 승인 안내',
        'body': '상세 본문 내용입니다.',
        'isRead': true,
        'folder': 'INBOX',
        'attachments': [
          {
            'id': 'att-uuid-3333',
            'fileName': 'schema_spec.pdf',
            'fileSize': 102400,
          }
        ],
      };

      when(mockDio.get('/api/inbox/messages/inb-uuid-1111-2222', queryParameters: anyNamed('queryParameters'), options: anyNamed('options'), cancelToken: anyNamed('cancelToken'), onReceiveProgress: anyNamed('onReceiveProgress')))
          .thenAnswer((_) async => Response(requestOptions: RequestOptions(path: '/api/inbox/messages/inb-uuid-1111-2222'), statusCode: 200, data: mockData));

      final item = await repository.getMessage('inb-uuid-1111-2222');

      expect(item.id, equals('inb-uuid-1111-2222'));
      expect(item.attachments.length, equals(1));
      expect(item.attachments.first.fileName, equals('schema_spec.pdf'));
    });

    test('sendMessage posts to /api/inbox/messages and returns created message', () async {
      final requestPayload = {
        'subject': '테스트 쪽지',
        'body': '쪽지 본문입니다.',
        'recipientUserIds': ['user02'],
      };

      when(mockDio.post('/api/inbox/messages', data: anyNamed('data'), queryParameters: anyNamed('queryParameters'), options: anyNamed('options'), cancelToken: anyNamed('cancelToken'), onSendProgress: anyNamed('onSendProgress'), onReceiveProgress: anyNamed('onReceiveProgress')))
          .thenAnswer((_) async => Response(
                requestOptions: RequestOptions(path: '/api/inbox/messages'),
                statusCode: 200,
                data: {
                  'id': 'new-inb-uuid-9999',
                  'senderId': 'current_user',
                  'senderName': '나',
                  'subject': '테스트 쪽지',
                  'body': '쪽지 본문입니다.',
                },
              ));

      final created = await repository.sendMessage(requestPayload);

      expect(created.id, equals('new-inb-uuid-9999'));
      expect(created.subject, equals('테스트 쪽지'));
    });

    test('toggleStar patches /api/inbox/messages/{id}/star', () async {
      when(mockDio.patch('/api/inbox/messages/inb-uuid-1111-2222/star', data: anyNamed('data'), queryParameters: anyNamed('queryParameters'), options: anyNamed('options'), cancelToken: anyNamed('cancelToken'))).thenAnswer((_) async => Response(
            requestOptions: RequestOptions(path: '/api/inbox/messages/inb-uuid-1111-2222/star'),
            statusCode: 200,
          ));

      await repository.toggleStar('inb-uuid-1111-2222');

      verify(mockDio.patch('/api/inbox/messages/inb-uuid-1111-2222/star', data: anyNamed('data'), queryParameters: anyNamed('queryParameters'), options: anyNamed('options'), cancelToken: anyNamed('cancelToken'))).called(1);
    });

    test('getFolderCounts returns folder counts', () async {
      final mockData = [
        {'folder': 'INBOX', 'total': 10, 'unread': 3},
        {'folder': 'SENT', 'total': 5, 'unread': 0},
      ];

      when(mockDio.get('/api/inbox/folder-counts', queryParameters: anyNamed('queryParameters'), options: anyNamed('options'), cancelToken: anyNamed('cancelToken'), onReceiveProgress: anyNamed('onReceiveProgress')))
          .thenAnswer((_) async => Response(requestOptions: RequestOptions(path: '/api/inbox/folder-counts'), statusCode: 200, data: mockData));

      final counts = await repository.getFolderCounts();

      expect(counts.length, equals(2));
      expect(counts.first.folder, equals('INBOX'));
      expect(counts.first.unread, equals(3));
    });
  });
}
