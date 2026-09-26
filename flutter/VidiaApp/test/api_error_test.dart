import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:vidia/services/api_exception.dart';
import 'package:vidia/services/api_service.dart';

import 'support/memory_token_store.dart';

void main() {
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
}
