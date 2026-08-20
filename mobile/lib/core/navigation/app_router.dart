import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mplatform_mobile/features/auth/presentation/providers/auth_provider.dart';
import 'package:mplatform_mobile/features/auth/presentation/screens/login_screen.dart';
import 'package:mplatform_mobile/features/navigation/presentation/screens/main_navigation_screen.dart';

import 'package:mplatform_mobile/features/inbox/presentation/screens/inbox_compose_screen.dart';
import 'package:mplatform_mobile/features/inbox/presentation/screens/inbox_detail_screen.dart';
import 'package:mplatform_mobile/features/inbox/presentation/screens/inbox_screen.dart';
import 'package:mplatform_mobile/features/profile/presentation/screens/profile_settings_screen.dart';

class RouterNotifier extends ChangeNotifier {
  final Ref _ref;
  RouterNotifier(this._ref) {
    _ref.listen(authControllerProvider, (_, __) {
      notifyListeners();
    });
  }
}

final routerNotifierProvider = Provider<RouterNotifier>((ref) {
  return RouterNotifier(ref);
});

final appRouterProvider = Provider<GoRouter>((ref) {
  final notifier = ref.watch(routerNotifierProvider);
  final authState = ref.watch(authControllerProvider);
  final isLoggedIn = authState.valueOrNull != null;

  return GoRouter(
    initialLocation: isLoggedIn ? '/main' : '/login',
    refreshListenable: notifier,
    routes: [
      GoRoute(
        path: '/',
        builder: (context, state) {
          if (isLoggedIn) {
            return const MainNavigationScreen();
          }
          return LoginScreen(
            onLoginSuccess: () {
              context.go('/main');
            },
          );
        },
      ),
      GoRoute(
        path: '/login',
        builder: (context, state) => LoginScreen(
          onLoginSuccess: () {
            context.go('/main');
          },
        ),
      ),
      GoRoute(
        path: '/main',
        builder: (context, state) => const MainNavigationScreen(),
      ),
      GoRoute(
        path: '/inbox',
        builder: (context, state) => const InboxScreen(),
      ),
      GoRoute(
        path: '/inbox/detail/:id',
        builder: (context, state) {
          final id = state.pathParameters['id'] ?? '';
          return InboxDetailScreen(messageId: id);
        },
      ),
      GoRoute(
        path: '/inbox/compose',
        builder: (context, state) => const InboxComposeScreen(),
      ),
      GoRoute(
        path: '/profile',
        builder: (context, state) => const ProfileSettingsScreen(),
      ),
    ],
    redirect: (BuildContext context, GoRouterState state) {
      final hasAuthCode = state.uri.queryParameters.containsKey('code') || (kIsWeb && Uri.base.queryParameters.containsKey('code'));
      final isLoginPage = state.matchedLocation == '/login' || state.matchedLocation == '/';
      final isLoggingIn = isLoginPage || hasAuthCode;

      if (!isLoggedIn && !isLoggingIn) {
        return '/login';
      }
      if (isLoggedIn && (state.matchedLocation == '/login' || state.matchedLocation == '/')) {
        return '/main';
      }
      return null;
    },
  );
});
