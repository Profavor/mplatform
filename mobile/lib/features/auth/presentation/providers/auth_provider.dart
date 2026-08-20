import 'dart:ui';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';
import 'package:mplatform_mobile/features/auth/data/repositories/auth_repository.dart';
import 'package:mplatform_mobile/features/auth/domain/models/user_model.dart';

final authRepositoryProvider = Provider<AuthRepository>((ref) {
  final dio = ref.watch(dioProvider);
  final storageService = ref.watch(storageServiceProvider);
  return AuthRepository(dio, storageService);
});

class AuthController extends StateNotifier<AsyncValue<UserModel?>> {
  final AuthRepository _repository;
  final StateController<VoidCallback?>? _onExpiredController;

  AuthController(this._repository, [this._onExpiredController]) : super(const AsyncValue.loading()) {
    Future.microtask(() {
      _onExpiredController?.state = forceUnauthenticated;
    });
    checkAuthStatus();
  }

  Future<void> checkAuthStatus() async {
    state = const AsyncValue.loading();
    try {
      final user = await _repository.getCurrentUser();
      state = AsyncValue.data(user);
    } catch (e, st) {
      state = AsyncValue.data(null);
    }
  }

  Future<bool> login(String username, String password) async {
    state = const AsyncValue.loading();
    try {
      final response = await _repository.login(username: username, password: password);
      state = AsyncValue.data(response.user);
      return true;
    } catch (e, st) {
      print('LOGIN ERROR: $e');
      print('STACK TRACE: $st');
      state = AsyncValue.error(e, st);
      return false;
    }
  }

  Future<bool> loginWithOidc({
    required String authCode,
    required String codeVerifier,
    String? tokenEndpoint,
    String? clientId,
    String? redirectUri,
  }) async {
    state = const AsyncValue.loading();
    try {
      final user = await _repository.loginWithOidc(
        authCode: authCode,
        codeVerifier: codeVerifier,
        tokenEndpoint: tokenEndpoint ?? 'http://localhost:8081/realms/mplatform/protocol/openid-connect/token',
        clientId: clientId ?? 'mdm-mobile',
        redirectUri: redirectUri ?? 'mplatform://oauth2redirect',
      );
      state = AsyncValue.data(user);
      return true;
    } catch (e, st) {
      state = AsyncValue.error(e, st);
      return false;
    }
  }

  Future<void> logout() async {
    state = const AsyncValue.loading();
    await _repository.logout();
    state = const AsyncValue.data(null);
  }

  void forceUnauthenticated() {
    state = const AsyncValue.data(null);
  }
}

final authControllerProvider = StateNotifierProvider<AuthController, AsyncValue<UserModel?>>((ref) {
  final repo = ref.watch(authRepositoryProvider);
  final expiredController = ref.watch(onAuthExpiredProvider.notifier);
  return AuthController(repo, expiredController);
});
