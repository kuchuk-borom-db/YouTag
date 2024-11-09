class ModelVideo {
  final String id;
  final String title;
  final String description;
  final String thumbnailUrl;
  final List<String> tags;

  ModelVideo(
      {required this.id,
      required this.title,
      required this.description,
      required this.tags,
      required this.thumbnailUrl});
}
