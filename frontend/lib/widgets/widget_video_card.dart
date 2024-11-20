import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../models/model_video.dart';

class WidgetVideo extends StatefulWidget {
  final ModelVideo video;
  final VoidCallback onTap;

  const WidgetVideo({
    super.key,
    required this.video,
    required this.onTap,
  });

  @override
  State<WidgetVideo> createState() => _WidgetVideoState();
}

class _WidgetVideoState extends State<WidgetVideo> {
  bool isHovered = false;

  @override
  Widget build(BuildContext context) {
    return MouseRegion(
      onEnter: (_) => setState(() => isHovered = true),
      onExit: (_) => setState(() => isHovered = false),
      child: GestureDetector(
        onTap: () {
          // Construct query string manually
          final queryString = Uri(queryParameters: {
            'id': widget.video.id,
            'title': widget.video.title,
            'thumbnail': widget.video.thumbnailUrl,
            'description': widget.video.description,
            'userTags': widget.video.userTags.join(','),
            // Join list into comma-separated string
          }).query;

          // Navigate using manually constructed URL
          context.go('/video?$queryString');
        },
        child: TweenAnimationBuilder(
          duration: const Duration(milliseconds: 200),
          tween: Tween<double>(
            begin: 1.0,
            end: isHovered ? 1.05 : 1.0,
          ),
          builder: (context, double scale, child) {
            return Transform.scale(
              scale: scale,
              child: child,
            );
          },
          child: Container(
            height: 200,
            margin: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(12),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(isHovered ? 0.3 : 0.2),
                  blurRadius: isHovered ? 12 : 8,
                  offset: const Offset(0, 2),
                ),
              ],
            ),
            clipBehavior: Clip.hardEdge,
            child: Stack(
              fit: StackFit.expand,
              children: [
                // Thumbnail
                Image.network(
                  widget.video.thumbnailUrl,
                  fit: BoxFit.cover,
                  errorBuilder: (context, error, stackTrace) {
                    return Container(
                      color: Colors.grey[300],
                      child: const Center(
                        child: Icon(
                          Icons.error_outline,
                          color: Colors.grey,
                          size: 32,
                        ),
                      ),
                    );
                  },
                ),
                // Gradient overlay
                Container(
                  decoration: BoxDecoration(
                    gradient: LinearGradient(
                      begin: Alignment.topCenter,
                      end: Alignment.bottomCenter,
                      colors: [
                        Colors.transparent,
                        Colors.black.withOpacity(isHovered ? 0.8 : 0.7),
                      ],
                      stops: const [0.5, 1.0],
                    ),
                  ),
                ),
                // Tags (visible only on hover)
                if (isHovered)
                  Positioned(
                    left: 12,
                    right: 12,
                    top: 12,
                    child: Wrap(
                      spacing: 8,
                      runSpacing: 8,
                      children: widget.video.userTags
                          .map((userTag) => Container(
                                padding: const EdgeInsets.symmetric(
                                  horizontal: 8,
                                  vertical: 4,
                                ),
                                decoration: BoxDecoration(
                                  color: Colors.black.withOpacity(0.6),
                                  borderRadius: BorderRadius.circular(16),
                                ),
                                child: Text(
                                  '#$userTag',
                                  style: const TextStyle(
                                    color: Colors.white,
                                    fontSize: 12,
                                    fontWeight: FontWeight.w500,
                                  ),
                                ),
                              ))
                          .toList(),
                    ),
                  ),
                // Title
                Positioned(
                  left: 12,
                  right: 12,
                  bottom: 12,
                  child: Text(
                    widget.video.title,
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 16,
                      fontWeight: FontWeight.w600,
                      shadows: [
                        Shadow(
                          offset: Offset(0, 1),
                          blurRadius: 2,
                          color: Colors.black45,
                        ),
                      ],
                    ),
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
