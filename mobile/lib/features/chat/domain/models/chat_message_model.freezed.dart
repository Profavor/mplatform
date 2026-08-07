// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'chat_message_model.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
  'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models',
);

ChatMessageModel _$ChatMessageModelFromJson(Map<String, dynamic> json) {
  return _ChatMessageModel.fromJson(json);
}

/// @nodoc
mixin _$ChatMessageModel {
  @JsonKey(name: 'id')
  String get messageId => throw _privateConstructorUsedError;
  String get roomId => throw _privateConstructorUsedError;
  @JsonKey(name: 'senderId')
  String get senderUsername => throw _privateConstructorUsedError;
  String get senderName => throw _privateConstructorUsedError;
  String get content => throw _privateConstructorUsedError;
  String get messageType => throw _privateConstructorUsedError;
  @JsonKey(name: 'fileUrl')
  String? get attachmentUrl => throw _privateConstructorUsedError;
  String? get fileName => throw _privateConstructorUsedError;
  int? get fileSize => throw _privateConstructorUsedError;
  @JsonKey(name: 'createdAt')
  String? get timestamp => throw _privateConstructorUsedError;
  int get unreadCount => throw _privateConstructorUsedError;
  bool get isRead => throw _privateConstructorUsedError;

  /// Serializes this ChatMessageModel to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of ChatMessageModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $ChatMessageModelCopyWith<ChatMessageModel> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ChatMessageModelCopyWith<$Res> {
  factory $ChatMessageModelCopyWith(
    ChatMessageModel value,
    $Res Function(ChatMessageModel) then,
  ) = _$ChatMessageModelCopyWithImpl<$Res, ChatMessageModel>;
  @useResult
  $Res call({
    @JsonKey(name: 'id') String messageId,
    String roomId,
    @JsonKey(name: 'senderId') String senderUsername,
    String senderName,
    String content,
    String messageType,
    @JsonKey(name: 'fileUrl') String? attachmentUrl,
    String? fileName,
    int? fileSize,
    @JsonKey(name: 'createdAt') String? timestamp,
    int unreadCount,
    bool isRead,
  });
}

/// @nodoc
class _$ChatMessageModelCopyWithImpl<$Res, $Val extends ChatMessageModel>
    implements $ChatMessageModelCopyWith<$Res> {
  _$ChatMessageModelCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of ChatMessageModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? messageId = null,
    Object? roomId = null,
    Object? senderUsername = null,
    Object? senderName = null,
    Object? content = null,
    Object? messageType = null,
    Object? attachmentUrl = freezed,
    Object? fileName = freezed,
    Object? fileSize = freezed,
    Object? timestamp = freezed,
    Object? unreadCount = null,
    Object? isRead = null,
  }) {
    return _then(
      _value.copyWith(
            messageId: null == messageId
                ? _value.messageId
                : messageId // ignore: cast_nullable_to_non_nullable
                      as String,
            roomId: null == roomId
                ? _value.roomId
                : roomId // ignore: cast_nullable_to_non_nullable
                      as String,
            senderUsername: null == senderUsername
                ? _value.senderUsername
                : senderUsername // ignore: cast_nullable_to_non_nullable
                      as String,
            senderName: null == senderName
                ? _value.senderName
                : senderName // ignore: cast_nullable_to_non_nullable
                      as String,
            content: null == content
                ? _value.content
                : content // ignore: cast_nullable_to_non_nullable
                      as String,
            messageType: null == messageType
                ? _value.messageType
                : messageType // ignore: cast_nullable_to_non_nullable
                      as String,
            attachmentUrl: freezed == attachmentUrl
                ? _value.attachmentUrl
                : attachmentUrl // ignore: cast_nullable_to_non_nullable
                      as String?,
            fileName: freezed == fileName
                ? _value.fileName
                : fileName // ignore: cast_nullable_to_non_nullable
                      as String?,
            fileSize: freezed == fileSize
                ? _value.fileSize
                : fileSize // ignore: cast_nullable_to_non_nullable
                      as int?,
            timestamp: freezed == timestamp
                ? _value.timestamp
                : timestamp // ignore: cast_nullable_to_non_nullable
                      as String?,
            unreadCount: null == unreadCount
                ? _value.unreadCount
                : unreadCount // ignore: cast_nullable_to_non_nullable
                      as int,
            isRead: null == isRead
                ? _value.isRead
                : isRead // ignore: cast_nullable_to_non_nullable
                      as bool,
          )
          as $Val,
    );
  }
}

/// @nodoc
abstract class _$$ChatMessageModelImplCopyWith<$Res>
    implements $ChatMessageModelCopyWith<$Res> {
  factory _$$ChatMessageModelImplCopyWith(
    _$ChatMessageModelImpl value,
    $Res Function(_$ChatMessageModelImpl) then,
  ) = __$$ChatMessageModelImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({
    @JsonKey(name: 'id') String messageId,
    String roomId,
    @JsonKey(name: 'senderId') String senderUsername,
    String senderName,
    String content,
    String messageType,
    @JsonKey(name: 'fileUrl') String? attachmentUrl,
    String? fileName,
    int? fileSize,
    @JsonKey(name: 'createdAt') String? timestamp,
    int unreadCount,
    bool isRead,
  });
}

/// @nodoc
class __$$ChatMessageModelImplCopyWithImpl<$Res>
    extends _$ChatMessageModelCopyWithImpl<$Res, _$ChatMessageModelImpl>
    implements _$$ChatMessageModelImplCopyWith<$Res> {
  __$$ChatMessageModelImplCopyWithImpl(
    _$ChatMessageModelImpl _value,
    $Res Function(_$ChatMessageModelImpl) _then,
  ) : super(_value, _then);

  /// Create a copy of ChatMessageModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? messageId = null,
    Object? roomId = null,
    Object? senderUsername = null,
    Object? senderName = null,
    Object? content = null,
    Object? messageType = null,
    Object? attachmentUrl = freezed,
    Object? fileName = freezed,
    Object? fileSize = freezed,
    Object? timestamp = freezed,
    Object? unreadCount = null,
    Object? isRead = null,
  }) {
    return _then(
      _$ChatMessageModelImpl(
        messageId: null == messageId
            ? _value.messageId
            : messageId // ignore: cast_nullable_to_non_nullable
                  as String,
        roomId: null == roomId
            ? _value.roomId
            : roomId // ignore: cast_nullable_to_non_nullable
                  as String,
        senderUsername: null == senderUsername
            ? _value.senderUsername
            : senderUsername // ignore: cast_nullable_to_non_nullable
                  as String,
        senderName: null == senderName
            ? _value.senderName
            : senderName // ignore: cast_nullable_to_non_nullable
                  as String,
        content: null == content
            ? _value.content
            : content // ignore: cast_nullable_to_non_nullable
                  as String,
        messageType: null == messageType
            ? _value.messageType
            : messageType // ignore: cast_nullable_to_non_nullable
                  as String,
        attachmentUrl: freezed == attachmentUrl
            ? _value.attachmentUrl
            : attachmentUrl // ignore: cast_nullable_to_non_nullable
                  as String?,
        fileName: freezed == fileName
            ? _value.fileName
            : fileName // ignore: cast_nullable_to_non_nullable
                  as String?,
        fileSize: freezed == fileSize
            ? _value.fileSize
            : fileSize // ignore: cast_nullable_to_non_nullable
                  as int?,
        timestamp: freezed == timestamp
            ? _value.timestamp
            : timestamp // ignore: cast_nullable_to_non_nullable
                  as String?,
        unreadCount: null == unreadCount
            ? _value.unreadCount
            : unreadCount // ignore: cast_nullable_to_non_nullable
                  as int,
        isRead: null == isRead
            ? _value.isRead
            : isRead // ignore: cast_nullable_to_non_nullable
                  as bool,
      ),
    );
  }
}

/// @nodoc
@JsonSerializable()
class _$ChatMessageModelImpl implements _ChatMessageModel {
  const _$ChatMessageModelImpl({
    @JsonKey(name: 'id') this.messageId = '',
    this.roomId = '',
    @JsonKey(name: 'senderId') this.senderUsername = 'SYSTEM',
    this.senderName = 'SYSTEM',
    this.content = '',
    this.messageType = 'TEXT',
    @JsonKey(name: 'fileUrl') this.attachmentUrl,
    this.fileName,
    this.fileSize,
    @JsonKey(name: 'createdAt') this.timestamp,
    this.unreadCount = 0,
    this.isRead = true,
  });

  factory _$ChatMessageModelImpl.fromJson(Map<String, dynamic> json) =>
      _$$ChatMessageModelImplFromJson(json);

  @override
  @JsonKey(name: 'id')
  final String messageId;
  @override
  @JsonKey()
  final String roomId;
  @override
  @JsonKey(name: 'senderId')
  final String senderUsername;
  @override
  @JsonKey()
  final String senderName;
  @override
  @JsonKey()
  final String content;
  @override
  @JsonKey()
  final String messageType;
  @override
  @JsonKey(name: 'fileUrl')
  final String? attachmentUrl;
  @override
  final String? fileName;
  @override
  final int? fileSize;
  @override
  @JsonKey(name: 'createdAt')
  final String? timestamp;
  @override
  @JsonKey()
  final int unreadCount;
  @override
  @JsonKey()
  final bool isRead;

  @override
  String toString() {
    return 'ChatMessageModel(messageId: $messageId, roomId: $roomId, senderUsername: $senderUsername, senderName: $senderName, content: $content, messageType: $messageType, attachmentUrl: $attachmentUrl, fileName: $fileName, fileSize: $fileSize, timestamp: $timestamp, unreadCount: $unreadCount, isRead: $isRead)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ChatMessageModelImpl &&
            (identical(other.messageId, messageId) ||
                other.messageId == messageId) &&
            (identical(other.roomId, roomId) || other.roomId == roomId) &&
            (identical(other.senderUsername, senderUsername) ||
                other.senderUsername == senderUsername) &&
            (identical(other.senderName, senderName) ||
                other.senderName == senderName) &&
            (identical(other.content, content) || other.content == content) &&
            (identical(other.messageType, messageType) ||
                other.messageType == messageType) &&
            (identical(other.attachmentUrl, attachmentUrl) ||
                other.attachmentUrl == attachmentUrl) &&
            (identical(other.fileName, fileName) ||
                other.fileName == fileName) &&
            (identical(other.fileSize, fileSize) ||
                other.fileSize == fileSize) &&
            (identical(other.timestamp, timestamp) ||
                other.timestamp == timestamp) &&
            (identical(other.unreadCount, unreadCount) ||
                other.unreadCount == unreadCount) &&
            (identical(other.isRead, isRead) || other.isRead == isRead));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
    runtimeType,
    messageId,
    roomId,
    senderUsername,
    senderName,
    content,
    messageType,
    attachmentUrl,
    fileName,
    fileSize,
    timestamp,
    unreadCount,
    isRead,
  );

  /// Create a copy of ChatMessageModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$ChatMessageModelImplCopyWith<_$ChatMessageModelImpl> get copyWith =>
      __$$ChatMessageModelImplCopyWithImpl<_$ChatMessageModelImpl>(
        this,
        _$identity,
      );

  @override
  Map<String, dynamic> toJson() {
    return _$$ChatMessageModelImplToJson(this);
  }
}

abstract class _ChatMessageModel implements ChatMessageModel {
  const factory _ChatMessageModel({
    @JsonKey(name: 'id') final String messageId,
    final String roomId,
    @JsonKey(name: 'senderId') final String senderUsername,
    final String senderName,
    final String content,
    final String messageType,
    @JsonKey(name: 'fileUrl') final String? attachmentUrl,
    final String? fileName,
    final int? fileSize,
    @JsonKey(name: 'createdAt') final String? timestamp,
    final int unreadCount,
    final bool isRead,
  }) = _$ChatMessageModelImpl;

  factory _ChatMessageModel.fromJson(Map<String, dynamic> json) =
      _$ChatMessageModelImpl.fromJson;

  @override
  @JsonKey(name: 'id')
  String get messageId;
  @override
  String get roomId;
  @override
  @JsonKey(name: 'senderId')
  String get senderUsername;
  @override
  String get senderName;
  @override
  String get content;
  @override
  String get messageType;
  @override
  @JsonKey(name: 'fileUrl')
  String? get attachmentUrl;
  @override
  String? get fileName;
  @override
  int? get fileSize;
  @override
  @JsonKey(name: 'createdAt')
  String? get timestamp;
  @override
  int get unreadCount;
  @override
  bool get isRead;

  /// Create a copy of ChatMessageModel
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$ChatMessageModelImplCopyWith<_$ChatMessageModelImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
