// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'classification_node_model.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
  'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models',
);

ClassificationNodeModel _$ClassificationNodeModelFromJson(
  Map<String, dynamic> json,
) {
  return _ClassificationNodeModel.fromJson(json);
}

/// @nodoc
mixin _$ClassificationNodeModel {
  String get id => throw _privateConstructorUsedError;
  String? get domainId => throw _privateConstructorUsedError;
  @JsonKey(fromJson: _parseLocalizedName)
  String get name => throw _privateConstructorUsedError;
  String get path => throw _privateConstructorUsedError;
  int get depth => throw _privateConstructorUsedError;
  List<ClassificationNodeModel> get children =>
      throw _privateConstructorUsedError;

  /// Serializes this ClassificationNodeModel to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of ClassificationNodeModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $ClassificationNodeModelCopyWith<ClassificationNodeModel> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ClassificationNodeModelCopyWith<$Res> {
  factory $ClassificationNodeModelCopyWith(
    ClassificationNodeModel value,
    $Res Function(ClassificationNodeModel) then,
  ) = _$ClassificationNodeModelCopyWithImpl<$Res, ClassificationNodeModel>;
  @useResult
  $Res call({
    String id,
    String? domainId,
    @JsonKey(fromJson: _parseLocalizedName) String name,
    String path,
    int depth,
    List<ClassificationNodeModel> children,
  });
}

/// @nodoc
class _$ClassificationNodeModelCopyWithImpl<
  $Res,
  $Val extends ClassificationNodeModel
>
    implements $ClassificationNodeModelCopyWith<$Res> {
  _$ClassificationNodeModelCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of ClassificationNodeModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? domainId = freezed,
    Object? name = null,
    Object? path = null,
    Object? depth = null,
    Object? children = null,
  }) {
    return _then(
      _value.copyWith(
            id: null == id
                ? _value.id
                : id // ignore: cast_nullable_to_non_nullable
                      as String,
            domainId: freezed == domainId
                ? _value.domainId
                : domainId // ignore: cast_nullable_to_non_nullable
                      as String?,
            name: null == name
                ? _value.name
                : name // ignore: cast_nullable_to_non_nullable
                      as String,
            path: null == path
                ? _value.path
                : path // ignore: cast_nullable_to_non_nullable
                      as String,
            depth: null == depth
                ? _value.depth
                : depth // ignore: cast_nullable_to_non_nullable
                      as int,
            children: null == children
                ? _value.children
                : children // ignore: cast_nullable_to_non_nullable
                      as List<ClassificationNodeModel>,
          )
          as $Val,
    );
  }
}

/// @nodoc
abstract class _$$ClassificationNodeModelImplCopyWith<$Res>
    implements $ClassificationNodeModelCopyWith<$Res> {
  factory _$$ClassificationNodeModelImplCopyWith(
    _$ClassificationNodeModelImpl value,
    $Res Function(_$ClassificationNodeModelImpl) then,
  ) = __$$ClassificationNodeModelImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({
    String id,
    String? domainId,
    @JsonKey(fromJson: _parseLocalizedName) String name,
    String path,
    int depth,
    List<ClassificationNodeModel> children,
  });
}

/// @nodoc
class __$$ClassificationNodeModelImplCopyWithImpl<$Res>
    extends
        _$ClassificationNodeModelCopyWithImpl<
          $Res,
          _$ClassificationNodeModelImpl
        >
    implements _$$ClassificationNodeModelImplCopyWith<$Res> {
  __$$ClassificationNodeModelImplCopyWithImpl(
    _$ClassificationNodeModelImpl _value,
    $Res Function(_$ClassificationNodeModelImpl) _then,
  ) : super(_value, _then);

  /// Create a copy of ClassificationNodeModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? domainId = freezed,
    Object? name = null,
    Object? path = null,
    Object? depth = null,
    Object? children = null,
  }) {
    return _then(
      _$ClassificationNodeModelImpl(
        id: null == id
            ? _value.id
            : id // ignore: cast_nullable_to_non_nullable
                  as String,
        domainId: freezed == domainId
            ? _value.domainId
            : domainId // ignore: cast_nullable_to_non_nullable
                  as String?,
        name: null == name
            ? _value.name
            : name // ignore: cast_nullable_to_non_nullable
                  as String,
        path: null == path
            ? _value.path
            : path // ignore: cast_nullable_to_non_nullable
                  as String,
        depth: null == depth
            ? _value.depth
            : depth // ignore: cast_nullable_to_non_nullable
                  as int,
        children: null == children
            ? _value._children
            : children // ignore: cast_nullable_to_non_nullable
                  as List<ClassificationNodeModel>,
      ),
    );
  }
}

/// @nodoc
@JsonSerializable()
class _$ClassificationNodeModelImpl implements _ClassificationNodeModel {
  const _$ClassificationNodeModelImpl({
    required this.id,
    this.domainId,
    @JsonKey(fromJson: _parseLocalizedName) this.name = '',
    this.path = '',
    this.depth = 0,
    final List<ClassificationNodeModel> children = const [],
  }) : _children = children;

  factory _$ClassificationNodeModelImpl.fromJson(Map<String, dynamic> json) =>
      _$$ClassificationNodeModelImplFromJson(json);

  @override
  final String id;
  @override
  final String? domainId;
  @override
  @JsonKey(fromJson: _parseLocalizedName)
  final String name;
  @override
  @JsonKey()
  final String path;
  @override
  @JsonKey()
  final int depth;
  final List<ClassificationNodeModel> _children;
  @override
  @JsonKey()
  List<ClassificationNodeModel> get children {
    if (_children is EqualUnmodifiableListView) return _children;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_children);
  }

  @override
  String toString() {
    return 'ClassificationNodeModel(id: $id, domainId: $domainId, name: $name, path: $path, depth: $depth, children: $children)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ClassificationNodeModelImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.domainId, domainId) ||
                other.domainId == domainId) &&
            (identical(other.name, name) || other.name == name) &&
            (identical(other.path, path) || other.path == path) &&
            (identical(other.depth, depth) || other.depth == depth) &&
            const DeepCollectionEquality().equals(other._children, _children));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
    runtimeType,
    id,
    domainId,
    name,
    path,
    depth,
    const DeepCollectionEquality().hash(_children),
  );

  /// Create a copy of ClassificationNodeModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$ClassificationNodeModelImplCopyWith<_$ClassificationNodeModelImpl>
  get copyWith =>
      __$$ClassificationNodeModelImplCopyWithImpl<
        _$ClassificationNodeModelImpl
      >(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$ClassificationNodeModelImplToJson(this);
  }
}

abstract class _ClassificationNodeModel implements ClassificationNodeModel {
  const factory _ClassificationNodeModel({
    required final String id,
    final String? domainId,
    @JsonKey(fromJson: _parseLocalizedName) final String name,
    final String path,
    final int depth,
    final List<ClassificationNodeModel> children,
  }) = _$ClassificationNodeModelImpl;

  factory _ClassificationNodeModel.fromJson(Map<String, dynamic> json) =
      _$ClassificationNodeModelImpl.fromJson;

  @override
  String get id;
  @override
  String? get domainId;
  @override
  @JsonKey(fromJson: _parseLocalizedName)
  String get name;
  @override
  String get path;
  @override
  int get depth;
  @override
  List<ClassificationNodeModel> get children;

  /// Create a copy of ClassificationNodeModel
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$ClassificationNodeModelImplCopyWith<_$ClassificationNodeModelImpl>
  get copyWith => throw _privateConstructorUsedError;
}
