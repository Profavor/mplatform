abstract class StorageService {
  Future<String?> getAccessToken();
  Future<String?> getRefreshToken();
  Future<void> saveTokens({required String accessToken, required String refreshToken});
  Future<void> deleteTokens();
  Future<String> getTimezone();
  Future<void> saveTimezone(String timezone);
}
