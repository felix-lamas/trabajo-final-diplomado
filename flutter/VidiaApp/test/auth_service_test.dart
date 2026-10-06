import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:vidia/services/api_service.dart';
import 'package:vidia/services/auth_service.dart';

import 'support/memory_token_store.dart';

Map<String, dynamic> profile({
  List<String> roles = const ['USUARIO'],
  String applicationState = 'NINGUNA',
}) =>
    {
      'id': '00000000-0000-0000-0000-000000000001',
      'nombres': 'Usuario',
      'apellidos': 'Demo',
      'correoElectronico': 'usuario@example.test',
      'correoVerificado': true,
      'tipoUsuario': 'INTERNO',
      'estadoSolicitudOrganizador': applicationState,
      'roles': roles,
    };

void main() {
  test('login respeta contrato, guarda token seguro vía TokenStore y rol',
      () async {
    final store = MemoryTokenStore();
    final client = MockClient((request) async {
      expect(request.method, 'POST');
      expect(request.url.path, '/api/v1/auth/login');
      expect(request.headers.containsKey('Authorization'), isFalse);
      expect(jsonDecode(request.body), {
        'correoElectronico': 'usuario@example.test',
        'contrasena': 'clave-de-prueba',
      });
      return http.Response(
          jsonEncode({'token': 'jwt-test-only', 'usuario': profile()}), 200);
    });
    final api = ApiService(
        baseUrl: 'https://api.example.test/api/v1',
        tokenStore: store,
        client: client);

    final session = await AuthService(api, store)
        .signIn(email: ' usuario@example.test ', password: 'clave-de-prueba');

    expect(session.user.correoElectronico, 'usuario@example.test');
    expect(session.user.isUsuario, isTrue);
    expect(session.user.isOrganizador, isFalse);
    expect(store.token, 'jwt-test-only');
    expect(store.user, isNotEmpty);
  });

  test('registro envía los campos del DTO y no inicia sesión', () async {
    final store = MemoryTokenStore();
    final api = ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient((request) async {
        expect(request.url.path, '/api/v1/auth/registro');
        expect(request.headers.containsKey('Authorization'), isFalse);
        expect(jsonDecode(request.body), {
          'nombres': 'Ana',
          'apellidos': 'Prueba',
          'correoElectronico': 'ana@example.test',
          'ci': '12345678',
          'ru': 'RU-20260001',
          'celular': '72900000',
          'contrasena': 'ClaveSeguro1!',
          'confirmacionContrasena': 'ClaveSeguro1!',
          'tipoUsuario': 'INTERNO',
        });
        return http.Response(
            jsonEncode({
              'correoElectronico': 'ana@example.test',
              'correoVerificado': false,
              'mensaje': 'Verifique su correo'
            }),
            200);
      }),
    );
    final result = await AuthService(api, store).register({
      'nombres': 'Ana',
      'apellidos': 'Prueba',
      'correoElectronico': 'ana@example.test',
      'ci': '12345678',
      'ru': 'RU-20260001',
      'celular': '72900000',
      'contrasena': 'ClaveSeguro1!',
      'confirmacionContrasena': 'ClaveSeguro1!',
      'tipoUsuario': 'INTERNO',
    });

    expect(result['correoVerificado'], false);
    expect(store.token, isNull);
  });

  test('verification and recovery use actual token/email fields', () async {
    final seen = <String>[];
    final store = MemoryTokenStore();
    final api = ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient((request) async {
        seen.add(request.url.path);
        final body = jsonDecode(request.body) as Map<String, dynamic>;
        if (request.url.path.endsWith('/verificar-correo')) {
          expect(body, {'token': 'mail-token'});
        }
        if (request.url.path.endsWith('/reenviar-verificacion') ||
            request.url.path.endsWith('/recuperar-contrasena')) {
          expect(body, {'correoElectronico': 'ana@example.test'});
        }
        if (request.url.path.endsWith('/restablecer-contrasena')) {
          expect(body, {
            'token': 'reset-token',
            'nuevaContrasena': 'NuevaClave1!',
            'confirmacion': 'NuevaClave1!'
          });
        }
        return http.Response('', 200);
      }),
    );
    final auth = AuthService(api, store);
    await auth.verifyEmail('mail-token');
    await auth.resendVerification('ana@example.test');
    await auth.requestPasswordReset('ana@example.test');
    await auth.resetPassword(
        token: 'reset-token',
        password: 'NuevaClave1!',
        confirmation: 'NuevaClave1!');
    expect(seen, [
      '/api/v1/auth/verificar-correo',
      '/api/v1/auth/reenviar-verificacion',
      '/api/v1/auth/recuperar-contrasena',
      '/api/v1/auth/restablecer-contrasena',
    ]);
  });

  test('restauración consulta perfil al backend y actualiza estado organizador',
      () async {
    final store = MemoryTokenStore()
      ..token = 'jwt-test-only'
      ..user = jsonEncode(profile());
    final api = ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient((request) async {
        expect(request.method, 'GET');
        expect(request.url.path, '/api/v1/usuarios/perfil');
        expect(request.headers['Authorization'], 'Bearer jwt-test-only');
        return http.Response(
            jsonEncode(profile(applicationState: 'PENDIENTE')), 200);
      }),
    );
    final user = await AuthService(api, store).restoreUser();
    expect(user?.organizadorPendiente, isTrue);
    expect(user?.roles, ['USUARIO']);
    expect(jsonDecode(store.user!)['estadoSolicitudOrganizador'], 'PENDIENTE');
  });

  test('logout revoca en backend y limpia almacenamiento incluso ante error',
      () async {
    final store = MemoryTokenStore()..token = 'jwt-test-only';
    var requestSeen = false;
    final api = ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient((request) async {
        requestSeen = true;
        expect(request.method, 'POST');
        expect(request.url.path, '/api/v1/auth/logout');
        expect(request.headers['Authorization'], 'Bearer jwt-test-only');
        return http.Response('', 204);
      }),
    );
    await AuthService(api, store).signOut();
    expect(requestSeen, isTrue);
    expect(store.token, isNull);
  });

  test('registro inválido conserva el error real del backend', () async {
    final store = MemoryTokenStore();
    final api = ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient((_) async => http.Response(
          jsonEncode({
            'codigo': 'VALIDATION_ERROR',
            'mensaje': 'Datos de entrada invalidos',
            'detalles': ['ru: El RU es obligatorio para usuarios UAJMS'],
          }),
          400)),
    );
    await expectLater(
      AuthService(api, store).register({'tipoUsuario': 'INTERNO'}),
      throwsA(isA<Exception>()),
    );
    expect(store.token, isNull);
  });

  test('logout limpia localmente aunque falle la petición al servidor',
      () async {
    final store = MemoryTokenStore()..token = 'jwt-test-only';
    final api = ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient((_) async => http.Response('', 503)),
    );
    await expectLater(
        AuthService(api, store).signOut(), throwsA(isA<Exception>()));
    expect(store.token, isNull);
  });

  test('cambio de contrasena respeta contrato y limpia sesion revocada',
      () async {
    final store = MemoryTokenStore()..token = 'jwt-test-only';
    final api = ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient((request) async {
        expect(request.method, 'POST');
        expect(request.url.path, '/api/v1/usuarios/cambiar-contrasena');
        expect(jsonDecode(request.body), {
          'contrasenaActual': 'Actual1!',
          'nuevaContrasena': 'NuevaClave1!',
          'confirmacion': 'NuevaClave1!',
        });
        return http.Response('', 200);
      }),
    );
    await AuthService(api, store).changePassword(
      currentPassword: 'Actual1!',
      newPassword: 'NuevaClave1!',
      confirmation: 'NuevaClave1!',
    );
    expect(store.token, isNull);
  });
}
