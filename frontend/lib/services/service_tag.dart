import 'dart:convert';

import 'package:frontend/main.dart';
import 'package:frontend/services/service_storage.dart';
import 'package:http/http.dart' as http;

import '../util/constants.dart';

class ServiceTag {
  final _url = '${Constants.serverUrl}/authenticated/tag/';
  final ServiceStorage storageService = getIt<ServiceStorage>();

  Future<List<String>> getAllTags(
    int skip,
    int limit,
  ) async {
    String? token = await storageService.getValue("token");
    if (token == null) {
      return [];
    }
    var response = await http
        .get(Uri.parse(_url), headers: {"Authorization": "Bearer $token"});
    if (response.statusCode != 200) {
      return [];
    }
    var parsedBody = json.decode(response.body);
    List<dynamic> dynamicTags = parsedBody['data'];
    List<String> tags = [];
    for (var t in dynamicTags) {
      tags.add(t);
    }
    return tags;
  }
}
