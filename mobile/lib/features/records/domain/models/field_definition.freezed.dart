// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'field_definition.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
  'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models',
);

FieldDefinition _$FieldDefinitionFromJson(Map<String, dynamic> json) {
  return _FieldDefinition.fromJson(json);
}

/// @nodoc
mixin _$FieldDefinition {
  String get id => throw _privateConstructorUsedError;
  @JsonKey(readValue: _readFieldName)
  String get fieldName => throw _privateConstructorUsedError;
  @JsonKey(readValue: _readFieldLabel, fromJson: _parseLocalizedName)
  String get fieldLabel => throw _privateConstructorUsedError;
  @JsonKey(readValue: _readFieldType)
  String get fieldType => throw _privateConstructorUsedError;
  bool get required => throw _privateConstructorUsedError;
  @JsonKey(readValue: _readShowInList)
  bool get showInList => throw _privateConstructorUsedError;
  @JsonKey(readValue: _readOptions)
  List<String> get options => throw _privateConstructorUsedError;
  @JsonKey(readValue: _readRawOptions)
  String? get rawOptions => throw _privateConstructorUsedError;
  @JsonKey(readValue: _readDisplayOrder)
  int get displayOrder => throw _privateConstructorUsedError;
  @JsonKey(readValue: _readIsEncrypted)
  bool get isEncrypted => throw _privateConstructorUsedError;
  String? get maskingPattern => throw _privateConstructorUsedError;
  @JsonKey(readValue: _readLocalizedGroup, fromJson: _parseLocalizedName)
  String get groupName => throw _privateConstructorUsedError;
  @JsonKey(readValue: _readLocalizedSector, fromJson: _parseLocalizedName)
  String get sectorName => throw _privateConstructorUsedError;

  /// Serializes this FieldDefinition to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of FieldDefinition
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $FieldDefinitionCopyWith<FieldDefinition> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $FieldDefinitionCopyWith<$Res> {
  factory $FieldDefinitionCopyWith(
    FieldDefinition value,
    $Res Function(FieldDefinition) then,
  ) = _$FieldDefinitionCopyWithImpl<$Res, FieldDefinition>;
  @useResult
  $Res call({
    String id,
    @JsonKey(readValue: _readFieldName) String fieldName,
    @JsonKey(readValue: _readFieldLabel, fromJson: _parseLocalizedName)
    String fieldLabel,
    @JsonKey(readValue: _readFieldType) String fieldType,
    bool required,
    @JsonKey(readValue: _readShowInList) bool showInList,
    @JsonKey(readValue: _readOptions) List<String> options,
    @JsonKey(readValue: _readRawOptions) String? rawOptions,
    @JsonKey(readValue: _readDisplayOrder) int displayOrder,
    @JsonKey(readValue: _readIsEncrypted) bool isEncrypted,
    String? maskingPattern,
    @JsonKey(readValue: _readLocalizedGroup, fromJson: _parseLocalizedName)
    String groupName,
    @JsonKey(readValue: _readLocalizedSector, fromJson: _parseLocalizedName)
    String sectorName,
  });
}

/// @nodoc
class _$FieldDefinitionCopyWithImpl<$Res, $Val extends FieldDefinition>
    implements $FieldDefinitionCopyWith<$Res> {
  _$FieldDefinitionCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of FieldDefinition
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? fieldName = null,
    Object? fieldLabel = null,
    Object? fieldType = null,
    Object? required = null,
    Object? showInList = null,
    Object? options = null,
    Object? rawOptions = freezed,
    Object? displayOrder = null,
    Object? isEncrypted = null,
    Object? maskingPattern = freezed,
    Object? groupName = null,
    Object? sectorName = null,
  }) {
    return _then(
      _value.copyWith(
            id: null == id
                ? _value.id
                : id // ignore: cast_nullable_to_non_nullable
                      as String,
            fieldName: null == fieldName
                ? _value.fieldName
                : fieldName // ignore: cast_nullable_to_non_nullable
                      as String,
            fieldLabel: null == fieldLabel
                ? _value.fieldLabel
                : fieldLabel // ignore: cast_nullable_to_non_nullable
                      as String,
            fieldType: null == fieldType
                ? _value.fieldType
                : fieldType // ignore: cast_nullable_to_non_nullable
                      as String,
            required: null == required
                ? _value.required
                : required // ignore: cast_nullable_to_non_nullable
                      as bool,
            showInList: null == showInList
                ? _value.showInList
                : showInList // ignore: cast_nullable_to_non_nullable
                      as bool,
            options: null == options
                ? _value.options
                : options // ignore: cast_nullable_to_non_nullable
                      as List<String>,
            rawOptions: freezed == rawOptions
                ? _value.rawOptions
                : rawOptions // ignore: cast_nullable_to_non_nullable
                      as String?,
            displayOrder: null == displayOrder
                ? _value.displayOrder
                : displayOrder // ignore: cast_nullable_to_non_nullable
                      as int,
            isEncrypted: null == isEncrypted
                ? _value.isEncrypted
                : isEncrypted // ignore: cast_nullable_to_non_nullable
                      as bool,
            maskingPattern: freezed == maskingPattern
                ? _value.maskingPattern
                : maskingPattern // ignore: cast_nullable_to_non_nullable
                      as String?,
            groupName: null == groupName
                ? _value.groupName
                : groupName // ignore: cast_nullable_to_non_nullable
                      as String,
            sectorName: null == sectorName
                ? _value.sectorName
                : sectorName // ignore: cast_nullable_to_non_nullable
                      as String,
          )
          as $Val,
    );
  }
}

/// @nodoc
abstract class _$$FieldDefinitionImplCopyWith<$Res>
    implements $FieldDefinitionCopyWith<$Res> {
  factory _$$FieldDefinitionImplCopyWith(
    _$FieldDefinitionImpl value,
    $Res Function(_$FieldDefinitionImpl) then,
  ) = __$$FieldDefinitionImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({
    String id,
    @JsonKey(readValue: _readFieldName) String fieldName,
    @JsonKey(readValue: _readFieldLabel, fromJson: _parseLocalizedName)
    String fieldLabel,
    @JsonKey(readValue: _readFieldType) String fieldType,
    bool required,
    @JsonKey(readValue: _readShowInList) bool showInList,
    @JsonKey(readValue: _readOptions) List<String> options,
    @JsonKey(readValue: _readRawOptions) String? rawOptions,
    @JsonKey(readValue: _readDisplayOrder) int displayOrder,
    @JsonKey(readValue: _readIsEncrypted) bool isEncrypted,
    String? maskingPattern,
    @JsonKey(readValue: _readLocalizedGroup, fromJson: _parseLocalizedName)
    String groupName,
    @JsonKey(readValue: _readLocalizedSector, fromJson: _parseLocalizedName)
    String sectorName,
  });
}

/// @nodoc
class __$$FieldDefinitionImplCopyWithImpl<$Res>
    extends _$FieldDefinitionCopyWithImpl<$Res, _$FieldDefinitionImpl>
    implements _$$FieldDefinitionImplCopyWith<$Res> {
  __$$FieldDefinitionImplCopyWithImpl(
    _$FieldDefinitionImpl _value,
    $Res Function(_$FieldDefinitionImpl) _then,
  ) : super(_value, _then);

  /// Create a copy of FieldDefinition
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? fieldName = null,
    Object? fieldLabel = null,
    Object? fieldType = null,
    Object? required = null,
    Object? showInList = null,
    Object? options = null,
    Object? rawOptions = freezed,
    Object? displayOrder = null,
    Object? isEncrypted = null,
    Object? maskingPattern = freezed,
    Object? groupName = null,
    Object? sectorName = null,
  }) {
    return _then(
      _$FieldDefinitionImpl(
        id: null == id
            ? _value.id
            : id // ignore: cast_nullable_to_non_nullable
                  as String,
        fieldName: null == fieldName
            ? _value.fieldName
            : fieldName // ignore: cast_nullable_to_non_nullable
                  as String,
        fieldLabel: null == fieldLabel
            ? _value.fieldLabel
            : fieldLabel // ignore: cast_nullable_to_non_nullable
                  as String,
        fieldType: null == fieldType
            ? _value.fieldType
            : fieldType // ignore: cast_nullable_to_non_nullable
                  as String,
        required: null == required
            ? _value.required
            : required // ignore: cast_nullable_to_non_nullable
                  as bool,
        showInList: null == showInList
            ? _value.showInList
            : showInList // ignore: cast_nullable_to_non_nullable
                  as bool,
        options: null == options
            ? _value._options
            : options // ignore: cast_nullable_to_non_nullable
                  as List<String>,
        rawOptions: freezed == rawOptions
            ? _value.rawOptions
            : rawOptions // ignore: cast_nullable_to_non_nullable
                  as String?,
        displayOrder: null == displayOrder
            ? _value.displayOrder
            : displayOrder // ignore: cast_nullable_to_non_nullable
                  as int,
        isEncrypted: null == isEncrypted
            ? _value.isEncrypted
            : isEncrypted // ignore: cast_nullable_to_non_nullable
                  as bool,
        maskingPattern: freezed == maskingPattern
            ? _value.maskingPattern
            : maskingPattern // ignore: cast_nullable_to_non_nullable
                  as String?,
        groupName: null == groupName
            ? _value.groupName
            : groupName // ignore: cast_nullable_to_non_nullable
                  as String,
        sectorName: null == sectorName
            ? _value.sectorName
            : sectorName // ignore: cast_nullable_to_non_nullable
                  as String,
      ),
    );
  }
}

/// @nodoc
@JsonSerializable()
class _$FieldDefinitionImpl implements _FieldDefinition {
  const _$FieldDefinitionImpl({
    required this.id,
    @JsonKey(readValue: _readFieldName) required this.fieldName,
    @JsonKey(readValue: _readFieldLabel, fromJson: _parseLocalizedName)
    required this.fieldLabel,
    @JsonKey(readValue: _readFieldType) required this.fieldType,
    this.required = false,
    @JsonKey(readValue: _readShowInList) this.showInList = false,
    @JsonKey(readValue: _readOptions) final List<String> options = const [],
    @JsonKey(readValue: _readRawOptions) this.rawOptions,
    @JsonKey(readValue: _readDisplayOrder) this.displayOrder = 0,
    @JsonKey(readValue: _readIsEncrypted) this.isEncrypted = false,
    this.maskingPattern,
    @JsonKey(readValue: _readLocalizedGroup, fromJson: _parseLocalizedName)
    this.groupName = '',
    @JsonKey(readValue: _readLocalizedSector, fromJson: _parseLocalizedName)
    this.sectorName = '',
  }) : _options = options;

  factory _$FieldDefinitionImpl.fromJson(Map<String, dynamic> json) =>
      _$$FieldDefinitionImplFromJson(json);

  @override
  final String id;
  @override
  @JsonKey(readValue: _readFieldName)
  final String fieldName;
  @override
  @JsonKey(readValue: _readFieldLabel, fromJson: _parseLocalizedName)
  final String fieldLabel;
  @override
  @JsonKey(readValue: _readFieldType)
  final String fieldType;
  @override
  @JsonKey()
  final bool required;
  @override
  @JsonKey(readValue: _readShowInList)
  final bool showInList;
  final List<String> _options;
  @override
  @JsonKey(readValue: _readOptions)
  List<String> get options {
    if (_options is EqualUnmodifiableListView) return _options;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_options);
  }

  @override
  @JsonKey(readValue: _readRawOptions)
  final String? rawOptions;
  @override
  @JsonKey(readValue: _readDisplayOrder)
  final int displayOrder;
  @override
  @JsonKey(readValue: _readIsEncrypted)
  final bool isEncrypted;
  @override
  final String? maskingPattern;
  @override
  @JsonKey(readValue: _readLocalizedGroup, fromJson: _parseLocalizedName)
  final String groupName;
  @override
  @JsonKey(readValue: _readLocalizedSector, fromJson: _parseLocalizedName)
  final String sectorName;

  @override
  String toString() {
    return 'FieldDefinition(id: $id, fieldName: $fieldName, fieldLabel: $fieldLabel, fieldType: $fieldType, required: $required, showInList: $showInList, options: $options, rawOptions: $rawOptions, displayOrder: $displayOrder, isEncrypted: $isEncrypted, maskingPattern: $maskingPattern, groupName: $groupName, sectorName: $sectorName)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$FieldDefinitionImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.fieldName, fieldName) ||
                other.fieldName == fieldName) &&
            (identical(other.fieldLabel, fieldLabel) ||
                other.fieldLabel == fieldLabel) &&
            (identical(other.fieldType, fieldType) ||
                other.fieldType == fieldType) &&
            (identical(other.required, required) ||
                other.required == required) &&
            (identical(other.showInList, showInList) ||
                other.showInList == showInList) &&
            const DeepCollectionEquality().equals(other._options, _options) &&
            (identical(other.rawOptions, rawOptions) ||
                other.rawOptions == rawOptions) &&
            (identical(other.displayOrder, displayOrder) ||
                other.displayOrder == displayOrder) &&
            (identical(other.isEncrypted, isEncrypted) ||
                other.isEncrypted == isEncrypted) &&
            (identical(other.maskingPattern, maskingPattern) ||
                other.maskingPattern == maskingPattern) &&
            (identical(other.groupName, groupName) ||
                other.groupName == groupName) &&
            (identical(other.sectorName, sectorName) ||
                other.sectorName == sectorName));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
    runtimeType,
    id,
    fieldName,
    fieldLabel,
    fieldType,
    required,
    showInList,
    const DeepCollectionEquality().hash(_options),
    rawOptions,
    displayOrder,
    isEncrypted,
    maskingPattern,
    groupName,
    sectorName,
  );

  /// Create a copy of FieldDefinition
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$FieldDefinitionImplCopyWith<_$FieldDefinitionImpl> get copyWith =>
      __$$FieldDefinitionImplCopyWithImpl<_$FieldDefinitionImpl>(
        this,
        _$identity,
      );

  @override
  Map<String, dynamic> toJson() {
    return _$$FieldDefinitionImplToJson(this);
  }
}

abstract class _FieldDefinition implements FieldDefinition {
  const factory _FieldDefinition({
    required final String id,
    @JsonKey(readValue: _readFieldName) required final String fieldName,
    @JsonKey(readValue: _readFieldLabel, fromJson: _parseLocalizedName)
    required final String fieldLabel,
    @JsonKey(readValue: _readFieldType) required final String fieldType,
    final bool required,
    @JsonKey(readValue: _readShowInList) final bool showInList,
    @JsonKey(readValue: _readOptions) final List<String> options,
    @JsonKey(readValue: _readRawOptions) final String? rawOptions,
    @JsonKey(readValue: _readDisplayOrder) final int displayOrder,
    @JsonKey(readValue: _readIsEncrypted) final bool isEncrypted,
    final String? maskingPattern,
    @JsonKey(readValue: _readLocalizedGroup, fromJson: _parseLocalizedName)
    final String groupName,
    @JsonKey(readValue: _readLocalizedSector, fromJson: _parseLocalizedName)
    final String sectorName,
  }) = _$FieldDefinitionImpl;

  factory _FieldDefinition.fromJson(Map<String, dynamic> json) =
      _$FieldDefinitionImpl.fromJson;

  @override
  String get id;
  @override
  @JsonKey(readValue: _readFieldName)
  String get fieldName;
  @override
  @JsonKey(readValue: _readFieldLabel, fromJson: _parseLocalizedName)
  String get fieldLabel;
  @override
  @JsonKey(readValue: _readFieldType)
  String get fieldType;
  @override
  bool get required;
  @override
  @JsonKey(readValue: _readShowInList)
  bool get showInList;
  @override
  @JsonKey(readValue: _readOptions)
  List<String> get options;
  @override
  @JsonKey(readValue: _readRawOptions)
  String? get rawOptions;
  @override
  @JsonKey(readValue: _readDisplayOrder)
  int get displayOrder;
  @override
  @JsonKey(readValue: _readIsEncrypted)
  bool get isEncrypted;
  @override
  String? get maskingPattern;
  @override
  @JsonKey(readValue: _readLocalizedGroup, fromJson: _parseLocalizedName)
  String get groupName;
  @override
  @JsonKey(readValue: _readLocalizedSector, fromJson: _parseLocalizedName)
  String get sectorName;

  /// Create a copy of FieldDefinition
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$FieldDefinitionImplCopyWith<_$FieldDefinitionImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
