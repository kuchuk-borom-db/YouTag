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
    final finalUrl = '$_url/video/?skip=$skip&limit=$limit';
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
        List<dynamic> userTags = element["userTags"];

        String id = videoDTO['id'];
        String title = videoDTO['title'];
        String description = videoDTO['description'];
        String thumbnailUrl = videoDTO['thumbnail'];
        List<String> tagsList = [];
        for (var t in userTags) {
          tagsList.add(t as String);
        }

        videos.add(ModelVideo(
            id: id,
            title: title,
            description: description,
            userTags: tagsList,
            thumbnailUrl: thumbnailUrl));
      }
      return videos;
    }
    throw ExceptionResponse(response.body, response.statusCode);
  }

  Future<bool> saveVideo(String videoId, List<String> userTags) async {
    if (kDebugMode) {
      print(("Saving video $videoId with userTags $userTags"));
    }
    final String? token = await storageService.getValue("token");
    if (token == null) throw ExceptionNoJwtTokenFound();
    var finalUrl = '$_url/video-userTag/$videoId';
    if (userTags.isNotEmpty) {
      finalUrl = "$finalUrl?userTags=${userTags.join(",")}";
    }

    var response = await http
        .post(Uri.parse(finalUrl), headers: {"Authorization": "Bearer $token"});
    if (response.statusCode == 200) {
      return true;
    }
    if (kDebugMode) {
      print("Something went wrong while saving video");
    }
    return false;
  }

  String? extractVideoId(String url) {
    // Direct ID pattern
    if (url.length >= 11 && !url.contains('/') && !url.contains('.')) {
      return url;
    }

    // Try to find video ID in various YouTube URL formats
    try {
      Uri uri = Uri.parse(url);

      // Handle youtube.com/watch?v= format
      if (uri.queryParameters.containsKey('v')) {
        return uri.queryParameters['v'];
      }

      // Handle youtu.be format
      if (uri.host == 'youtu.be') {
        return uri.pathSegments.first;
      }

      // Handle m.youtube.com format
      if (uri.host.contains('youtube.com') &&
          uri.pathSegments.contains('watch')) {
        return uri.queryParameters['v'];
      }
    } catch (e) {
      if (kDebugMode) {
        print('Error parsing URL: $e');
      }
      return null;
    }
    return null;
  }
}
