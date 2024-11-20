import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../models/model_video.dart';
import '../widgets/widget_video_card.dart';

class PageSearch extends StatefulWidget {
  final List<String> initialTags;
  final List<String> initialTitles;
  final List<String> initialIds;
  final bool autoSearch;

  const PageSearch({
    super.key,
    this.initialTags = const [],
    this.initialTitles = const [],
    this.initialIds = const [],
    this.autoSearch = false,
  });

  @override
  State<PageSearch> createState() => _PageSearchState();
}

class _PageSearchState extends State<PageSearch> with TickerProviderStateMixin {
  late final List<String> userTags;
  late final List<String> titles;
  late final List<String> ids;
  final TextEditingController _tagController = TextEditingController();
  final TextEditingController _titleController = TextEditingController();
  final TextEditingController _idController = TextEditingController();

  late final AnimationController _fadeController;
  late final AnimationController _slideController;

  // Placeholder results
  final List<ModelVideo> searchResults = List.generate(
    10,
    (index) => ModelVideo(
      id: 'VIDEO_${index + 1}',
      title: 'Search Result ${index + 1}: Amazing Discovery',
      description: 'This is a placeholder description for video ${index + 1}',
      thumbnailUrl: 'https://picsum.photos/seed/${index + 100}/300/200',
      userTags: ['nature', 'adventure', 'discovery'],
    ),
  );

  bool _isSearching = false;
  bool _hasSearched = false;

  @override
  void initState() {
    super.initState();
    // Initialize lists with initial values
    userTags = List.from(widget.initialTags);
    titles = List.from(widget.initialTitles);
    ids = List.from(widget.initialIds);

    _fadeController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 800),
    );

    _slideController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1000),
    );

    _fadeController.forward();
    _slideController.forward();

    // Perform automatic search if needed
    if (widget.autoSearch) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        _performSearch();
      });
    }
  }

  @override
  void dispose() {
    _tagController.dispose();
    _titleController.dispose();
    _idController.dispose();
    _fadeController.dispose();
    _slideController.dispose();
    super.dispose();
  }

  void _updateUrl() {
    final queryParams = <String, String>{};
    if (userTags.isNotEmpty) queryParams['userTags'] = userTags.join(',');
    if (titles.isNotEmpty) queryParams['titles'] = titles.join(',');
    if (ids.isNotEmpty) queryParams['ids'] = ids.join(',');

    if (context.mounted) {
      final queryString = queryParams.isEmpty
          ? ''
          : '?${Uri(queryParameters: queryParams).query}';
      context.go('/search$queryString');
    }
  }

  void _addItem(
      String value, List<String> list, TextEditingController controller) {
    if (value.trim().isNotEmpty) {
      setState(() {
        list.add(value.trim());
        controller.clear();
        _updateUrl();
      });
    }
  }

  void _removeItem(int index, List<String> list) {
    setState(() {
      list.removeAt(index);
      _updateUrl();
    });
  }

  void _performSearch() {
    setState(() {
      _isSearching = true;
      _hasSearched = true;
    });

    // Update URL with current search parameters
    _updateUrl();

    // Simulate search delay
    Future.delayed(const Duration(milliseconds: 800), () {
      setState(() {
        _isSearching = false;
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: CustomScrollView(
          slivers: [
            // Search Fields Section
            SliverToBoxAdapter(
              child: FadeTransition(
                opacity: _fadeController,
                child: SlideTransition(
                  position: Tween<Offset>(
                    begin: const Offset(0, -0.5),
                    end: Offset.zero,
                  ).animate(CurvedAnimation(
                    parent: _slideController,
                    curve: Curves.easeOutCubic,
                  )),
                  child: Container(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            IconButton(
                              icon: const Icon(Icons.arrow_back),
                              onPressed: () => context.go('/'),
                            ),
                            const SizedBox(width: 8),
                            const Text(
                              'Advanced Search',
                              style: TextStyle(
                                fontSize: 28,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 20),

                        // Tags Field
                        _buildSearchField(
                          'Tags',
                          'Add userTags to search',
                          _tagController,
                          userTags,
                          Icons.local_offer_outlined,
                        ),
                        const SizedBox(height: 16),

                        // Titles Field
                        _buildSearchField(
                          'Video Titles',
                          'Add titles to search',
                          _titleController,
                          titles,
                          Icons.title_outlined,
                        ),
                        const SizedBox(height: 16),

                        // IDs Field
                        _buildSearchField(
                          'Video IDs',
                          'Add video IDs to search',
                          _idController,
                          ids,
                          Icons.video_library_outlined,
                        ),
                        const SizedBox(height: 24),

                        // Search Button
                        SizedBox(
                          width: double.infinity,
                          child: ElevatedButton(
                            onPressed: _performSearch,
                            style: ElevatedButton.styleFrom(
                              padding: const EdgeInsets.symmetric(vertical: 16),
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(12),
                              ),
                            ),
                            child: _isSearching
                                ? const SizedBox(
                                    height: 20,
                                    width: 20,
                                    child: CircularProgressIndicator(
                                      color: Colors.white,
                                      strokeWidth: 2,
                                    ),
                                  )
                                : const Text(
                                    'Search',
                                    style: TextStyle(
                                      fontSize: 16,
                                      fontWeight: FontWeight.bold,
                                    ),
                                  ),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ),

            // Results Section
            if (_hasSearched) ...[
              if (_isSearching)
                const SliverFillRemaining(
                  child: Center(
                    child: CircularProgressIndicator(),
                  ),
                )
              else if (searchResults.isEmpty)
                SliverFillRemaining(
                  child: Center(
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(
                          Icons.search_off_rounded,
                          size: 64,
                          color: Colors.grey[400],
                        ),
                        const SizedBox(height: 16),
                        Text(
                          'No results found',
                          style: Theme.of(context)
                              .textTheme
                              .headlineSmall
                              ?.copyWith(
                                color: Colors.grey[600],
                              ),
                        ),
                        const SizedBox(height: 8),
                        Text(
                          'Try adjusting your search criteria',
                          style:
                              Theme.of(context).textTheme.bodyLarge?.copyWith(
                                    color: Colors.grey[600],
                                  ),
                        ),
                      ],
                    ),
                  ),
                )
              else
                SliverPadding(
                  padding: const EdgeInsets.all(16),
                  sliver: SliverGrid(
                    gridDelegate:
                        const SliverGridDelegateWithFixedCrossAxisCount(
                      crossAxisCount: 2,
                      childAspectRatio: 1.6,
                      crossAxisSpacing: 16,
                      mainAxisSpacing: 16,
                    ),
                    delegate: SliverChildBuilderDelegate(
                      (context, index) {
                        return FadeTransition(
                          opacity: Tween<double>(begin: 0, end: 1).animate(
                            CurvedAnimation(
                              parent: _fadeController,
                              curve: Interval(
                                index * 0.1,
                                1.0,
                                curve: Curves.easeInOut,
                              ),
                            ),
                          ),
                          child: SlideTransition(
                            position: Tween<Offset>(
                              begin: const Offset(0, 0.5),
                              end: Offset.zero,
                            ).animate(
                              CurvedAnimation(
                                parent: _slideController,
                                curve: Interval(
                                  index * 0.1,
                                  1.0,
                                  curve: Curves.easeOutCubic,
                                ),
                              ),
                            ),
                            child: WidgetVideo(
                              video: searchResults[index],
                              onTap: () {
                                // Navigate to video page with query parameters
                                final video = searchResults[index];
                                final queryParams = {
                                  'id': video.id,
                                  'title': Uri.encodeComponent(video.title),
                                  'thumbnailUrl':
                                      Uri.encodeComponent(video.thumbnailUrl),
                                  'description':
                                      Uri.encodeComponent(video.description),
                                  'userTags': video.userTags.join(','),
                                };

                                final queryString =
                                    Uri(queryParameters: queryParams).query;
                                context.go('/video?$queryString');
                              },
                            ),
                          ),
                        );
                      },
                      childCount: searchResults.length,
                    ),
                  ),
                ),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildSearchField(
    String label,
    String hint,
    TextEditingController controller,
    List<String> items,
    IconData icon,
  ) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: const TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.w600,
          ),
        ),
        const SizedBox(height: 8),
        Row(
          children: [
            Expanded(
              child: TextField(
                controller: controller,
                decoration: InputDecoration(
                  hintText: hint,
                  prefixIcon: Icon(icon),
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                ),
                onSubmitted: (value) => _addItem(value, items, controller),
              ),
            ),
            const SizedBox(width: 8),
            IconButton(
              onPressed: () => _addItem(controller.text, items, controller),
              icon: const Icon(Icons.add_circle_outline),
              style: IconButton.styleFrom(
                backgroundColor:
                    Theme.of(context).primaryColor.withOpacity(0.1),
              ),
            ),
          ],
        ),
        if (items.isNotEmpty)
          Padding(
            padding: const EdgeInsets.only(top: 8),
            child: Wrap(
              spacing: 8,
              runSpacing: 8,
              children: items.asMap().entries.map((entry) {
                return Chip(
                  label: Text(entry.value),
                  deleteIcon: const Icon(Icons.close, size: 18),
                  onDeleted: () => _removeItem(entry.key, items),
                );
              }).toList(),
            ),
          ),
      ],
    );
  }
}
