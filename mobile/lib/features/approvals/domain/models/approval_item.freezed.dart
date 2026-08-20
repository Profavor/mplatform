// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'approval_item.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
  'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models',
);

/// @nodoc
mixin _$ApprovalItem {
  String get approvalId => throw _privateConstructorUsedError;
  String get targetType => throw _privateConstructorUsedError;
  String get targetId => throw _privateConstructorUsedError;
  String get requester => throw _privateConstructorUsedError;
  String get status => throw _privateConstructorUsedError;
  String? get requestReason => throw _privateConstructorUsedError;
  String? get requestDate => throw _privateConstructorUsedError;
  String? get reviewedDate => throw _privateConstructorUsedError;
  Map<String, dynamic> get payload => throw _privateConstructorUsedError;
  List<dynamic> get steps => throw _privateConstructorUsedError;
  String? get domainId => throw _privateConstructorUsedError;
  String? get nodeId => throw _privateConstructorUsedError;
  String? get domainName => throw _privateConstructorUsedError;
  String? get idAttribute => throw _privateConstructorUsedError;
  String? get nameAttribute => throw _privateConstructorUsedError;
  String? get classificationPath => throw _privateConstructorUsedError;

  /// Create a copy of ApprovalItem
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $ApprovalItemCopyWith<ApprovalItem> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ApprovalItemCopyWith<$Res> {
  factory $ApprovalItemCopyWith(
    ApprovalItem value,
    $Res Function(ApprovalItem) then,
  ) = _$ApprovalItemCopyWithImpl<$Res, ApprovalItem>;
  @useResult
  $Res call({
    String approvalId,
    String targetType,
    String targetId,
    String requester,
    String status,
    String? requestReason,
    String? requestDate,
    String? reviewedDate,
    Map<String, dynamic> payload,
    List<dynamic> steps,
    String? domainId,
    String? nodeId,
    String? domainName,
    String? idAttribute,
    String? nameAttribute,
    String? classificationPath,
  });
}

/// @nodoc
class _$ApprovalItemCopyWithImpl<$Res, $Val extends ApprovalItem>
    implements $ApprovalItemCopyWith<$Res> {
  _$ApprovalItemCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of ApprovalItem
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? approvalId = null,
    Object? targetType = null,
    Object? targetId = null,
    Object? requester = null,
    Object? status = null,
    Object? requestReason = freezed,
    Object? requestDate = freezed,
    Object? reviewedDate = freezed,
    Object? payload = null,
    Object? steps = null,
    Object? domainId = freezed,
    Object? nodeId = freezed,
    Object? domainName = freezed,
    Object? idAttribute = freezed,
    Object? nameAttribute = freezed,
    Object? classificationPath = freezed,
  }) {
    return _then(
      _value.copyWith(
            approvalId: null == approvalId
                ? _value.approvalId
                : approvalId // ignore: cast_nullable_to_non_nullable
                      as String,
            targetType: null == targetType
                ? _value.targetType
                : targetType // ignore: cast_nullable_to_non_nullable
                      as String,
            targetId: null == targetId
                ? _value.targetId
                : targetId // ignore: cast_nullable_to_non_nullable
                      as String,
            requester: null == requester
                ? _value.requester
                : requester // ignore: cast_nullable_to_non_nullable
                      as String,
            status: null == status
                ? _value.status
                : status // ignore: cast_nullable_to_non_nullable
                      as String,
            requestReason: freezed == requestReason
                ? _value.requestReason
                : requestReason // ignore: cast_nullable_to_non_nullable
                      as String?,
            requestDate: freezed == requestDate
                ? _value.requestDate
                : requestDate // ignore: cast_nullable_to_non_nullable
                      as String?,
            reviewedDate: freezed == reviewedDate
                ? _value.reviewedDate
                : reviewedDate // ignore: cast_nullable_to_non_nullable
                      as String?,
            payload: null == payload
                ? _value.payload
                : payload // ignore: cast_nullable_to_non_nullable
                      as Map<String, dynamic>,
            steps: null == steps
                ? _value.steps
                : steps // ignore: cast_nullable_to_non_nullable
                      as List<dynamic>,
            domainId: freezed == domainId
                ? _value.domainId
                : domainId // ignore: cast_nullable_to_non_nullable
                      as String?,
            nodeId: freezed == nodeId
                ? _value.nodeId
                : nodeId // ignore: cast_nullable_to_non_nullable
                      as String?,
            domainName: freezed == domainName
                ? _value.domainName
                : domainName // ignore: cast_nullable_to_non_nullable
                      as String?,
            idAttribute: freezed == idAttribute
                ? _value.idAttribute
                : idAttribute // ignore: cast_nullable_to_non_nullable
                      as String?,
            nameAttribute: freezed == nameAttribute
                ? _value.nameAttribute
                : nameAttribute // ignore: cast_nullable_to_non_nullable
                      as String?,
            classificationPath: freezed == classificationPath
                ? _value.classificationPath
                : classificationPath // ignore: cast_nullable_to_non_nullable
                      as String?,
          )
          as $Val,
    );
  }
}

/// @nodoc
abstract class _$$ApprovalItemImplCopyWith<$Res>
    implements $ApprovalItemCopyWith<$Res> {
  factory _$$ApprovalItemImplCopyWith(
    _$ApprovalItemImpl value,
    $Res Function(_$ApprovalItemImpl) then,
  ) = __$$ApprovalItemImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({
    String approvalId,
    String targetType,
    String targetId,
    String requester,
    String status,
    String? requestReason,
    String? requestDate,
    String? reviewedDate,
    Map<String, dynamic> payload,
    List<dynamic> steps,
    String? domainId,
    String? nodeId,
    String? domainName,
    String? idAttribute,
    String? nameAttribute,
    String? classificationPath,
  });
}

/// @nodoc
class __$$ApprovalItemImplCopyWithImpl<$Res>
    extends _$ApprovalItemCopyWithImpl<$Res, _$ApprovalItemImpl>
    implements _$$ApprovalItemImplCopyWith<$Res> {
  __$$ApprovalItemImplCopyWithImpl(
    _$ApprovalItemImpl _value,
    $Res Function(_$ApprovalItemImpl) _then,
  ) : super(_value, _then);

  /// Create a copy of ApprovalItem
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? approvalId = null,
    Object? targetType = null,
    Object? targetId = null,
    Object? requester = null,
    Object? status = null,
    Object? requestReason = freezed,
    Object? requestDate = freezed,
    Object? reviewedDate = freezed,
    Object? payload = null,
    Object? steps = null,
    Object? domainId = freezed,
    Object? nodeId = freezed,
    Object? domainName = freezed,
    Object? idAttribute = freezed,
    Object? nameAttribute = freezed,
    Object? classificationPath = freezed,
  }) {
    return _then(
      _$ApprovalItemImpl(
        approvalId: null == approvalId
            ? _value.approvalId
            : approvalId // ignore: cast_nullable_to_non_nullable
                  as String,
        targetType: null == targetType
            ? _value.targetType
            : targetType // ignore: cast_nullable_to_non_nullable
                  as String,
        targetId: null == targetId
            ? _value.targetId
            : targetId // ignore: cast_nullable_to_non_nullable
                  as String,
        requester: null == requester
            ? _value.requester
            : requester // ignore: cast_nullable_to_non_nullable
                  as String,
        status: null == status
            ? _value.status
            : status // ignore: cast_nullable_to_non_nullable
                  as String,
        requestReason: freezed == requestReason
            ? _value.requestReason
            : requestReason // ignore: cast_nullable_to_non_nullable
                  as String?,
        requestDate: freezed == requestDate
            ? _value.requestDate
            : requestDate // ignore: cast_nullable_to_non_nullable
                  as String?,
        reviewedDate: freezed == reviewedDate
            ? _value.reviewedDate
            : reviewedDate // ignore: cast_nullable_to_non_nullable
                  as String?,
        payload: null == payload
            ? _value._payload
            : payload // ignore: cast_nullable_to_non_nullable
                  as Map<String, dynamic>,
        steps: null == steps
            ? _value._steps
            : steps // ignore: cast_nullable_to_non_nullable
                  as List<dynamic>,
        domainId: freezed == domainId
            ? _value.domainId
            : domainId // ignore: cast_nullable_to_non_nullable
                  as String?,
        nodeId: freezed == nodeId
            ? _value.nodeId
            : nodeId // ignore: cast_nullable_to_non_nullable
                  as String?,
        domainName: freezed == domainName
            ? _value.domainName
            : domainName // ignore: cast_nullable_to_non_nullable
                  as String?,
        idAttribute: freezed == idAttribute
            ? _value.idAttribute
            : idAttribute // ignore: cast_nullable_to_non_nullable
                  as String?,
        nameAttribute: freezed == nameAttribute
            ? _value.nameAttribute
            : nameAttribute // ignore: cast_nullable_to_non_nullable
                  as String?,
        classificationPath: freezed == classificationPath
            ? _value.classificationPath
            : classificationPath // ignore: cast_nullable_to_non_nullable
                  as String?,
      ),
    );
  }
}

/// @nodoc

class _$ApprovalItemImpl implements _ApprovalItem {
  const _$ApprovalItemImpl({
    required this.approvalId,
    required this.targetType,
    required this.targetId,
    required this.requester,
    required this.status,
    this.requestReason,
    this.requestDate,
    this.reviewedDate,
    final Map<String, dynamic> payload = const {},
    final List<dynamic> steps = const [],
    this.domainId,
    this.nodeId,
    this.domainName,
    this.idAttribute,
    this.nameAttribute,
    this.classificationPath,
  }) : _payload = payload,
       _steps = steps;

  @override
  final String approvalId;
  @override
  final String targetType;
  @override
  final String targetId;
  @override
  final String requester;
  @override
  final String status;
  @override
  final String? requestReason;
  @override
  final String? requestDate;
  @override
  final String? reviewedDate;
  final Map<String, dynamic> _payload;
  @override
  @JsonKey()
  Map<String, dynamic> get payload {
    if (_payload is EqualUnmodifiableMapView) return _payload;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableMapView(_payload);
  }

  final List<dynamic> _steps;
  @override
  @JsonKey()
  List<dynamic> get steps {
    if (_steps is EqualUnmodifiableListView) return _steps;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_steps);
  }

  @override
  final String? domainId;
  @override
  final String? nodeId;
  @override
  final String? domainName;
  @override
  final String? idAttribute;
  @override
  final String? nameAttribute;
  @override
  final String? classificationPath;

  @override
  String toString() {
    return 'ApprovalItem(approvalId: $approvalId, targetType: $targetType, targetId: $targetId, requester: $requester, status: $status, requestReason: $requestReason, requestDate: $requestDate, reviewedDate: $reviewedDate, payload: $payload, steps: $steps, domainId: $domainId, nodeId: $nodeId, domainName: $domainName, idAttribute: $idAttribute, nameAttribute: $nameAttribute, classificationPath: $classificationPath)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ApprovalItemImpl &&
            (identical(other.approvalId, approvalId) ||
                other.approvalId == approvalId) &&
            (identical(other.targetType, targetType) ||
                other.targetType == targetType) &&
            (identical(other.targetId, targetId) ||
                other.targetId == targetId) &&
            (identical(other.requester, requester) ||
                other.requester == requester) &&
            (identical(other.status, status) || other.status == status) &&
            (identical(other.requestReason, requestReason) ||
                other.requestReason == requestReason) &&
            (identical(other.requestDate, requestDate) ||
                other.requestDate == requestDate) &&
            (identical(other.reviewedDate, reviewedDate) ||
                other.reviewedDate == reviewedDate) &&
            const DeepCollectionEquality().equals(other._payload, _payload) &&
            const DeepCollectionEquality().equals(other._steps, _steps) &&
            (identical(other.domainId, domainId) ||
                other.domainId == domainId) &&
            (identical(other.nodeId, nodeId) || other.nodeId == nodeId) &&
            (identical(other.domainName, domainName) ||
                other.domainName == domainName) &&
            (identical(other.idAttribute, idAttribute) ||
                other.idAttribute == idAttribute) &&
            (identical(other.nameAttribute, nameAttribute) ||
                other.nameAttribute == nameAttribute) &&
            (identical(other.classificationPath, classificationPath) ||
                other.classificationPath == classificationPath));
  }

  @override
  int get hashCode => Object.hash(
    runtimeType,
    approvalId,
    targetType,
    targetId,
    requester,
    status,
    requestReason,
    requestDate,
    reviewedDate,
    const DeepCollectionEquality().hash(_payload),
    const DeepCollectionEquality().hash(_steps),
    domainId,
    nodeId,
    domainName,
    idAttribute,
    nameAttribute,
    classificationPath,
  );

  /// Create a copy of ApprovalItem
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$ApprovalItemImplCopyWith<_$ApprovalItemImpl> get copyWith =>
      __$$ApprovalItemImplCopyWithImpl<_$ApprovalItemImpl>(this, _$identity);
}

abstract class _ApprovalItem implements ApprovalItem {
  const factory _ApprovalItem({
    required final String approvalId,
    required final String targetType,
    required final String targetId,
    required final String requester,
    required final String status,
    final String? requestReason,
    final String? requestDate,
    final String? reviewedDate,
    final Map<String, dynamic> payload,
    final List<dynamic> steps,
    final String? domainId,
    final String? nodeId,
    final String? domainName,
    final String? idAttribute,
    final String? nameAttribute,
    final String? classificationPath,
  }) = _$ApprovalItemImpl;

  @override
  String get approvalId;
  @override
  String get targetType;
  @override
  String get targetId;
  @override
  String get requester;
  @override
  String get status;
  @override
  String? get requestReason;
  @override
  String? get requestDate;
  @override
  String? get reviewedDate;
  @override
  Map<String, dynamic> get payload;
  @override
  List<dynamic> get steps;
  @override
  String? get domainId;
  @override
  String? get nodeId;
  @override
  String? get domainName;
  @override
  String? get idAttribute;
  @override
  String? get nameAttribute;
  @override
  String? get classificationPath;

  /// Create a copy of ApprovalItem
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$ApprovalItemImplCopyWith<_$ApprovalItemImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
