import 'package:flutter/material.dart';
import 'package:flutter_web_plugins/flutter_web_plugins.dart';
import 'package:frontend/services/service_auth.dart';
import 'package:frontend/services/service_storage.dart';
import 'package:frontend/services/service_user.dart';
import 'package:frontend/services/service_video.dart';
import 'package:get_it/get_it.dart';

import 'app_theme.dart';
import 'config/config_router.dart';

final getIt = GetIt.instance;

void setupGetIt() {
  getIt.registerSingleton<ServiceAuth>(ServiceAuth());
  getIt.registerSingleton<ServiceStorage>(ServiceStorage());
  getIt.registerSingleton<ServiceUser>(ServiceUser(getIt<ServiceStorage>()));
  getIt.registerSingleton<ServiceVideo>(
      ServiceVideo(storageService: getIt<ServiceStorage>()));
}

void main() {
  setupGetIt();
  usePathUrlStrategy();
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  ThemeMode _themeMode = ThemeMode.system;

  void toggleTheme(bool isOn) {
    setState(() {
      _themeMode = isOn ? ThemeMode.dark : ThemeMode.light;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      title: 'Youtag',
      themeMode: _themeMode,
      theme: AppTheme.lightTheme,
      darkTheme: AppTheme.darkTheme,
      routerConfig: ConfigRouter().router,
    );
  }
}
//TODO Show suggestions when adding tags to videos
//TODO Add button to bulk select videos in home page for bulk tag adding or deleting. Make sure it supports multi page. Show selected videos in a scrollable list too :
//TODO Add option beside video widget to delete it or add tags to it using modal
//TODO In Video Page add option to delete and add tags to it. Use common modal for deleting and tags for all of the mentioned todo
//TODO Get all tags of user and view as a list in homepage
