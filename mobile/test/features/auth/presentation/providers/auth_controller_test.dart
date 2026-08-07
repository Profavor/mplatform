import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:mplatform_mobile/features/auth/data/repositories/auth_repository.dart';
import 'package:mplatform_mobile/features/auth/domain/models/auth_response.dart';
import 'package:mplatform_mobile/features/auth/domain/models/user_model.dart';
import 'package:mplatform_mobile/features/auth/presentation/providers/auth_provider.dart';

import 'auth_controller_test.mocks.dart';

@GenerateMocks([AuthRepository])
void main() {
  group('AuthController Tests (TDD)', () {
    late MockAuthRepository mockRepository;

    setUp(() {
      mockRepository = MockAuthRepository();
    });

    test('initializes by checking auth status and sets user when logged in', () async {
      const mockUser = UserModel(
        id: '1',
        username: 'admin',
        name: 'Admin',
        role: 'ADMIN',
      );
      when(mockRepository.getCurrentUser()).thenAnswer((_) async => mockUser);

      final controller = AuthController(mockRepository);

      // Wait for async checkAuthStatus in constructor
      await Future.delayed(const Duration(milliseconds: 50));

      expect(controller.state.value, equals(mockUser));
      expect(controller.state.isLoading, isFalse);
    });

    test('login successfully updates state with authenticated UserModel', () async {
      const mockUser = UserModel(
        id: '1',
        username: 'admin',
        name: 'Admin',
        role: 'ADMIN',
      );
      const mockAuthResponse = AuthResponse(
        accessToken: 'token',
        refreshToken: 'refresh',
        user: mockUser,
      );
      when(mockRepository.getCurrentUser()).thenAnswer((_) async => null);
      when(mockRepository.login(username: 'admin', password: 'password')).thenAnswer((_) async => mockAuthResponse);

      final controller = AuthController(mockRepository);
      await Future.delayed(const Duration(milliseconds: 20));

      final success = await controller.login('admin', 'password');

      expect(success, isTrue);
      expect(controller.state.value, equals(mockUser));
    });

    test('logout clears user state to null', () async {
      when(mockRepository.getCurrentUser()).thenAnswer((_) async => null);
      when(mockRepository.logout()).thenAnswer((_) async => {});

      final controller = AuthController(mockRepository);
      await Future.delayed(const Duration(milliseconds: 20));

      await controller.logout();

      expect(controller.state.value, isNull);
      verify(mockRepository.logout()).called(1);
    });
  });
}
