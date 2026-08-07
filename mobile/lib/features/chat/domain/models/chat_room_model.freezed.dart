// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'chat_room_model.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
  'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models',
);

ChatRoomModel _$ChatRoomModelFromJson(Map<String, dynamic> json) {
  return _ChatRoomModel.fromJson(json);
}

/// @nodoc
mixin _$ChatRoomModel {
  @JsonKey(name: 'id')
  String get roomId => throw _privateConstructorUsedError;
  @JsonKey(name: 'name')
  String get title => throw _privateConstructorUsedError;
  List<String> get participantNames => throw _privateConstructorUsedError;
  String? get createdBy => throw _privateConstructorUsedError;
  String? get lastMessage => throw _privateConstructorUsedError;
  int get unreadCount => throw _privateConstructorUsedError;
  @JsonKey(name: 'lastMessageAt')
  String? get updatedAt => throw _privateConstructorUsedError;

  /// Serializes this ChatRoomModel to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of ChatRoomModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $ChatRoomModelCopyWith<ChatRoomModel> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ChatRoomModelCopyWith<$Res> {
  factory $ChatRoomModelCopyWith(
    ChatRoomModel value,
    $Res Function(ChatRoomModel) then,
  ) = _$ChatRoomModelCopyWithImpl<$Res, ChatRoomModel>;
  @useResult
  $Res call({
    @JsonKey(name: 'id') String roomId,
    @JsonKey(name: 'name') String title,
    List<String> participantNames,
    String? createdBy,
    String? lastMessage,
    int unreadCount,
    @JsonKey(name: 'lastMessageAt') String? updatedAt,
  });
}

/// @nodoc
class _$ChatRoomModelCopyWithImpl<$Res, $Val extends ChatRoomModel>
    implements $ChatRoomModelCopyWith<$Res> {
  _$ChatRoomModelCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of ChatRoomModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? roomId = null,
    Object? title = null,
    Object? participantNames = null,
    Object? createdBy = freezed,
    Object? lastMessage = freezed,
    Object? unreadCount = null,
    Object? updatedAt = freezed,
  }) {
    return _then(
      _value.copyWith(
            roomId: null == roomId
                ? _value.roomId
                : roomId // ignore: cast_nullable_to_non_nullable
                      as String,
            title: null == title
                ? _value.title
                : title // ignore: cast_nullable_to_non_nullable
                      as String,
            participantNames: null == participantNames
                ? _value.participantNames
                : participantNames // ignore: cast_nullable_to_non_nullable
                      as List<String>,
            createdBy: freezed == createdBy
                ? _value.createdBy
                : createdBy // ignore: cast_nullable_to_non_nullable
                      as String?,
            lastMessage: freezed == lastMessage
                ? _value.lastMessage
                : lastMessage // ignore: cast_nullable_to_non_nullable
                      as String?,
            unreadCount: null == unreadCount
                ? _value.unreadCount
                : unreadCount // ignore: cast_nullable_to_non_nullable
                      as int,
            updatedAt: freezed == updatedAt
                ? _value.updatedAt
                : updatedAt // ignore: cast_nullable_to_non_nullable
                      as String?,
          )
          as $Val,
    );
  }
}

/// @nodoc
abstract class _$$ChatRoomModelImplCopyWith<$Res>
    implements $ChatRoomModelCopyWith<$Res> {
  factory _$$ChatRoomModelImplCopyWith(
    _$ChatRoomModelImpl value,
    $Res Function(_$ChatRoomModelImpl) then,
  ) = __$$ChatRoomModelImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({
    @JsonKey(name: 'id') String roomId,
    @JsonKey(name: 'name') String title,
    List<String> participantNames,
    String? createdBy,
    String? lastMessage,
    int unreadCount,
    @JsonKey(name: 'lastMessageAt') String? updatedAt,
  });
}

/// @nodoc
class __$$ChatRoomModelImplCopyWithImpl<$Res>
    extends _$ChatRoomModelCopyWithImpl<$Res, _$ChatRoomModelImpl>
    implements _$$ChatRoomModelImplCopyWith<$Res> {
  __$$ChatRoomModelImplCopyWithImpl(
    _$ChatRoomModelImpl _value,
    $Res Function(_$ChatRoomModelImpl) _then,
  ) : super(_value, _then);

  /// Create a copy of ChatRoomModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? roomId = null,
    Object? title = null,
    Object? participantNames = null,
    Object? createdBy = freezed,
    Object? lastMessage = freezed,
    Object? unreadCount = null,
    Object? updatedAt = freezed,
  }) {
    return _then(
      _$ChatRoomModelImpl(
        roomId: null == roomId
            ? _value.roomId
            : roomId // ignore: cast_nullable_to_non_nullable
                  as String,
        title: null == title
            ? _value.title
            : title // ignore: cast_nullable_to_non_nullable
                  as String,
        participantNames: null == participantNames
            ? _value._participantNames
            : participantNames // ignore: cast_nullable_to_non_nullable
                  as List<String>,
        createdBy: freezed == createdBy
            ? _value.createdBy
            : createdBy // ignore: cast_nullable_to_non_nullable
                  as String?,
        lastMessage: freezed == lastMessage
            ? _value.lastMessage
            : lastMessage // ignore: cast_nullable_to_non_nullable
                  as String?,
        unreadCount: null == unreadCount
            ? _value.unreadCount
            : unreadCount // ignore: cast_nullable_to_non_nullable
                  as int,
        updatedAt: freezed == updatedAt
            ? _value.updatedAt
            : updatedAt // ignore: cast_nullable_to_non_nullable
                  as String?,
      ),
    );
  }
}

/// @nodoc
@JsonSerializable()
class _$ChatRoomModelImpl implements _ChatRoomModel {
  const _$ChatRoomModelImpl({
    @JsonKey(name: 'id') required this.roomId,
    @JsonKey(name: 'name') required this.title,
    final List<String> participantNames = const [],
    this.createdBy,
    this.lastMessage,
    this.unreadCount = 0,
    @JsonKey(name: 'lastMessageAt') this.updatedAt,
  }) : _participantNames = participantNames;

  factory _$ChatRoomModelImpl.fromJson(Map<String, dynamic> json) =>
      _$$ChatRoomModelImplFromJson(json);

  @override
  @JsonKey(name: 'id')
  final String roomId;
  @override
  @JsonKey(name: 'name')
  final String title;
  final List<String> _participantNames;
  @override
  @JsonKey()
  List<String> get participantNames {
    if (_participantNames is EqualUnmodifiableListView)
      return _participantNames;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_participantNames);
  }

  @override
  final String? createdBy;
  @override
  final String? lastMessage;
  @override
  @JsonKey()
  final int unreadCount;
  @override
  @JsonKey(name: 'lastMessageAt')
  final String? updatedAt;

  @override
  String toString() {
    return 'ChatRoomModel(roomId: $roomId, title: $title, participantNames: $participantNames, createdBy: $createdBy, lastMessage: $lastMessage, unreadCount: $unreadCount, updatedAt: $updatedAt)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ChatRoomModelImpl &&
            (identical(other.roomId, roomId) || other.roomId == roomId) &&
            (identical(other.title, title) || other.title == title) &&
            const DeepCollectionEquality().equals(
              other._participantNames,
              _participantNames,
            ) &&
            (identical(other.createdBy, createdBy) ||
                other.createdBy == createdBy) &&
            (identical(other.lastMessage, lastMessage) ||
                other.lastMessage == lastMessage) &&
            (identical(other.unreadCount, unreadCount) ||
                other.unreadCount == unreadCount) &&
            (identical(other.updatedAt, updatedAt) ||
                other.updatedAt == updatedAt));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
    runtimeType,
    roomId,
    title,
    const DeepCollectionEquality().hash(_participantNames),
    createdBy,
    lastMessage,
    unreadCount,
    updatedAt,
  );

  /// Create a copy of ChatRoomModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$ChatRoomModelImplCopyWith<_$ChatRoomModelImpl> get copyWith =>
      __$$ChatRoomModelImplCopyWithImpl<_$ChatRoomModelImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$ChatRoomModelImplToJson(this);
  }
}

abstract class _ChatRoomModel implements ChatRoomModel {
  const factory _ChatRoomModel({
    @JsonKey(name: 'id') required final String roomId,
    @JsonKey(name: 'name') required final String title,
    final List<String> participantNames,
    final String? createdBy,
    final String? lastMessage,
    final int unreadCount,
    @JsonKey(name: 'lastMessageAt') final String? updatedAt,
  }) = _$ChatRoomModelImpl;

  factory _ChatRoomModel.fromJson(Map<String, dynamic> json) =
      _$ChatRoomModelImpl.fromJson;

  @override
  @JsonKey(name: 'id')
  String get roomId;
  @override
  @JsonKey(name: 'name')
  String get title;
  @override
  List<String> get participantNames;
  @override
  String? get createdBy;
  @override
  String? get lastMessage;
  @override
  int get unreadCount;
  @override
  @JsonKey(name: 'lastMessageAt')
  String? get updatedAt;

  /// Create a copy of ChatRoomModel
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$ChatRoomModelImplCopyWith<_$ChatRoomModelImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
