import 'package:dio/dio.dart';
import 'package:mplatform_mobile/core/auth/oidc_service.dart';
import 'package:mplatform_mobile/core/storage/storage_service.dart';
import 'package:mplatform_mobile/features/auth/domain/models/auth_response.dart';
import 'package:mplatform_mobile/features/auth/domain/models/user_model.dart';

class AuthRepository {
  final Dio _dio;
  final StorageService _storageService;
  final OidcService _oidcService;

  AuthRepository(this._dio, this._storageService, [OidcService? oidcService])
      : _oidcService = oidcService ?? OidcService();

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

  Future<UserModel> loginWithOidc({
    required String authCode,
    required String codeVerifier,
    String tokenEndpoint = 'http://localhost:8081/realms/mplatform/protocol/openid-connect/token',
    String clientId = 'mdm-mobile',
    String redirectUri = 'mplatform://oauth2redirect',
  }) async {
    print('[OIDC Repo] Exchanging code for tokens at: $tokenEndpoint');
    final tokens = await _oidcService.exchangeCodeForTokens(
      dio: _dio,
      tokenEndpoint: tokenEndpoint,
      clientId: clientId,
      redirectUri: redirectUri,
      code: authCode,
      codeVerifier: codeVerifier,
    );

    final accessToken = tokens['access_token'] as String;
    final refreshToken = tokens['refresh_token'] as String?;
    print('[OIDC Repo] Tokens received. AccessToken len: ${accessToken.length}');

    await _storageService.saveTokens(
      accessToken: accessToken,
      refreshToken: refreshToken ?? '',
    );
    print('[OIDC Repo] Tokens saved to storage. Fetching /api/auth/me...');

    final user = await getCurrentUser();
    if (user == null) {
      print('[OIDC Repo Error] Failed to fetch user profile from /api/auth/me');
      throw Exception('Failed to fetch user profile after OIDC login');
    }
    print('[OIDC Repo Success] User profile loaded: ${user.username} (${user.id})');
    return user;
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
    if (token == null || token.isEmpty) {
      print('[OIDC Repo getCurrentUser] No token in storage');
      return null;
    }

    try {
      final response = await _dio.get(
        '/api/auth/me',
        options: Options(
          headers: {'Authorization': 'Bearer $token'},
        ),
      );
      print('[OIDC Repo getCurrentUser] /api/auth/me response: ${response.data}');
      return UserModel.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      print('[OIDC Repo getCurrentUser Error]: $e');
      return null;
    }
  }

  Future<List<UserModel>> getUsers() async {
    final response = await _dio.get('/api/users');
    final data = response.data as List;
    return data.map((json) => UserModel.fromJson(json as Map<String, dynamic>)).toList();
  }
}
