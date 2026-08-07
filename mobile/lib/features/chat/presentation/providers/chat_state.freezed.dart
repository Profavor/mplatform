// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'chat_state.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
  'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models',
);

/// @nodoc
mixin _$ChatState {
  List<ChatRoomModel> get rooms => throw _privateConstructorUsedError;
  List<ChatMessageModel> get activeMessages =>
      throw _privateConstructorUsedError;
  String? get selectedRoomId => throw _privateConstructorUsedError;
  bool get isLoadingRooms => throw _privateConstructorUsedError;
  bool get isLoadingMessages => throw _privateConstructorUsedError;
  bool get isSending => throw _privateConstructorUsedError;
  Set<String> get onlineUserIds => throw _privateConstructorUsedError;
  int get totalUnreadCount => throw _privateConstructorUsedError;
  String? get errorMessage => throw _privateConstructorUsedError;

  /// Create a copy of ChatState
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $ChatStateCopyWith<ChatState> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ChatStateCopyWith<$Res> {
  factory $ChatStateCopyWith(ChatState value, $Res Function(ChatState) then) =
      _$ChatStateCopyWithImpl<$Res, ChatState>;
  @useResult
  $Res call({
    List<ChatRoomModel> rooms,
    List<ChatMessageModel> activeMessages,
    String? selectedRoomId,
    bool isLoadingRooms,
    bool isLoadingMessages,
    bool isSending,
    Set<String> onlineUserIds,
    int totalUnreadCount,
    String? errorMessage,
  });
}

/// @nodoc
class _$ChatStateCopyWithImpl<$Res, $Val extends ChatState>
    implements $ChatStateCopyWith<$Res> {
  _$ChatStateCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of ChatState
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? rooms = null,
    Object? activeMessages = null,
    Object? selectedRoomId = freezed,
    Object? isLoadingRooms = null,
    Object? isLoadingMessages = null,
    Object? isSending = null,
    Object? onlineUserIds = null,
    Object? totalUnreadCount = null,
    Object? errorMessage = freezed,
  }) {
    return _then(
      _value.copyWith(
            rooms: null == rooms
                ? _value.rooms
                : rooms // ignore: cast_nullable_to_non_nullable
                      as List<ChatRoomModel>,
            activeMessages: null == activeMessages
                ? _value.activeMessages
                : activeMessages // ignore: cast_nullable_to_non_nullable
                      as List<ChatMessageModel>,
            selectedRoomId: freezed == selectedRoomId
                ? _value.selectedRoomId
                : selectedRoomId // ignore: cast_nullable_to_non_nullable
                      as String?,
            isLoadingRooms: null == isLoadingRooms
                ? _value.isLoadingRooms
                : isLoadingRooms // ignore: cast_nullable_to_non_nullable
                      as bool,
            isLoadingMessages: null == isLoadingMessages
                ? _value.isLoadingMessages
                : isLoadingMessages // ignore: cast_nullable_to_non_nullable
                      as bool,
            isSending: null == isSending
                ? _value.isSending
                : isSending // ignore: cast_nullable_to_non_nullable
                      as bool,
            onlineUserIds: null == onlineUserIds
                ? _value.onlineUserIds
                : onlineUserIds // ignore: cast_nullable_to_non_nullable
                      as Set<String>,
            totalUnreadCount: null == totalUnreadCount
                ? _value.totalUnreadCount
                : totalUnreadCount // ignore: cast_nullable_to_non_nullable
                      as int,
            errorMessage: freezed == errorMessage
                ? _value.errorMessage
                : errorMessage // ignore: cast_nullable_to_non_nullable
                      as String?,
          )
          as $Val,
    );
  }
}

/// @nodoc
abstract class _$$ChatStateImplCopyWith<$Res>
    implements $ChatStateCopyWith<$Res> {
  factory _$$ChatStateImplCopyWith(
    _$ChatStateImpl value,
    $Res Function(_$ChatStateImpl) then,
  ) = __$$ChatStateImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({
    List<ChatRoomModel> rooms,
    List<ChatMessageModel> activeMessages,
    String? selectedRoomId,
    bool isLoadingRooms,
    bool isLoadingMessages,
    bool isSending,
    Set<String> onlineUserIds,
    int totalUnreadCount,
    String? errorMessage,
  });
}

/// @nodoc
class __$$ChatStateImplCopyWithImpl<$Res>
    extends _$ChatStateCopyWithImpl<$Res, _$ChatStateImpl>
    implements _$$ChatStateImplCopyWith<$Res> {
  __$$ChatStateImplCopyWithImpl(
    _$ChatStateImpl _value,
    $Res Function(_$ChatStateImpl) _then,
  ) : super(_value, _then);

  /// Create a copy of ChatState
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? rooms = null,
    Object? activeMessages = null,
    Object? selectedRoomId = freezed,
    Object? isLoadingRooms = null,
    Object? isLoadingMessages = null,
    Object? isSending = null,
    Object? onlineUserIds = null,
    Object? totalUnreadCount = null,
    Object? errorMessage = freezed,
  }) {
    return _then(
      _$ChatStateImpl(
        rooms: null == rooms
            ? _value._rooms
            : rooms // ignore: cast_nullable_to_non_nullable
                  as List<ChatRoomModel>,
        activeMessages: null == activeMessages
            ? _value._activeMessages
            : activeMessages // ignore: cast_nullable_to_non_nullable
                  as List<ChatMessageModel>,
        selectedRoomId: freezed == selectedRoomId
            ? _value.selectedRoomId
            : selectedRoomId // ignore: cast_nullable_to_non_nullable
                  as String?,
        isLoadingRooms: null == isLoadingRooms
            ? _value.isLoadingRooms
            : isLoadingRooms // ignore: cast_nullable_to_non_nullable
                  as bool,
        isLoadingMessages: null == isLoadingMessages
            ? _value.isLoadingMessages
            : isLoadingMessages // ignore: cast_nullable_to_non_nullable
                  as bool,
        isSending: null == isSending
            ? _value.isSending
            : isSending // ignore: cast_nullable_to_non_nullable
                  as bool,
        onlineUserIds: null == onlineUserIds
            ? _value._onlineUserIds
            : onlineUserIds // ignore: cast_nullable_to_non_nullable
                  as Set<String>,
        totalUnreadCount: null == totalUnreadCount
            ? _value.totalUnreadCount
            : totalUnreadCount // ignore: cast_nullable_to_non_nullable
                  as int,
        errorMessage: freezed == errorMessage
            ? _value.errorMessage
            : errorMessage // ignore: cast_nullable_to_non_nullable
                  as String?,
      ),
    );
  }
}

/// @nodoc

class _$ChatStateImpl implements _ChatState {
  const _$ChatStateImpl({
    final List<ChatRoomModel> rooms = const [],
    final List<ChatMessageModel> activeMessages = const [],
    this.selectedRoomId,
    this.isLoadingRooms = false,
    this.isLoadingMessages = false,
    this.isSending = false,
    final Set<String> onlineUserIds = const {},
    this.totalUnreadCount = 0,
    this.errorMessage,
  }) : _rooms = rooms,
       _activeMessages = activeMessages,
       _onlineUserIds = onlineUserIds;

  final List<ChatRoomModel> _rooms;
  @override
  @JsonKey()
  List<ChatRoomModel> get rooms {
    if (_rooms is EqualUnmodifiableListView) return _rooms;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_rooms);
  }

  final List<ChatMessageModel> _activeMessages;
  @override
  @JsonKey()
  List<ChatMessageModel> get activeMessages {
    if (_activeMessages is EqualUnmodifiableListView) return _activeMessages;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_activeMessages);
  }

  @override
  final String? selectedRoomId;
  @override
  @JsonKey()
  final bool isLoadingRooms;
  @override
  @JsonKey()
  final bool isLoadingMessages;
  @override
  @JsonKey()
  final bool isSending;
  final Set<String> _onlineUserIds;
  @override
  @JsonKey()
  Set<String> get onlineUserIds {
    if (_onlineUserIds is EqualUnmodifiableSetView) return _onlineUserIds;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableSetView(_onlineUserIds);
  }

  @override
  @JsonKey()
  final int totalUnreadCount;
  @override
  final String? errorMessage;

  @override
  String toString() {
    return 'ChatState(rooms: $rooms, activeMessages: $activeMessages, selectedRoomId: $selectedRoomId, isLoadingRooms: $isLoadingRooms, isLoadingMessages: $isLoadingMessages, isSending: $isSending, onlineUserIds: $onlineUserIds, totalUnreadCount: $totalUnreadCount, errorMessage: $errorMessage)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ChatStateImpl &&
            const DeepCollectionEquality().equals(other._rooms, _rooms) &&
            const DeepCollectionEquality().equals(
              other._activeMessages,
              _activeMessages,
            ) &&
            (identical(other.selectedRoomId, selectedRoomId) ||
                other.selectedRoomId == selectedRoomId) &&
            (identical(other.isLoadingRooms, isLoadingRooms) ||
                other.isLoadingRooms == isLoadingRooms) &&
            (identical(other.isLoadingMessages, isLoadingMessages) ||
                other.isLoadingMessages == isLoadingMessages) &&
            (identical(other.isSending, isSending) ||
                other.isSending == isSending) &&
            const DeepCollectionEquality().equals(
              other._onlineUserIds,
              _onlineUserIds,
            ) &&
            (identical(other.totalUnreadCount, totalUnreadCount) ||
                other.totalUnreadCount == totalUnreadCount) &&
            (identical(other.errorMessage, errorMessage) ||
                other.errorMessage == errorMessage));
  }

  @override
  int get hashCode => Object.hash(
    runtimeType,
    const DeepCollectionEquality().hash(_rooms),
    const DeepCollectionEquality().hash(_activeMessages),
    selectedRoomId,
    isLoadingRooms,
    isLoadingMessages,
    isSending,
    const DeepCollectionEquality().hash(_onlineUserIds),
    totalUnreadCount,
    errorMessage,
  );

  /// Create a copy of ChatState
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$ChatStateImplCopyWith<_$ChatStateImpl> get copyWith =>
      __$$ChatStateImplCopyWithImpl<_$ChatStateImpl>(this, _$identity);
}

abstract class _ChatState implements ChatState {
  const factory _ChatState({
    final List<ChatRoomModel> rooms,
    final List<ChatMessageModel> activeMessages,
    final String? selectedRoomId,
    final bool isLoadingRooms,
    final bool isLoadingMessages,
    final bool isSending,
    final Set<String> onlineUserIds,
    final int totalUnreadCount,
    final String? errorMessage,
  }) = _$ChatStateImpl;

  @override
  List<ChatRoomModel> get rooms;
  @override
  List<ChatMessageModel> get activeMessages;
  @override
  String? get selectedRoomId;
  @override
  bool get isLoadingRooms;
  @override
  bool get isLoadingMessages;
  @override
  bool get isSending;
  @override
  Set<String> get onlineUserIds;
  @override
  int get totalUnreadCount;
  @override
  String? get errorMessage;

  /// Create a copy of ChatState
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$ChatStateImplCopyWith<_$ChatStateImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
