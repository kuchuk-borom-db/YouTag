import 'package:universal_html/html.dart' as html;

class ServiceCookie {
  // Default expiry duration
  static const Duration defaultExpiry = Duration(days: 7);

  /// Saves a cookie with the given key and value
  /// [key] - The cookie key
  /// [value] - The cookie value
  /// [expiryDuration] - Optional custom expiry duration, defaults to 7 days
  void saveCookie(
    String key,
    String value, {
    Duration? expiryDuration,
  }) {
    final expiryDate = DateTime.now().add(expiryDuration ?? defaultExpiry);
    html.document.cookie =
        '$key=$value; expires=${expiryDate.toUtc()}; path=/; secure; samesite=strict';
  }

  /// Retrieves a cookie value by its key
  /// Returns null if cookie doesn't exist
  String? getCookie(String key) {
    final cookies = html.document.cookie?.split('; ');
    if (cookies == null || cookies.isEmpty) return null;

    final cookieEntry = cookies.firstWhere(
      (cookie) => cookie.startsWith('$key='),
      orElse: () => '',
    );

    if (cookieEntry.isEmpty) return null;

    return cookieEntry.split('=')[1];
  }

  /// Removes a cookie by its key
  void removeCookie(String key) {
    html.document.cookie =
        '$key=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
  }

  /// Saves multiple cookies at once
  /// [cookies] - Map of key-value pairs to save
  /// [expiryDuration] - Optional custom expiry duration for all cookies
  void saveMultipleCookies(
    Map<String, String> cookies, {
    Duration? expiryDuration,
  }) {
    cookies.forEach((key, value) {
      saveCookie(key, value, expiryDuration: expiryDuration);
    });
  }

  /// Retrieves multiple cookies by their keys
  /// Returns a map of existing cookies, excluding any keys not found
  Map<String, String> getMultipleCookies(List<String> keys) {
    final Map<String, String> result = {};
    for (final key in keys) {
      final value = getCookie(key);
      if (value != null) {
        result[key] = value;
      }
    }
    return result;
  }

  /// Removes multiple cookies by their keys
  void removeMultipleCookies(List<String> keys) {
    keys.forEach(removeCookie);
  }

  /// Checks if a cookie exists
  bool hasCookie(String key) {
    return getCookie(key) != null;
  }
}
