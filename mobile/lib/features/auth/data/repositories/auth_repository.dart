import 'package:dio/dio.dart';
import 'package:mplatform_mobile/core/storage/storage_service.dart';
import 'package:mplatform_mobile/features/auth/domain/models/auth_response.dart';
import 'package:mplatform_mobile/features/auth/domain/models/user_model.dart';

class AuthRepository {
  final Dio _dio;
  final StorageService _storageService;

  AuthRepository(this._dio, this._storageService);

  Future<AuthResponse> login({required String username, required String password}) async {
    final response = await _dio.post(
      '/api/auth/login',
      data: {'username': username, 'password': password},
    );

    final authResponse = AuthResponse.fromJson(response.data as Map<String, dynamic>);
    await _storageService.saveTokens(
      accessToken: authResponse.accessToken,
      refreshToken: authResponse.refreshToken,
    );
    return authResponse;
  }

  Future<void> logout() async {
    try {
      await _dio.post('/api/auth/logout');
    } catch (_) {
      // Ignore network failure on logout
    } finally {
      await _storageService.deleteTokens();
    }
  }

  Future<UserModel?> getCurrentUser() async {
    final token = await _storageService.getAccessToken();
    if (token == null || token.isEmpty) return null;

    try {
      final response = await _dio.get('/api/auth/me');
      return UserModel.fromJson(response.data as Map<String, dynamic>);
    } catch (_) {
      return null;
    }
  }

  Future<List<UserModel>> getUsers() async {
    final response = await _dio.get('/api/users');
    final data = response.data as List;
    return data.map((json) => UserModel.fromJson(json as Map<String, dynamic>)).toList();
  }
}
