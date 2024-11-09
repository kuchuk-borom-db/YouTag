import 'package:flutter/material.dart';

class AppTheme {
  // Colors
  static const Color primaryRed = Color(0xFFFF0000);
  static const Color primaryDarkRed = Color(0xFFCC0000);
  static const Color secondaryGrey = Color(0xFF282828);

  // Light Theme
  static final ThemeData lightTheme = ThemeData(
    useMaterial3: true,
    brightness: Brightness.light,
    primaryColor: primaryRed,
    scaffoldBackgroundColor: Colors.grey[100],
    cardColor: Colors.white,
    colorScheme: const ColorScheme.light(
      primary: primaryRed,
      secondary: secondaryGrey,
      surface: Colors.white,
    ),
    elevatedButtonTheme: ElevatedButtonThemeData(
      style: ElevatedButton.styleFrom(
        backgroundColor: primaryRed,
        foregroundColor: Colors.white,
      ),
    ),
    textTheme: const TextTheme(
      displayLarge: TextStyle(color: Colors.black87),
      bodyLarge: TextStyle(color: Colors.black87),
      bodyMedium: TextStyle(color: Colors.black87),
    ),
  );

  // Dark Theme
  static final ThemeData darkTheme = ThemeData(
    useMaterial3: true,
    brightness: Brightness.dark,
    primaryColor: primaryDarkRed,
    scaffoldBackgroundColor: Colors.grey[900],
    cardColor: Colors.grey[850],
    colorScheme: ColorScheme.dark(
      primary: primaryDarkRed,
      secondary: secondaryGrey,
      surface: Colors.grey[850]!,
    ),
    elevatedButtonTheme: ElevatedButtonThemeData(
      style: ElevatedButton.styleFrom(
        backgroundColor: primaryDarkRed,
        foregroundColor: Colors.white,
      ),
    ),
    textTheme: const TextTheme(
      displayLarge: TextStyle(color: Colors.white),
      bodyLarge: TextStyle(color: Colors.white),
      bodyMedium: TextStyle(color: Colors.white70),
    ),
  );
}
