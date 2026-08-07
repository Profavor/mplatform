// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chat_room_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$ChatRoomModelImpl _$$ChatRoomModelImplFromJson(Map<String, dynamic> json) =>
    _$ChatRoomModelImpl(
      roomId: json['id'] as String,
      title: json['name'] as String,
      participantNames:
          (json['participantNames'] as List<dynamic>?)
              ?.map((e) => e as String)
              .toList() ??
          const [],
      createdBy: json['createdBy'] as String?,
      lastMessage: json['lastMessage'] as String?,
      unreadCount: (json['unreadCount'] as num?)?.toInt() ?? 0,
      updatedAt: json['lastMessageAt'] as String?,
    );

Map<String, dynamic> _$$ChatRoomModelImplToJson(_$ChatRoomModelImpl instance) =>
    <String, dynamic>{
      'id': instance.roomId,
      'name': instance.title,
      'participantNames': instance.participantNames,
      'createdBy': instance.createdBy,
      'lastMessage': instance.lastMessage,
      'unreadCount': instance.unreadCount,
      'lastMessageAt': instance.updatedAt,
    };
