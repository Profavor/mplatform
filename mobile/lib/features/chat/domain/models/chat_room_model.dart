import 'package:freezed_annotation/freezed_annotation.dart';

part 'chat_room_model.freezed.dart';
part 'chat_room_model.g.dart';

@freezed
class ChatRoomModel with _$ChatRoomModel {
  const factory ChatRoomModel({
    @JsonKey(name: 'id') required String roomId,
    @JsonKey(name: 'name') required String title,
    @Default([]) List<String> participantNames,
    String? createdBy,
    String? lastMessage,
    @Default(0) int unreadCount,
    @JsonKey(name: 'lastMessageAt') String? updatedAt,
  }) = _ChatRoomModel;

  factory ChatRoomModel.fromJson(Map<String, dynamic> json) => _$ChatRoomModelFromJson(json);
}
