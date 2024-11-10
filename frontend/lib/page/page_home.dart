import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

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

  // Sample video data
  final List<ModelVideo> videos = List.generate(
    20,
    (index) => ModelVideo(
      id: 'ZS7qHon1AlI',
      title: 'Original | Kaitor ni chati | Kuku ft Paradox',
      description:
          'GOSPEL SONG :)My first original song sang in my own mother tongue (Kokborok)Funny how it took 5+ years and a secondary vocalist with sick voice',
      thumbnailUrl:
          'https://i.ytimg.com/vi/ZS7qHon1AlI/hqdefault.jpg?sqp=-oaymwEmCOADEOgC8quKqQMa8AEB-AH-CYAC0AWKAgwIABABGH8gHChMMA8=&rs=AOn4CLADdIMjhsmBof7-dbtm02hDpfsyWg',
      tags: [
        'tag1',
        'tag2',
        'tag3',
        'tag1',
        'tag2',
        'tag3',
        'tag1',
        'tag2',
        'tag3',
        'tag1',
        'tag2',
        'tag3',
        'tag1',
        'tag2',
        'tag3',
        'tag1',
        'tag2',
        'tag3',
      ],
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
      body: SingleChildScrollView(
        child: Column(
          children: [
            // Developer credit moved below navbar
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
                    children: List.generate(
                      20,
                      (index) => Padding(
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
                                color: Colors.primaries[
                                    index % Colors.primaries.length][100],
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
                                  color: Colors.primaries[
                                      index % Colors.primaries.length][900],
                                  fontWeight: FontWeight.w500,
                                ),
                              ),
                            ),
                          ),
                        ),
                      ),
                    ),
                  ),
                ),
              ),
            ),

            // Videos Grid with VideoCard
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: LayoutBuilder(
                builder: (context, constraints) {
                  return GridView.builder(
                    physics: const NeverScrollableScrollPhysics(),
                    shrinkWrap: true,
                    gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                      crossAxisCount: (screenWidth < 600) ? 1 : 2,
                      childAspectRatio: (screenWidth < 600) ? 1.5 : 1.6,
                      crossAxisSpacing: 8,
                      mainAxisSpacing: 8,
                    ),
                    itemCount: videos.length,
                    itemBuilder: (context, index) {
                      return WidgetVideo(
                        video: videos[index],
                        onTap:
                            () {}, // Empty callback since navigation is handled in VideoCard
                      );
                    },
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
