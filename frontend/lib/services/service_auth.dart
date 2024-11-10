import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;

import '../util/constants.dart';

class ServiceAuth {
  final _url = Constants.serverUrl;

  Future<String> getGoogleLoginUrl() async {
    final uri = Uri.parse('$_url/public/auth/login/google');
    final response = await http.get(uri);
    if (response.statusCode == 200) {
      return json.decode(response.body)["data"];
    } else {
      throw Exception(
          "Failed to get Login url : ${json.decode(response.body)["msg"]}");
    }
  }

  Future<String> exchangeGoogleTokenForJWTToken(
      String code, String state) async {
    try {
      final uri = Uri.parse('$_url/public/auth/redirect/google')
          .replace(queryParameters: {"code": code, "state": state});

      if (kDebugMode) {
        print('Attempting to exchange token at URL: $uri');
        print('Code: $code');
        print('State: $state');
      }

      final response = await http.get(
        uri,
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
      );

      if (kDebugMode) {
        print('Response status: ${response.statusCode}');
        print('Response body: ${response.body}');
      }

      if (response.statusCode == 200) {
        final decodedResponse = json.decode(response.body);
        if (kDebugMode) {
          print('Decoded response: $decodedResponse');
        }
        return decodedResponse["data"] as String;
      } else {
        String errorMessage =
            'Failed to exchange token. Status: ${response.statusCode}.';
        try {
          if (response.body.isNotEmpty) {
            final decoded = json.decode(response.body);
            errorMessage +=
                ' Message: ${decoded["msg"] ?? decoded["message"] ?? "Unknown error"}';
          }
        } catch (e) {
          errorMessage += ' Raw response: ${response.body}';
        }
        throw Exception(errorMessage);
      }
    } catch (e) {
      if (kDebugMode) {
        print('Error during token exchange: $e');
      }
      rethrow; // Use rethrow to preserve the stack trace
    }
  }
}
