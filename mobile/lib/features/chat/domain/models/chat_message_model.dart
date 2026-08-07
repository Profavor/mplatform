import 'package:freezed_annotation/freezed_annotation.dart';

part 'chat_message_model.freezed.dart';
part 'chat_message_model.g.dart';

@freezed
class ChatMessageModel with _$ChatMessageModel {
  const factory ChatMessageModel({
    @JsonKey(name: 'id') @Default('') String messageId,
    @Default('') String roomId,
    @JsonKey(name: 'senderId') @Default('SYSTEM') String senderUsername,
    @Default('SYSTEM') String senderName,
    @Default('') String content,
    @Default('TEXT') String messageType,
    @JsonKey(name: 'fileUrl') String? attachmentUrl,
    String? fileName,
    int? fileSize,
    @JsonKey(name: 'createdAt') String? timestamp,
    @Default(0) int unreadCount,
    @Default(true) bool isRead,
  }) = _ChatMessageModel;

  factory ChatMessageModel.fromJson(Map<String, dynamic> json) =>
      _$ChatMessageModelFromJson(json);
}
