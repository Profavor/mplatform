// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'inbox_recipient_model.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
  'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models',
);

/// @nodoc
mixin _$InboxRecipientModel {
  String get userId => throw _privateConstructorUsedError;
  String get name => throw _privateConstructorUsedError;
  String? get email => throw _privateConstructorUsedError;
  String get recipientType =>
      throw _privateConstructorUsedError; // "TO", "CC", "BCC"
  bool get isRead => throw _privateConstructorUsedError;
  String? get readAt => throw _privateConstructorUsedError;
  bool get isRecalled => throw _privateConstructorUsedError;
  String? get recalledAt => throw _privateConstructorUsedError;

  /// Create a copy of InboxRecipientModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $InboxRecipientModelCopyWith<InboxRecipientModel> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $InboxRecipientModelCopyWith<$Res> {
  factory $InboxRecipientModelCopyWith(
    InboxRecipientModel value,
    $Res Function(InboxRecipientModel) then,
  ) = _$InboxRecipientModelCopyWithImpl<$Res, InboxRecipientModel>;
  @useResult
  $Res call({
    String userId,
    String name,
    String? email,
    String recipientType,
    bool isRead,
    String? readAt,
    bool isRecalled,
    String? recalledAt,
  });
}

/// @nodoc
class _$InboxRecipientModelCopyWithImpl<$Res, $Val extends InboxRecipientModel>
    implements $InboxRecipientModelCopyWith<$Res> {
  _$InboxRecipientModelCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of InboxRecipientModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? userId = null,
    Object? name = null,
    Object? email = freezed,
    Object? recipientType = null,
    Object? isRead = null,
    Object? readAt = freezed,
    Object? isRecalled = null,
    Object? recalledAt = freezed,
  }) {
    return _then(
      _value.copyWith(
            userId: null == userId
                ? _value.userId
                : userId // ignore: cast_nullable_to_non_nullable
                      as String,
            name: null == name
                ? _value.name
                : name // ignore: cast_nullable_to_non_nullable
                      as String,
            email: freezed == email
                ? _value.email
                : email // ignore: cast_nullable_to_non_nullable
                      as String?,
            recipientType: null == recipientType
                ? _value.recipientType
                : recipientType // ignore: cast_nullable_to_non_nullable
                      as String,
            isRead: null == isRead
                ? _value.isRead
                : isRead // ignore: cast_nullable_to_non_nullable
                      as bool,
            readAt: freezed == readAt
                ? _value.readAt
                : readAt // ignore: cast_nullable_to_non_nullable
                      as String?,
            isRecalled: null == isRecalled
                ? _value.isRecalled
                : isRecalled // ignore: cast_nullable_to_non_nullable
                      as bool,
            recalledAt: freezed == recalledAt
                ? _value.recalledAt
                : recalledAt // ignore: cast_nullable_to_non_nullable
                      as String?,
          )
          as $Val,
    );
  }
}

/// @nodoc
abstract class _$$InboxRecipientModelImplCopyWith<$Res>
    implements $InboxRecipientModelCopyWith<$Res> {
  factory _$$InboxRecipientModelImplCopyWith(
    _$InboxRecipientModelImpl value,
    $Res Function(_$InboxRecipientModelImpl) then,
  ) = __$$InboxRecipientModelImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({
    String userId,
    String name,
    String? email,
    String recipientType,
    bool isRead,
    String? readAt,
    bool isRecalled,
    String? recalledAt,
  });
}

/// @nodoc
class __$$InboxRecipientModelImplCopyWithImpl<$Res>
    extends _$InboxRecipientModelCopyWithImpl<$Res, _$InboxRecipientModelImpl>
    implements _$$InboxRecipientModelImplCopyWith<$Res> {
  __$$InboxRecipientModelImplCopyWithImpl(
    _$InboxRecipientModelImpl _value,
    $Res Function(_$InboxRecipientModelImpl) _then,
  ) : super(_value, _then);

  /// Create a copy of InboxRecipientModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? userId = null,
    Object? name = null,
    Object? email = freezed,
    Object? recipientType = null,
    Object? isRead = null,
    Object? readAt = freezed,
    Object? isRecalled = null,
    Object? recalledAt = freezed,
  }) {
    return _then(
      _$InboxRecipientModelImpl(
        userId: null == userId
            ? _value.userId
            : userId // ignore: cast_nullable_to_non_nullable
                  as String,
        name: null == name
            ? _value.name
            : name // ignore: cast_nullable_to_non_nullable
                  as String,
        email: freezed == email
            ? _value.email
            : email // ignore: cast_nullable_to_non_nullable
                  as String?,
        recipientType: null == recipientType
            ? _value.recipientType
            : recipientType // ignore: cast_nullable_to_non_nullable
                  as String,
        isRead: null == isRead
            ? _value.isRead
            : isRead // ignore: cast_nullable_to_non_nullable
                  as bool,
        readAt: freezed == readAt
            ? _value.readAt
            : readAt // ignore: cast_nullable_to_non_nullable
                  as String?,
        isRecalled: null == isRecalled
            ? _value.isRecalled
            : isRecalled // ignore: cast_nullable_to_non_nullable
                  as bool,
        recalledAt: freezed == recalledAt
            ? _value.recalledAt
            : recalledAt // ignore: cast_nullable_to_non_nullable
                  as String?,
      ),
    );
  }
}

/// @nodoc

class _$InboxRecipientModelImpl implements _InboxRecipientModel {
  const _$InboxRecipientModelImpl({
    required this.userId,
    required this.name,
    this.email,
    required this.recipientType,
    this.isRead = false,
    this.readAt,
    this.isRecalled = false,
    this.recalledAt,
  });

  @override
  final String userId;
  @override
  final String name;
  @override
  final String? email;
  @override
  final String recipientType;
  // "TO", "CC", "BCC"
  @override
  @JsonKey()
  final bool isRead;
  @override
  final String? readAt;
  @override
  @JsonKey()
  final bool isRecalled;
  @override
  final String? recalledAt;

  @override
  String toString() {
    return 'InboxRecipientModel(userId: $userId, name: $name, email: $email, recipientType: $recipientType, isRead: $isRead, readAt: $readAt, isRecalled: $isRecalled, recalledAt: $recalledAt)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$InboxRecipientModelImpl &&
            (identical(other.userId, userId) || other.userId == userId) &&
            (identical(other.name, name) || other.name == name) &&
            (identical(other.email, email) || other.email == email) &&
            (identical(other.recipientType, recipientType) ||
                other.recipientType == recipientType) &&
            (identical(other.isRead, isRead) || other.isRead == isRead) &&
            (identical(other.readAt, readAt) || other.readAt == readAt) &&
            (identical(other.isRecalled, isRecalled) ||
                other.isRecalled == isRecalled) &&
            (identical(other.recalledAt, recalledAt) ||
                other.recalledAt == recalledAt));
  }

  @override
  int get hashCode => Object.hash(
    runtimeType,
    userId,
    name,
    email,
    recipientType,
    isRead,
    readAt,
    isRecalled,
    recalledAt,
  );

  /// Create a copy of InboxRecipientModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$InboxRecipientModelImplCopyWith<_$InboxRecipientModelImpl> get copyWith =>
      __$$InboxRecipientModelImplCopyWithImpl<_$InboxRecipientModelImpl>(
        this,
        _$identity,
      );
}

abstract class _InboxRecipientModel implements InboxRecipientModel {
  const factory _InboxRecipientModel({
    required final String userId,
    required final String name,
    final String? email,
    required final String recipientType,
    final bool isRead,
    final String? readAt,
    final bool isRecalled,
    final String? recalledAt,
  }) = _$InboxRecipientModelImpl;

  @override
  String get userId;
  @override
  String get name;
  @override
  String? get email;
  @override
  String get recipientType; // "TO", "CC", "BCC"
  @override
  bool get isRead;
  @override
  String? get readAt;
  @override
  bool get isRecalled;
  @override
  String? get recalledAt;

  /// Create a copy of InboxRecipientModel
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$InboxRecipientModelImplCopyWith<_$InboxRecipientModelImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
