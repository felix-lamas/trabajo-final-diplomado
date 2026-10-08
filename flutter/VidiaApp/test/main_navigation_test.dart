import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:provider/provider.dart';
import 'package:vidia/app.dart';
import 'package:vidia/controllers/account_overview_controller.dart';
import 'package:vidia/controllers/session_controller.dart';
import 'package:vidia/repositories/asistencia_repository.dart';
import 'package:vidia/repositories/backend_categoria_repository.dart';
import 'package:vidia/repositories/backend_evento_repository.dart';
import 'package:vidia/repositories/backend_inscripcion_repository.dart';
import 'package:vidia/repositories/categoria_repository.dart';
import 'package:vidia/repositories/certificado_repository.dart';
import 'package:vidia/repositories/evento_repository.dart';
import 'package:vidia/repositories/inscripcion_repository.dart';
import 'package:vidia/repositories/pago_repository.dart';
import 'package:vidia/screens/app_information_screen.dart';
import 'package:vidia/screens/attendance_sessions_screen.dart';
import 'package:vidia/screens/event_detail_screen.dart';
import 'package:vidia/screens/history_screen.dart';
import 'package:vidia/screens/login_screen.dart';
import 'package:vidia/screens/my_attendance_screen.dart';
import 'package:vidia/screens/my_certificates_screen.dart';
import 'package:vidia/screens/my_payments_screen.dart';
import 'package:vidia/screens/my_registrations_screen.dart';
import 'package:vidia/screens/payment_management_screen.dart';
import 'package:vidia/screens/profile_screen.dart';
import 'package:vidia/screens/registration_confirmation_screen.dart';
import 'package:vidia/services/api_service.dart';
import 'package:vidia/services/auth_service.dart';
import 'support/memory_token_store.dart';

final _event = <String, Object?>{
  'id': 'event-1',
  'titulo': 'Curso de Vidia',
  'descripcion': 'Un curso de prueba',
  'modalidad': 'VIRTUAL',
  'tipoInscripcion': 'PAGO',
  'costo': 50,
  'fechaInicio': '2099-08-10',
  'horaInicio': '10:00',
  'estado': 'PUBLICADO',
  'requiereInscripcion': true,
  'categoriaNombre': 'Curso',
};
final _registration = <String, Object?>{
  'id': 'registration-1',
  'eventoId': 'event-1',
  'eventoTitulo': 'Curso de Vidia',
  'estado': 'CONFIRMADA',
  'fechaInscripcion': '2026-01-01',
};
final _certificate = <String, Object?>{
  'id': 'certificate-1',
  'evento': 'Curso de Vidia',
  'tipoCertificado': 'CURRICULAR',
  'codigoCertificado': 'CERT-TEST',
  'estado': 'GENERADO',
};

class _Fixture {
  _Fixture({
    this.empty = false,
    this.paymentError = false,
    this.activeSession = true,
    this.recorded = false,
    this.participant = true,
  });
  final bool empty, paymentError, activeSession, recorded, participant;
  final requests = <http.Request>[];
  bool enrolled = true;
  late final store = MemoryTokenStore();
  late final api = ApiService(
    baseUrl: 'https://api.example.test/api/v1',
    tokenStore: store,
    client: MockClient((request) async {
      requests.add(request);
      final path = request.url.path.replaceFirst('/api/v1', '');
      Object? data;
      final user = {
        'id': 'user-1',
        'nombres': 'Felix',
        'apellidos': 'Prueba',
        'correoElectronico': 'felix@example.test',
        'correoVerificado': true,
        'roles': [participant ? 'USUARIO' : 'ORGANIZADOR'],
        'ci': '1234',
      };
      switch (path) {
        case '/auth/login':
          data = {'token': 'test-session', 'usuario': user};
        case '/auth/logout':
          return http.Response('', 200);
        case '/usuarios/perfil':
          data = user;
        case '/eventos/publicados':
        case '/eventos/publicados/buscar':
          data = empty ? [] : [_event];
        case '/eventos/event-1':
          data = _event;
        case '/categorias-evento/activas':
          data = [];
        case '/inscripciones/mis-inscripciones':
          data = empty || !enrolled ? [] : [_registration];
        case '/inscripciones':
          enrolled = true;
          data = _registration;
        case '/pagos/mis-pagos':
          if (paymentError) return http.Response('', 503);
          data = empty
              ? []
              : [
                  {
                    'id': 'payment-1',
                    'inscripcionId': 'registration-1',
                    'eventoTitulo': 'Curso de Vidia',
                    'monto': 50,
                    'estado': 'PENDIENTE_VALIDACION',
                  },
                ];
        case '/asistencias/mis-asistencias':
          data = empty || !recorded
              ? []
              : [
                  {
                    'id': 'attendance-1',
                    'sesionEventoId': 'session-1',
                    'evento': 'Curso de Vidia',
                    'sesion': 'Sesión de apertura',
                    'fechaHoraRegistro': '2026-01-02T10:00:00',
                    'resultadoValidacion': 'VALIDADA',
                  },
                ];
        case '/eventos/event-1/sesiones':
          data = [
            {
              'id': 'session-1',
              'eventoId': 'event-1',
              'nombre': 'Sesión de apertura',
              'requiereAsistencia': true,
              'activa': activeSession,
              'historica': false,
              'fecha': '2026-01-02',
            },
          ];
        case '/certificados/mis-certificados':
          data = empty ? [] : [_certificate];
        default:
          return http.Response('', 404);
      }
      return http.Response(
        jsonEncode(data),
        200,
        headers: {'content-type': 'application/json'},
      );
    }),
  );
  late final auth = AuthService(api, store);
  late final session = SessionController(auth);
  late final events = BackendEventoRepository(api);
  late final registrations = BackendInscripcionRepository(api);
  late final payments = BackendPagoRepository(api);
  late final attendance = BackendAsistenciaRepository(api);
  late final certificates = BackendCertificadoRepository(api);

  AccountOverviewController overview() => AccountOverviewController(
    eventsRepository: events,
    registrationsRepository: registrations,
    paymentsRepository: payments,
    attendanceRepository: attendance,
    certificatesRepository: certificates,
  );

  Future<void> mount(WidgetTester tester) async {
    await session.restore();
    await session.login(email: 'felix@example.test', password: 'test-password');
    await tester.pumpWidget(
      MultiProvider(
        providers: [
          Provider<AuthService>.value(value: auth),
          ChangeNotifierProvider<SessionController>.value(value: session),
          Provider<EventoRepository>.value(value: events),
          Provider<CategoriaRepository>.value(
            value: BackendCategoriaRepository(api),
          ),
          Provider<InscripcionRepository>.value(value: registrations),
          Provider<PagoRepository>.value(value: payments),
          Provider<AsistenciaRepository>.value(value: attendance),
          Provider<CertificadoRepository>.value(value: certificates),
        ],
        child: const VidiaApp(),
      ),
    );
    await tester.pumpAndSettle();
  }
}

Future<void> _tab(WidgetTester tester, String label) async {
  await tester.tap(
    find.descendant(of: find.byType(NavigationBar), matching: find.text(label)),
  );
  await tester.pumpAndSettle();
}

Future<void> _back(WidgetTester tester) async {
  await tester.pageBack();
  await tester.pumpAndSettle();
}

Future<void> _tap(WidgetTester tester, String label) async {
  if (find.text(label).evaluate().isEmpty) {
    final scrollable = find.byType(Scrollable).last;
    await tester.drag(scrollable, const Offset(0, 2000));
    await tester.pumpAndSettle();
    await tester.scrollUntilVisible(
      find.text(label),
      180,
      scrollable: scrollable,
    );
  }
  await Scrollable.ensureVisible(
    tester.element(find.text(label).last),
    alignment: 0.5,
  );
  await tester.pumpAndSettle();
  await tester.tap(find.text(label).last);
  await tester.pumpAndSettle();
}

void main() {
  WidgetController.hitTestWarningShouldBeFatal = true;
  testWidgets(
    'price filter uses existing query and clearing restores published events',
    (tester) async {
      final fixture = _Fixture();
      await fixture.mount(tester);
      await _tab(tester, 'Eventos');
      await _tap(tester, 'Filtrar eventos');
      await _tap(tester, 'Todos');
      await _tap(tester, 'Pagados');
      expect(fixture.requests.last.url.queryParameters['tipo'], 'PAGO');
      await _tap(tester, 'Limpiar filtros');
      expect(fixture.requests.last.url.path, '/api/v1/eventos/publicados');
      expect(tester.takeException(), isNull);
    },
  );
  testWidgets('four destinations, real summary, lazy tabs and system back', (
    tester,
  ) async {
    final fixture = _Fixture();
    await fixture.mount(tester);
    expect(find.byType(NavigationDestination), findsNWidgets(4));
    expect(find.text('Hola, Felix'), findsOneWidget);
    expect(find.text('Asistencia disponible'), findsOneWidget);
    expect(find.text('Tienes pagos pendientes'), findsOneWidget);
    expect(find.text('Certificados disponibles'), findsOneWidget);
    expect(
      fixture.requests.where(
        (request) => request.url.path.endsWith('/categorias-evento/activas'),
      ),
      isEmpty,
    );
    await _tab(tester, 'Eventos');
    await tester.enterText(find.byType(TextField), 'Curso');
    await tester.pump(const Duration(milliseconds: 400));
    await tester.pumpAndSettle();
    expect(fixture.requests.last.url.queryParameters['texto'], 'Curso');
    await _tab(tester, 'Actividad');
    expect(find.text('1 inscripción'), findsOneWidget);
    expect(find.text('1 pago · 1 pendiente'), findsOneWidget);
    await _tab(tester, 'Eventos');
    expect(
      tester.widget<TextField>(find.byType(TextField)).controller!.text,
      'Curso',
    );
    await tester.binding.handlePopRoute();
    await tester.pumpAndSettle();
    expect(
      tester.widget<NavigationBar>(find.byType(NavigationBar)).selectedIndex,
      0,
    );
    expect(tester.takeException(), isNull);
  });

  testWidgets(
    'Activity preserves payments, certificates, registrations and history routes',
    (tester) async {
      await _Fixture().mount(tester);
      await _tab(tester, 'Actividad');
      await _tap(tester, 'Mis pagos');
      expect(find.byType(MyPaymentsScreen), findsOneWidget);
      await _tap(tester, 'Curso de Vidia');
      expect(find.byType(PaymentManagementScreen), findsOneWidget);
      expect(find.text('Pendiente de validación'), findsOneWidget);
      await _back(tester);
      await _back(tester);
      await _tap(tester, 'Mis inscripciones');
      expect(find.byType(MyRegistrationsScreen), findsOneWidget);
      await _tap(tester, 'Curso de Vidia');
      expect(find.byType(EventDetailScreen), findsOneWidget);
      await _back(tester);
      await _back(tester);
      await _tap(tester, 'Mis certificados');
      expect(find.byType(MyCertificatesScreen), findsOneWidget);
      expect(find.byTooltip('Verificar certificado'), findsOneWidget);
      await _back(tester);
      await _tap(tester, 'Historial');
      expect(find.byType(HistoryScreen), findsOneWidget);
      await _back(tester);
      expect(
        tester.widget<NavigationBar>(find.byType(NavigationBar)).selectedIndex,
        2,
      );
      expect(tester.takeException(), isNull);
    },
  );

  testWidgets(
    'attendance groups records and reuses the existing sessions/QR entry',
    (tester) async {
      await _Fixture(recorded: true).mount(tester);
      expect(find.text('Asistencia disponible'), findsNothing);
      await _tab(tester, 'Actividad');
      await _tap(tester, 'Mis asistencias');
      expect(find.byType(MyAttendanceScreen), findsOneWidget);
      expect(find.text('Curso de Vidia'), findsOneWidget);
      expect(find.text('Sesión de apertura'), findsOneWidget);
      await _tap(tester, 'Registrar asistencia');
      expect(find.byType(AttendanceRegistrationScreen), findsOneWidget);
      await _tap(tester, 'Curso de Vidia');
      expect(find.byType(AttendanceSessionsScreen), findsOneWidget);
      expect(find.text('Registrada'), findsOneWidget);
      await _back(tester);
      await _back(tester);
      await _back(tester);
      expect(tester.takeException(), isNull);
    },
  );

  testWidgets(
    'active unrecorded session keeps QR action, inactive session does not advertise availability',
    (tester) async {
      await _Fixture().mount(tester);
      await _tap(tester, 'Asistencia disponible');
      expect(find.byType(AttendanceSessionsScreen), findsOneWidget);
      expect(find.text('Escanear código de asistencia'), findsOneWidget);
      await _back(tester);
      await tester.pumpWidget(const SizedBox());
      await _Fixture(activeSession: false).mount(tester);
      expect(find.text('Asistencia disponible'), findsNothing);
    },
  );

  testWidgets(
    'Profile information, password entry and logout preserve authentication',
    (tester) async {
      final fixture = _Fixture();
      await fixture.mount(tester);
      await _tab(tester, 'Perfil');
      expect(find.byType(ProfileScreen), findsOneWidget);
      expect(find.text('Felix Prueba'), findsOneWidget);
      expect(find.text('Correo verificado'), findsOneWidget);
      for (final label in ['Ayuda', 'Contacto', 'Acerca de Vidia']) {
        await _tap(tester, label);
        expect(find.byType(AppInformationScreen), findsOneWidget);
        await _back(tester);
      }
      await _tap(tester, 'Cambiar contraseña');
      expect(find.byType(AlertDialog), findsOneWidget);
      await _tap(tester, 'Cancelar');
      await _tap(tester, 'Cerrar sesión');
      expect(find.byType(LoginScreen), findsOneWidget);
      expect(fixture.session.isAuthenticated, isFalse);
      expect(tester.takeException(), isNull);
    },
  );

  testWidgets('event enrollment and confirmation return to the Events tab', (
    tester,
  ) async {
    final fixture = _Fixture()..enrolled = false;
    await fixture.mount(tester);
    await _tab(tester, 'Eventos');
    await _tap(tester, 'Curso de Vidia');
    expect(find.byType(EventDetailScreen), findsOneWidget);
    await _tap(tester, 'Solicitar inscripción · Bs. 50.00');
    expect(find.byType(RegistrationConfirmationScreen), findsOneWidget);
    expect(
      fixture.requests
          .where(
            (request) =>
                request.method == 'POST' &&
                request.url.path.endsWith('/inscripciones'),
          )
          .length,
      1,
    );
    await _back(tester);
    expect(
      tester.widget<NavigationBar>(find.byType(NavigationBar)).selectedIndex,
      1,
    );
    expect(tester.takeException(), isNull);
  });

  testWidgets(
    'failed section is not a zero count and other sections remain available',
    (tester) async {
      await _Fixture(paymentError: true).mount(tester);
      expect(find.text('Tienes pagos pendientes'), findsNothing);
      await _tab(tester, 'Actividad');
      expect(find.text('No disponible · toca para consultar'), findsOneWidget);
      expect(find.text('1 certificado'), findsOneWidget);
      await _tap(tester, 'Mis pagos');
      expect(find.text('No pudimos consultar tus pagos.'), findsOneWidget);
      expect(tester.takeException(), isNull);
    },
  );

  testWidgets(
    'empty states and no confirmed registrations do not invent activity',
    (tester) async {
      await _Fixture(empty: true).mount(tester);
      expect(find.text('Sin actividades próximas confirmadas'), findsOneWidget);
      expect(find.text('Asistencia disponible'), findsNothing);
      await _tab(tester, 'Actividad');
      expect(find.text('0 inscripciones'), findsOneWidget);
      await _tap(tester, 'Mis asistencias');
      expect(
        find.text('Todavía no tienes asistencias registradas.'),
        findsOneWidget,
      );
      await _tap(tester, 'Registrar asistencia');
      expect(
        find.text(
          'Necesitas una inscripción confirmada para registrar asistencia.',
        ),
        findsOneWidget,
      );
    },
  );

  testWidgets('organizer restrictions remain and Profile is still available', (
    tester,
  ) async {
    final fixture = _Fixture(participant: false);
    await fixture.mount(tester);
    expect(
      find.textContaining('Canal móvil para participantes'),
      findsOneWidget,
    );
    expect(
      fixture.requests.any((request) => request.url.path.contains('/mis-')),
      isFalse,
    );
    await _tab(tester, 'Perfil');
    expect(find.byType(ProfileScreen), findsOneWidget);
  });

  for (final (size, scale) in [
    (const Size(320, 568), 1.0),
    (const Size(412, 915), 1.0),
    (const Size(740, 360), 1.0),
    (const Size(320, 568), 1.8),
  ]) {
    testWidgets(
      'no overflow across main tabs, filters and information at $size scale $scale',
      (tester) async {
        tester.view.physicalSize = size;
        tester.view.devicePixelRatio = 1;
        tester.platformDispatcher.textScaleFactorTestValue = scale;
        addTearDown(tester.view.resetPhysicalSize);
        addTearDown(tester.view.resetDevicePixelRatio);
        addTearDown(tester.platformDispatcher.clearTextScaleFactorTestValue);
        await _Fixture().mount(tester);
        for (final label in ['Eventos', 'Actividad', 'Perfil']) {
          await _tab(tester, label);
          expect(tester.takeException(), isNull);
        }
        await _tap(tester, 'Ayuda');
        expect(tester.takeException(), isNull);
        await _back(tester);
        await _tab(tester, 'Eventos');
        await _tap(tester, 'Filtrar eventos');
        expect(tester.takeException(), isNull);
      },
    );
  }
}
