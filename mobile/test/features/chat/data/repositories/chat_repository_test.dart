import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:mplatform_mobile/features/chat/data/repositories/chat_repository.dart';

import 'chat_repository_test.mocks.dart';

@GenerateMocks([Dio])
void main() {
  group('ChatRepository Tests (TDD - 10개 채팅 API 연동 & Safe Delete Rules)', () {
    late MockDio mockDio;
    late ChatRepository repository;

    setUp(() {
      mockDio = MockDio();
      repository = ChatRepository(mockDio);
    });

    test('getChatRooms requests /api/chat/rooms and returns parsed ChatRoomModel list', () async {
      final mockData = [
        {
          'roomId': 'room-uuid-1',
          'title': '개발팀 마스터 데이터 논의',
          'participantNames': ['kim_developer', 'lee_admin'],
          'lastMessage': '결재 승인 완료되었습니다.',
          'unreadCount': 2,
          'updatedAt': '2026-08-06T16:00:00Z',
        }
      ];

      when(mockDio.get('/api/chat/rooms', queryParameters: anyNamed('queryParameters'), options: anyNamed('options'), cancelToken: anyNamed('cancelToken'), onReceiveProgress: anyNamed('onReceiveProgress')))
          .thenAnswer((_) async => Response(requestOptions: RequestOptions(path: '/api/chat/rooms'), statusCode: 200, data: mockData));

      final rooms = await repository.getChatRooms();

      expect(rooms.length, equals(1));
      expect(rooms.first.title, equals('개발팀 마스터 데이터 논의'));
      expect(rooms.first.unreadCount, equals(2));
    });

    test('sendMessage sends message payload and returns new ChatMessageModel', () async {
      final resData = {
        'messageId': 'msg-uuid-101',
        'roomId': 'room-uuid-1',
        'senderUsername': 'user_kim',
        'senderName': '김개발',
        'content': '안녕하세요!',
        'timestamp': '2026-08-06T16:05:00Z',
        'isRead': false,
      };

      when(mockDio.post(
        '/api/chat/rooms/room-uuid-1/messages',
        data: argThat(equals({'content': '안녕하세요!'}), named: 'data'),
        options: anyNamed('options'),
        cancelToken: anyNamed('cancelToken'),
        onSendProgress: anyNamed('onSendProgress'),
        onReceiveProgress: anyNamed('onReceiveProgress'),
      )).thenAnswer((_) async => Response(requestOptions: RequestOptions(path: '/api/chat/rooms/room-uuid-1/messages'), statusCode: 200, data: resData));

      final message = await repository.sendMessage('room-uuid-1', content: '안녕하세요!');

      expect(message.content, equals('안녕하세요!'));
      expect(message.senderName, equals('김개발'));
    });

    test('deleteMessage performs targeted single-record delete without truncate or mass wipe', () async {
      when(mockDio.delete('/api/chat/messages/msg-uuid-101', data: anyNamed('data'), queryParameters: anyNamed('queryParameters'), options: anyNamed('options'), cancelToken: anyNamed('cancelToken')))
          .thenAnswer((_) async => Response(requestOptions: RequestOptions(path: '/api/chat/messages/msg-uuid-101'), statusCode: 200));

      final success = await repository.deleteMessage('msg-uuid-101');

      expect(success, isTrue);
      verify(mockDio.delete('/api/chat/messages/msg-uuid-101', data: anyNamed('data'), queryParameters: anyNamed('queryParameters'), options: anyNamed('options'), cancelToken: anyNamed('cancelToken'))).called(1);
    });
  });
}
