import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:vidia/services/api_service.dart';
import 'package:vidia/services/auth_service.dart';

import 'support/memory_token_store.dart';

void main() {
  test('login respeta el contrato y persiste la sesión', () async {
    final store = MemoryTokenStore();
    final client = MockClient((request) async {
      expect(request.method, 'POST');
      expect(request.url.path, '/api/v1/auth/login');
      expect(request.headers.containsKey('Authorization'), isFalse);
      expect(jsonDecode(request.body), {
        'correoElectronico': 'usuario@demo.local',
        'contrasena': 'clave-de-prueba',
      });
      return http.Response(
        jsonEncode({
          'token': 'jwt-de-prueba',
          'usuario': {
            'id': '00000000-0000-0000-0000-000000000001',
            'nombres': 'Usuario',
            'apellidos': 'Demo',
            'correoElectronico': 'usuario@demo.local',
            'roles': ['USUARIO'],
          },
        }),
        200,
      );
    });
    final api = ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: client,
    );

    final session = await AuthService(api, store).signIn(
      email: ' usuario@demo.local ',
      password: 'clave-de-prueba',
    );

    expect(session.user.correoElectronico, 'usuario@demo.local');
    expect(session.user.roles, ['USUARIO']);
    expect(store.token, 'jwt-de-prueba');
    expect(store.user, isNotEmpty);
  });
}
