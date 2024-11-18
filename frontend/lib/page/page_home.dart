import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
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
  final TextEditingController _tagsController = TextEditingController();
  final TextEditingController _linkController = TextEditingController();
  bool _isSaving = false;
  String? _saveError;
  bool _saved = false;
  final ServiceVideo videoService = getIt<ServiceVideo>();

  List<ModelVideo> videos = [];
  bool isLoading = false;
  String? error;
  String? noMoreVideosMessage;

  // Pagination parameters
  int currentPage = 1;
  final int limit = 2;

  // Extract tags into class variable
  Set<String> availableTags = <String>{};
  List<String> tagSuggestions = [];
  bool _showSuggestions = false;

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

    _tagsController.addListener(_onTagInput);

    _controller.forward();
    _loadVideos();
  }

  void _updateAvailableTags() {
    availableTags = Set<String>.from(
      videos.expand((video) => video.tags),
    );
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
            currentPage--;
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

      _updateAvailableTags(); // Update available tags after loading videos
    } catch (e) {
      setState(() {
        error = e.toString();
        isLoading = false;
      });
    }
  }
  void _onTagInput() {
    final String input = _tagsController.text;
    if (input.isEmpty) {
      setState(() {
        tagSuggestions = [];
        _showSuggestions = false;
      });
      return;
    }

    // Get the current tag being typed (after the last comma)
    final String currentTag = input.split(',').last.trim().toLowerCase();

    if (currentTag.isEmpty) {
      setState(() {
        tagSuggestions = [];
        _showSuggestions = false;
      });
      return;
    }

    // Filter suggestions based on the current input
    setState(() {
      tagSuggestions = availableTags
          .where((tag) => tag.toLowerCase().startsWith(currentTag))
          .toList()
        ..sort();
      _showSuggestions = tagSuggestions.isNotEmpty;
    });
  }

  void _selectSuggestion(String suggestion) {
    final currentTags = _tagsController.text.split(',');
    currentTags.removeLast(); // Remove the partial tag
    currentTags.add(suggestion); // Add the selected suggestion

    final newText = currentTags.where((tag) => tag.isNotEmpty).join(', ');
    _tagsController.value = TextEditingValue(
      text: newText.isEmpty ? suggestion : '$newText, ',
      selection: TextSelection.collapsed(
        offset: (newText.isEmpty ? suggestion : '$newText, ').length,
      ),
    );

    setState(() {
      _showSuggestions = false;
    });
  }

  // Tag validation function
  bool _isValidTag(String tag) {
    // Only allow alphanumeric characters and hyphens, no spaces
    final RegExp validTagRegex = RegExp(r'^[a-zA-Z0-9-]+$');
    return validTagRegex.hasMatch(tag);
  }

  List<String> _validateAndFormatTags(String input) {
    return input
        .split(',')
        .map((tag) => tag.trim())
        .where((tag) => tag.isNotEmpty && _isValidTag(tag))
        .toList();
  }

  void _showModal() {
    _tagsController.clear();
    _linkController.clear();
    _saved = false;
    _saveError = null;

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.redAccent,
      builder: (BuildContext context) {
        return StatefulBuilder(
          builder: (BuildContext context, StateSetter setModalState) {
            return Container(
              height: MediaQuery.of(context).size.height * 0.8,
              decoration: const BoxDecoration(
                color: Colors.redAccent,
                borderRadius: BorderRadius.only(
                  topLeft: Radius.circular(20),
                  topRight: Radius.circular(20),
                ),
              ),
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Handle bar at top
                    Center(
                      child: Container(
                        margin: const EdgeInsets.only(bottom: 16),
                        width: 40,
                        height: 4,
                        decoration: BoxDecoration(
                          color: Colors.grey[300],
                          borderRadius: BorderRadius.circular(2),
                        ),
                      ),
                    ),

                    const Text(
                      'Add New Video',
                      style: TextStyle(
                        fontSize: 24,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 24),

                    TextField(
                      controller: _linkController,
                      decoration: InputDecoration(
                        labelText: 'YouTube Link',
                        hintText: 'Enter YouTube video URL or ID',
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                        ),
                        prefixIcon: const Icon(Icons.link),
                      ),
                    ),
                    const SizedBox(height: 16),

                    // Tags Input
                    TextField(
                      controller: _tagsController,
                      decoration: InputDecoration(
                        fillColor: Colors.amberAccent,
                        labelText: 'Tags',
                        hintText: 'Enter tags separated by commas',
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                        ),
                        prefixIcon: const Icon(Icons.tag),
                        helperText: 'Use only letters, numbers, and hyphens',
                        errorText: _tagsController.text.isNotEmpty &&
                            !_validateAndFormatTags(_tagsController.text)
                                .any((tag) => tag == _tagsController.text.split(',').last.trim())
                            ? 'Invalid tag format'
                            : null,
                      ),
                      inputFormatters: [
                        FilteringTextInputFormatter.allow(RegExp(r'[a-zA-Z0-9-,]')),
                      ],
                    ),
                    const SizedBox(height: 8),

                    // Tag Suggestions
                    if (_showSuggestions)
                      Container(
                        height: 100,
                        decoration: BoxDecoration(
                          border: Border.all(color: Colors.grey.shade300),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: ListView.builder(
                          shrinkWrap: true,
                          itemCount: tagSuggestions.length,
                          itemBuilder: (context, index) {
                            return ListTile(
                              dense: true,
                              title: Text(tagSuggestions[index]),
                              onTap: () => _selectSuggestion(tagSuggestions[index]),
                            );
                          },
                        ),
                      ),

                    // Error message if any
                    if (_saveError != null)
                      Padding(
                        padding: const EdgeInsets.only(bottom: 16),
                        child: Text(
                          _saveError!,
                          style: const TextStyle(
                            color: Colors.red,
                            fontSize: 14,
                          ),
                        ),
                      ),

                    // Save Button
                    SizedBox(
                      width: double.infinity,
                      child: ElevatedButton(
                        style: ElevatedButton.styleFrom(
                          padding: const EdgeInsets.symmetric(vertical: 16),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(8),
                          ),
                        ),
                        onPressed: _isSaving || _saved
                            ? null
                            : () async {
                                setModalState(() {
                                  _isSaving = true;
                                  _saveError = null;
                                });

                                // Extract video ID
                                final videoId = videoService
                                    .extractVideoId(_linkController.text);
                                if (videoId == null) {
                                  setModalState(() {
                                    _saveError =
                                        'Invalid YouTube URL or video ID';
                                    _isSaving = false;
                                  });
                                  return;
                                }

                                // Process tags
                                final tags = _tagsController.text
                                    .split(',')
                                    .map((tag) => tag.trim())
                                    .where((tag) => tag.isNotEmpty)
                                    .toList();

                                if (tags.isEmpty) {
                                  setModalState(() {
                                    _saveError =
                                        'Please enter at least one tag';
                                    _isSaving = false;
                                  });
                                  return;
                                }

                                try {
                                  var success = await videoService.saveVideo(
                                      videoId, tags);

                                  if (success) {
                                    setModalState(() {
                                      _saved = true;
                                      _isSaving = false;
                                    });

                                    // Show success message and close modal after delay
                                    ScaffoldMessenger.of(context).showSnackBar(
                                      const SnackBar(
                                        content: Row(
                                          children: [
                                            Icon(Icons.check_circle,
                                                color: Colors.white),
                                            SizedBox(width: 8),
                                            Text('Video saved successfully!'),
                                          ],
                                        ),
                                        backgroundColor: Colors.green,
                                        duration: Duration(seconds: 2),
                                      ),
                                    );

                                    // Refresh videos after saving
                                    setState(() {
                                      currentPage = 1;
                                    });
                                    await _loadVideos();

                                    // Close modal after a short delay
                                    Future.delayed(const Duration(seconds: 2),
                                        () {
                                      Navigator.pop(context);
                                    });
                                  } else {
                                    setModalState(() {
                                      _saveError =
                                          'Failed to save video. Please try again.';
                                      _isSaving = false;
                                    });

                                    ScaffoldMessenger.of(context).showSnackBar(
                                      const SnackBar(
                                        content: Row(
                                          children: [
                                            Icon(Icons.error,
                                                color: Colors.white),
                                            SizedBox(width: 8),
                                            Text('Failed to save video'),
                                          ],
                                        ),
                                        backgroundColor: Colors.red,
                                        duration: Duration(seconds: 3),
                                      ),
                                    );
                                  }
                                } catch (e) {
                                  setModalState(() {
                                    _saveError =
                                        'Error saving video: ${e.toString()}';
                                    _isSaving = false;
                                  });
                                }
                              },
                        child: _isSaving
                            ? const SizedBox(
                                height: 20,
                                width: 20,
                                child: CircularProgressIndicator(
                                  strokeWidth: 2,
                                  valueColor: AlwaysStoppedAnimation<Color>(
                                      Colors.white),
                                ),
                              )
                            : _saved
                                ? const Row(
                                    mainAxisAlignment: MainAxisAlignment.center,
                                    children: [
                                      Icon(Icons.check),
                                      SizedBox(width: 8),
                                      Text('Saved!'),
                                    ],
                                  )
                                : const Text('Save Video'),
                      ),
                    ),
                  ],
                ),
              ),
            );
          },
        );
      },
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    _tagsScrollController.dispose();
    _tagsController.dispose();
    _linkController.dispose();
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
                    //TODO Extract these tags into a class level variable so that it can be used for save video modal tag suggestion
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
