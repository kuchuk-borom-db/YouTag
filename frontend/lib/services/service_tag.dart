import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:frontend/main.dart';
import 'package:frontend/services/service_storage.dart';
import 'package:http/http.dart' as http;

import '../util/constants.dart';

class ServiceTag {
  final _url = '${Constants.serverUrl}/authenticated/video-userTag/';
  final ServiceStorage storageService = getIt<ServiceStorage>();

  Future<List<String>?> getTags(int skip, int limit,
      {String containing = ""}) async {
    if (kDebugMode) {
      print(
          "Getting all userTags or containing $containing with skip $skip and limit $limit");
    }
    String? token = await storageService.getValue("token");
    if (token == null) {
      return null;
    }
    var response = await http
        .get(Uri.parse(_url), headers: {"Authorization": "Bearer $token"});
    if (response.statusCode != 200) {
      return null;
    }

    var parsedBody = json.decode(response.body);


  }
}
