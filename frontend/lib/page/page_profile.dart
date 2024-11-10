import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../main.dart';
import '../models/model_user.dart';
import '../services/service_storage.dart';
import '../services/service_user.dart';
import '../widgets/widget_custom_navbar.dart';

class PageProfile extends StatefulWidget {
  const PageProfile({super.key});

  @override
  State<PageProfile> createState() => _PageProfileState();
}

class _PageProfileState extends State<PageProfile>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _fadeAnimation;
  late Animation<Offset> _slideAnimation;

  final ServiceUser _serviceUser = getIt<ServiceUser>();
  ModelUser? _currentUser;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 1500),
      vsync: this,
    );

    _fadeAnimation = Tween<double>(begin: 0.0, end: 1.0).animate(
      CurvedAnimation(parent: _controller, curve: Curves.easeIn),
    );

    _slideAnimation = Tween<Offset>(
      begin: const Offset(0, 0.5),
      end: Offset.zero,
    ).animate(CurvedAnimation(parent: _controller, curve: Curves.easeOutCubic));

    _loadUserInfo();
  }

  Future<void> _loadUserInfo() async {
    setState(() => _isLoading = true);
    try {
      final user = await _serviceUser.getUserInfo();
      setState(() {
        _currentUser = user;
        _isLoading = false;
      });
      _controller.forward();
    } catch (e) {
      setState(() {
        _currentUser = null;
        _isLoading = false;
      });
      await Future.delayed(const Duration(seconds: 1));
      if (mounted) {
        context.go("/login");
      }
    }
  }

  Future<Widget> loadImageWithRetry(String imageUrl,
      {int maxRetries = 3}) async {
    int attempts = 0;
    while (attempts < maxRetries) {
      try {
        return Image.network(
          imageUrl,
          fit: BoxFit.cover,
          errorBuilder: (context, error, stackTrace) {
            throw error; // Propagate the error to trigger retry
          },
        );
      } catch (e) {
        attempts++;
        if (attempts == maxRetries) {
          return Image.asset(
            'assets/images/youtag.png',
            fit: BoxFit.cover,
          );
        }
        await Future.delayed(Duration(seconds: attempts * 2));
      }
    }
    return Image.asset(
      'assets/images/youtag.png',
      fit: BoxFit.cover,
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const Scaffold(
        body: Center(
          child: CircularProgressIndicator(),
        ),
      );
    }

    return Scaffold(
      appBar: CustomNavBar(
        onSearchTap: () {
          // Implement search functionality or navigation
        },
        onLogoTap: () => context.go('/'),
        onProfileTap: () {
          // Already on profile page, can be no-op or implement specific behavior
        },
      ),
      body: Stack(
        children: [
          Center(
            child: SingleChildScrollView(
              child: Column(
                children: [
                  SlideTransition(
                    position: _slideAnimation,
                    child: FadeTransition(
                      opacity: _fadeAnimation,
                      child: Container(
                        constraints: const BoxConstraints(maxWidth: 600),
                        padding: const EdgeInsets.all(24.0),
                        child: Column(
                          children: [
                            // Profile Picture
                            Container(
                              width: 150,
                              height: 150,
                              decoration: BoxDecoration(
                                shape: BoxShape.circle,
                                border: Border.all(color: Colors.red, width: 3),
                                boxShadow: [
                                  BoxShadow(
                                    color: Colors.red.withOpacity(0.3),
                                    blurRadius: 15,
                                    spreadRadius: 5,
                                  ),
                                ],
                              ),
                              child: ClipOval(
                                child: _currentUser?.thumbnail != null
                                    ? Container(
                                        width: 150,
                                        height: 150,
                                        decoration: BoxDecoration(
                                          shape: BoxShape.circle,
                                          border: Border.all(
                                              color: Colors.red, width: 3),
                                          boxShadow: [
                                            BoxShadow(
                                              color:
                                                  Colors.red.withOpacity(0.3),
                                              blurRadius: 15,
                                              spreadRadius: 5,
                                            ),
                                          ],
                                        ),
                                        child: ClipOval(
                                          child: _currentUser?.thumbnail != null
                                              ? _buildProfileImage()
                                              : Image.asset(
                                                  'assets/images/youtag.png',
                                                  fit: BoxFit.cover,
                                                ),
                                        ),
                                      )
                                    : Image.asset(
                                        'assets/images/youtag.png',
                                        fit: BoxFit.cover,
                                      ),
                              ),
                            ),
                            const SizedBox(height: 24),
                            // Name
                            Text(
                              _currentUser?.name ?? 'Unknown User',
                              style: Theme.of(context)
                                  .textTheme
                                  .headlineMedium
                                  ?.copyWith(
                                    color: Colors.white,
                                    fontWeight: FontWeight.bold,
                                  ),
                            ),
                            const SizedBox(height: 8),
                            // Email
                            Text(
                              _currentUser?.email ?? 'No email available',
                              style: Theme.of(context)
                                  .textTheme
                                  .titleMedium
                                  ?.copyWith(
                                    color: Colors.grey[400],
                                  ),
                            ),
                            const SizedBox(height: 32),
                            // Logout Button
                            ElevatedButton.icon(
                              onPressed: _handleLogout,
                              style: ElevatedButton.styleFrom(
                                backgroundColor: Colors.red,
                                padding: const EdgeInsets.symmetric(
                                  horizontal: 32,
                                  vertical: 16,
                                ),
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(30),
                                ),
                              ),
                              icon:
                                  const Icon(Icons.logout, color: Colors.white),
                              label: const Text(
                                'Logout',
                                style: TextStyle(
                                  fontSize: 16,
                                  color: Colors.white,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                  Text(
                    'Developed by Kuchuk Borom Debbarma',
                    style: TextStyle(
                      color: Colors.grey[400],
                      fontSize: 14,
                    ),
                  ),
                ],
              ),
            ),
          ),
          // Credit text positioned at the bottom
        ],
      ),
    );
  }

  Future<void> _handleLogout() async {
    await getIt<ServiceStorage>().removeValue("token");
    if (mounted) {
      context.go('/login');
    }
  }

  Widget _buildProfileImage() {
    if (_currentUser?.thumbnail != null) {
      try {
        return Image.memory(
          _currentUser!.thumbnail,
          fit: BoxFit.cover,
          errorBuilder: (context, error, stackTrace) {
            if (kDebugMode) {
              print('Error loading profile image: $error');
            }
            return Image.asset(
              'assets/images/youtag.png',
              fit: BoxFit.cover,
            );
          },
        );
      } catch (e) {
        if (kDebugMode) {
          print('Exception while displaying profile image: $e');
        }
        return Image.asset(
          'assets/images/youtag.png',
          fit: BoxFit.cover,
        );
      }
    }
    return Image.asset(
      'assets/images/youtag.png',
      fit: BoxFit.cover,
    );
  }
}
