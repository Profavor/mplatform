// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'chat_attachment.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
  'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models',
);

ChatAttachment _$ChatAttachmentFromJson(Map<String, dynamic> json) {
  return _ChatAttachment.fromJson(json);
}

/// @nodoc
mixin _$ChatAttachment {
  String get fileName => throw _privateConstructorUsedError;
  String get fileUrl => throw _privateConstructorUsedError;
  String get fileType => throw _privateConstructorUsedError;
  int get fileSize => throw _privateConstructorUsedError;

  /// Serializes this ChatAttachment to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of ChatAttachment
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $ChatAttachmentCopyWith<ChatAttachment> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ChatAttachmentCopyWith<$Res> {
  factory $ChatAttachmentCopyWith(
    ChatAttachment value,
    $Res Function(ChatAttachment) then,
  ) = _$ChatAttachmentCopyWithImpl<$Res, ChatAttachment>;
  @useResult
  $Res call({String fileName, String fileUrl, String fileType, int fileSize});
}

/// @nodoc
class _$ChatAttachmentCopyWithImpl<$Res, $Val extends ChatAttachment>
    implements $ChatAttachmentCopyWith<$Res> {
  _$ChatAttachmentCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of ChatAttachment
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? fileName = null,
    Object? fileUrl = null,
    Object? fileType = null,
    Object? fileSize = null,
  }) {
    return _then(
      _value.copyWith(
            fileName: null == fileName
                ? _value.fileName
                : fileName // ignore: cast_nullable_to_non_nullable
                      as String,
            fileUrl: null == fileUrl
                ? _value.fileUrl
                : fileUrl // ignore: cast_nullable_to_non_nullable
                      as String,
            fileType: null == fileType
                ? _value.fileType
                : fileType // ignore: cast_nullable_to_non_nullable
                      as String,
            fileSize: null == fileSize
                ? _value.fileSize
                : fileSize // ignore: cast_nullable_to_non_nullable
                      as int,
          )
          as $Val,
    );
  }
}

/// @nodoc
abstract class _$$ChatAttachmentImplCopyWith<$Res>
    implements $ChatAttachmentCopyWith<$Res> {
  factory _$$ChatAttachmentImplCopyWith(
    _$ChatAttachmentImpl value,
    $Res Function(_$ChatAttachmentImpl) then,
  ) = __$$ChatAttachmentImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String fileName, String fileUrl, String fileType, int fileSize});
}

/// @nodoc
class __$$ChatAttachmentImplCopyWithImpl<$Res>
    extends _$ChatAttachmentCopyWithImpl<$Res, _$ChatAttachmentImpl>
    implements _$$ChatAttachmentImplCopyWith<$Res> {
  __$$ChatAttachmentImplCopyWithImpl(
    _$ChatAttachmentImpl _value,
    $Res Function(_$ChatAttachmentImpl) _then,
  ) : super(_value, _then);

  /// Create a copy of ChatAttachment
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? fileName = null,
    Object? fileUrl = null,
    Object? fileType = null,
    Object? fileSize = null,
  }) {
    return _then(
      _$ChatAttachmentImpl(
        fileName: null == fileName
            ? _value.fileName
            : fileName // ignore: cast_nullable_to_non_nullable
                  as String,
        fileUrl: null == fileUrl
            ? _value.fileUrl
            : fileUrl // ignore: cast_nullable_to_non_nullable
                  as String,
        fileType: null == fileType
            ? _value.fileType
            : fileType // ignore: cast_nullable_to_non_nullable
                  as String,
        fileSize: null == fileSize
            ? _value.fileSize
            : fileSize // ignore: cast_nullable_to_non_nullable
                  as int,
      ),
    );
  }
}

/// @nodoc
@JsonSerializable()
class _$ChatAttachmentImpl implements _ChatAttachment {
  const _$ChatAttachmentImpl({
    required this.fileName,
    required this.fileUrl,
    required this.fileType,
    this.fileSize = 0,
  });

  factory _$ChatAttachmentImpl.fromJson(Map<String, dynamic> json) =>
      _$$ChatAttachmentImplFromJson(json);

  @override
  final String fileName;
  @override
  final String fileUrl;
  @override
  final String fileType;
  @override
  @JsonKey()
  final int fileSize;

  @override
  String toString() {
    return 'ChatAttachment(fileName: $fileName, fileUrl: $fileUrl, fileType: $fileType, fileSize: $fileSize)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ChatAttachmentImpl &&
            (identical(other.fileName, fileName) ||
                other.fileName == fileName) &&
            (identical(other.fileUrl, fileUrl) || other.fileUrl == fileUrl) &&
            (identical(other.fileType, fileType) ||
                other.fileType == fileType) &&
            (identical(other.fileSize, fileSize) ||
                other.fileSize == fileSize));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode =>
      Object.hash(runtimeType, fileName, fileUrl, fileType, fileSize);

  /// Create a copy of ChatAttachment
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$ChatAttachmentImplCopyWith<_$ChatAttachmentImpl> get copyWith =>
      __$$ChatAttachmentImplCopyWithImpl<_$ChatAttachmentImpl>(
        this,
        _$identity,
      );

  @override
  Map<String, dynamic> toJson() {
    return _$$ChatAttachmentImplToJson(this);
  }
}

abstract class _ChatAttachment implements ChatAttachment {
  const factory _ChatAttachment({
    required final String fileName,
    required final String fileUrl,
    required final String fileType,
    final int fileSize,
  }) = _$ChatAttachmentImpl;

  factory _ChatAttachment.fromJson(Map<String, dynamic> json) =
      _$ChatAttachmentImpl.fromJson;

  @override
  String get fileName;
  @override
  String get fileUrl;
  @override
  String get fileType;
  @override
  int get fileSize;

  /// Create a copy of ChatAttachment
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$ChatAttachmentImplCopyWith<_$ChatAttachmentImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
