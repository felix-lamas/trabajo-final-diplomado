import 'dart:async';

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'controllers/session_controller.dart';
import 'screens/auth_link_screens.dart';
import 'screens/auth_gate.dart';
import 'services/auth_link_coordinator.dart';
import 'theme/app_theme.dart';

final appNavigatorKey = GlobalKey<NavigatorState>();

class VidiaApp extends StatefulWidget {
  const VidiaApp({super.key, this.authLinks});
  final AuthLinkCoordinator? authLinks;
  @override
  State<VidiaApp> createState() => _VidiaAppState();
}

class _VidiaAppState extends State<VidiaApp> with WidgetsBindingObserver {
  bool _resumed = true;
  bool _sessionReady = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _resumed =
        WidgetsBinding.instance.lifecycleState == null ||
        WidgetsBinding.instance.lifecycleState == AppLifecycleState.resumed;
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    _resumed = state == AppLifecycleState.resumed;
    widget.authLinks?.setReady(
      _resumed && _sessionReady && appNavigatorKey.currentState != null,
    );
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    if (widget.authLinks != null) unawaited(widget.authLinks!.dispose());
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    _sessionReady = !context.watch<SessionController>().initializing;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) {
        widget.authLinks?.setReady(
          _resumed && _sessionReady && appNavigatorKey.currentState != null,
        );
      }
    });
    return MaterialApp(
      navigatorKey: appNavigatorKey,
      debugShowCheckedModeBanner: false,
      title: 'Vidia',
      theme: AppTheme.light(),
      darkTheme: AppTheme.dark(),
      themeMode: ThemeMode.system,
      home: const AuthGate(),
      onGenerateRoute: (settings) {
        final uri = Uri.tryParse(settings.name ?? '');
        final link = uri == null ? null : AuthLink.parse(uri, internal: true);
        if (link != null) return authLinkRoute(link);
        return MaterialPageRoute<void>(
          builder: (_) => const Scaffold(
            body: Center(
              child: Text('El enlace no es válido. Solicita otro correo.'),
            ),
          ),
        );
      },
    );
  }
}
