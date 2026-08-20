abstract class StorageService {
  Future<String?> getAccessToken();
  Future<String?> getRefreshToken();
  Future<void> saveTokens({required String accessToken, required String refreshToken});
  Future<void> deleteTokens();
  Future<String> getTimezone();
  Future<String?> getOidcVerifier();
  Future<void> saveOidcVerifier(String verifier);
  Future<void> deleteOidcVerifier();
}
