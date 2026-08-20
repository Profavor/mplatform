// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'inbox_state.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
  'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models',
);

/// @nodoc
mixin _$InboxState {
  String get currentFolder => throw _privateConstructorUsedError;
  List<InboxMessageModel> get messages => throw _privateConstructorUsedError;
  List<InboxFolderCountModel> get folderCounts =>
      throw _privateConstructorUsedError;
  int get unreadTotal => throw _privateConstructorUsedError;
  bool get isLoading => throw _privateConstructorUsedError;
  bool get hasMore => throw _privateConstructorUsedError;
  int get page => throw _privateConstructorUsedError;
  String? get keyword => throw _privateConstructorUsedError;
  String? get errorMessage => throw _privateConstructorUsedError;

  /// Create a copy of InboxState
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $InboxStateCopyWith<InboxState> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $InboxStateCopyWith<$Res> {
  factory $InboxStateCopyWith(
    InboxState value,
    $Res Function(InboxState) then,
  ) = _$InboxStateCopyWithImpl<$Res, InboxState>;
  @useResult
  $Res call({
    String currentFolder,
    List<InboxMessageModel> messages,
    List<InboxFolderCountModel> folderCounts,
    int unreadTotal,
    bool isLoading,
    bool hasMore,
    int page,
    String? keyword,
    String? errorMessage,
  });
}

/// @nodoc
class _$InboxStateCopyWithImpl<$Res, $Val extends InboxState>
    implements $InboxStateCopyWith<$Res> {
  _$InboxStateCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of InboxState
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? currentFolder = null,
    Object? messages = null,
    Object? folderCounts = null,
    Object? unreadTotal = null,
    Object? isLoading = null,
    Object? hasMore = null,
    Object? page = null,
    Object? keyword = freezed,
    Object? errorMessage = freezed,
  }) {
    return _then(
      _value.copyWith(
            currentFolder: null == currentFolder
                ? _value.currentFolder
                : currentFolder // ignore: cast_nullable_to_non_nullable
                      as String,
            messages: null == messages
                ? _value.messages
                : messages // ignore: cast_nullable_to_non_nullable
                      as List<InboxMessageModel>,
            folderCounts: null == folderCounts
                ? _value.folderCounts
                : folderCounts // ignore: cast_nullable_to_non_nullable
                      as List<InboxFolderCountModel>,
            unreadTotal: null == unreadTotal
                ? _value.unreadTotal
                : unreadTotal // ignore: cast_nullable_to_non_nullable
                      as int,
            isLoading: null == isLoading
                ? _value.isLoading
                : isLoading // ignore: cast_nullable_to_non_nullable
                      as bool,
            hasMore: null == hasMore
                ? _value.hasMore
                : hasMore // ignore: cast_nullable_to_non_nullable
                      as bool,
            page: null == page
                ? _value.page
                : page // ignore: cast_nullable_to_non_nullable
                      as int,
            keyword: freezed == keyword
                ? _value.keyword
                : keyword // ignore: cast_nullable_to_non_nullable
                      as String?,
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
abstract class _$$InboxStateImplCopyWith<$Res>
    implements $InboxStateCopyWith<$Res> {
  factory _$$InboxStateImplCopyWith(
    _$InboxStateImpl value,
    $Res Function(_$InboxStateImpl) then,
  ) = __$$InboxStateImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({
    String currentFolder,
    List<InboxMessageModel> messages,
    List<InboxFolderCountModel> folderCounts,
    int unreadTotal,
    bool isLoading,
    bool hasMore,
    int page,
    String? keyword,
    String? errorMessage,
  });
}

/// @nodoc
class __$$InboxStateImplCopyWithImpl<$Res>
    extends _$InboxStateCopyWithImpl<$Res, _$InboxStateImpl>
    implements _$$InboxStateImplCopyWith<$Res> {
  __$$InboxStateImplCopyWithImpl(
    _$InboxStateImpl _value,
    $Res Function(_$InboxStateImpl) _then,
  ) : super(_value, _then);

  /// Create a copy of InboxState
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? currentFolder = null,
    Object? messages = null,
    Object? folderCounts = null,
    Object? unreadTotal = null,
    Object? isLoading = null,
    Object? hasMore = null,
    Object? page = null,
    Object? keyword = freezed,
    Object? errorMessage = freezed,
  }) {
    return _then(
      _$InboxStateImpl(
        currentFolder: null == currentFolder
            ? _value.currentFolder
            : currentFolder // ignore: cast_nullable_to_non_nullable
                  as String,
        messages: null == messages
            ? _value._messages
            : messages // ignore: cast_nullable_to_non_nullable
                  as List<InboxMessageModel>,
        folderCounts: null == folderCounts
            ? _value._folderCounts
            : folderCounts // ignore: cast_nullable_to_non_nullable
                  as List<InboxFolderCountModel>,
        unreadTotal: null == unreadTotal
            ? _value.unreadTotal
            : unreadTotal // ignore: cast_nullable_to_non_nullable
                  as int,
        isLoading: null == isLoading
            ? _value.isLoading
            : isLoading // ignore: cast_nullable_to_non_nullable
                  as bool,
        hasMore: null == hasMore
            ? _value.hasMore
            : hasMore // ignore: cast_nullable_to_non_nullable
                  as bool,
        page: null == page
            ? _value.page
            : page // ignore: cast_nullable_to_non_nullable
                  as int,
        keyword: freezed == keyword
            ? _value.keyword
            : keyword // ignore: cast_nullable_to_non_nullable
                  as String?,
        errorMessage: freezed == errorMessage
            ? _value.errorMessage
            : errorMessage // ignore: cast_nullable_to_non_nullable
                  as String?,
      ),
    );
  }
}

/// @nodoc

class _$InboxStateImpl implements _InboxState {
  const _$InboxStateImpl({
    this.currentFolder = 'INBOX',
    final List<InboxMessageModel> messages = const [],
    final List<InboxFolderCountModel> folderCounts = const [],
    this.unreadTotal = 0,
    this.isLoading = false,
    this.hasMore = true,
    this.page = 0,
    this.keyword,
    this.errorMessage,
  }) : _messages = messages,
       _folderCounts = folderCounts;

  @override
  @JsonKey()
  final String currentFolder;
  final List<InboxMessageModel> _messages;
  @override
  @JsonKey()
  List<InboxMessageModel> get messages {
    if (_messages is EqualUnmodifiableListView) return _messages;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_messages);
  }

  final List<InboxFolderCountModel> _folderCounts;
  @override
  @JsonKey()
  List<InboxFolderCountModel> get folderCounts {
    if (_folderCounts is EqualUnmodifiableListView) return _folderCounts;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_folderCounts);
  }

  @override
  @JsonKey()
  final int unreadTotal;
  @override
  @JsonKey()
  final bool isLoading;
  @override
  @JsonKey()
  final bool hasMore;
  @override
  @JsonKey()
  final int page;
  @override
  final String? keyword;
  @override
  final String? errorMessage;

  @override
  String toString() {
    return 'InboxState(currentFolder: $currentFolder, messages: $messages, folderCounts: $folderCounts, unreadTotal: $unreadTotal, isLoading: $isLoading, hasMore: $hasMore, page: $page, keyword: $keyword, errorMessage: $errorMessage)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$InboxStateImpl &&
            (identical(other.currentFolder, currentFolder) ||
                other.currentFolder == currentFolder) &&
            const DeepCollectionEquality().equals(other._messages, _messages) &&
            const DeepCollectionEquality().equals(
              other._folderCounts,
              _folderCounts,
            ) &&
            (identical(other.unreadTotal, unreadTotal) ||
                other.unreadTotal == unreadTotal) &&
            (identical(other.isLoading, isLoading) ||
                other.isLoading == isLoading) &&
            (identical(other.hasMore, hasMore) || other.hasMore == hasMore) &&
            (identical(other.page, page) || other.page == page) &&
            (identical(other.keyword, keyword) || other.keyword == keyword) &&
            (identical(other.errorMessage, errorMessage) ||
                other.errorMessage == errorMessage));
  }

  @override
  int get hashCode => Object.hash(
    runtimeType,
    currentFolder,
    const DeepCollectionEquality().hash(_messages),
    const DeepCollectionEquality().hash(_folderCounts),
    unreadTotal,
    isLoading,
    hasMore,
    page,
    keyword,
    errorMessage,
  );

  /// Create a copy of InboxState
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$InboxStateImplCopyWith<_$InboxStateImpl> get copyWith =>
      __$$InboxStateImplCopyWithImpl<_$InboxStateImpl>(this, _$identity);
}

abstract class _InboxState implements InboxState {
  const factory _InboxState({
    final String currentFolder,
    final List<InboxMessageModel> messages,
    final List<InboxFolderCountModel> folderCounts,
    final int unreadTotal,
    final bool isLoading,
    final bool hasMore,
    final int page,
    final String? keyword,
    final String? errorMessage,
  }) = _$InboxStateImpl;

  @override
  String get currentFolder;
  @override
  List<InboxMessageModel> get messages;
  @override
  List<InboxFolderCountModel> get folderCounts;
  @override
  int get unreadTotal;
  @override
  bool get isLoading;
  @override
  bool get hasMore;
  @override
  int get page;
  @override
  String? get keyword;
  @override
  String? get errorMessage;

  /// Create a copy of InboxState
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$InboxStateImplCopyWith<_$InboxStateImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
