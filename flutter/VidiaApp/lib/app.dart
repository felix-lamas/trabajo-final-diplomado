import 'package:flutter/material.dart';

import 'screens/auth_gate.dart';
import 'theme/app_theme.dart';

class VidiaApp extends StatelessWidget {
  const VidiaApp({super.key});

  @override
  Widget build(BuildContext context) => MaterialApp(
        debugShowCheckedModeBanner: false,
        title: 'Vidia',
        theme: AppTheme.light(),
        darkTheme: AppTheme.dark(),
        themeMode: ThemeMode.system,
        home: const AuthGate(),
      );
}
