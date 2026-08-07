import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/features/auth/data/repositories/auth_repository.dart';
import 'package:mplatform_mobile/features/auth/domain/models/auth_response.dart';
import 'package:mplatform_mobile/features/auth/domain/models/user_model.dart';
import 'package:mplatform_mobile/features/auth/presentation/providers/auth_provider.dart';
import 'package:mplatform_mobile/features/auth/presentation/screens/login_screen.dart';

import 'login_screen_test.mocks.dart';

@GenerateMocks([AuthRepository])
void main() {
  group('LoginScreen Widget Tests (TDD)', () {
    late MockAuthRepository mockAuthRepository;

    setUp(() {
      mockAuthRepository = MockAuthRepository();
      when(mockAuthRepository.getCurrentUser()).thenAnswer((_) async => null);
    });

    Widget createTestWidget({VoidCallback? onLoginSuccess}) {
      return ProviderScope(
        overrides: [
          authRepositoryProvider.overrideWithValue(mockAuthRepository),
        ],
        child: MaterialApp(
          localizationsDelegates: const [
            AppLocalizations.delegate,
            GlobalMaterialLocalizations.delegate,
            GlobalWidgetsLocalizations.delegate,
            GlobalCupertinoLocalizations.delegate,
          ],
          supportedLocales: const [Locale('ko'), Locale('en')],
          locale: const Locale('ko'),
          home: LoginScreen(onLoginSuccess: onLoginSuccess),
        ),
      );
    }

    testWidgets('renders username and password inputs with localized text', (WidgetTester tester) async {
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();

      expect(find.text('아이디'), findsOneWidget);
      expect(find.text('로그인'), findsWidgets);
      expect(find.byType(TextField), findsNWidgets(2));
    });

    testWidgets('calls login and triggers callback on successful login', (WidgetTester tester) async {
      bool loginCallbackCalled = false;
      const mockUser = UserModel(id: '1', username: 'tester', name: 'Tester', role: 'USER');
      const mockResponse = AuthResponse(accessToken: 'token', refreshToken: 'refresh', user: mockUser);

      when(mockAuthRepository.login(username: 'tester', password: 'password123')).thenAnswer((_) async => mockResponse);

      await tester.pumpWidget(createTestWidget(onLoginSuccess: () {
        loginCallbackCalled = true;
      }));
      await tester.pumpAndSettle();

      final textFields = find.byType(TextField);
      await tester.enterText(textFields.first, 'tester');
      await tester.enterText(textFields.last, 'password123');
      await tester.pump();

      final loginButton = find.byType(ElevatedButton);
      await tester.tap(loginButton);
      await tester.pump(); // Start async login
      await tester.pumpAndSettle(); // Complete login

      expect(loginCallbackCalled, isTrue);
      verify(mockAuthRepository.login(username: 'tester', password: 'password123')).called(1);
    });
  });
}
