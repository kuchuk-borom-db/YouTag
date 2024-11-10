import 'dart:typed_data';

class ModelUser {
  final String name;
  final String email;
  final Uint8List thumbnail;

  ModelUser({required this.thumbnail, required this.email, required this.name});
}
