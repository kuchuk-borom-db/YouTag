import 'package:flutter/material.dart';
import 'package:frontend/page/login/page_redirect_google.dart';
import 'package:go_router/go_router.dart';

class ConfigRouter {
  final GoRouter router = GoRouter(
    routes: [
      GoRoute(
        path: '/',
        builder: (context, state) => const Text("HomePage"),
      ),
      GoRoute(
        // This should match your redirect path
        path: '/api/public/auth/redirect/google',
        builder: (context, state) => PageRedirectGoogle(
          queryParameters: state.uri.queryParameters,
        ),
      ),
    ],
  );
}
