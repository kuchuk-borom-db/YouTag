import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:frontend/services/service_video.dart';
import 'package:go_router/go_router.dart';

import '../main.dart';
import '../models/model_video.dart';
import '../widgets/widget_custom_navbar.dart';
import '../widgets/widget_video_card.dart';

class PageHome extends StatefulWidget {
  const PageHome({super.key});

  @override
  State<PageHome> createState() => _PageHomeState();
}

class _PageHomeState extends State<PageHome>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _fadeAnimation;
  final ScrollController _tagsScrollController = ScrollController();
  final ServiceVideo videoService = getIt<ServiceVideo>();

  List<ModelVideo> videos = [];
  bool isLoading = false;
  String? error;
  String? noMoreVideosMessage;

  // Pagination parameters
  int currentPage = 1;
  final int limit = 2;

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

    _controller.forward();
    _loadVideos();
  }

  Future<void> _loadVideos() async {
    if (kDebugMode) {
      print("Loading videos for page: $currentPage");
    }
    if (isLoading) return;

    setState(() {
      isLoading = true;
      error = null;
      noMoreVideosMessage = null;
    });

    try {
      final skip = (currentPage - 1) * limit;
      final newVideos = await videoService.getAllVideos(
        skip: skip,
        limit: limit,
      );

      setState(() {
        if (newVideos.isEmpty) {
          if (currentPage > 1) {
            currentPage--; // Go back to previous page if we've gone too far
            noMoreVideosMessage = 'End of videos reached';
          } else {
            noMoreVideosMessage = 'No videos available';
          }
        } else {
          videos = newVideos;
          noMoreVideosMessage = null;
        }
        isLoading = false;
      });
    } catch (e) {
      setState(() {
        error = e.toString();
        isLoading = false;
      });
    }
  }

  void _showModal() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (BuildContext context) {
        return Container(
          height: MediaQuery.of(context).size.height * 0.8,
          decoration: const BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.only(
              topLeft: Radius.circular(20),
              topRight: Radius.circular(20),
            ),
          ),
          child: Column(
            children: [
              Container(
                margin: const EdgeInsets.only(top: 8),
                width: 40,
                height: 4,
                decoration: BoxDecoration(
                  color: Colors.grey[300],
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
              const Expanded(
                child: Center(
                  child: Text(
                    'Modal Content (Coming Soon)',
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    _tagsScrollController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final screenWidth = MediaQuery.of(context).size.width;

    return Scaffold(
      floatingActionButton: FloatingActionButton(
        onPressed: _showModal,
        child: const Icon(Icons.add),
      ),
      appBar: CustomNavBar(
        onProfileTap: () {
          if (kDebugMode) {
            print('Profile tapped');
          }
          context.go("/profile");
        },
        onSearchTap: () {
          if (kDebugMode) {
            print('Navigate to search page');
          }
        },
        onLogoTap: () {
          if (kDebugMode) {
            print('Logo tapped');
          }
        },
      ),
      body: RefreshIndicator(
        onRefresh: () async {
          setState(() {
            currentPage = 1;
          });
          await _loadVideos();
        },
        child: SingleChildScrollView(
          child: Column(
            children: [
              FadeTransition(
                opacity: _fadeAnimation,
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Text(
                    'Developed by Kuchuk Borom Debbarma',
                    style: TextStyle(
                      color: Colors.grey[800],
                      fontSize: 14,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ),
              ),

              // Tags Section
              Container(
                height: 50,
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
                child: Scrollbar(
                  controller: _tagsScrollController,
                  thumbVisibility: true,
                  child: SingleChildScrollView(
                    controller: _tagsScrollController,
                    scrollDirection: Axis.horizontal,
                    physics: const AlwaysScrollableScrollPhysics(),
                    child: Row(
                      children: Set<String>.from(
                        videos.expand((video) => video.tags),
                      )
                          .map((tag) => Padding(
                                padding: const EdgeInsets.symmetric(
                                  vertical: 8,
                                  horizontal: 4,
                                ),
                                child: Material(
                                  color: Colors.transparent,
                                  child: InkWell(
                                    onTap: () {},
                                    borderRadius: BorderRadius.circular(20),
                                    child: Container(
                                      padding: const EdgeInsets.symmetric(
                                        horizontal: 12,
                                        vertical: 6,
                                      ),
                                      decoration: BoxDecoration(
                                        color: Colors.primaries[tag.hashCode %
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
                                        tag,
                                        style: TextStyle(
                                          color: Colors.primaries[tag.hashCode %
                                              Colors.primaries.length][900],
                                          fontWeight: FontWeight.w500,
                                        ),
                                      ),
                                    ),
                                  ),
                                ),
                              ))
                          .toList(),
                    ),
                  ),
                ),
              ),

              // Error or No More Videos message
              if (error != null || noMoreVideosMessage != null)
                Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Text(
                    error ?? noMoreVideosMessage!,
                    style: TextStyle(
                      color: error != null ? Colors.red : Colors.grey[600],
                      fontStyle:
                          error != null ? FontStyle.normal : FontStyle.italic,
                    ),
                  ),
                ),

              // Videos Grid with VideoCard
              Padding(
                padding: const EdgeInsets.all(8.0),
                child: LayoutBuilder(
                  builder: (context, constraints) {
                    return Column(
                      children: [
                        GridView.builder(
                          physics: const NeverScrollableScrollPhysics(),
                          shrinkWrap: true,
                          gridDelegate:
                              SliverGridDelegateWithFixedCrossAxisCount(
                            crossAxisCount: (screenWidth < 600) ? 1 : 2,
                            childAspectRatio: (screenWidth < 600) ? 1.5 : 1.6,
                            crossAxisSpacing: 8,
                            mainAxisSpacing: 8,
                          ),
                          itemCount: videos.length,
                          itemBuilder: (context, index) {
                            return WidgetVideo(
                              video: videos[index],
                              onTap: () {}, // Handle video tap
                            );
                          },
                        ),
                        if (isLoading)
                          const Padding(
                            padding: EdgeInsets.all(16.0),
                            child: Center(
                              child: CircularProgressIndicator(),
                            ),
                          ),
                        // Pagination Controls
                        if (!isLoading && videos.isNotEmpty)
                          Padding(
                            padding: const EdgeInsets.all(16.0),
                            child: Row(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                IconButton(
                                  icon: const Icon(Icons.arrow_back),
                                  onPressed: currentPage > 1
                                      ? () {
                                          setState(() {
                                            currentPage--;
                                          });
                                          _loadVideos();
                                        }
                                      : null,
                                ),
                                Padding(
                                  padding: const EdgeInsets.symmetric(
                                      horizontal: 16.0),
                                  child: Text(
                                    'Page $currentPage',
                                    style: const TextStyle(
                                      fontSize: 16,
                                      fontWeight: FontWeight.bold,
                                    ),
                                  ),
                                ),
                                IconButton(
                                  icon: const Icon(Icons.arrow_forward),
                                  onPressed: !isLoading &&
                                          (noMoreVideosMessage == null ||
                                              !noMoreVideosMessage!
                                                  .contains('End of videos'))
                                      ? () {
                                          setState(() {
                                            currentPage++;
                                          });
                                          _loadVideos();
                                        }
                                      : null,
                                ),
                              ],
                            ),
                          ),
                      ],
                    );
                  },
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
