import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../main.dart';
import '../../../page/home/page_home.dart';
import '../../../page/login/page_login.dart';
import '../../../page/login/page_redirect_google.dart';
import '../services/service_storage.dart';

class ConfigRouter {
  GoRouter router = GoRouter(
    initialLocation: '/',
    debugLogDiagnostics: true,
    redirect: (context, state) async {
      var storageService = getIt<ServiceStorage>();

      if (state.uri.path == '/splash') {
        return null;
      }

      final isLoggedIn = await storageService.hasValue("token");
      if (kDebugMode) {
        print('Current path: ${state.uri.path}');
        print('Is logged in: $isLoggedIn');
        if (isLoggedIn) {
          final token = await storageService.getValue("token");
          print('Current token: $token');
        }
      }

      final isGoingToLogin = state.uri.path == '/login' ||
          state.uri.path == "/api/public/auth/redirect/google";

      if (!isLoggedIn && !isGoingToLogin) {
        if (kDebugMode) {
          print('Redirecting to login because not logged in');
        }
        return '/login';
      }

      if (isLoggedIn && isGoingToLogin) {
        if (kDebugMode) {
          print('Redirecting to home because already logged in');
        }
        return '/';
      }

      return null;
    },
    routes: [
      GoRoute(
        path: "/splash",
        builder: (context, state) => const SplashScreen(),
      ),
      GoRoute(
        path: "/",
        builder: (context, state) => const PageHome(),
      ),
      GoRoute(
        path: '/login',
        builder: (context, state) => const PageLogin(),
      ),
      GoRoute(
        path: '/api/public/auth/redirect/google',
        name: 'googleRedirect',
        builder: (context, state) {
          if (kDebugMode) {
            print('Matched google redirect route');
            print('Current URL: ${Uri.base}');
            print('Query parameters: ${state.uri.queryParameters}');
          }

          final params = state.uri.queryParameters.isNotEmpty
              ? state.uri.queryParameters
              : Uri.base.queryParameters;
          return PageRedirectGoogle(
            queryParameters: params,
          );
        },
      ),
    ],
  );
}

class SplashScreen extends StatelessWidget {
  const SplashScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      body: Center(
        child: CircularProgressIndicator(),
      ),
    );
  }
}
