import 'package:freezed_annotation/freezed_annotation.dart';

part 'user_model.freezed.dart';
part 'user_model.g.dart';

String _idFromJson(dynamic value) => value?.toString() ?? '';
List<String> _permissionsFromJson(dynamic value) {
  if (value == null) return [];
  if (value is List) return value.map((e) => e.toString()).toList();
  return [value.toString()];
}

@freezed
class UserModel with _$UserModel {
  const factory UserModel({
    @JsonKey(fromJson: _idFromJson) required String id,
    required String username,
    @Default('') String name,
    required String role,
    @JsonKey(fromJson: _permissionsFromJson) @Default([]) List<String> permissions,
    @Default('') String department,
    @Default('') String email,
    @Default('') String timezone,
    String? orgName,
    String? deptName,
  }) = _UserModel;

  factory UserModel.fromJson(Map<String, dynamic> json) => _$UserModelFromJson(json);
}
