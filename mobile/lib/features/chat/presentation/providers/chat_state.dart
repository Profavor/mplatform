import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:mplatform_mobile/features/chat/domain/models/chat_message_model.dart';
import 'package:mplatform_mobile/features/chat/domain/models/chat_room_model.dart';

part 'chat_state.freezed.dart';

@freezed
class ChatState with _$ChatState {
  const factory ChatState({
    @Default([]) List<ChatRoomModel> rooms,
    @Default([]) List<ChatMessageModel> activeMessages,
    String? selectedRoomId,
    @Default(false) bool isLoadingRooms,
    @Default(false) bool isLoadingMessages,
    @Default(false) bool isSending,
    @Default({}) Set<String> onlineUserIds,
    @Default(0) int totalUnreadCount,
    String? errorMessage,
  }) = _ChatState;
}
