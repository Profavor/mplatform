// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chat_message_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$ChatMessageModelImpl _$$ChatMessageModelImplFromJson(
  Map<String, dynamic> json,
) => _$ChatMessageModelImpl(
  messageId: json['id'] as String? ?? '',
  roomId: json['roomId'] as String? ?? '',
  senderUsername: json['senderId'] as String? ?? 'SYSTEM',
  senderName: json['senderName'] as String? ?? 'SYSTEM',
  content: json['content'] as String? ?? '',
  messageType: json['messageType'] as String? ?? 'TEXT',
  attachmentUrl: json['fileUrl'] as String?,
  fileName: json['fileName'] as String?,
  fileSize: (json['fileSize'] as num?)?.toInt(),
  timestamp: json['createdAt'] as String?,
  unreadCount: (json['unreadCount'] as num?)?.toInt() ?? 0,
  isRead: json['isRead'] as bool? ?? true,
);

Map<String, dynamic> _$$ChatMessageModelImplToJson(
  _$ChatMessageModelImpl instance,
) => <String, dynamic>{
  'id': instance.messageId,
  'roomId': instance.roomId,
  'senderId': instance.senderUsername,
  'senderName': instance.senderName,
  'content': instance.content,
  'messageType': instance.messageType,
  'fileUrl': instance.attachmentUrl,
  'fileName': instance.fileName,
  'fileSize': instance.fileSize,
  'createdAt': instance.timestamp,
  'unreadCount': instance.unreadCount,
  'isRead': instance.isRead,
};
