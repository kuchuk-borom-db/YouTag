import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';

class ServiceStorage {
  static const Duration defaultExpiry = Duration(days: 7);

  /// Saves a value with the given key
  /// [key] - The storage key
  /// [value] - The value to store
  /// [expiryDuration] - Optional custom expiry duration, defaults to 7 days
  Future<bool> saveValue(
    String key,
    String value, {
    Duration? expiryDuration = const Duration(days: 10),
  }) async {
    try {
      final prefs = await SharedPreferences.getInstance();

      // Save the value
      final savedValue = await prefs.setString(key, value);

      // Save expiry timestamp if duration is provided
      if (expiryDuration != null) {
        final expiryDate = DateTime.now().add(expiryDuration);
        await prefs.setInt('${key}_expiry', expiryDate.millisecondsSinceEpoch);
      }

      return savedValue;
    } catch (e) {
      if (kDebugMode) {
        print('Error saving to SharedPreferences: $e');
      }
      return false;
    }
  }

  /// Retrieves a value by its key
  /// Returns null if value doesn't exist or has expired
  Future<String?> getValue(String key) async {
    try {
      final prefs = await SharedPreferences.getInstance();

      // Check expiry if it exists
      final expiryTimestamp = prefs.getInt('${key}_expiry');
      if (expiryTimestamp != null) {
        final expiryDate = DateTime.fromMillisecondsSinceEpoch(expiryTimestamp);
        if (DateTime.now().isAfter(expiryDate)) {
          // Value has expired, remove it and return null
          await removeValue(key);
          return null;
        }
      }

      return prefs.getString(key);
    } catch (e) {
      if (kDebugMode) {
        print('Error reading from SharedPreferences: $e');
      }
      return null;
    }
  }

  /// Removes a value by its key
  Future<bool> removeValue(String key) async {
    try {
      final prefs = await SharedPreferences.getInstance();

      // Remove both value and its expiry
      await prefs.remove('${key}_expiry');
      return await prefs.remove(key);
    } catch (e) {
      if (kDebugMode) {
        print('Error removing from SharedPreferences: $e');
      }
      return false;
    }
  }

  /// Saves multiple values at once
  /// [values] - Map of key-value pairs to save
  /// [expiryDuration] - Optional custom expiry duration for all values
  Future<bool> saveMultipleValues(
    Map<String, String> values, {
    Duration? expiryDuration,
  }) async {
    try {
      bool allSaved = true;
      for (final entry in values.entries) {
        final success = await saveValue(
          entry.key,
          entry.value,
          expiryDuration: expiryDuration,
        );
        if (!success) allSaved = false;
      }
      return allSaved;
    } catch (e) {
      if (kDebugMode) {
        print('Error saving multiple values to SharedPreferences: $e');
      }
      return false;
    }
  }

  /// Retrieves multiple values by their keys
  /// Returns a map of existing values, excluding any keys not found or expired
  Future<Map<String, String>> getMultipleValues(List<String> keys) async {
    final Map<String, String> result = {};
    for (final key in keys) {
      final value = await getValue(key);
      if (value != null) {
        result[key] = value;
      }
    }
    return result;
  }

  /// Removes multiple values by their keys
  Future<bool> removeMultipleValues(List<String> keys) async {
    try {
      bool allRemoved = true;
      for (final key in keys) {
        final success = await removeValue(key);
        if (!success) allRemoved = false;
      }
      return allRemoved;
    } catch (e) {
      if (kDebugMode) {
        print('Error removing multiple values from SharedPreferences: $e');
      }
      return false;
    }
  }

  /// Checks if a value exists and hasn't expired
  Future<bool> hasValue(String key) async {
    final value = await getValue(key);
    return value != null;
  }

  /// Clears all stored values
  Future<bool> clearAll() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      return await prefs.clear();
    } catch (e) {
      if (kDebugMode) {
        print('Error clearing SharedPreferences: $e');
      }
      return false;
    }
  }
}
