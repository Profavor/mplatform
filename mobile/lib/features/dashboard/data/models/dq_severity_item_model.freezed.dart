// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'dq_severity_item_model.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
  'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models',
);

DqSeverityItemModel _$DqSeverityItemModelFromJson(Map<String, dynamic> json) {
  return _DqSeverityItemModel.fromJson(json);
}

/// @nodoc
mixin _$DqSeverityItemModel {
  String get severity => throw _privateConstructorUsedError;
  int get count => throw _privateConstructorUsedError;

  /// Serializes this DqSeverityItemModel to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of DqSeverityItemModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $DqSeverityItemModelCopyWith<DqSeverityItemModel> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $DqSeverityItemModelCopyWith<$Res> {
  factory $DqSeverityItemModelCopyWith(
    DqSeverityItemModel value,
    $Res Function(DqSeverityItemModel) then,
  ) = _$DqSeverityItemModelCopyWithImpl<$Res, DqSeverityItemModel>;
  @useResult
  $Res call({String severity, int count});
}

/// @nodoc
class _$DqSeverityItemModelCopyWithImpl<$Res, $Val extends DqSeverityItemModel>
    implements $DqSeverityItemModelCopyWith<$Res> {
  _$DqSeverityItemModelCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of DqSeverityItemModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({Object? severity = null, Object? count = null}) {
    return _then(
      _value.copyWith(
            severity: null == severity
                ? _value.severity
                : severity // ignore: cast_nullable_to_non_nullable
                      as String,
            count: null == count
                ? _value.count
                : count // ignore: cast_nullable_to_non_nullable
                      as int,
          )
          as $Val,
    );
  }
}

/// @nodoc
abstract class _$$DqSeverityItemModelImplCopyWith<$Res>
    implements $DqSeverityItemModelCopyWith<$Res> {
  factory _$$DqSeverityItemModelImplCopyWith(
    _$DqSeverityItemModelImpl value,
    $Res Function(_$DqSeverityItemModelImpl) then,
  ) = __$$DqSeverityItemModelImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String severity, int count});
}

/// @nodoc
class __$$DqSeverityItemModelImplCopyWithImpl<$Res>
    extends _$DqSeverityItemModelCopyWithImpl<$Res, _$DqSeverityItemModelImpl>
    implements _$$DqSeverityItemModelImplCopyWith<$Res> {
  __$$DqSeverityItemModelImplCopyWithImpl(
    _$DqSeverityItemModelImpl _value,
    $Res Function(_$DqSeverityItemModelImpl) _then,
  ) : super(_value, _then);

  /// Create a copy of DqSeverityItemModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({Object? severity = null, Object? count = null}) {
    return _then(
      _$DqSeverityItemModelImpl(
        severity: null == severity
            ? _value.severity
            : severity // ignore: cast_nullable_to_non_nullable
                  as String,
        count: null == count
            ? _value.count
            : count // ignore: cast_nullable_to_non_nullable
                  as int,
      ),
    );
  }
}

/// @nodoc
@JsonSerializable()
class _$DqSeverityItemModelImpl implements _DqSeverityItemModel {
  const _$DqSeverityItemModelImpl({
    required this.severity,
    required this.count,
  });

  factory _$DqSeverityItemModelImpl.fromJson(Map<String, dynamic> json) =>
      _$$DqSeverityItemModelImplFromJson(json);

  @override
  final String severity;
  @override
  final int count;

  @override
  String toString() {
    return 'DqSeverityItemModel(severity: $severity, count: $count)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$DqSeverityItemModelImpl &&
            (identical(other.severity, severity) ||
                other.severity == severity) &&
            (identical(other.count, count) || other.count == count));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, severity, count);

  /// Create a copy of DqSeverityItemModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$DqSeverityItemModelImplCopyWith<_$DqSeverityItemModelImpl> get copyWith =>
      __$$DqSeverityItemModelImplCopyWithImpl<_$DqSeverityItemModelImpl>(
        this,
        _$identity,
      );

  @override
  Map<String, dynamic> toJson() {
    return _$$DqSeverityItemModelImplToJson(this);
  }
}

abstract class _DqSeverityItemModel implements DqSeverityItemModel {
  const factory _DqSeverityItemModel({
    required final String severity,
    required final int count,
  }) = _$DqSeverityItemModelImpl;

  factory _DqSeverityItemModel.fromJson(Map<String, dynamic> json) =
      _$DqSeverityItemModelImpl.fromJson;

  @override
  String get severity;
  @override
  int get count;

  /// Create a copy of DqSeverityItemModel
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$DqSeverityItemModelImplCopyWith<_$DqSeverityItemModelImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
