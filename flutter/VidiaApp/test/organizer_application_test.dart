import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:vidia/screens/organizer_application_screen.dart';
import 'package:vidia/services/api_service.dart';
import 'package:vidia/services/auth_service.dart';
import 'support/memory_token_store.dart';

void main() {
  late List<http.Request> requests;
  late Map<String, dynamic> application;
  late AuthService auth;
  bool failPost = false;
  setUp(() {
    requests = [];
    failPost = false;
    application = {'estado': 'NINGUNA', 'puedeSolicitar': true};
    final store = MemoryTokenStore()..token = 'local-test-only';
    auth = AuthService(
      ApiService(
        baseUrl: 'https://example.test/api/v1',
        tokenStore: store,
        client: MockClient((request) async {
          requests.add(request);
          if (request.url.path.endsWith('/tipos-eventos')) {
            return http.Response(
              jsonEncode([
                {'codigo': 'CURSOS_TALLERES', 'nombre': 'Cursos y talleres'},
              ]),
              200,
            );
          }
          if (request.method == 'POST') {
            if (failPost) {
              return http.Response(
                jsonEncode({'mensaje': 'Solicitud duplicada'}),
                400,
              );
            }
            application = {
              ...application,
              'estado': 'PENDIENTE',
              'puedeSolicitar': false,
            };
          }
          return http.Response(jsonEncode(application), 200);
        }),
      ),
      store,
    );
  });
  Future<void> open(WidgetTester tester) async {
    await tester.pumpWidget(
      MaterialApp(home: OrganizerApplicationScreen(auth: auth)),
    );
    await tester.pumpAndSettle();
  }

  Future<void> submit(WidgetTester tester) async {
    await tester.enterText(
      find.byType(TextFormField).first,
      'Deseo organizar eventos universitarios educativos',
    );
    await tester.tap(find.byType(CheckboxListTile).first);
    await tester.ensureVisible(find.text('Solicitar ser organizador'));
    await tester.tap(find.text('Solicitar ser organizador'));
    await tester.pumpAndSettle();
  }

  testWidgets('valida motivo sin enviar al backend', (tester) async {
    await open(tester);
    await tester.ensureVisible(find.text('Solicitar ser organizador'));
    await tester.tap(find.text('Solicitar ser organizador'));
    await tester.pumpAndSettle();
    expect(find.text('Escribe entre 30 y 1000 caracteres.'), findsOneWidget);
    expect(requests.where((r) => r.method == 'POST'), isEmpty);
  });
  testWidgets('envia contrato autenticado y muestra pendiente', (tester) async {
    await open(tester);
    await submit(tester);
    final post = requests.singleWhere((r) => r.method == 'POST');
    expect(post.url.path, '/api/v1/usuarios/solicitud-organizador');
    expect(post.headers.containsKey('Authorization'), true);
    expect(jsonDecode(post.body), {
      'motivoSolicitud': 'Deseo organizar eventos universitarios educativos',
      'tiposEventos': ['CURSOS_TALLERES'],
      'informacionAdicional': '',
    });
    expect(find.text('Estado: PENDIENTE'), findsOneWidget);
    expect(find.byType(TextFormField), findsNothing);
  });
  for (final state in ['PENDIENTE', 'APROBADA']) {
    testWidgets('$state no permite duplicar', (tester) async {
      application = {'estado': state, 'puedeSolicitar': false};
      await open(tester);
      expect(find.text('Estado: $state'), findsOneWidget);
      expect(find.byType(TextFormField), findsNothing);
      expect(requests.where((r) => r.method == 'POST'), isEmpty);
    });
  }
  testWidgets('rechazo muestra motivo y permite nueva solicitud autorizada', (
    tester,
  ) async {
    application = {
      'estado': 'RECHAZADA',
      'puedeSolicitar': true,
      'motivoRechazo': 'Completa la propuesta',
    };
    await open(tester);
    expect(
      find.text('Motivo del rechazo: Completa la propuesta'),
      findsOneWidget,
    );
    await submit(tester);
    expect(find.text('Estado: PENDIENTE'), findsOneWidget);
  });
  testWidgets('error backend conserva formulario', (tester) async {
    failPost = true;
    await open(tester);
    await submit(tester);
    expect(find.text('Solicitud duplicada'), findsOneWidget);
    expect(find.byType(TextFormField), findsNWidgets(2));
  });
  test('tipos provienen del catalogo del backend', () async {
    final types = await auth.organizerEventTypes();
    expect(types.single.code, 'CURSOS_TALLERES');
    expect(types.single.name, 'Cursos y talleres');
  });
  test('estado historico admite campos omitidos', () async {
    application = {'estado': 'RECHAZADA', 'puedeSolicitar': true};
    final result = await auth.organizerApplication();
    expect(result.reason, isNull);
    expect(result.eventTypes, isEmpty);
  });
  testWidgets('rechazada sin permiso backend no habilita nueva solicitud', (
    tester,
  ) async {
    application = {'estado': 'RECHAZADA', 'puedeSolicitar': false};
    await open(tester);
    expect(find.byType(TextFormField), findsNothing);
  });
  testWidgets('exige seleccionar tipo sin realizar POST', (tester) async {
    await open(tester);
    await tester.enterText(
      find.byType(TextFormField).first,
      'Deseo organizar eventos universitarios educativos',
    );
    await tester.ensureVisible(find.text('Solicitar ser organizador'));
    await tester.tap(find.text('Solicitar ser organizador'));
    await tester.pumpAndSettle();
    expect(find.text('Selecciona al menos un tipo de evento.'), findsOneWidget);
    expect(requests.where((r) => r.method == 'POST'), isEmpty);
  });
  testWidgets('error de red al cargar permite reintentar', (tester) async {
    final store = MemoryTokenStore()..token = 'local-test-only';
    auth = AuthService(
      ApiService(
        baseUrl: 'https://example.test/api/v1',
        tokenStore: store,
        client: MockClient((_) async => throw Exception('test network')),
      ),
      store,
    );
    await open(tester);
    expect(find.text('Actualizar estado'), findsOneWidget);
    expect(find.byType(TextFormField), findsNothing);
    expect(tester.takeException(), isNull);
  });
  testWidgets('formulario no desborda en telefono pequeno', (tester) async {
    tester.view.physicalSize = const Size(320, 568);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    await open(tester);
    await tester.ensureVisible(find.text('Solicitar ser organizador'));
    expect(tester.takeException(), isNull);
  });
}
