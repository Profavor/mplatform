import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';
import 'package:mplatform_mobile/core/providers/locale_provider.dart';
import 'package:mplatform_mobile/features/auth/data/repositories/auth_repository.dart';
import 'package:mplatform_mobile/features/auth/domain/models/user_model.dart';
import 'package:mplatform_mobile/features/auth/presentation/providers/auth_provider.dart';
import 'package:mplatform_mobile/features/profile/presentation/screens/profile_settings_screen.dart';
import 'package:shared_preferences/shared_preferences.dart';

class FakeAuthRepo implements AuthRepository {
  @override
  Future<List<UserModel>> getUsers() async => [];
  @override
  noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeAuthController extends AuthController {
  _FakeAuthController(super.repo, AsyncValue<UserModel?> initialState) {
    state = initialState;
  }
  @override
  Future<void> checkAuthStatus() async {}
}

void main() {
  group('ProfileSettingsScreen Widget Tests (TDD - Locale, Timezone, User Info)', () {
    late SharedPreferences sharedPreferences;

    setUp(() async {
      SharedPreferences.setMockInitialValues({
        'user_personal_timezone': 'Asia/Seoul',
      });
      sharedPreferences = await SharedPreferences.getInstance();
    });

    Widget createWidgetUnderTest() {
      final fakeRepo = FakeAuthRepo();
      return ProviderScope(
        overrides: [
          sharedPreferencesProvider.overrideWithValue(sharedPreferences),
          authControllerProvider.overrideWith((ref) => _FakeAuthController(
                fakeRepo,
                const AsyncValue.data(UserModel(
                  id: 'user-uuid-1234',
                  username: 'tester01',
                  name: '테스터 홍',
                  role: 'ROLE_ADMIN',
                  department: '품질관리팀',
                )),
              )),
        ],
        child: Consumer(
          builder: (context, ref, child) {
            final locale = ref.watch(localeProvider);
            return MaterialApp(
              localizationsDelegates: const [
                AppLocalizations.delegate,
                GlobalMaterialLocalizations.delegate,
                GlobalWidgetsLocalizations.delegate,
                GlobalCupertinoLocalizations.delegate,
              ],
              supportedLocales: AppLocalizations.supportedLocales,
              locale: locale,
              home: const ProfileSettingsScreen(),
            );
          },
        ),
      );
    }

    testWidgets('renders user profile card, language switcher, and timezone selector', (tester) async {
      await tester.pumpWidget(createWidgetUnderTest());
      await tester.pumpAndSettle();

      // Verify User Information
      expect(find.text('테스터 홍'), findsOneWidget);
      expect(find.textContaining('시스템 관리자'), findsOneWidget);
      expect(find.textContaining('품질관리팀'), findsOneWidget);

      // Verify Language Radio Tiles
      expect(find.text('한국어 (Korean)'), findsOneWidget);
      expect(find.text('English'), findsOneWidget);

      // Verify Timezone dropdown
      expect(find.text('Asia/Seoul (KST, UTC+9)'), findsOneWidget);
    });
  });
}
