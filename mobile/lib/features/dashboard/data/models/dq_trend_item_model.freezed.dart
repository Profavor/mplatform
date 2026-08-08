// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'dq_trend_item_model.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
  'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models',
);

DqTrendItemModel _$DqTrendItemModelFromJson(Map<String, dynamic> json) {
  return _DqTrendItemModel.fromJson(json);
}

/// @nodoc
mixin _$DqTrendItemModel {
  String get date => throw _privateConstructorUsedError;
  int get count => throw _privateConstructorUsedError;

  /// Serializes this DqTrendItemModel to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of DqTrendItemModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $DqTrendItemModelCopyWith<DqTrendItemModel> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $DqTrendItemModelCopyWith<$Res> {
  factory $DqTrendItemModelCopyWith(
    DqTrendItemModel value,
    $Res Function(DqTrendItemModel) then,
  ) = _$DqTrendItemModelCopyWithImpl<$Res, DqTrendItemModel>;
  @useResult
  $Res call({String date, int count});
}

/// @nodoc
class _$DqTrendItemModelCopyWithImpl<$Res, $Val extends DqTrendItemModel>
    implements $DqTrendItemModelCopyWith<$Res> {
  _$DqTrendItemModelCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of DqTrendItemModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({Object? date = null, Object? count = null}) {
    return _then(
      _value.copyWith(
            date: null == date
                ? _value.date
                : date // ignore: cast_nullable_to_non_nullable
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
abstract class _$$DqTrendItemModelImplCopyWith<$Res>
    implements $DqTrendItemModelCopyWith<$Res> {
  factory _$$DqTrendItemModelImplCopyWith(
    _$DqTrendItemModelImpl value,
    $Res Function(_$DqTrendItemModelImpl) then,
  ) = __$$DqTrendItemModelImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String date, int count});
}

/// @nodoc
class __$$DqTrendItemModelImplCopyWithImpl<$Res>
    extends _$DqTrendItemModelCopyWithImpl<$Res, _$DqTrendItemModelImpl>
    implements _$$DqTrendItemModelImplCopyWith<$Res> {
  __$$DqTrendItemModelImplCopyWithImpl(
    _$DqTrendItemModelImpl _value,
    $Res Function(_$DqTrendItemModelImpl) _then,
  ) : super(_value, _then);

  /// Create a copy of DqTrendItemModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({Object? date = null, Object? count = null}) {
    return _then(
      _$DqTrendItemModelImpl(
        date: null == date
            ? _value.date
            : date // ignore: cast_nullable_to_non_nullable
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
class _$DqTrendItemModelImpl implements _DqTrendItemModel {
  const _$DqTrendItemModelImpl({required this.date, required this.count});

  factory _$DqTrendItemModelImpl.fromJson(Map<String, dynamic> json) =>
      _$$DqTrendItemModelImplFromJson(json);

  @override
  final String date;
  @override
  final int count;

  @override
  String toString() {
    return 'DqTrendItemModel(date: $date, count: $count)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$DqTrendItemModelImpl &&
            (identical(other.date, date) || other.date == date) &&
            (identical(other.count, count) || other.count == count));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, date, count);

  /// Create a copy of DqTrendItemModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$DqTrendItemModelImplCopyWith<_$DqTrendItemModelImpl> get copyWith =>
      __$$DqTrendItemModelImplCopyWithImpl<_$DqTrendItemModelImpl>(
        this,
        _$identity,
      );

  @override
  Map<String, dynamic> toJson() {
    return _$$DqTrendItemModelImplToJson(this);
  }
}

abstract class _DqTrendItemModel implements DqTrendItemModel {
  const factory _DqTrendItemModel({
    required final String date,
    required final int count,
  }) = _$DqTrendItemModelImpl;

  factory _DqTrendItemModel.fromJson(Map<String, dynamic> json) =
      _$DqTrendItemModelImpl.fromJson;

  @override
  String get date;
  @override
  int get count;

  /// Create a copy of DqTrendItemModel
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$DqTrendItemModelImplCopyWith<_$DqTrendItemModelImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
