import 'package:frontend/page/login/page_login.dart';
import 'package:frontend/page/login/page_redirect_google.dart';
import 'package:go_router/go_router.dart';

class ConfigRouter {
  final GoRouter router = GoRouter(
    initialLocation: '/',
    debugLogDiagnostics: true,
    routes: [
      GoRoute(
        path: '/',
        builder: (context, state) => const PageLogin(),
      ),
      GoRoute(
        path: '/api/public/auth/redirect/google',
        name: 'googleRedirect',
        builder: (context, state) {
          print('Matched google redirect route');
          print('Current URL: ${Uri.base}');
          print('Query parameters: ${state.uri.queryParameters}');

          // Handle both direct parameters and full URL parameters
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
