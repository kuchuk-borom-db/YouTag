import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

class PageRedirectGoogle extends StatelessWidget {
  final Map<String, String> queryParameters;

  const PageRedirectGoogle({
    super.key,
    required this.queryParameters,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('OAuth Redirect Results'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'OAuth Parameters Received:',
              style: TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 20),
            if (queryParameters.isEmpty)
              const Text('No parameters received')
            else
              ...queryParameters.entries.map((entry) => Card(
                    margin: const EdgeInsets.only(bottom: 8),
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            entry.key,
                            style: const TextStyle(
                              fontWeight: FontWeight.bold,
                              fontSize: 16,
                            ),
                          ),
                          const SizedBox(height: 8),
                          Text(
                            entry.value,
                            style: const TextStyle(
                              fontSize: 14,
                            ),
                          ),
                        ],
                      ),
                    ),
                  )),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: () {
                // Navigate back to home or to your main app screen
                context.go('/');
              },
              child: const Text('Back to Home'),
            ),
            const SizedBox(height: 20),
            // Debug information
            const Text(
              'Raw Query String:',
              style: TextStyle(
                fontWeight: FontWeight.bold,
              ),
            ),
            Text(Uri.base.toString()),
          ],
        ),
      ),
    );
  }
}
