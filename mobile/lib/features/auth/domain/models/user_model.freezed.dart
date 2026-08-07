// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'user_model.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
  'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models',
);

UserModel _$UserModelFromJson(Map<String, dynamic> json) {
  return _UserModel.fromJson(json);
}

/// @nodoc
mixin _$UserModel {
  @JsonKey(fromJson: _idFromJson)
  String get id => throw _privateConstructorUsedError;
  String get username => throw _privateConstructorUsedError;
  String get name => throw _privateConstructorUsedError;
  String get role => throw _privateConstructorUsedError;
  @JsonKey(fromJson: _permissionsFromJson)
  List<String> get permissions => throw _privateConstructorUsedError;
  String get department => throw _privateConstructorUsedError;
  String get email => throw _privateConstructorUsedError;
  String get timezone => throw _privateConstructorUsedError;
  String? get orgName => throw _privateConstructorUsedError;
  String? get deptName => throw _privateConstructorUsedError;

  /// Serializes this UserModel to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of UserModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $UserModelCopyWith<UserModel> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $UserModelCopyWith<$Res> {
  factory $UserModelCopyWith(UserModel value, $Res Function(UserModel) then) =
      _$UserModelCopyWithImpl<$Res, UserModel>;
  @useResult
  $Res call({
    @JsonKey(fromJson: _idFromJson) String id,
    String username,
    String name,
    String role,
    @JsonKey(fromJson: _permissionsFromJson) List<String> permissions,
    String department,
    String email,
    String timezone,
    String? orgName,
    String? deptName,
  });
}

/// @nodoc
class _$UserModelCopyWithImpl<$Res, $Val extends UserModel>
    implements $UserModelCopyWith<$Res> {
  _$UserModelCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of UserModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? username = null,
    Object? name = null,
    Object? role = null,
    Object? permissions = null,
    Object? department = null,
    Object? email = null,
    Object? timezone = null,
    Object? orgName = freezed,
    Object? deptName = freezed,
  }) {
    return _then(
      _value.copyWith(
            id: null == id
                ? _value.id
                : id // ignore: cast_nullable_to_non_nullable
                      as String,
            username: null == username
                ? _value.username
                : username // ignore: cast_nullable_to_non_nullable
                      as String,
            name: null == name
                ? _value.name
                : name // ignore: cast_nullable_to_non_nullable
                      as String,
            role: null == role
                ? _value.role
                : role // ignore: cast_nullable_to_non_nullable
                      as String,
            permissions: null == permissions
                ? _value.permissions
                : permissions // ignore: cast_nullable_to_non_nullable
                      as List<String>,
            department: null == department
                ? _value.department
                : department // ignore: cast_nullable_to_non_nullable
                      as String,
            email: null == email
                ? _value.email
                : email // ignore: cast_nullable_to_non_nullable
                      as String,
            timezone: null == timezone
                ? _value.timezone
                : timezone // ignore: cast_nullable_to_non_nullable
                      as String,
            orgName: freezed == orgName
                ? _value.orgName
                : orgName // ignore: cast_nullable_to_non_nullable
                      as String?,
            deptName: freezed == deptName
                ? _value.deptName
                : deptName // ignore: cast_nullable_to_non_nullable
                      as String?,
          )
          as $Val,
    );
  }
}

/// @nodoc
abstract class _$$UserModelImplCopyWith<$Res>
    implements $UserModelCopyWith<$Res> {
  factory _$$UserModelImplCopyWith(
    _$UserModelImpl value,
    $Res Function(_$UserModelImpl) then,
  ) = __$$UserModelImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({
    @JsonKey(fromJson: _idFromJson) String id,
    String username,
    String name,
    String role,
    @JsonKey(fromJson: _permissionsFromJson) List<String> permissions,
    String department,
    String email,
    String timezone,
    String? orgName,
    String? deptName,
  });
}

/// @nodoc
class __$$UserModelImplCopyWithImpl<$Res>
    extends _$UserModelCopyWithImpl<$Res, _$UserModelImpl>
    implements _$$UserModelImplCopyWith<$Res> {
  __$$UserModelImplCopyWithImpl(
    _$UserModelImpl _value,
    $Res Function(_$UserModelImpl) _then,
  ) : super(_value, _then);

  /// Create a copy of UserModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? username = null,
    Object? name = null,
    Object? role = null,
    Object? permissions = null,
    Object? department = null,
    Object? email = null,
    Object? timezone = null,
    Object? orgName = freezed,
    Object? deptName = freezed,
  }) {
    return _then(
      _$UserModelImpl(
        id: null == id
            ? _value.id
            : id // ignore: cast_nullable_to_non_nullable
                  as String,
        username: null == username
            ? _value.username
            : username // ignore: cast_nullable_to_non_nullable
                  as String,
        name: null == name
            ? _value.name
            : name // ignore: cast_nullable_to_non_nullable
                  as String,
        role: null == role
            ? _value.role
            : role // ignore: cast_nullable_to_non_nullable
                  as String,
        permissions: null == permissions
            ? _value._permissions
            : permissions // ignore: cast_nullable_to_non_nullable
                  as List<String>,
        department: null == department
            ? _value.department
            : department // ignore: cast_nullable_to_non_nullable
                  as String,
        email: null == email
            ? _value.email
            : email // ignore: cast_nullable_to_non_nullable
                  as String,
        timezone: null == timezone
            ? _value.timezone
            : timezone // ignore: cast_nullable_to_non_nullable
                  as String,
        orgName: freezed == orgName
            ? _value.orgName
            : orgName // ignore: cast_nullable_to_non_nullable
                  as String?,
        deptName: freezed == deptName
            ? _value.deptName
            : deptName // ignore: cast_nullable_to_non_nullable
                  as String?,
      ),
    );
  }
}

/// @nodoc
@JsonSerializable()
class _$UserModelImpl implements _UserModel {
  const _$UserModelImpl({
    @JsonKey(fromJson: _idFromJson) required this.id,
    required this.username,
    this.name = '',
    required this.role,
    @JsonKey(fromJson: _permissionsFromJson)
    final List<String> permissions = const [],
    this.department = '',
    this.email = '',
    this.timezone = '',
    this.orgName,
    this.deptName,
  }) : _permissions = permissions;

  factory _$UserModelImpl.fromJson(Map<String, dynamic> json) =>
      _$$UserModelImplFromJson(json);

  @override
  @JsonKey(fromJson: _idFromJson)
  final String id;
  @override
  final String username;
  @override
  @JsonKey()
  final String name;
  @override
  final String role;
  final List<String> _permissions;
  @override
  @JsonKey(fromJson: _permissionsFromJson)
  List<String> get permissions {
    if (_permissions is EqualUnmodifiableListView) return _permissions;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_permissions);
  }

  @override
  @JsonKey()
  final String department;
  @override
  @JsonKey()
  final String email;
  @override
  @JsonKey()
  final String timezone;
  @override
  final String? orgName;
  @override
  final String? deptName;

  @override
  String toString() {
    return 'UserModel(id: $id, username: $username, name: $name, role: $role, permissions: $permissions, department: $department, email: $email, timezone: $timezone, orgName: $orgName, deptName: $deptName)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$UserModelImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.username, username) ||
                other.username == username) &&
            (identical(other.name, name) || other.name == name) &&
            (identical(other.role, role) || other.role == role) &&
            const DeepCollectionEquality().equals(
              other._permissions,
              _permissions,
            ) &&
            (identical(other.department, department) ||
                other.department == department) &&
            (identical(other.email, email) || other.email == email) &&
            (identical(other.timezone, timezone) ||
                other.timezone == timezone) &&
            (identical(other.orgName, orgName) || other.orgName == orgName) &&
            (identical(other.deptName, deptName) ||
                other.deptName == deptName));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
    runtimeType,
    id,
    username,
    name,
    role,
    const DeepCollectionEquality().hash(_permissions),
    department,
    email,
    timezone,
    orgName,
    deptName,
  );

  /// Create a copy of UserModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$UserModelImplCopyWith<_$UserModelImpl> get copyWith =>
      __$$UserModelImplCopyWithImpl<_$UserModelImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$UserModelImplToJson(this);
  }
}

abstract class _UserModel implements UserModel {
  const factory _UserModel({
    @JsonKey(fromJson: _idFromJson) required final String id,
    required final String username,
    final String name,
    required final String role,
    @JsonKey(fromJson: _permissionsFromJson) final List<String> permissions,
    final String department,
    final String email,
    final String timezone,
    final String? orgName,
    final String? deptName,
  }) = _$UserModelImpl;

  factory _UserModel.fromJson(Map<String, dynamic> json) =
      _$UserModelImpl.fromJson;

  @override
  @JsonKey(fromJson: _idFromJson)
  String get id;
  @override
  String get username;
  @override
  String get name;
  @override
  String get role;
  @override
  @JsonKey(fromJson: _permissionsFromJson)
  List<String> get permissions;
  @override
  String get department;
  @override
  String get email;
  @override
  String get timezone;
  @override
  String? get orgName;
  @override
  String? get deptName;

  /// Create a copy of UserModel
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$UserModelImplCopyWith<_$UserModelImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
