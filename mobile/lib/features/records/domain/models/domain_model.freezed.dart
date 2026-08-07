// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'domain_model.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
  'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models',
);

DomainModel _$DomainModelFromJson(Map<String, dynamic> json) {
  return _DomainModel.fromJson(json);
}

/// @nodoc
mixin _$DomainModel {
  String get id => throw _privateConstructorUsedError;
  @JsonKey(fromJson: _parseLocalizedName)
  String get name => throw _privateConstructorUsedError;
  @JsonKey(fromJson: _parseLocalizedName)
  String get description => throw _privateConstructorUsedError;
  bool get active => throw _privateConstructorUsedError;
  String? get identifierFieldId => throw _privateConstructorUsedError;
  String? get displayNameFieldId => throw _privateConstructorUsedError;
  String? get descriptionFieldId => throw _privateConstructorUsedError;

  /// Serializes this DomainModel to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of DomainModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $DomainModelCopyWith<DomainModel> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $DomainModelCopyWith<$Res> {
  factory $DomainModelCopyWith(
    DomainModel value,
    $Res Function(DomainModel) then,
  ) = _$DomainModelCopyWithImpl<$Res, DomainModel>;
  @useResult
  $Res call({
    String id,
    @JsonKey(fromJson: _parseLocalizedName) String name,
    @JsonKey(fromJson: _parseLocalizedName) String description,
    bool active,
    String? identifierFieldId,
    String? displayNameFieldId,
    String? descriptionFieldId,
  });
}

/// @nodoc
class _$DomainModelCopyWithImpl<$Res, $Val extends DomainModel>
    implements $DomainModelCopyWith<$Res> {
  _$DomainModelCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of DomainModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? name = null,
    Object? description = null,
    Object? active = null,
    Object? identifierFieldId = freezed,
    Object? displayNameFieldId = freezed,
    Object? descriptionFieldId = freezed,
  }) {
    return _then(
      _value.copyWith(
            id: null == id
                ? _value.id
                : id // ignore: cast_nullable_to_non_nullable
                      as String,
            name: null == name
                ? _value.name
                : name // ignore: cast_nullable_to_non_nullable
                      as String,
            description: null == description
                ? _value.description
                : description // ignore: cast_nullable_to_non_nullable
                      as String,
            active: null == active
                ? _value.active
                : active // ignore: cast_nullable_to_non_nullable
                      as bool,
            identifierFieldId: freezed == identifierFieldId
                ? _value.identifierFieldId
                : identifierFieldId // ignore: cast_nullable_to_non_nullable
                      as String?,
            displayNameFieldId: freezed == displayNameFieldId
                ? _value.displayNameFieldId
                : displayNameFieldId // ignore: cast_nullable_to_non_nullable
                      as String?,
            descriptionFieldId: freezed == descriptionFieldId
                ? _value.descriptionFieldId
                : descriptionFieldId // ignore: cast_nullable_to_non_nullable
                      as String?,
          )
          as $Val,
    );
  }
}

/// @nodoc
abstract class _$$DomainModelImplCopyWith<$Res>
    implements $DomainModelCopyWith<$Res> {
  factory _$$DomainModelImplCopyWith(
    _$DomainModelImpl value,
    $Res Function(_$DomainModelImpl) then,
  ) = __$$DomainModelImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({
    String id,
    @JsonKey(fromJson: _parseLocalizedName) String name,
    @JsonKey(fromJson: _parseLocalizedName) String description,
    bool active,
    String? identifierFieldId,
    String? displayNameFieldId,
    String? descriptionFieldId,
  });
}

/// @nodoc
class __$$DomainModelImplCopyWithImpl<$Res>
    extends _$DomainModelCopyWithImpl<$Res, _$DomainModelImpl>
    implements _$$DomainModelImplCopyWith<$Res> {
  __$$DomainModelImplCopyWithImpl(
    _$DomainModelImpl _value,
    $Res Function(_$DomainModelImpl) _then,
  ) : super(_value, _then);

  /// Create a copy of DomainModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? name = null,
    Object? description = null,
    Object? active = null,
    Object? identifierFieldId = freezed,
    Object? displayNameFieldId = freezed,
    Object? descriptionFieldId = freezed,
  }) {
    return _then(
      _$DomainModelImpl(
        id: null == id
            ? _value.id
            : id // ignore: cast_nullable_to_non_nullable
                  as String,
        name: null == name
            ? _value.name
            : name // ignore: cast_nullable_to_non_nullable
                  as String,
        description: null == description
            ? _value.description
            : description // ignore: cast_nullable_to_non_nullable
                  as String,
        active: null == active
            ? _value.active
            : active // ignore: cast_nullable_to_non_nullable
                  as bool,
        identifierFieldId: freezed == identifierFieldId
            ? _value.identifierFieldId
            : identifierFieldId // ignore: cast_nullable_to_non_nullable
                  as String?,
        displayNameFieldId: freezed == displayNameFieldId
            ? _value.displayNameFieldId
            : displayNameFieldId // ignore: cast_nullable_to_non_nullable
                  as String?,
        descriptionFieldId: freezed == descriptionFieldId
            ? _value.descriptionFieldId
            : descriptionFieldId // ignore: cast_nullable_to_non_nullable
                  as String?,
      ),
    );
  }
}

/// @nodoc
@JsonSerializable()
class _$DomainModelImpl implements _DomainModel {
  const _$DomainModelImpl({
    required this.id,
    @JsonKey(fromJson: _parseLocalizedName) required this.name,
    @JsonKey(fromJson: _parseLocalizedName) this.description = '',
    this.active = true,
    this.identifierFieldId,
    this.displayNameFieldId,
    this.descriptionFieldId,
  });

  factory _$DomainModelImpl.fromJson(Map<String, dynamic> json) =>
      _$$DomainModelImplFromJson(json);

  @override
  final String id;
  @override
  @JsonKey(fromJson: _parseLocalizedName)
  final String name;
  @override
  @JsonKey(fromJson: _parseLocalizedName)
  final String description;
  @override
  @JsonKey()
  final bool active;
  @override
  final String? identifierFieldId;
  @override
  final String? displayNameFieldId;
  @override
  final String? descriptionFieldId;

  @override
  String toString() {
    return 'DomainModel(id: $id, name: $name, description: $description, active: $active, identifierFieldId: $identifierFieldId, displayNameFieldId: $displayNameFieldId, descriptionFieldId: $descriptionFieldId)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$DomainModelImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.name, name) || other.name == name) &&
            (identical(other.description, description) ||
                other.description == description) &&
            (identical(other.active, active) || other.active == active) &&
            (identical(other.identifierFieldId, identifierFieldId) ||
                other.identifierFieldId == identifierFieldId) &&
            (identical(other.displayNameFieldId, displayNameFieldId) ||
                other.displayNameFieldId == displayNameFieldId) &&
            (identical(other.descriptionFieldId, descriptionFieldId) ||
                other.descriptionFieldId == descriptionFieldId));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
    runtimeType,
    id,
    name,
    description,
    active,
    identifierFieldId,
    displayNameFieldId,
    descriptionFieldId,
  );

  /// Create a copy of DomainModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$DomainModelImplCopyWith<_$DomainModelImpl> get copyWith =>
      __$$DomainModelImplCopyWithImpl<_$DomainModelImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$DomainModelImplToJson(this);
  }
}

abstract class _DomainModel implements DomainModel {
  const factory _DomainModel({
    required final String id,
    @JsonKey(fromJson: _parseLocalizedName) required final String name,
    @JsonKey(fromJson: _parseLocalizedName) final String description,
    final bool active,
    final String? identifierFieldId,
    final String? displayNameFieldId,
    final String? descriptionFieldId,
  }) = _$DomainModelImpl;

  factory _DomainModel.fromJson(Map<String, dynamic> json) =
      _$DomainModelImpl.fromJson;

  @override
  String get id;
  @override
  @JsonKey(fromJson: _parseLocalizedName)
  String get name;
  @override
  @JsonKey(fromJson: _parseLocalizedName)
  String get description;
  @override
  bool get active;
  @override
  String? get identifierFieldId;
  @override
  String? get displayNameFieldId;
  @override
  String? get descriptionFieldId;

  /// Create a copy of DomainModel
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$DomainModelImplCopyWith<_$DomainModelImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
