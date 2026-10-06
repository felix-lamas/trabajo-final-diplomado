import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:vidia/repositories/backend_evento_repository.dart';
import 'package:vidia/services/api_exception.dart';
import 'package:vidia/services/api_service.dart';

import 'support/memory_token_store.dart';

void main() {
  const eventJson = {
    'id': '10000000-0000-0000-0000-000000000001',
    'titulo': 'Taller Flutter',
    'descripcion': 'Evento de prueba',
    'objetivos': 'Aprender',
    'categoriaNombre': 'Taller',
    'modalidad': 'PRESENCIAL',
    'tipoInscripcion': 'GRATUITO',
    'costo': 0,
    'fechaInicio': '2026-10-15',
    'fechaFin': '2026-10-15',
    'horaInicio': '09:30:00',
    'horaFin': '11:30:00',
    'estado': 'PUBLICADO',
    'requiereInscripcion': true,
    'cupoLimitado': true,
    'cupoDisponible': 20,
    'emiteCertificado': true,
  };

  test('consulta catálogo y detalle por los endpoints públicos reales',
      () async {
    final requests = <http.Request>[];
    final client = MockClient((request) async {
      requests.add(request);
      if (request.url.path.endsWith('/publicados')) {
        return http.Response(jsonEncode([eventJson]), 200);
      }
      return http.Response(jsonEncode(eventJson), 200);
    });
    final repository = BackendEventoRepository(
      ApiService(
        baseUrl: 'https://api.example.test/api/v1',
        tokenStore: MemoryTokenStore(),
        client: client,
      ),
    );

    final events = await repository.fetchPublished();
    final detail = await repository.fetchById(events.single.id);

    expect(events.single.titulo, 'Taller Flutter');
    expect(events.single.esGratuito, isTrue);
    expect(detail.horaInicio, '09:30');
    expect(requests[0].url.path, '/api/v1/eventos/publicados');
    expect(
      requests[1].url.path,
      '/api/v1/eventos/10000000-0000-0000-0000-000000000001',
    );
    expect(
        requests
            .every((request) => !request.headers.containsKey('Authorization')),
        isTrue);
  });

  test('búsqueda envía exclusivamente filtros soportados por backend',
      () async {
    late http.Request captured;
    final client = MockClient((request) async {
      captured = request;
      return http.Response('[]', 200);
    });
    final repository = BackendEventoRepository(ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: MemoryTokenStore(),
      client: client,
    ));
    await repository.searchPublished(
      text: ' innovación ',
      categoryId: '30000000-0000-0000-0000-000000000001',
      type: 'PAGO',
      modality: 'VIRTUAL',
    );
    expect(captured.url.path, '/api/v1/eventos/publicados/buscar');
    expect(captured.url.queryParameters, {
      'texto': 'innovación',
      'categoriaId': '30000000-0000-0000-0000-000000000001',
      'tipo': 'PAGO',
      'modalidad': 'VIRTUAL',
    });
    expect(captured.headers.containsKey('Authorization'), isFalse);
  });

  test('catálogo vacío devuelve lista vacía', () async {
    final repository = BackendEventoRepository(ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: MemoryTokenStore(),
      client: MockClient((_) async => http.Response('[]', 200)),
    ));
    expect(await repository.fetchPublished(), isEmpty);
  });

  test('error del catálogo se presenta como excepción segura del API',
      () async {
    final repository = BackendEventoRepository(ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: MemoryTokenStore(),
      client: MockClient((_) async => http.Response(
            jsonEncode(
                {'codigo': 'INTERNAL_ERROR', 'mensaje': 'detalle interno'}),
            500,
          )),
    ));
    await expectLater(
      repository.fetchPublished(),
      throwsA(isA<ApiException>().having(
        (error) => error.message,
        'message',
        'Ocurrió un error en el servidor. Inténtalo más tarde.',
      )),
    );
  });

  test('detalle no visible conserva el 404 real del backend', () async {
    final repository = BackendEventoRepository(ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: MemoryTokenStore(),
      client: MockClient((_) async => http.Response(
            jsonEncode({
              'codigo': 'RESOURCE_NOT_FOUND',
              'mensaje': 'Evento no encontrado'
            }),
            404,
          )),
    ));
    await expectLater(
      repository.fetchById('10000000-0000-0000-0000-000000000099'),
      throwsA(isA<ApiException>()
          .having((error) => error.statusCode, 'statusCode', 404)),
    );
  });
}
