// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'record_item.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
  'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models',
);

RecordItem _$RecordItemFromJson(Map<String, dynamic> json) {
  return _RecordItem.fromJson(json);
}

/// @nodoc
mixin _$RecordItem {
  @JsonKey(name: 'id', fromJson: _idFromJson)
  String get recordId => throw _privateConstructorUsedError;
  @JsonKey(readValue: _readDomainId, fromJson: _idFromJson)
  String get domainId => throw _privateConstructorUsedError;
  @JsonKey(name: 'data', fromJson: _dataFromJson)
  Map<String, dynamic> get attributes => throw _privateConstructorUsedError;
  @JsonKey(readValue: _readNodeId, fromJson: _idFromJson)
  String? get nodeId => throw _privateConstructorUsedError;
  @JsonKey(readValue: _readNodeName, fromJson: _parseLocalizedName)
  String get nodeName => throw _privateConstructorUsedError;
  @JsonKey(readValue: _readNodePath)
  String get nodePath => throw _privateConstructorUsedError;
  String? get createdBy => throw _privateConstructorUsedError;
  String? get createdAt => throw _privateConstructorUsedError;
  String? get updatedAt => throw _privateConstructorUsedError;

  /// Serializes this RecordItem to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of RecordItem
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $RecordItemCopyWith<RecordItem> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $RecordItemCopyWith<$Res> {
  factory $RecordItemCopyWith(
    RecordItem value,
    $Res Function(RecordItem) then,
  ) = _$RecordItemCopyWithImpl<$Res, RecordItem>;
  @useResult
  $Res call({
    @JsonKey(name: 'id', fromJson: _idFromJson) String recordId,
    @JsonKey(readValue: _readDomainId, fromJson: _idFromJson) String domainId,
    @JsonKey(name: 'data', fromJson: _dataFromJson)
    Map<String, dynamic> attributes,
    @JsonKey(readValue: _readNodeId, fromJson: _idFromJson) String? nodeId,
    @JsonKey(readValue: _readNodeName, fromJson: _parseLocalizedName)
    String nodeName,
    @JsonKey(readValue: _readNodePath) String nodePath,
    String? createdBy,
    String? createdAt,
    String? updatedAt,
  });
}

/// @nodoc
class _$RecordItemCopyWithImpl<$Res, $Val extends RecordItem>
    implements $RecordItemCopyWith<$Res> {
  _$RecordItemCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of RecordItem
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? recordId = null,
    Object? domainId = null,
    Object? attributes = null,
    Object? nodeId = freezed,
    Object? nodeName = null,
    Object? nodePath = null,
    Object? createdBy = freezed,
    Object? createdAt = freezed,
    Object? updatedAt = freezed,
  }) {
    return _then(
      _value.copyWith(
            recordId: null == recordId
                ? _value.recordId
                : recordId // ignore: cast_nullable_to_non_nullable
                      as String,
            domainId: null == domainId
                ? _value.domainId
                : domainId // ignore: cast_nullable_to_non_nullable
                      as String,
            attributes: null == attributes
                ? _value.attributes
                : attributes // ignore: cast_nullable_to_non_nullable
                      as Map<String, dynamic>,
            nodeId: freezed == nodeId
                ? _value.nodeId
                : nodeId // ignore: cast_nullable_to_non_nullable
                      as String?,
            nodeName: null == nodeName
                ? _value.nodeName
                : nodeName // ignore: cast_nullable_to_non_nullable
                      as String,
            nodePath: null == nodePath
                ? _value.nodePath
                : nodePath // ignore: cast_nullable_to_non_nullable
                      as String,
            createdBy: freezed == createdBy
                ? _value.createdBy
                : createdBy // ignore: cast_nullable_to_non_nullable
                      as String?,
            createdAt: freezed == createdAt
                ? _value.createdAt
                : createdAt // ignore: cast_nullable_to_non_nullable
                      as String?,
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
abstract class _$$RecordItemImplCopyWith<$Res>
    implements $RecordItemCopyWith<$Res> {
  factory _$$RecordItemImplCopyWith(
    _$RecordItemImpl value,
    $Res Function(_$RecordItemImpl) then,
  ) = __$$RecordItemImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({
    @JsonKey(name: 'id', fromJson: _idFromJson) String recordId,
    @JsonKey(readValue: _readDomainId, fromJson: _idFromJson) String domainId,
    @JsonKey(name: 'data', fromJson: _dataFromJson)
    Map<String, dynamic> attributes,
    @JsonKey(readValue: _readNodeId, fromJson: _idFromJson) String? nodeId,
    @JsonKey(readValue: _readNodeName, fromJson: _parseLocalizedName)
    String nodeName,
    @JsonKey(readValue: _readNodePath) String nodePath,
    String? createdBy,
    String? createdAt,
    String? updatedAt,
  });
}

/// @nodoc
class __$$RecordItemImplCopyWithImpl<$Res>
    extends _$RecordItemCopyWithImpl<$Res, _$RecordItemImpl>
    implements _$$RecordItemImplCopyWith<$Res> {
  __$$RecordItemImplCopyWithImpl(
    _$RecordItemImpl _value,
    $Res Function(_$RecordItemImpl) _then,
  ) : super(_value, _then);

  /// Create a copy of RecordItem
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? recordId = null,
    Object? domainId = null,
    Object? attributes = null,
    Object? nodeId = freezed,
    Object? nodeName = null,
    Object? nodePath = null,
    Object? createdBy = freezed,
    Object? createdAt = freezed,
    Object? updatedAt = freezed,
  }) {
    return _then(
      _$RecordItemImpl(
        recordId: null == recordId
            ? _value.recordId
            : recordId // ignore: cast_nullable_to_non_nullable
                  as String,
        domainId: null == domainId
            ? _value.domainId
            : domainId // ignore: cast_nullable_to_non_nullable
                  as String,
        attributes: null == attributes
            ? _value._attributes
            : attributes // ignore: cast_nullable_to_non_nullable
                  as Map<String, dynamic>,
        nodeId: freezed == nodeId
            ? _value.nodeId
            : nodeId // ignore: cast_nullable_to_non_nullable
                  as String?,
        nodeName: null == nodeName
            ? _value.nodeName
            : nodeName // ignore: cast_nullable_to_non_nullable
                  as String,
        nodePath: null == nodePath
            ? _value.nodePath
            : nodePath // ignore: cast_nullable_to_non_nullable
                  as String,
        createdBy: freezed == createdBy
            ? _value.createdBy
            : createdBy // ignore: cast_nullable_to_non_nullable
                  as String?,
        createdAt: freezed == createdAt
            ? _value.createdAt
            : createdAt // ignore: cast_nullable_to_non_nullable
                  as String?,
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
class _$RecordItemImpl implements _RecordItem {
  const _$RecordItemImpl({
    @JsonKey(name: 'id', fromJson: _idFromJson) required this.recordId,
    @JsonKey(readValue: _readDomainId, fromJson: _idFromJson)
    this.domainId = '',
    @JsonKey(name: 'data', fromJson: _dataFromJson)
    final Map<String, dynamic> attributes = const {},
    @JsonKey(readValue: _readNodeId, fromJson: _idFromJson) this.nodeId,
    @JsonKey(readValue: _readNodeName, fromJson: _parseLocalizedName)
    this.nodeName = '',
    @JsonKey(readValue: _readNodePath) this.nodePath = '',
    this.createdBy,
    this.createdAt,
    this.updatedAt,
  }) : _attributes = attributes;

  factory _$RecordItemImpl.fromJson(Map<String, dynamic> json) =>
      _$$RecordItemImplFromJson(json);

  @override
  @JsonKey(name: 'id', fromJson: _idFromJson)
  final String recordId;
  @override
  @JsonKey(readValue: _readDomainId, fromJson: _idFromJson)
  final String domainId;
  final Map<String, dynamic> _attributes;
  @override
  @JsonKey(name: 'data', fromJson: _dataFromJson)
  Map<String, dynamic> get attributes {
    if (_attributes is EqualUnmodifiableMapView) return _attributes;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableMapView(_attributes);
  }

  @override
  @JsonKey(readValue: _readNodeId, fromJson: _idFromJson)
  final String? nodeId;
  @override
  @JsonKey(readValue: _readNodeName, fromJson: _parseLocalizedName)
  final String nodeName;
  @override
  @JsonKey(readValue: _readNodePath)
  final String nodePath;
  @override
  final String? createdBy;
  @override
  final String? createdAt;
  @override
  final String? updatedAt;

  @override
  String toString() {
    return 'RecordItem(recordId: $recordId, domainId: $domainId, attributes: $attributes, nodeId: $nodeId, nodeName: $nodeName, nodePath: $nodePath, createdBy: $createdBy, createdAt: $createdAt, updatedAt: $updatedAt)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$RecordItemImpl &&
            (identical(other.recordId, recordId) ||
                other.recordId == recordId) &&
            (identical(other.domainId, domainId) ||
                other.domainId == domainId) &&
            const DeepCollectionEquality().equals(
              other._attributes,
              _attributes,
            ) &&
            (identical(other.nodeId, nodeId) || other.nodeId == nodeId) &&
            (identical(other.nodeName, nodeName) ||
                other.nodeName == nodeName) &&
            (identical(other.nodePath, nodePath) ||
                other.nodePath == nodePath) &&
            (identical(other.createdBy, createdBy) ||
                other.createdBy == createdBy) &&
            (identical(other.createdAt, createdAt) ||
                other.createdAt == createdAt) &&
            (identical(other.updatedAt, updatedAt) ||
                other.updatedAt == updatedAt));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
    runtimeType,
    recordId,
    domainId,
    const DeepCollectionEquality().hash(_attributes),
    nodeId,
    nodeName,
    nodePath,
    createdBy,
    createdAt,
    updatedAt,
  );

  /// Create a copy of RecordItem
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$RecordItemImplCopyWith<_$RecordItemImpl> get copyWith =>
      __$$RecordItemImplCopyWithImpl<_$RecordItemImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$RecordItemImplToJson(this);
  }
}

abstract class _RecordItem implements RecordItem {
  const factory _RecordItem({
    @JsonKey(name: 'id', fromJson: _idFromJson) required final String recordId,
    @JsonKey(readValue: _readDomainId, fromJson: _idFromJson)
    final String domainId,
    @JsonKey(name: 'data', fromJson: _dataFromJson)
    final Map<String, dynamic> attributes,
    @JsonKey(readValue: _readNodeId, fromJson: _idFromJson)
    final String? nodeId,
    @JsonKey(readValue: _readNodeName, fromJson: _parseLocalizedName)
    final String nodeName,
    @JsonKey(readValue: _readNodePath) final String nodePath,
    final String? createdBy,
    final String? createdAt,
    final String? updatedAt,
  }) = _$RecordItemImpl;

  factory _RecordItem.fromJson(Map<String, dynamic> json) =
      _$RecordItemImpl.fromJson;

  @override
  @JsonKey(name: 'id', fromJson: _idFromJson)
  String get recordId;
  @override
  @JsonKey(readValue: _readDomainId, fromJson: _idFromJson)
  String get domainId;
  @override
  @JsonKey(name: 'data', fromJson: _dataFromJson)
  Map<String, dynamic> get attributes;
  @override
  @JsonKey(readValue: _readNodeId, fromJson: _idFromJson)
  String? get nodeId;
  @override
  @JsonKey(readValue: _readNodeName, fromJson: _parseLocalizedName)
  String get nodeName;
  @override
  @JsonKey(readValue: _readNodePath)
  String get nodePath;
  @override
  String? get createdBy;
  @override
  String? get createdAt;
  @override
  String? get updatedAt;

  /// Create a copy of RecordItem
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$RecordItemImplCopyWith<_$RecordItemImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
