import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:vidia/repositories/backend_inscripcion_repository.dart';
import 'package:vidia/services/api_service.dart';

import 'support/memory_token_store.dart';

void main() {
  test('inscripción usa JWT y envía únicamente eventoId', () async {
    final store = MemoryTokenStore()..token = 'jwt-de-prueba';
    late http.Request captured;
    final client = MockClient((request) async {
      captured = request;
      return http.Response(
        jsonEncode({
          'id': '20000000-0000-0000-0000-000000000001',
          'eventoId': '10000000-0000-0000-0000-000000000001',
          'eventoTitulo': 'Taller Flutter',
          'fechaInscripcion': '2026-09-25T18:30:00',
          'estado': 'CONFIRMADA',
        }),
        201,
      );
    });
    final repository = BackendInscripcionRepository(
      ApiService(
        baseUrl: 'https://api.example.test/api/v1',
        tokenStore: store,
        client: client,
      ),
    );

    final registration = await repository.create(
      '10000000-0000-0000-0000-000000000001',
    );

    expect(captured.method, 'POST');
    expect(captured.url.path, '/api/v1/inscripciones');
    expect(captured.headers['Authorization'], 'Bearer jwt-de-prueba');
    expect(jsonDecode(captured.body), {
      'eventoId': '10000000-0000-0000-0000-000000000001',
    });
    expect(registration.estado, 'CONFIRMADA');
  });

  test('mis inscripciones usa el endpoint protegido real', () async {
    final store = MemoryTokenStore()..token = 'jwt-de-prueba';
    final client = MockClient((request) async {
      expect(request.method, 'GET');
      expect(request.url.path, '/api/v1/inscripciones/mis-inscripciones');
      expect(request.headers['Authorization'], 'Bearer jwt-de-prueba');
      return http.Response(
        jsonEncode([
          {
            'id': '20000000-0000-0000-0000-000000000001',
            'eventoId': '10000000-0000-0000-0000-000000000001',
            'eventoTitulo': 'Taller Flutter',
            'fechaInscripcion': '2026-09-25T18:30:00',
            'estado': 'CONFIRMADA',
          }
        ]),
        200,
      );
    });
    final repository = BackendInscripcionRepository(
      ApiService(
        baseUrl: 'https://api.example.test/api/v1',
        tokenStore: store,
        client: client,
      ),
    );

    final registrations = await repository.fetchMine();

    expect(registrations, hasLength(1));
    expect(registrations.single.eventoTitulo, 'Taller Flutter');
  });

  test('inscripción pagada refleja el estado real PENDIENTE_PAGO', () async {
    final store = MemoryTokenStore()..token = 'jwt-de-prueba';
    final client = MockClient((request) async => http.Response(
        jsonEncode({
          'id': '20000000-0000-0000-0000-000000000002',
          'eventoId': '10000000-0000-0000-0000-000000000001',
          'eventoTitulo': 'Seminario pagado',
          'fechaInscripcion': '2026-09-25T18:30:00',
          'estado': 'PENDIENTE_PAGO',
        }),
        201));
    final repository = BackendInscripcionRepository(ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: client,
    ));
    final result =
        await repository.create('10000000-0000-0000-0000-000000000001');
    expect(result.estado, 'PENDIENTE_PAGO');
  });

  test('cancelación usa PATCH al endpoint real protegido', () async {
    final store = MemoryTokenStore()..token = 'jwt-de-prueba';
    late http.Request captured;
    final client = MockClient((request) async {
      captured = request;
      return http.Response('', 200);
    });
    final repository = BackendInscripcionRepository(ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: client,
    ));
    await repository.cancel('20000000-0000-0000-0000-000000000001');
    expect(captured.method, 'PATCH');
    expect(captured.url.path,
        '/api/v1/inscripciones/20000000-0000-0000-0000-000000000001/cancelar');
    expect(captured.headers['Authorization'], 'Bearer jwt-de-prueba');
  });
}
