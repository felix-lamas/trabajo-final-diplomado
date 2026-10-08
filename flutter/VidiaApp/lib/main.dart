import 'dart:async';

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'app.dart';
import 'config/app_config.dart';
import 'controllers/session_controller.dart';
import 'repositories/backend_evento_repository.dart';
import 'repositories/backend_categoria_repository.dart';
import 'repositories/categoria_repository.dart';
import 'repositories/backend_inscripcion_repository.dart';
import 'repositories/evento_repository.dart';
import 'repositories/inscripcion_repository.dart';
import 'repositories/pago_repository.dart';
import 'repositories/asistencia_repository.dart';
import 'repositories/certificado_repository.dart';
import 'services/attendance_location_source.dart';
import 'services/certificado_document_manager.dart';
import 'services/certificado_file_service.dart';
import 'services/api_service.dart';
import 'services/auth_service.dart';
import 'services/token_store.dart';
import 'screens/auth_gate.dart';
import 'screens/auth_link_screens.dart';
import 'services/auth_link_coordinator.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  final links = AuthLinkCoordinator(
    source: PlatformAuthLinkSource(),
    onLink: (link) => appNavigatorKey.currentState?.pushAndRemoveUntil<void>(
        authLinkRoute(link), (route) => route.isFirst),
  );
  unawaited(links.start());
  final tokenStore = SecureTokenStore();
  final api = ApiService(baseUrl: ApiConfig.baseUrl, tokenStore: tokenStore);
  final authService = AuthService(api, tokenStore);
  final session = SessionController(authService);
  api.onUnauthorized = session.expire;
  session.onSessionExpired = () async {
    appNavigatorKey.currentState?.pushAndRemoveUntil<void>(
      MaterialPageRoute<void>(builder: (_) => const AuthGate()),
      (_) => false,
    );
  };

  runApp(
    MultiProvider(
      providers: [
        Provider<ApiService>.value(value: api),
        Provider<AuthService>.value(value: authService),
        ChangeNotifierProvider<SessionController>.value(value: session),
        Provider<EventoRepository>.value(
          value: BackendEventoRepository(api),
        ),
        Provider<CategoriaRepository>.value(
          value: BackendCategoriaRepository(api),
        ),
        Provider<InscripcionRepository>.value(
          value: BackendInscripcionRepository(api),
        ),
        Provider<PagoRepository>.value(value: BackendPagoRepository(api)),
        Provider<AsistenciaRepository>.value(
          value: BackendAsistenciaRepository(api),
        ),
        Provider<CertificadoRepository>.value(
          value: BackendCertificadoRepository(api),
        ),
        Provider<CertificadoFileService>.value(
          value: PlatformCertificadoFileService(),
        ),
        Provider<CertificadoDocumentManager>(
          create: (context) => CertificadoDocumentManager(
            repository: context.read<CertificadoRepository>(),
            files: context.read<CertificadoFileService>(),
          ),
        ),
        Provider<AttendanceLocationSource>.value(
          value: GeolocatorAttendanceLocationSource(),
        ),
      ],
      child: VidiaApp(authLinks: links),
    ),
  );
  unawaited(session.restore());
}
