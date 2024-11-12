import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:frontend/models/exceptions/exception_no_jwt_token_found.dart';
import 'package:frontend/models/exceptions/exception_response.dart';
import 'package:frontend/models/model_video.dart';
import 'package:frontend/services/service_storage.dart';
import 'package:frontend/util/constants.dart';
import 'package:http/http.dart' as http;

class ServiceVideo {
  final ServiceStorage storageService;

  ServiceVideo({required this.storageService});

  final _url = '${Constants.serverUrl}/authenticated';

  Future<List<ModelVideo>> getAllVideos({int skip = 0, int limit = 0}) async {
    if (kDebugMode) {
      print("ServiceVideo.Getting all videos triggered");
    }
    final String? token = await storageService.getValue("token");
    if (token == null) throw ExceptionNoJwtTokenFound();
    final finalUrl = '$_url/tag/?skip=$skip&limit=$limit';
    if (kDebugMode) {
      print(
          "Getting all videos skipping $skip and limiting to $limit from $finalUrl");
    }
    var response = await http
        .get(Uri.parse(finalUrl), headers: {"Authorization": "Bearer $token"});
    if (response.statusCode == 200) {
      List<dynamic> videosRAW = jsonDecode(response.body)["data"];
      List<ModelVideo> videos = [];
      for (var element in videosRAW) {
        var videoDTO = element['videoDTO'];
        List<dynamic> tags = element["tags"];

        String id = videoDTO['id'];
        String title = videoDTO['title'];
        String description = videoDTO['description'];
        String thumbnailUrl = videoDTO['thumbnail'];
        List<String> tagsList = [];
        for (var t in tags) {
          tagsList.add(t as String);
        }

        videos.add(ModelVideo(
            id: id,
            title: title,
            description: description,
            tags: tagsList,
            thumbnailUrl: thumbnailUrl));
      }
      return videos;
    }
    throw ExceptionResponse(response.body, response.statusCode);
  }
}
