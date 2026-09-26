import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'app.dart';
import 'config/app_config.dart';
import 'controllers/session_controller.dart';
import 'repositories/backend_evento_repository.dart';
import 'repositories/backend_inscripcion_repository.dart';
import 'repositories/evento_repository.dart';
import 'repositories/inscripcion_repository.dart';
import 'services/api_service.dart';
import 'services/auth_service.dart';
import 'services/token_store.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  final tokenStore = SecureTokenStore();
  final api = ApiService(baseUrl: ApiConfig.baseUrl, tokenStore: tokenStore);
  final authService = AuthService(api, tokenStore);
  final session = SessionController(authService);
  api.onUnauthorized = session.expire;
  await session.restore();

  runApp(
    MultiProvider(
      providers: [
        Provider<ApiService>.value(value: api),
        Provider<AuthService>.value(value: authService),
        ChangeNotifierProvider<SessionController>.value(value: session),
        Provider<EventoRepository>.value(
          value: BackendEventoRepository(api),
        ),
        Provider<InscripcionRepository>.value(
          value: BackendInscripcionRepository(api),
        ),
      ],
      child: const VidiaApp(),
    ),
  );
}
