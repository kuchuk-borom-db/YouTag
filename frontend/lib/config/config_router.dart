import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:frontend/page/page_profile.dart';
import 'package:go_router/go_router.dart';

import '../../../models/model_video.dart';
import '../../../page/page_home.dart';
import '../../../page/page_login.dart';
import '../../../page/page_redirect_google.dart';
import '../../../page/page_search.dart';
import '../../../page/page_video.dart';
import '../main.dart';
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
        path: '/profile',
        builder: (context, state) => const PageProfile(),
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
      GoRoute(
        path: '/video',
        builder: (context, state) {
          ModelVideo? video;

          // If extra failed, try to construct from query parameters
          if (state.uri.queryParameters.isNotEmpty) {
            try {
              video = ModelVideo(
                id: state.uri.queryParameters['id'] ?? '',
                title: Uri.decodeComponent(
                    state.uri.queryParameters['title'] ?? ''),
                thumbnailUrl: Uri.decodeComponent(
                    state.uri.queryParameters['thumbnailUrl'] ?? ''),
                description: Uri.decodeComponent(
                    state.uri.queryParameters['description'] ?? ''),
                tags: (state.uri.queryParameters['tags'] ?? '')
                    .split(',')
                    .where((tag) => tag.isNotEmpty)
                    .toList(),
              );
            } catch (e) {
              if (kDebugMode) {
                print('Failed to construct video from query parameters: $e');
              }
            }
          }
          if (video == null) {
            return Scaffold(
              body: Center(
                child: Text(
                  'Video not found',
                  style: Theme.of(context).textTheme.headlineMedium,
                ),
              ),
            );
          }
          return PageVideo(video: video);
        },
      ),
      GoRoute(
        path: '/search',
        builder: (context, state) {
          final params = state.uri.queryParameters;
          final tags = params['tags']
                  ?.split(',')
                  .where((tag) => tag.isNotEmpty)
                  .toList() ??
              [];
          final titles = params['titles']
                  ?.split(',')
                  .where((title) => title.isNotEmpty)
                  .toList() ??
              [];
          final ids =
              params['ids']?.split(',').where((id) => id.isNotEmpty).toList() ??
                  [];

          return PageSearch(
            initialTags: tags,
            initialTitles: titles,
            initialIds: ids,
            autoSearch: tags.isNotEmpty || titles.isNotEmpty || ids.isNotEmpty,
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
