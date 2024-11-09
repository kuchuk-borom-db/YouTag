import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../main.dart';
import '../../models/model_video.dart';
import '../../modules/shared/services/service_storage.dart';
import '../../widgets/widget_video_card.dart';

class PageHome extends StatefulWidget {
  const PageHome({super.key});

  @override
  State<PageHome> createState() => _PageHomeState();
}

class _PageHomeState extends State<PageHome>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _fadeAnimation;
  late Animation<Offset> _slideAnimation;
  final ScrollController _tagsScrollController = ScrollController();

  // Sample video data
  final List<ModelVideo> videos = List.generate(
    20,
    (index) => ModelVideo(
      id: 'video_$index',
      title: 'Amazing Video ${index + 1}: ${_getRandomTitle()}',
      description: 'This is video description $index',
      thumbnailUrl: 'https://picsum.photos/seed/$index/300/200',
      tags: ['tag1', 'tag2', 'tag3'],
    ),
  );

  // Helper method to generate random titles
  static String _getRandomTitle() {
    final List<String> adjectives = [
      'Awesome',
      'Incredible',
      'Mind-blowing',
      'Fascinating',
      'Epic',
      'Beautiful',
      'Amazing',
      'Stunning'
    ];
    final List<String> subjects = [
      'Nature',
      'Technology',
      'Adventure',
      'Discovery',
      'Journey',
      'Experience',
      'Moment',
      'Creation'
    ];

    adjectives.shuffle();
    subjects.shuffle();
    return '${adjectives.first} ${subjects.first}';
  }

  Future<void> _handleLogout() async {
    await getIt<ServiceStorage>().removeValue("token");
    if (mounted) {
      context.go('/login');
    }
  }

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 1500),
      vsync: this,
    );

    _fadeAnimation = Tween<double>(begin: 0.0, end: 1.0).animate(
      CurvedAnimation(parent: _controller, curve: Curves.easeInOut),
    );

    _slideAnimation = Tween<Offset>(
      begin: const Offset(0, 0.5),
      end: Offset.zero,
    ).animate(CurvedAnimation(parent: _controller, curve: Curves.easeOutBack));

    _controller.forward();
  }

  @override
  void dispose() {
    _controller.dispose();
    _tagsScrollController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        children: [
          // Top Navigation Bar
          FadeTransition(
            opacity: _fadeAnimation,
            child: Container(
              padding: const EdgeInsets.all(16.0),
              decoration: BoxDecoration(
                color: Colors.black38,
                boxShadow: [
                  BoxShadow(
                    color: Colors.grey.withOpacity(0.2),
                    spreadRadius: 1,
                    blurRadius: 5,
                  ),
                ],
              ),
              child: Row(
                children: [
                  // Logo
                  Hero(
                    tag: 'logo',
                    child: Image.asset(
                      'assets/images/youtag.png',
                      height: 50,
                    ),
                  ),
                  const SizedBox(width: 20),

                  // Developer credit
                  Text(
                    'Developed by Kuchuk Borom Debbarma',
                    style: TextStyle(
                      color: Colors.grey[800],
                      fontSize: 14,
                      fontWeight: FontWeight.w500,
                    ),
                  ),

                  const Spacer(),

                  // Search Icon Button
                  IconButton(
                    icon: const Icon(Icons.search, size: 28),
                    onPressed: () {
                      // Navigation will be handled later
                      if (kDebugMode) {
                        print('Navigate to search page');
                      }
                    },
                    tooltip: 'Search',
                  ),
                  const SizedBox(width: 20),

                  // User Profile
                  CircleAvatar(
                    radius: 20,
                    backgroundColor: Colors.blue[100],
                    child: const Icon(Icons.person, color: Colors.blue),
                  ),
                ],
              ),
            ),
          ),

          // Main Content
          Expanded(
            child: SlideTransition(
              position: _slideAnimation,
              child: FadeTransition(
                opacity: _fadeAnimation,
                child: Column(
                  children: [
                    // Tags Section
                    Container(
                      height: 60,
                      padding: const EdgeInsets.symmetric(horizontal: 16),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        boxShadow: [
                          BoxShadow(
                            color: Colors.grey.withOpacity(0.1),
                            spreadRadius: 1,
                            blurRadius: 3,
                          ),
                        ],
                      ),
                      child: MouseRegion(
                        child: Scrollbar(
                          controller: _tagsScrollController,
                          thumbVisibility: true,
                          child: ListView.builder(
                            controller: _tagsScrollController,
                            scrollDirection: Axis.horizontal,
                            itemCount: 20,
                            itemBuilder: (context, index) {
                              return Padding(
                                padding: const EdgeInsets.symmetric(
                                  vertical: 10,
                                  horizontal: 4,
                                ),
                                child: Material(
                                  color: Colors.transparent,
                                  child: InkWell(
                                    onTap: () {},
                                    borderRadius: BorderRadius.circular(20),
                                    child: Container(
                                      padding: const EdgeInsets.symmetric(
                                        horizontal: 16,
                                        vertical: 8,
                                      ),
                                      decoration: BoxDecoration(
                                        color: Colors.primaries[index %
                                            Colors.primaries.length][100],
                                        borderRadius: BorderRadius.circular(20),
                                        boxShadow: [
                                          BoxShadow(
                                            color: Colors.grey.withOpacity(0.1),
                                            spreadRadius: 1,
                                            blurRadius: 2,
                                          ),
                                        ],
                                      ),
                                      child: Text(
                                        'Tag ${index + 1}',
                                        style: TextStyle(
                                          color: Colors.primaries[index %
                                              Colors.primaries.length][900],
                                          fontWeight: FontWeight.w500,
                                        ),
                                      ),
                                    ),
                                  ),
                                ),
                              );
                            },
                          ),
                        ),
                      ),
                    ),

                    // Videos Grid with VideoCard
                    Expanded(
                      child: Stack(
                        children: [
                          Scrollbar(
                            child: Padding(
                              padding: const EdgeInsets.all(16.0),
                              child: GridView.builder(
                                gridDelegate:
                                    const SliverGridDelegateWithMaxCrossAxisExtent(
                                  maxCrossAxisExtent: 300,
                                  childAspectRatio: 16 / 9,
                                  crossAxisSpacing: 16,
                                  mainAxisSpacing: 16,
                                ),
                                itemCount: videos.length,
                                itemBuilder: (context, index) {
                                  return VideoCard(
                                    video: videos[index],
                                    onTap: () {
                                      if (kDebugMode) {
                                        print(
                                            'Tapped video: ${videos[index].id}');
                                      }
                                      // Handle video tap
                                    },
                                  );
                                },
                              ),
                            ),
                          ),

                          // Logout button overlay
                          Positioned(
                            right: 16,
                            bottom: 16,
                            child: ElevatedButton.icon(
                              onPressed: _handleLogout,
                              icon: const Icon(Icons.logout),
                              label: const Text('Logout'),
                              style: ElevatedButton.styleFrom(
                                backgroundColor: Colors.red[400],
                                foregroundColor: Colors.white,
                                padding: const EdgeInsets.symmetric(
                                  horizontal: 20,
                                  vertical: 12,
                                ),
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(8),
                                ),
                                elevation: 2,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
