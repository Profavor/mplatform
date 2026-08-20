// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'inbox_attachment_model.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
  'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models',
);

/// @nodoc
mixin _$InboxAttachmentModel {
  String get id => throw _privateConstructorUsedError;
  String get fileName => throw _privateConstructorUsedError;
  int get fileSize => throw _privateConstructorUsedError;
  String? get contentType => throw _privateConstructorUsedError;

  /// Create a copy of InboxAttachmentModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $InboxAttachmentModelCopyWith<InboxAttachmentModel> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $InboxAttachmentModelCopyWith<$Res> {
  factory $InboxAttachmentModelCopyWith(
    InboxAttachmentModel value,
    $Res Function(InboxAttachmentModel) then,
  ) = _$InboxAttachmentModelCopyWithImpl<$Res, InboxAttachmentModel>;
  @useResult
  $Res call({String id, String fileName, int fileSize, String? contentType});
}

/// @nodoc
class _$InboxAttachmentModelCopyWithImpl<
  $Res,
  $Val extends InboxAttachmentModel
>
    implements $InboxAttachmentModelCopyWith<$Res> {
  _$InboxAttachmentModelCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of InboxAttachmentModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? fileName = null,
    Object? fileSize = null,
    Object? contentType = freezed,
  }) {
    return _then(
      _value.copyWith(
            id: null == id
                ? _value.id
                : id // ignore: cast_nullable_to_non_nullable
                      as String,
            fileName: null == fileName
                ? _value.fileName
                : fileName // ignore: cast_nullable_to_non_nullable
                      as String,
            fileSize: null == fileSize
                ? _value.fileSize
                : fileSize // ignore: cast_nullable_to_non_nullable
                      as int,
            contentType: freezed == contentType
                ? _value.contentType
                : contentType // ignore: cast_nullable_to_non_nullable
                      as String?,
          )
          as $Val,
    );
  }
}

/// @nodoc
abstract class _$$InboxAttachmentModelImplCopyWith<$Res>
    implements $InboxAttachmentModelCopyWith<$Res> {
  factory _$$InboxAttachmentModelImplCopyWith(
    _$InboxAttachmentModelImpl value,
    $Res Function(_$InboxAttachmentModelImpl) then,
  ) = __$$InboxAttachmentModelImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String id, String fileName, int fileSize, String? contentType});
}

/// @nodoc
class __$$InboxAttachmentModelImplCopyWithImpl<$Res>
    extends _$InboxAttachmentModelCopyWithImpl<$Res, _$InboxAttachmentModelImpl>
    implements _$$InboxAttachmentModelImplCopyWith<$Res> {
  __$$InboxAttachmentModelImplCopyWithImpl(
    _$InboxAttachmentModelImpl _value,
    $Res Function(_$InboxAttachmentModelImpl) _then,
  ) : super(_value, _then);

  /// Create a copy of InboxAttachmentModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? fileName = null,
    Object? fileSize = null,
    Object? contentType = freezed,
  }) {
    return _then(
      _$InboxAttachmentModelImpl(
        id: null == id
            ? _value.id
            : id // ignore: cast_nullable_to_non_nullable
                  as String,
        fileName: null == fileName
            ? _value.fileName
            : fileName // ignore: cast_nullable_to_non_nullable
                  as String,
        fileSize: null == fileSize
            ? _value.fileSize
            : fileSize // ignore: cast_nullable_to_non_nullable
                  as int,
        contentType: freezed == contentType
            ? _value.contentType
            : contentType // ignore: cast_nullable_to_non_nullable
                  as String?,
      ),
    );
  }
}

/// @nodoc

class _$InboxAttachmentModelImpl implements _InboxAttachmentModel {
  const _$InboxAttachmentModelImpl({
    required this.id,
    required this.fileName,
    required this.fileSize,
    this.contentType,
  });

  @override
  final String id;
  @override
  final String fileName;
  @override
  final int fileSize;
  @override
  final String? contentType;

  @override
  String toString() {
    return 'InboxAttachmentModel(id: $id, fileName: $fileName, fileSize: $fileSize, contentType: $contentType)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$InboxAttachmentModelImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.fileName, fileName) ||
                other.fileName == fileName) &&
            (identical(other.fileSize, fileSize) ||
                other.fileSize == fileSize) &&
            (identical(other.contentType, contentType) ||
                other.contentType == contentType));
  }

  @override
  int get hashCode =>
      Object.hash(runtimeType, id, fileName, fileSize, contentType);

  /// Create a copy of InboxAttachmentModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$InboxAttachmentModelImplCopyWith<_$InboxAttachmentModelImpl>
  get copyWith =>
      __$$InboxAttachmentModelImplCopyWithImpl<_$InboxAttachmentModelImpl>(
        this,
        _$identity,
      );
}

abstract class _InboxAttachmentModel implements InboxAttachmentModel {
  const factory _InboxAttachmentModel({
    required final String id,
    required final String fileName,
    required final int fileSize,
    final String? contentType,
  }) = _$InboxAttachmentModelImpl;

  @override
  String get id;
  @override
  String get fileName;
  @override
  int get fileSize;
  @override
  String? get contentType;

  /// Create a copy of InboxAttachmentModel
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$InboxAttachmentModelImplCopyWith<_$InboxAttachmentModelImpl>
  get copyWith => throw _privateConstructorUsedError;
}
