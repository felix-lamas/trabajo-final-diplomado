import 'dart:async';
import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:vidia/services/api_exception.dart';
import 'package:vidia/services/api_service.dart';

import 'support/memory_token_store.dart';

void main() {
  test('pagos mapea los errores de ownership, estado y archivo', () async {
    for (final (status, code, expected) in [
      (401, 'AUTH_REQUIRED', 'Tu sesión expiró o fue revocada. Inicia sesión nuevamente.'),
      (403, 'ACCESS_DENIED', 'No tienes permisos para realizar esta operación.'),
      (404, 'PAYMENT_RECEIPT_NOT_FOUND', 'Recurso no encontrado.'),
      (409, 'PAYMENT_INVALID_STATE', 'El estado actual del pago no admite esta operacion.'),
      (413, 'FILE_TOO_LARGE', 'El archivo supera el tamano maximo permitido.'),
    ]) {
      final store = MemoryTokenStore()..token = 'jwt';
      final api = ApiService(
        baseUrl: 'https://api.example.test/api/v1',
        tokenStore: store,
        client: MockClient((_) async => http.Response(
              jsonEncode({'codigo': code, 'mensaje': 'Error demo'}),
              status,
            )),
      );

      await expectLater(
        api.getBinary('/pagos/$code/comprobante'),
        throwsA(isA<ApiException>()
            .having((error) => error.statusCode, 'status', status)
            .having((error) => error.message, 'message', expected)),
      );
      if (status == 401) expect(store.token, isNull);
    }
  });

  test('409 se presenta como inscripción duplicada', () async {
    final store = MemoryTokenStore()..token = 'jwt';
    final api = ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient(
        (_) async => http.Response(
          jsonEncode({'mensaje': 'Conflicto'}),
          409,
        ),
      ),
    );

    expect(
      () => api.post('/inscripciones', body: {'eventoId': 'id'}),
      throwsA(
        isA<ApiException>().having(
          (error) => error.message,
          'message',
          'Ya estás inscrito en este evento.',
        ),
      ),
    );
  });

  test('401 protegido limpia el token y notifica expiración', () async {
    final store = MemoryTokenStore()..token = 'jwt';
    var expired = false;
    final api = ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient((_) async => http.Response('', 401)),
    )..onUnauthorized = () async => expired = true;

    await expectLater(api.get('/inscripciones/mis-inscripciones'),
        throwsA(isA<ApiException>()));

    expect(store.token, isNull);
    expect(expired, isTrue);
  });

  test('409 interpreta los códigos reales de duplicado y capacidad', () async {
    for (final (code, expected) in [
      ('INSCRIPTION_DUPLICATED', 'Ya tienes una inscripción en este evento.'),
      ('INSCRIPTION_CAPACITY_FULL', 'El evento ya no tiene cupos disponibles.'),
    ]) {
      final api = ApiService(
        baseUrl: 'https://api.example.test/api/v1',
        tokenStore: MemoryTokenStore()..token = 'jwt',
        client: MockClient((_) async => http.Response(
              jsonEncode({'codigo': code, 'mensaje': 'Conflicto'}),
              409,
            )),
      );
      await expectLater(
        api.post('/inscripciones', body: {'eventoId': 'id'}),
        throwsA(isA<ApiException>()
            .having((error) => error.message, 'message', expected)),
      );
    }
  });

  test('concurrent 401 responses share one session-expiration callback',
      () async {
    final store = MemoryTokenStore()..token = 'jwt';
    final bothArrived = Completer<void>();
    var responses = 0;
    var expirations = 0;
    final api = ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient((_) async {
        responses++;
        if (responses == 2) bothArrived.complete();
        await bothArrived.future;
        return http.Response('', 401);
      }),
    )..onUnauthorized = () async {
        expirations++;
        await Future<void>.delayed(const Duration(milliseconds: 20));
      };

    await Future.wait([
      api.get('/usuarios/perfil'),
      api.get('/usuarios/perfil'),
    ].map((request) => request.catchError((_) => null)));

    expect(expirations, 1);
    expect(store.token, isNull);
  });
}
