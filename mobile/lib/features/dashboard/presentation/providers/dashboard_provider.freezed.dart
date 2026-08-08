// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'dashboard_provider.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
  'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models',
);

/// @nodoc
mixin _$DashboardState {
  bool get isLoading => throw _privateConstructorUsedError;
  String? get errorMessage => throw _privateConstructorUsedError;
  DashboardStatsModel? get stats => throw _privateConstructorUsedError;
  List<DqTrendItemModel> get dqTrends => throw _privateConstructorUsedError;
  List<DqSeverityItemModel> get dqSeverity =>
      throw _privateConstructorUsedError;

  /// Create a copy of DashboardState
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $DashboardStateCopyWith<DashboardState> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $DashboardStateCopyWith<$Res> {
  factory $DashboardStateCopyWith(
    DashboardState value,
    $Res Function(DashboardState) then,
  ) = _$DashboardStateCopyWithImpl<$Res, DashboardState>;
  @useResult
  $Res call({
    bool isLoading,
    String? errorMessage,
    DashboardStatsModel? stats,
    List<DqTrendItemModel> dqTrends,
    List<DqSeverityItemModel> dqSeverity,
  });

  $DashboardStatsModelCopyWith<$Res>? get stats;
}

/// @nodoc
class _$DashboardStateCopyWithImpl<$Res, $Val extends DashboardState>
    implements $DashboardStateCopyWith<$Res> {
  _$DashboardStateCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of DashboardState
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? isLoading = null,
    Object? errorMessage = freezed,
    Object? stats = freezed,
    Object? dqTrends = null,
    Object? dqSeverity = null,
  }) {
    return _then(
      _value.copyWith(
            isLoading: null == isLoading
                ? _value.isLoading
                : isLoading // ignore: cast_nullable_to_non_nullable
                      as bool,
            errorMessage: freezed == errorMessage
                ? _value.errorMessage
                : errorMessage // ignore: cast_nullable_to_non_nullable
                      as String?,
            stats: freezed == stats
                ? _value.stats
                : stats // ignore: cast_nullable_to_non_nullable
                      as DashboardStatsModel?,
            dqTrends: null == dqTrends
                ? _value.dqTrends
                : dqTrends // ignore: cast_nullable_to_non_nullable
                      as List<DqTrendItemModel>,
            dqSeverity: null == dqSeverity
                ? _value.dqSeverity
                : dqSeverity // ignore: cast_nullable_to_non_nullable
                      as List<DqSeverityItemModel>,
          )
          as $Val,
    );
  }

  /// Create a copy of DashboardState
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $DashboardStatsModelCopyWith<$Res>? get stats {
    if (_value.stats == null) {
      return null;
    }

    return $DashboardStatsModelCopyWith<$Res>(_value.stats!, (value) {
      return _then(_value.copyWith(stats: value) as $Val);
    });
  }
}

/// @nodoc
abstract class _$$DashboardStateImplCopyWith<$Res>
    implements $DashboardStateCopyWith<$Res> {
  factory _$$DashboardStateImplCopyWith(
    _$DashboardStateImpl value,
    $Res Function(_$DashboardStateImpl) then,
  ) = __$$DashboardStateImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({
    bool isLoading,
    String? errorMessage,
    DashboardStatsModel? stats,
    List<DqTrendItemModel> dqTrends,
    List<DqSeverityItemModel> dqSeverity,
  });

  @override
  $DashboardStatsModelCopyWith<$Res>? get stats;
}

/// @nodoc
class __$$DashboardStateImplCopyWithImpl<$Res>
    extends _$DashboardStateCopyWithImpl<$Res, _$DashboardStateImpl>
    implements _$$DashboardStateImplCopyWith<$Res> {
  __$$DashboardStateImplCopyWithImpl(
    _$DashboardStateImpl _value,
    $Res Function(_$DashboardStateImpl) _then,
  ) : super(_value, _then);

  /// Create a copy of DashboardState
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? isLoading = null,
    Object? errorMessage = freezed,
    Object? stats = freezed,
    Object? dqTrends = null,
    Object? dqSeverity = null,
  }) {
    return _then(
      _$DashboardStateImpl(
        isLoading: null == isLoading
            ? _value.isLoading
            : isLoading // ignore: cast_nullable_to_non_nullable
                  as bool,
        errorMessage: freezed == errorMessage
            ? _value.errorMessage
            : errorMessage // ignore: cast_nullable_to_non_nullable
                  as String?,
        stats: freezed == stats
            ? _value.stats
            : stats // ignore: cast_nullable_to_non_nullable
                  as DashboardStatsModel?,
        dqTrends: null == dqTrends
            ? _value._dqTrends
            : dqTrends // ignore: cast_nullable_to_non_nullable
                  as List<DqTrendItemModel>,
        dqSeverity: null == dqSeverity
            ? _value._dqSeverity
            : dqSeverity // ignore: cast_nullable_to_non_nullable
                  as List<DqSeverityItemModel>,
      ),
    );
  }
}

/// @nodoc

class _$DashboardStateImpl implements _DashboardState {
  const _$DashboardStateImpl({
    this.isLoading = false,
    this.errorMessage,
    this.stats,
    final List<DqTrendItemModel> dqTrends = const [],
    final List<DqSeverityItemModel> dqSeverity = const [],
  }) : _dqTrends = dqTrends,
       _dqSeverity = dqSeverity;

  @override
  @JsonKey()
  final bool isLoading;
  @override
  final String? errorMessage;
  @override
  final DashboardStatsModel? stats;
  final List<DqTrendItemModel> _dqTrends;
  @override
  @JsonKey()
  List<DqTrendItemModel> get dqTrends {
    if (_dqTrends is EqualUnmodifiableListView) return _dqTrends;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_dqTrends);
  }

  final List<DqSeverityItemModel> _dqSeverity;
  @override
  @JsonKey()
  List<DqSeverityItemModel> get dqSeverity {
    if (_dqSeverity is EqualUnmodifiableListView) return _dqSeverity;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_dqSeverity);
  }

  @override
  String toString() {
    return 'DashboardState(isLoading: $isLoading, errorMessage: $errorMessage, stats: $stats, dqTrends: $dqTrends, dqSeverity: $dqSeverity)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$DashboardStateImpl &&
            (identical(other.isLoading, isLoading) ||
                other.isLoading == isLoading) &&
            (identical(other.errorMessage, errorMessage) ||
                other.errorMessage == errorMessage) &&
            (identical(other.stats, stats) || other.stats == stats) &&
            const DeepCollectionEquality().equals(other._dqTrends, _dqTrends) &&
            const DeepCollectionEquality().equals(
              other._dqSeverity,
              _dqSeverity,
            ));
  }

  @override
  int get hashCode => Object.hash(
    runtimeType,
    isLoading,
    errorMessage,
    stats,
    const DeepCollectionEquality().hash(_dqTrends),
    const DeepCollectionEquality().hash(_dqSeverity),
  );

  /// Create a copy of DashboardState
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$DashboardStateImplCopyWith<_$DashboardStateImpl> get copyWith =>
      __$$DashboardStateImplCopyWithImpl<_$DashboardStateImpl>(
        this,
        _$identity,
      );
}

abstract class _DashboardState implements DashboardState {
  const factory _DashboardState({
    final bool isLoading,
    final String? errorMessage,
    final DashboardStatsModel? stats,
    final List<DqTrendItemModel> dqTrends,
    final List<DqSeverityItemModel> dqSeverity,
  }) = _$DashboardStateImpl;

  @override
  bool get isLoading;
  @override
  String? get errorMessage;
  @override
  DashboardStatsModel? get stats;
  @override
  List<DqTrendItemModel> get dqTrends;
  @override
  List<DqSeverityItemModel> get dqSeverity;

  /// Create a copy of DashboardState
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$DashboardStateImplCopyWith<_$DashboardStateImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
