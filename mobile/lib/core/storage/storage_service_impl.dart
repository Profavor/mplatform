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
    return await _secureStorage.read(key: _keyAccessToken);
  }

  @override
  Future<String?> getRefreshToken() async {
    return await _secureStorage.read(key: _keyRefreshToken);
  }

  @override
  Future<void> saveTokens({required String accessToken, required String refreshToken}) async {
    await _secureStorage.write(key: _keyAccessToken, value: accessToken);
    await _secureStorage.write(key: _keyRefreshToken, value: refreshToken);
  }

  @override
  Future<void> deleteTokens() async {
    await _secureStorage.delete(key: _keyAccessToken);
    await _secureStorage.delete(key: _keyRefreshToken);
  }

  @override
  Future<String> getTimezone() async {
    return _prefs.getString(_keyTimezone) ?? 'Asia/Seoul';
  }

  @override
  Future<void> saveTimezone(String timezone) async {
    await _prefs.setString(_keyTimezone, timezone);
  }
}
