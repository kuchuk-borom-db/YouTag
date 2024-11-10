import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:get_it/get_it.dart';
import 'package:go_router/go_router.dart';

import '../models/model_user.dart';
import '../services/service_user.dart';

class CustomNavBar extends StatefulWidget implements PreferredSizeWidget {
  final VoidCallback onSearchTap;
  final VoidCallback onLogoTap;
  final VoidCallback onProfileTap;

  const CustomNavBar({
    super.key,
    required this.onProfileTap,
    required this.onSearchTap,
    required this.onLogoTap,
  });

  @override
  Size get preferredSize => const Size.fromHeight(56.0);

  @override
  State<CustomNavBar> createState() => _CustomNavBarState();
}

class _CustomNavBarState extends State<CustomNavBar> {
  final ServiceUser _serviceUser = GetIt.instance<ServiceUser>();
  ModelUser? _currentUser;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
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

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: const Color(0xFF0F0F0F),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.1),
            blurRadius: 4,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: SafeArea(
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 16.0),
          height: 56.0,
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              // Logo section
              MouseRegion(
                cursor: SystemMouseCursors.click,
                child: GestureDetector(
                  onTap: widget.onLogoTap,
                  child: Row(
                    children: [
                      Image.asset(
                        'assets/images/youtag.png',
                        width: 32,
                        height: 32,
                      ),
                      const SizedBox(width: 8),
                      Text(
                        'YouTAG',
                        style: Theme.of(context).textTheme.titleLarge?.copyWith(
                              color: Colors.white,
                              fontWeight: FontWeight.bold,
                            ),
                      ),
                    ],
                  ),
                ),
              ),

              // Right side buttons
              Row(
                children: [
                  // Search button with hover effect
                  _NavBarIconButton(
                    icon: Icons.search,
                    onTap: widget.onSearchTap,
                    tooltip: 'Search',
                  ),
                  const SizedBox(width: 16),

                  // Profile avatar with hover effect
                  MouseRegion(
                    cursor: SystemMouseCursors.click,
                    child: GestureDetector(
                      onTap: widget.onProfileTap,
                      child: Container(
                        width: 32,
                        height: 32,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          border: Border.all(
                            color: Colors.white.withOpacity(0.1),
                            width: 2,
                          ),
                        ),
                        child: ClipOval(
                          child: _buildProfileImage(),
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildProfileImage() {
    if (_isLoading) {
      return _buildLoadingIndicator();
    }

    if (_currentUser?.thumbnail != null) {
      try {
        return Image.memory(
          _currentUser!.thumbnail,
          fit: BoxFit.cover,
          errorBuilder: (context, error, stackTrace) {
            if (kDebugMode) {
              print('Error loading profile image: $error');
            }
            return _buildDefaultAvatar();
          },
        );
      } catch (e) {
        if (kDebugMode) {
          print('Exception while displaying profile image: $e');
        }
        return _buildDefaultAvatar();
      }
    }

    return _buildDefaultAvatar();
  }

  Widget _buildLoadingIndicator() {
    return Container(
      color: Colors.grey[800],
      child: const Center(
        child: SizedBox(
          width: 16,
          height: 16,
          child: CircularProgressIndicator(
            strokeWidth: 2,
            valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
          ),
        ),
      ),
    );
  }

  Widget _buildDefaultAvatar() {
    return Container(
      color: Colors.grey[800],
      child: const Icon(
        Icons.person,
        color: Colors.white,
        size: 20,
      ),
    );
  }
}

// Custom icon button with hover effect (unchanged)
class _NavBarIconButton extends StatefulWidget {
  final IconData icon;
  final VoidCallback onTap;
  final String tooltip;

  const _NavBarIconButton({
    required this.icon,
    required this.onTap,
    required this.tooltip,
  });

  @override
  _NavBarIconButtonState createState() => _NavBarIconButtonState();
}

class _NavBarIconButtonState extends State<_NavBarIconButton> {
  bool isHovered = false;

  @override
  Widget build(BuildContext context) {
    return MouseRegion(
      cursor: SystemMouseCursors.click,
      onEnter: (_) => setState(() => isHovered = true),
      onExit: (_) => setState(() => isHovered = false),
      child: Tooltip(
        message: widget.tooltip,
        child: InkWell(
          onTap: widget.onTap,
          borderRadius: BorderRadius.circular(50),
          child: AnimatedContainer(
            duration: const Duration(milliseconds: 200),
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: isHovered ? Colors.grey[800] : Colors.transparent,
              shape: BoxShape.circle,
            ),
            child: Icon(
              widget.icon,
              color: Colors.white,
              size: 24,
            ),
          ),
        ),
      ),
    );
  }
}
