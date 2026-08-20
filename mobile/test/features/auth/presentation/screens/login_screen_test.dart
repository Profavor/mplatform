import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:mplatform_mobile/core/auth/oidc_service.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';
import 'package:mplatform_mobile/features/auth/data/repositories/auth_repository.dart';
import 'package:mplatform_mobile/features/auth/presentation/providers/auth_provider.dart';
import 'package:mplatform_mobile/features/auth/presentation/screens/login_screen.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'login_screen_test.mocks.dart';

@GenerateMocks([AuthRepository, OidcService])
void main() {
  group('LoginScreen Widget Tests (TDD - Pure Keycloak SSO Only)', () {
    late MockAuthRepository mockAuthRepo;
    late MockOidcService mockOidcService;
    late SharedPreferences prefs;

    setUp(() async {
      SharedPreferences.setMockInitialValues({});
      prefs = await SharedPreferences.getInstance();
      mockAuthRepo = MockAuthRepository();
      mockOidcService = MockOidcService();

      when(mockAuthRepo.getCurrentUser()).thenAnswer((_) async => null);
    });

    Widget createTestWidget({VoidCallback? onLoginSuccess}) {
      return ProviderScope(
        overrides: [
          sharedPreferencesProvider.overrideWithValue(prefs),
          authRepositoryProvider.overrideWithValue(mockAuthRepo),
          oidcServiceProvider.overrideWithValue(mockOidcService),
        ],
        child: MaterialApp(
          localizationsDelegates: const [
            AppLocalizations.delegate,
            GlobalMaterialLocalizations.delegate,
            GlobalWidgetsLocalizations.delegate,
            GlobalCupertinoLocalizations.delegate,
          ],
          supportedLocales: AppLocalizations.supportedLocales,
          locale: const Locale('ko'),
          home: LoginScreen(onLoginSuccess: onLoginSuccess),
        ),
      );
    }

    testWidgets('renders Keycloak SSO button cleanly without standard username/password input fields', (tester) async {
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();

      // Verify Keycloak SSO Button is present
      expect(find.text('Keycloak SSO로 로그인'), findsOneWidget);

      // Verify standard input fields are NOT present
      expect(find.byType(TextField), findsNothing);
      expect(find.text('아이디'), findsNothing);
      expect(find.text('비밀번호'), findsNothing);

      // Verify Copyright
      expect(find.text('© 2026 Domain System. All rights reserved.'), findsOneWidget);
    });

    testWidgets('tapping Keycloak SSO button generates PKCE parameters and constructs auth URL', (tester) async {
      when(mockOidcService.generateCodeVerifier()).thenReturn('mock_verifier_xyz');
      when(mockOidcService.generateCodeChallenge('mock_verifier_xyz')).thenReturn('mock_challenge_abc');
      when(mockOidcService.buildAuthorizationUrl(
        issuer: anyNamed('issuer'),
        clientId: anyNamed('clientId'),
        redirectUri: anyNamed('redirectUri'),
        codeChallenge: anyNamed('codeChallenge'),
        scope: anyNamed('scope'),
      )).thenReturn('http://localhost:8081/realms/mplatform/protocol/openid-connect/auth');

      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();

      await tester.tap(find.text('Keycloak SSO로 로그인'));
      await tester.pump(const Duration(milliseconds: 100));

      verify(mockOidcService.generateCodeVerifier()).called(1);
      verify(mockOidcService.generateCodeChallenge('mock_verifier_xyz')).called(1);
      verify(mockOidcService.buildAuthorizationUrl(
        issuer: anyNamed('issuer'),
        clientId: anyNamed('clientId'),
        redirectUri: anyNamed('redirectUri'),
        codeChallenge: 'mock_challenge_abc',
        scope: anyNamed('scope'),
      )).called(1);
    });
  });
}
