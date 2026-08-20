// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'inbox_folder_count_model.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
  'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models',
);

/// @nodoc
mixin _$InboxFolderCountModel {
  String get folder => throw _privateConstructorUsedError;
  int get total => throw _privateConstructorUsedError;
  int get unread => throw _privateConstructorUsedError;

  /// Create a copy of InboxFolderCountModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $InboxFolderCountModelCopyWith<InboxFolderCountModel> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $InboxFolderCountModelCopyWith<$Res> {
  factory $InboxFolderCountModelCopyWith(
    InboxFolderCountModel value,
    $Res Function(InboxFolderCountModel) then,
  ) = _$InboxFolderCountModelCopyWithImpl<$Res, InboxFolderCountModel>;
  @useResult
  $Res call({String folder, int total, int unread});
}

/// @nodoc
class _$InboxFolderCountModelCopyWithImpl<
  $Res,
  $Val extends InboxFolderCountModel
>
    implements $InboxFolderCountModelCopyWith<$Res> {
  _$InboxFolderCountModelCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of InboxFolderCountModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? folder = null,
    Object? total = null,
    Object? unread = null,
  }) {
    return _then(
      _value.copyWith(
            folder: null == folder
                ? _value.folder
                : folder // ignore: cast_nullable_to_non_nullable
                      as String,
            total: null == total
                ? _value.total
                : total // ignore: cast_nullable_to_non_nullable
                      as int,
            unread: null == unread
                ? _value.unread
                : unread // ignore: cast_nullable_to_non_nullable
                      as int,
          )
          as $Val,
    );
  }
}

/// @nodoc
abstract class _$$InboxFolderCountModelImplCopyWith<$Res>
    implements $InboxFolderCountModelCopyWith<$Res> {
  factory _$$InboxFolderCountModelImplCopyWith(
    _$InboxFolderCountModelImpl value,
    $Res Function(_$InboxFolderCountModelImpl) then,
  ) = __$$InboxFolderCountModelImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String folder, int total, int unread});
}

/// @nodoc
class __$$InboxFolderCountModelImplCopyWithImpl<$Res>
    extends
        _$InboxFolderCountModelCopyWithImpl<$Res, _$InboxFolderCountModelImpl>
    implements _$$InboxFolderCountModelImplCopyWith<$Res> {
  __$$InboxFolderCountModelImplCopyWithImpl(
    _$InboxFolderCountModelImpl _value,
    $Res Function(_$InboxFolderCountModelImpl) _then,
  ) : super(_value, _then);

  /// Create a copy of InboxFolderCountModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? folder = null,
    Object? total = null,
    Object? unread = null,
  }) {
    return _then(
      _$InboxFolderCountModelImpl(
        folder: null == folder
            ? _value.folder
            : folder // ignore: cast_nullable_to_non_nullable
                  as String,
        total: null == total
            ? _value.total
            : total // ignore: cast_nullable_to_non_nullable
                  as int,
        unread: null == unread
            ? _value.unread
            : unread // ignore: cast_nullable_to_non_nullable
                  as int,
      ),
    );
  }
}

/// @nodoc

class _$InboxFolderCountModelImpl implements _InboxFolderCountModel {
  const _$InboxFolderCountModelImpl({
    required this.folder,
    this.total = 0,
    this.unread = 0,
  });

  @override
  final String folder;
  @override
  @JsonKey()
  final int total;
  @override
  @JsonKey()
  final int unread;

  @override
  String toString() {
    return 'InboxFolderCountModel(folder: $folder, total: $total, unread: $unread)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$InboxFolderCountModelImpl &&
            (identical(other.folder, folder) || other.folder == folder) &&
            (identical(other.total, total) || other.total == total) &&
            (identical(other.unread, unread) || other.unread == unread));
  }

  @override
  int get hashCode => Object.hash(runtimeType, folder, total, unread);

  /// Create a copy of InboxFolderCountModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$InboxFolderCountModelImplCopyWith<_$InboxFolderCountModelImpl>
  get copyWith =>
      __$$InboxFolderCountModelImplCopyWithImpl<_$InboxFolderCountModelImpl>(
        this,
        _$identity,
      );
}

abstract class _InboxFolderCountModel implements InboxFolderCountModel {
  const factory _InboxFolderCountModel({
    required final String folder,
    final int total,
    final int unread,
  }) = _$InboxFolderCountModelImpl;

  @override
  String get folder;
  @override
  int get total;
  @override
  int get unread;

  /// Create a copy of InboxFolderCountModel
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$InboxFolderCountModelImplCopyWith<_$InboxFolderCountModelImpl>
  get copyWith => throw _privateConstructorUsedError;
}
