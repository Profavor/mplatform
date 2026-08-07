// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'records_page_response.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
  'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models',
);

RecordsPageResponse _$RecordsPageResponseFromJson(Map<String, dynamic> json) {
  return _RecordsPageResponse.fromJson(json);
}

/// @nodoc
mixin _$RecordsPageResponse {
  List<RecordItem> get content => throw _privateConstructorUsedError;
  int get totalElements => throw _privateConstructorUsedError;
  int get totalPages => throw _privateConstructorUsedError;
  int get number => throw _privateConstructorUsedError;
  int get size => throw _privateConstructorUsedError;
  bool get first => throw _privateConstructorUsedError;
  bool get last => throw _privateConstructorUsedError;

  /// Serializes this RecordsPageResponse to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of RecordsPageResponse
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $RecordsPageResponseCopyWith<RecordsPageResponse> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $RecordsPageResponseCopyWith<$Res> {
  factory $RecordsPageResponseCopyWith(
    RecordsPageResponse value,
    $Res Function(RecordsPageResponse) then,
  ) = _$RecordsPageResponseCopyWithImpl<$Res, RecordsPageResponse>;
  @useResult
  $Res call({
    List<RecordItem> content,
    int totalElements,
    int totalPages,
    int number,
    int size,
    bool first,
    bool last,
  });
}

/// @nodoc
class _$RecordsPageResponseCopyWithImpl<$Res, $Val extends RecordsPageResponse>
    implements $RecordsPageResponseCopyWith<$Res> {
  _$RecordsPageResponseCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of RecordsPageResponse
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? content = null,
    Object? totalElements = null,
    Object? totalPages = null,
    Object? number = null,
    Object? size = null,
    Object? first = null,
    Object? last = null,
  }) {
    return _then(
      _value.copyWith(
            content: null == content
                ? _value.content
                : content // ignore: cast_nullable_to_non_nullable
                      as List<RecordItem>,
            totalElements: null == totalElements
                ? _value.totalElements
                : totalElements // ignore: cast_nullable_to_non_nullable
                      as int,
            totalPages: null == totalPages
                ? _value.totalPages
                : totalPages // ignore: cast_nullable_to_non_nullable
                      as int,
            number: null == number
                ? _value.number
                : number // ignore: cast_nullable_to_non_nullable
                      as int,
            size: null == size
                ? _value.size
                : size // ignore: cast_nullable_to_non_nullable
                      as int,
            first: null == first
                ? _value.first
                : first // ignore: cast_nullable_to_non_nullable
                      as bool,
            last: null == last
                ? _value.last
                : last // ignore: cast_nullable_to_non_nullable
                      as bool,
          )
          as $Val,
    );
  }
}

/// @nodoc
abstract class _$$RecordsPageResponseImplCopyWith<$Res>
    implements $RecordsPageResponseCopyWith<$Res> {
  factory _$$RecordsPageResponseImplCopyWith(
    _$RecordsPageResponseImpl value,
    $Res Function(_$RecordsPageResponseImpl) then,
  ) = __$$RecordsPageResponseImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({
    List<RecordItem> content,
    int totalElements,
    int totalPages,
    int number,
    int size,
    bool first,
    bool last,
  });
}

/// @nodoc
class __$$RecordsPageResponseImplCopyWithImpl<$Res>
    extends _$RecordsPageResponseCopyWithImpl<$Res, _$RecordsPageResponseImpl>
    implements _$$RecordsPageResponseImplCopyWith<$Res> {
  __$$RecordsPageResponseImplCopyWithImpl(
    _$RecordsPageResponseImpl _value,
    $Res Function(_$RecordsPageResponseImpl) _then,
  ) : super(_value, _then);

  /// Create a copy of RecordsPageResponse
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? content = null,
    Object? totalElements = null,
    Object? totalPages = null,
    Object? number = null,
    Object? size = null,
    Object? first = null,
    Object? last = null,
  }) {
    return _then(
      _$RecordsPageResponseImpl(
        content: null == content
            ? _value._content
            : content // ignore: cast_nullable_to_non_nullable
                  as List<RecordItem>,
        totalElements: null == totalElements
            ? _value.totalElements
            : totalElements // ignore: cast_nullable_to_non_nullable
                  as int,
        totalPages: null == totalPages
            ? _value.totalPages
            : totalPages // ignore: cast_nullable_to_non_nullable
                  as int,
        number: null == number
            ? _value.number
            : number // ignore: cast_nullable_to_non_nullable
                  as int,
        size: null == size
            ? _value.size
            : size // ignore: cast_nullable_to_non_nullable
                  as int,
        first: null == first
            ? _value.first
            : first // ignore: cast_nullable_to_non_nullable
                  as bool,
        last: null == last
            ? _value.last
            : last // ignore: cast_nullable_to_non_nullable
                  as bool,
      ),
    );
  }
}

/// @nodoc
@JsonSerializable()
class _$RecordsPageResponseImpl implements _RecordsPageResponse {
  const _$RecordsPageResponseImpl({
    final List<RecordItem> content = const [],
    this.totalElements = 0,
    this.totalPages = 0,
    this.number = 0,
    this.size = 20,
    this.first = true,
    this.last = true,
  }) : _content = content;

  factory _$RecordsPageResponseImpl.fromJson(Map<String, dynamic> json) =>
      _$$RecordsPageResponseImplFromJson(json);

  final List<RecordItem> _content;
  @override
  @JsonKey()
  List<RecordItem> get content {
    if (_content is EqualUnmodifiableListView) return _content;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_content);
  }

  @override
  @JsonKey()
  final int totalElements;
  @override
  @JsonKey()
  final int totalPages;
  @override
  @JsonKey()
  final int number;
  @override
  @JsonKey()
  final int size;
  @override
  @JsonKey()
  final bool first;
  @override
  @JsonKey()
  final bool last;

  @override
  String toString() {
    return 'RecordsPageResponse(content: $content, totalElements: $totalElements, totalPages: $totalPages, number: $number, size: $size, first: $first, last: $last)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$RecordsPageResponseImpl &&
            const DeepCollectionEquality().equals(other._content, _content) &&
            (identical(other.totalElements, totalElements) ||
                other.totalElements == totalElements) &&
            (identical(other.totalPages, totalPages) ||
                other.totalPages == totalPages) &&
            (identical(other.number, number) || other.number == number) &&
            (identical(other.size, size) || other.size == size) &&
            (identical(other.first, first) || other.first == first) &&
            (identical(other.last, last) || other.last == last));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
    runtimeType,
    const DeepCollectionEquality().hash(_content),
    totalElements,
    totalPages,
    number,
    size,
    first,
    last,
  );

  /// Create a copy of RecordsPageResponse
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$RecordsPageResponseImplCopyWith<_$RecordsPageResponseImpl> get copyWith =>
      __$$RecordsPageResponseImplCopyWithImpl<_$RecordsPageResponseImpl>(
        this,
        _$identity,
      );

  @override
  Map<String, dynamic> toJson() {
    return _$$RecordsPageResponseImplToJson(this);
  }
}

abstract class _RecordsPageResponse implements RecordsPageResponse {
  const factory _RecordsPageResponse({
    final List<RecordItem> content,
    final int totalElements,
    final int totalPages,
    final int number,
    final int size,
    final bool first,
    final bool last,
  }) = _$RecordsPageResponseImpl;

  factory _RecordsPageResponse.fromJson(Map<String, dynamic> json) =
      _$RecordsPageResponseImpl.fromJson;

  @override
  List<RecordItem> get content;
  @override
  int get totalElements;
  @override
  int get totalPages;
  @override
  int get number;
  @override
  int get size;
  @override
  bool get first;
  @override
  bool get last;

  /// Create a copy of RecordsPageResponse
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$RecordsPageResponseImplCopyWith<_$RecordsPageResponseImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
