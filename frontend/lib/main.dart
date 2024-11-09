import 'package:flutter/material.dart';
import 'package:flutter_web_plugins/flutter_web_plugins.dart';
import 'package:frontend/modules/auth/api/service_auth.dart';
import 'package:frontend/modules/shared/config/config_router.dart';
import 'package:frontend/modules/shared/services/service_storage.dart';
import 'package:get_it/get_it.dart';

import 'app_theme.dart';

final getIt = GetIt.instance;

void setupGetIt() {
  getIt.registerSingleton<ServiceAuth>(ServiceAuth());
  getIt.registerSingleton<ServiceStorage>(ServiceStorage());
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
