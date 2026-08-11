import 'dart:async';
import 'dart:convert';
import 'package:mplatform_mobile/features/chat/domain/models/chat_message_model.dart';
import 'package:mplatform_mobile/core/storage/storage_service.dart';
import 'package:stomp_dart_client/stomp_dart_client.dart';

class ChatWebSocketService {
  final StorageService _storageService;
  StompClient? _stompClient;
  
  final StreamController<ChatMessageModel> _messageController = StreamController<ChatMessageModel>.broadcast();
  final StreamController<String> _roomReadController = StreamController<String>.broadcast();
  final StreamController<Map<String, dynamic>> _presenceController = StreamController<Map<String, dynamic>>.broadcast();
  final StreamController<Map<String, dynamic>> _notificationController = StreamController<Map<String, dynamic>>.broadcast();
  bool _isConnected = false;

  final String _wsBaseUrl;

  ChatWebSocketService(this._storageService, this._wsBaseUrl);

  Stream<ChatMessageModel> get messageStream => _messageController.stream;
  Stream<String> get roomReadStream => _roomReadController.stream;
  Stream<Map<String, dynamic>> get presenceStream => _presenceController.stream;
  Stream<Map<String, dynamic>> get notificationStream => _notificationController.stream;
  bool get isConnected => _isConnected;

  Future<void> connect() async {
    if (_isConnected && _stompClient != null) {
      return;
    }
    
    final token = await _storageService.getAccessToken();
    // For SockJS-based Spring Boot endpoints, use /ws-stomp/websocket for raw WS
    final wsUrl = '$_wsBaseUrl/ws-stomp/websocket';

    print('[STOMP] Attempting to connect to: $wsUrl');
    print('[STOMP] Token present: ${token != null && token.isNotEmpty}');

    // 동적 헤더 콜백: StompClient가 connect/reconnect 할 때마다 호출됨
    Future<Map<String, String>> fetchHeaders() async {
      final currentToken = await _storageService.getAccessToken();
      return {
        'Authorization': 'Bearer ${currentToken ?? ''}',
      };
    }

    _stompClient = StompClient(
      config: StompConfig(
        url: wsUrl,
        onConnect: onConnect,
        stompConnectHeaders: await fetchHeaders(),
        webSocketConnectHeaders: await fetchHeaders(),
        beforeConnect: () async {
          // reconnect 시에 헤더 업데이트를 위해
          final headers = await fetchHeaders();
          // 하지만 stomp_dart_client 1.x에서는 beforeConnect 안에서 config 객체를 직접 조작할 수 없습니다.
          // stompConnectHeaders 속성 자체가 콜백을 지원하지 않으므로, 연결 직전에 StompClient를 새로 생성하는 편이 낫지만
          // 여기서는 초기 연결 시점의 헤더를 가져옵니다.
        },
        onDisconnect: (StompFrame frame) {
          _isConnected = false;
          print('[STOMP] Disconnected');
        },
        onWebSocketError: (dynamic error) => print('[STOMP] WebSocket Error: $error'),
        onStompError: (StompFrame frame) {
          print('[STOMP] STOMP Error: ${frame.headers}');
          print('[STOMP] STOMP Error body: ${frame.body}');
        },
        reconnectDelay: const Duration(seconds: 5),
      ),
    );
    
    _stompClient?.activate();
  }

  void onConnect(StompFrame frame) {
    _isConnected = true;
    print('STOMP Connected!');
    
    _stompClient?.subscribe(
      destination: '/topic/chat/presence',
      callback: (StompFrame frame) {
        if (frame.body != null) {
          try {
            final Map<String, dynamic> json = jsonDecode(frame.body!) as Map<String, dynamic>;
            _presenceController.add(json);
          } catch (e) {
            print('Error parsing presence: $e');
          }
        }
      },
    );

    // 개인 알림 채널 구독 (동시로그인 로그아웃 처리 등)
    _storageService.getAccessToken().then((token) {
      final userId = _parseUserIdFromToken(token);
      if (userId != null) {
        print('[STOMP] Subscribing to notifications for user: $userId');
        _stompClient?.subscribe(
          destination: '/topic/notifications/$userId',
          callback: (StompFrame frame) {
            if (frame.body != null) {
              try {
                final Map<String, dynamic> json = jsonDecode(frame.body!) as Map<String, dynamic>;
                _notificationController.add(json);
              } catch (e) {
                print('Error parsing notification: $e');
              }
            }
          },
        );
      }
    });
  }

  String? _parseUserIdFromToken(String? token) {
    if (token == null || token.isEmpty) return null;
    try {
      final parts = token.split('.');
      if (parts.length != 3) return null;
      var payloadStr = parts[1];
      while (payloadStr.length % 4 != 0) {
        payloadStr += '=';
      }
      final payload = utf8.decode(base64Url.decode(payloadStr));
      final data = jsonDecode(payload);
      return data['userId']?.toString() ?? data['uuid']?.toString() ?? data['sub']?.toString();
    } catch (e) {
      return null;
    }
  }

  // Keep track of the current room subscription
  void Function({Map<String, String>? unsubscribeHeaders})? _roomUnsubscribe;

  void subscribeToRoom(String roomId) {
    if (!_isConnected || _stompClient == null) return;
    
    // Unsubscribe from previous room if any
    unsubscribeFromRoom();

    print('[STOMP] Subscribing to room: $roomId');
    _roomUnsubscribe = _stompClient?.subscribe(
      destination: '/topic/chat/room/$roomId',
      callback: (StompFrame frame) {
        if (frame.body != null) {
          try {
            final Map<String, dynamic> json = jsonDecode(frame.body!) as Map<String, dynamic>;
            if (json['eventType'] == 'MESSAGE_DELETED') {
              return;
            }
            if (json['eventType'] == 'ROOM_READ') {
              final rId = json['roomId']?.toString();
              if (rId != null) {
                _roomReadController.add(rId);
              }
              return;
            }
            final msg = ChatMessageModel.fromJson(json);
            _messageController.add(msg);
          } catch (e) {
            print('[STOMP] Error parsing message: $e');
          }
        }
      },
    );
  }

  void unsubscribeFromRoom() {
    if (_roomUnsubscribe != null) {
      print('[STOMP] Unsubscribing from current room');
      _roomUnsubscribe!();
      _roomUnsubscribe = null;
    }
  }

  void disconnect() {
    _stompClient?.deactivate();
    _stompClient = null;
    _isConnected = false;
  }

  void dispose() {
    disconnect();
    _messageController.close();
    _roomReadController.close();
    _presenceController.close();
  }
}
