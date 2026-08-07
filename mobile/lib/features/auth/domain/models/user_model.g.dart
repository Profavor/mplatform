// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$UserModelImpl _$$UserModelImplFromJson(Map<String, dynamic> json) =>
    _$UserModelImpl(
      id: _idFromJson(json['id']),
      username: json['username'] as String,
      name: json['name'] as String? ?? '',
      role: json['role'] as String,
      permissions: json['permissions'] == null
          ? const []
          : _permissionsFromJson(json['permissions']),
      department: json['department'] as String? ?? '',
      email: json['email'] as String? ?? '',
      timezone: json['timezone'] as String? ?? '',
      orgName: json['orgName'] as String?,
      deptName: json['deptName'] as String?,
    );

Map<String, dynamic> _$$UserModelImplToJson(_$UserModelImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'username': instance.username,
      'name': instance.name,
      'role': instance.role,
      'permissions': instance.permissions,
      'department': instance.department,
      'email': instance.email,
      'timezone': instance.timezone,
      'orgName': instance.orgName,
      'deptName': instance.deptName,
    };
