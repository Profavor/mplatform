import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mplatform_mobile/features/auth/presentation/providers/auth_provider.dart';
import 'package:mplatform_mobile/features/auth/presentation/screens/login_screen.dart';
import 'package:mplatform_mobile/features/navigation/presentation/screens/main_navigation_screen.dart';

final appRouterProvider = Provider<GoRouter>((ref) {
  final authState = ref.watch(authControllerProvider);
  final isLoggedIn = authState.valueOrNull != null;

  return GoRouter(
    initialLocation: isLoggedIn ? '/main' : '/login',
    routes: [
      GoRoute(
        path: '/',
        redirect: (context, state) => isLoggedIn ? '/main' : '/login',
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
    ],
    redirect: (BuildContext context, GoRouterState state) {
      final isLoggingIn = state.uri.toString() == '/login';

      if (!isLoggedIn && !isLoggingIn) {
        return '/login';
      }
      if (isLoggedIn && isLoggingIn) {
        return '/main';
      }
      return null;
    },
  );
});
