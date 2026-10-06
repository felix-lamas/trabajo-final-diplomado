import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:vidia/controllers/session_controller.dart';
import 'package:vidia/services/api_service.dart';
import 'package:vidia/services/auth_service.dart';

import 'support/memory_token_store.dart';

Map<String, dynamic> profile(List<String> roles, String state) => {
      'id': 'user-id',
      'nombres': 'Prueba',
      'apellidos': 'Vidia',
      'correoElectronico': 'prueba@example.test',
      'correoVerificado': true,
      'tipoUsuario': 'EXTERNO',
      'estadoSolicitudOrganizador': state,
      'roles': roles,
    };

void main() {
  test('login identifica los tres roles reales sin inferirlos de caché',
      () async {
    for (final role in ['USUARIO', 'ORGANIZADOR', 'ADMINISTRADOR']) {
      final store = MemoryTokenStore();
      final api = ApiService(
        baseUrl: 'https://api.example.test/api/v1',
        tokenStore: store,
        client: MockClient((_) async => http.Response(
            jsonEncode({
              'token': 'test-token',
              'usuario': profile([role], 'NINGUNA'),
            }),
            200)),
      );
      final controller = SessionController(AuthService(api, store));
      expect(
          await controller.login(
              email: 'prueba@example.test', password: 'test'),
          isTrue);
      expect(controller.user!.roles, [role]);
      expect(controller.user!.isUsuario, role == 'USUARIO');
      expect(controller.user!.isOrganizador, role == 'ORGANIZADOR');
      expect(controller.user!.isAdministrador, role == 'ADMINISTRADOR');
    }
  });

  test('login con EMAIL_NOT_VERIFIED muestra flujo de verificación', () async {
    final store = MemoryTokenStore();
    final api = ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient((_) async => http.Response(
          jsonEncode({
            'codigo': 'EMAIL_NOT_VERIFIED',
            'mensaje': 'Debe verificar su correo antes de iniciar sesion',
          }),
          400)),
    );
    final controller = SessionController(AuthService(api, store));
    expect(
        await controller.login(email: 'prueba@example.test', password: 'test'),
        isFalse);
    expect(controller.message, contains('todavía no está verificado'));
    expect(store.token, isNull);
  });

  test('login incorrecto muestra el error del backend sin persistir sesión',
      () async {
    final store = MemoryTokenStore();
    final api = ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient((_) async => http.Response(
          jsonEncode({
            'codigo': 'AUTH_INVALID_CREDENTIALS',
            'mensaje': 'Correo electronico o contrasena incorrectos',
          }),
          401)),
    );
    final controller = SessionController(AuthService(api, store));

    expect(
        await controller.login(
            email: 'usuario@example.test', password: 'incorrecta'),
        isFalse);
    expect(controller.message, contains('incorrectos'));
    expect(controller.isAuthenticated, isFalse);
    expect(store.token, isNull);
  });

  test('401 en restauración limpia la sesión y expira una sola vez', () async {
    final store = MemoryTokenStore()..token = 'expired-test-token';
    var expired = 0;
    final api = ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient((_) async => http.Response('', 401)),
    );
    final controller = SessionController(AuthService(api, store));
    controller.onSessionExpired = () async => expired++;
    api.onUnauthorized = controller.expire;

    await controller.restore();

    expect(controller.isAuthenticated, isFalse);
    expect(controller.initializing, isFalse);
    expect(store.token, isNull);
    expect(expired, 1);
  });

  test(
      'error de red al restaurar no abre sesión basada solo en perfil cacheado',
      () async {
    final store = MemoryTokenStore()
      ..token = 'token-test'
      ..user = jsonEncode(profile(['USUARIO'], 'NINGUNA'));
    final api = ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient((_) async => throw Exception('network')),
    );
    final controller = SessionController(AuthService(api, store));

    await controller.restore();

    expect(controller.isAuthenticated, isFalse);
    expect(controller.restoreError, isNotNull);
    expect(store.token, 'token-test');
  });

  test('usuario con solicitud pendiente conserva solo rol USUARIO', () async {
    final store = MemoryTokenStore()..token = 'active-test-token';
    final api = ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient((_) async =>
          http.Response(jsonEncode(profile(['USUARIO'], 'PENDIENTE')), 200)),
    );
    final controller = SessionController(AuthService(api, store));
    await controller.restore();

    expect(controller.user!.organizadorPendiente, isTrue);
    expect(controller.user!.isOrganizador, isFalse);
    expect(controller.user!.roles, ['USUARIO']);
  });

  test('logout intenta revocación del backend y siempre limpia sesión local',
      () async {
    final store = MemoryTokenStore()..token = 'active-test-token';
    var calls = 0;
    final api = ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient((_) async {
        calls++;
        return http.Response('', 204);
      }),
    );
    final controller = SessionController(AuthService(api, store));
    await controller.restore();
    await controller.logout();

    expect(calls, 2); // profile restore and logout
    expect(controller.isAuthenticated, isFalse);
    expect(store.token, isNull);
  });
}
