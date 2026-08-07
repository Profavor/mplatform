import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:mplatform_mobile/features/auth/domain/models/user_model.dart';

part 'auth_response.freezed.dart';

@freezed
class AuthResponse with _$AuthResponse {
  const factory AuthResponse({
    required String accessToken,
    required String refreshToken,
    required UserModel user,
  }) = _AuthResponse;

  factory AuthResponse.fromJson(Map<String, dynamic> json) {
    final token = (json['token'] ?? json['accessToken']) as String? ?? '';
    final refreshToken = json['refreshToken'] as String? ?? '';

    UserModel user;
    if (json['user'] != null && json['user'] is Map<String, dynamic>) {
      user = UserModel.fromJson(json['user'] as Map<String, dynamic>);
    } else {
      user = UserModel.fromJson(json);
    }

    return AuthResponse(
      accessToken: token,
      refreshToken: refreshToken,
      user: user,
    );
  }
}
