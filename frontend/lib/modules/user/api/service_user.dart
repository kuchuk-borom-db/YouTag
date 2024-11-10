import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart' show rootBundle; // Add this import
import 'package:frontend/modules/shared/constants.dart';
import 'package:frontend/modules/shared/exceptions/exception_no_jwt_token_found.dart';
import 'package:frontend/modules/shared/exceptions/exception_response.dart';
import 'package:frontend/modules/shared/services/service_storage.dart';
import 'package:http/http.dart' as http;

import '../../../models/model_user.dart';

class ServiceUser {
  final _url = "${Constants.serverUrl}/authenticated/user/";
  final ServiceStorage serviceStorage;
  ModelUser? currentUser;

  ServiceUser(this.serviceStorage);

  Future<Uint8List> _loadImageFromUrl(String url) async {
    try {
      final response = await http.get(Uri.parse(url));
      if (response.statusCode == 200) {
        return response.bodyBytes;
      }
      // If network image fails, load default image
      return _loadDefaultImage();
    } catch (e) {
      if (kDebugMode) {
        print('Failed to load network image: $e');
      }
      // If any error occurs, load default image
      return _loadDefaultImage();
    }
  }

  Future<Uint8List> _loadDefaultImage() async {
    try {
      // Load the default image from assets
      final byteData = await rootBundle.load('assets/images/youtag.png');
      return byteData.buffer.asUint8List();
    } catch (e) {
      if (kDebugMode) {
        print('Failed to load default image: $e');
      }
      // If even default image fails, return an empty transparent pixel
      return Uint8List.fromList([0, 0, 0, 0]); // RGBA transparent pixel
    }
  }

  Future<ModelUser> getUserInfo() async {
    if (currentUser != null) {
      if (kDebugMode) {
        print("Existing User : ${currentUser?.email}, ${currentUser?.name}");
      }
      return currentUser!;
    }

    final token = await serviceStorage.getValue("token");
    if (token == null) {
      throw ExceptionNoJwtTokenFound();
    }

    if (kDebugMode) {
      print("getting user info from server $_url and token $token");
    }

    var response = await http
        .get(Uri.parse(_url), headers: {"Authorization": 'Bearer $token'});

    if (response.statusCode == 200) {
      var parsedBody = json.decode(response.body);
      String email = parsedBody['data']["email"];
      String name = parsedBody['data']['name'];
      String thumbnailUrl = parsedBody['data']['pic'];

      if (kDebugMode) {
        print("Loading user data: $email, $name");
        print("Loading thumbnail from: $thumbnailUrl");
      }

      // Load the image data with fallback to default image
      final thumbnailData = await _loadImageFromUrl(thumbnailUrl);

      // Create and cache the user
      currentUser = ModelUser(
        thumbnail: thumbnailData,
        email: email,
        name: name,
      );

      if (kDebugMode) {
        print(
            "User loaded successfully with ${thumbnailData.length} bytes of image data");
      }

      return currentUser!;
    }

    throw ExceptionResponse(response.body, response.statusCode);
  }
}
