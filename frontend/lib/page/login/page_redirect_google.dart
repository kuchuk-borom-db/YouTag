import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:frontend/main.dart';
import 'package:frontend/modules/auth/api/service_auth.dart';
import 'package:frontend/modules/shared/services/service_cookie.dart';

class PageRedirectGoogle extends StatefulWidget {
  final Map<String, String> queryParameters;

  const PageRedirectGoogle({
    super.key,
    required this.queryParameters,
  });

  @override
  State<PageRedirectGoogle> createState() => _PageRedirectGoogleState();
}

class _PageRedirectGoogleState extends State<PageRedirectGoogle> {
  bool _isLoading = true;
  String? _error;
  Map<String, dynamic>? _response;

  @override
  void initState() {
    super.initState();
    _exchangeToken();
  }

  Future<void> _exchangeToken() async {
    var auth = getIt<ServiceAuth>();
    if (widget.queryParameters["code"] == null ||
        widget.queryParameters["state"] == null) {
      setState(() {
        _error = "Code and/or state missing from query parameter";
        _isLoading = false;
      });
      return;
    }

    try {
      final token = await auth.exchangeGoogleTokenForJWTToken(
          widget.queryParameters["code"]!, widget.queryParameters["state"]!);

      // Save the token
      getIt<ServiceCookie>().saveCookie("token", token);

      // Update state to show success
      setState(() {
        _response = {
          'message': 'Successfully authenticated with Google',
          'data': token,
        };
        _isLoading = false;
      });

      // Optional: Add navigation logic here
      // For example, after a short delay:
      // Future.delayed(const Duration(seconds: 2), () {
      //   Navigator.of(context).pushReplacementNamed('/home');
      // });
    } catch (e) {
      if (kDebugMode) {
        print('Error during token exchange: $e');
      }
      setState(() {
        _error = 'An error occurred while connecting to the server.';
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Container(
          constraints: const BoxConstraints(maxWidth: 400),
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              // Google Logo
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: Colors.white,
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.1),
                      blurRadius: 10,
                      spreadRadius: 1,
                    ),
                  ],
                ),
                child: const Icon(
                  Icons.g_mobiledata,
                  size: 48,
                  color: Colors.blue,
                ),
              ),
              const SizedBox(height: 32),

              if (_isLoading) ...[
                const CircularProgressIndicator(),
                const SizedBox(height: 24),
                const Text(
                  'Exchanging OAuth credentials for JWT Token',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 12),
                const Text(
                  'Please wait while we complete your sign-in',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    color: Colors.grey,
                  ),
                ),
              ] else if (_error != null) ...[
                const Icon(
                  Icons.error_outline,
                  color: Colors.red,
                  size: 48,
                ),
                const SizedBox(height: 24),
                Text(
                  _error!,
                  textAlign: TextAlign.center,
                  style: const TextStyle(
                    color: Colors.red,
                    fontSize: 16,
                  ),
                ),
                const SizedBox(height: 24),
                ElevatedButton(
                  onPressed: () {
                    setState(() {
                      _error = null;
                      _isLoading = true;
                    });
                    _exchangeToken();
                  },
                  child: const Text('Try Again'),
                ),
              ] else if (_response != null) ...[
                const Icon(
                  Icons.check_circle_outline,
                  color: Colors.green,
                  size: 48,
                ),
                const SizedBox(height: 24),
                const Text(
                  'Authentication Successful!',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                    color: Colors.green,
                  ),
                ),
                const SizedBox(height: 12),
                Text(
                  _response!['message'] ?? 'Redirecting you to the app...',
                  textAlign: TextAlign.center,
                  style: const TextStyle(
                    color: Colors.grey,
                  ),
                ),
              ],

              if (kDebugMode && _response != null) ...[
                const SizedBox(height: 24),
                const Divider(),
                const SizedBox(height: 24),
                const Text(
                  'Debug Information',
                  style: TextStyle(
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 12),
                Text(
                  'JWT Token: ${_response!['data']}',
                  style: const TextStyle(
                    fontSize: 12,
                    fontFamily: 'monospace',
                  ),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}
