import 'package:flutter/foundation.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:mplatform_mobile/core/storage/storage_service.dart';
import 'package:shared_preferences/shared_preferences.dart';

class StorageServiceImpl implements StorageService {
  final FlutterSecureStorage _secureStorage;
  final SharedPreferences _prefs;

  static const String _keyAccessToken = 'jwt_access_token';
  static const String _keyRefreshToken = 'jwt_refresh_token';
  static const String _keyTimezone = 'user_personal_timezone';

  StorageServiceImpl(this._secureStorage, this._prefs);

  @override
  Future<String?> getAccessToken() async {
    final prefToken = _prefs.getString(_keyAccessToken);
    if (prefToken != null && prefToken.isNotEmpty) {
      return prefToken;
    }
    if (!kIsWeb) {
      try {
        return await _secureStorage.read(key: _keyAccessToken);
      } catch (_) {}
    }
    return null;
  }

  @override
  Future<String?> getRefreshToken() async {
    final prefToken = _prefs.getString(_keyRefreshToken);
    if (prefToken != null && prefToken.isNotEmpty) {
      return prefToken;
    }
    if (!kIsWeb) {
      try {
        return await _secureStorage.read(key: _keyRefreshToken);
      } catch (_) {}
    }
    return null;
  }

  @override
  Future<void> saveTokens({required String accessToken, required String refreshToken}) async {
    await _prefs.setString(_keyAccessToken, accessToken);
    await _prefs.setString(_keyRefreshToken, refreshToken);
    if (!kIsWeb) {
      try {
        await _secureStorage.write(key: _keyAccessToken, value: accessToken);
        await _secureStorage.write(key: _keyRefreshToken, value: refreshToken);
      } catch (_) {}
    }
  }

  @override
  Future<void> deleteTokens() async {
    await _prefs.remove(_keyAccessToken);
    await _prefs.remove(_keyRefreshToken);
    if (!kIsWeb) {
      try {
        await _secureStorage.delete(key: _keyAccessToken);
        await _secureStorage.delete(key: _keyRefreshToken);
      } catch (_) {}
    }
  }

  @override
  Future<String> getTimezone() async {
    return _prefs.getString(_keyTimezone) ?? 'Asia/Seoul';
  }

  @override
  Future<void> saveTimezone(String timezone) async {
    await _prefs.setString(_keyTimezone, timezone);
  }

  @override
  Future<String?> getOidcVerifier() async {
    return _prefs.getString('oidc_code_verifier');
  }

  @override
  Future<void> saveOidcVerifier(String verifier) async {
    await _prefs.setString('oidc_code_verifier', verifier);
  }

  @override
  Future<void> deleteOidcVerifier() async {
    await _prefs.remove('oidc_code_verifier');
  }
}
