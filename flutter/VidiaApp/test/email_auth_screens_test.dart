import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:provider/provider.dart';
import 'package:vidia/controllers/session_controller.dart';
import 'package:vidia/screens/auth_link_screens.dart';
import 'package:vidia/screens/email_auth_screens.dart';
import 'package:vidia/services/api_service.dart';
import 'package:vidia/services/auth_service.dart';

import 'support/memory_token_store.dart';

void main() {
  Future<MemoryTokenStore> mount(
    WidgetTester tester,
    Widget screen,
    Future<http.Response> Function(http.Request) handler,
  ) async {
    final store = MemoryTokenStore()..token = 'existing-jwt';
    final auth = AuthService(
      ApiService(
        baseUrl: 'https://api.example.test/api/v1',
        tokenStore: store,
        client: MockClient(handler),
      ),
      store,
    );
    await tester.pumpWidget(
      MultiProvider(
        providers: [
          Provider<AuthService>.value(value: auth),
          ChangeNotifierProvider(create: (_) => SessionController(auth)),
        ],
        child: MaterialApp(home: screen),
      ),
    );
    await tester.pumpAndSettle();
    return store;
  }

  for (final recovery in [true, false]) {
    testWidgets('${recovery ? 'recovery' : 'resend'} only requires email', (
      tester,
    ) async {
      var calls = 0;
      await mount(
        tester,
        recovery
            ? const PasswordRecoveryScreen()
            : const EmailVerificationPendingScreen(),
        (request) async {
          calls++;
          expect(request.method, 'POST');
          expect(
            request.url.path,
            recovery
                ? '/api/v1/auth/recuperar-contrasena'
                : '/api/v1/auth/reenviar-verificacion',
          );
          expect(jsonDecode(request.body), {
            'correoElectronico': 'ana@example.test',
          });
          expect(request.headers.containsKey('Authorization'), isFalse);
          return http.Response('', 200);
        },
      );
      expect(find.byType(TextFormField), findsOneWidget);
      await tester.enterText(find.byType(TextFormField), 'ana@example.test');
      await tester.tap(
        find.text(
          recovery ? 'Solicitar recuperación' : 'Reenviar verificación',
        ),
      );
      await tester.pumpAndSettle();
      expect(calls, 1);
      expect(
        find.textContaining('Si la cuenta requiere'),
        recovery ? findsNothing : findsOneWidget,
      );
      if (recovery) {
        expect(find.text('Revisa tu correo'), findsOneWidget);
        expect(
          find.textContaining('Si el correo corresponde a una cuenta'),
          findsOneWidget,
        );
      }
      expect(find.textContaining('Token de'), findsNothing);
    });
  }

  testWidgets(
    'verification automatically posts token without showing or persisting it',
    (tester) async {
      var calls = 0;
      final store = await mount(
        tester,
        const VerifyEmailLinkScreen(token: 'secret-test-only'),
        (request) async {
          calls++;
          expect(request.url.path, '/api/v1/auth/verificar-correo');
          expect(jsonDecode(request.body), {'token': 'secret-test-only'});
          expect(request.headers.containsKey('Authorization'), isFalse);
          return http.Response('', 200);
        },
      );
      expect(calls, 1);
      expect(find.textContaining('Correo verificado'), findsOneWidget);
      expect(find.textContaining('secret-test-only'), findsNothing);
      expect(store.token, 'existing-jwt');
    },
  );

  testWidgets(
    'reset only sends token after password confirmation and clears session',
    (tester) async {
      var calls = 0;
      final store = await mount(
        tester,
        const ResetPasswordScreen(token: 'reset-secret-test'),
        (request) async {
          calls++;
          expect(request.url.path, '/api/v1/auth/restablecer-contrasena');
          expect(jsonDecode(request.body), {
            'token': 'reset-secret-test',
            'nuevaContrasena': 'NuevaClave1!',
            'confirmacion': 'NuevaClave1!',
          });
          expect(request.headers.containsKey('Authorization'), isFalse);
          return http.Response('', 200);
        },
      );
      expect(calls, 0);
      expect(find.byType(TextFormField), findsNWidgets(2));
      await tester.enterText(find.byType(TextFormField).at(0), 'NuevaClave1!');
      await tester.enterText(find.byType(TextFormField).at(1), 'wrong');
      await tester.tap(find.text('Restablecer contraseña'));
      await tester.pumpAndSettle();
      expect(calls, 0);
      await tester.enterText(find.byType(TextFormField).at(1), 'NuevaClave1!');
      await tester.tap(find.text('Restablecer contraseña'));
      await tester.pumpAndSettle();
      expect(calls, 1);
      expect(store.token, isNull);
      expect(find.textContaining('reset-secret-test'), findsNothing);
      expect(find.textContaining('Contraseña restablecida'), findsOneWidget);
    },
  );

  for (final code in [
    'EMAIL_VERIFICATION_TOKEN_EXPIRED',
    'EMAIL_VERIFICATION_TOKEN_USED',
    'EMAIL_VERIFICATION_TOKEN_INVALID',
  ]) {
    testWidgets(
      'verification represents $code without leaking backend details',
      (tester) async {
        await mount(
          tester,
          const VerifyEmailLinkScreen(token: 'secret-test'),
          (_) async => http.Response(
            jsonEncode({'codigo': code, 'mensaje': 'secret-test'}),
            400,
          ),
        );
        expect(find.text('Solicitar otro enlace'), findsOneWidget);
        expect(find.text('Reintentar'), findsNothing);
        expect(find.textContaining('secret-test'), findsNothing);
        expect(
          find.textContaining(
            code.endsWith('EXPIRED')
                ? 'expiró'
                : code.endsWith('USED')
                ? 'ya fue utilizado'
                : 'no es válido',
          ),
          findsOneWidget,
        );
      },
    );
  }

  testWidgets(
    'network failure can retry verification once without duplicate requests',
    (tester) async {
      var calls = 0;
      await mount(tester, const VerifyEmailLinkScreen(token: 'secret-test'), (
        _,
      ) async {
        if (++calls == 1) throw http.ClientException('network');
        return http.Response('', 200);
      });
      expect(find.text('Reintentar'), findsOneWidget);
      await tester.tap(find.text('Reintentar'));
      await tester.pumpAndSettle();
      expect(calls, 2);
      expect(find.textContaining('Correo verificado'), findsOneWidget);
    },
  );

  for (final recovery in [true, false]) {
    testWidgets(
      '${recovery ? 'recovery' : 'resend'} network error restores button',
      (tester) async {
        await mount(
          tester,
          recovery
              ? const PasswordRecoveryScreen(initialEmail: 'ana@example.test')
              : const EmailVerificationPendingScreen(
                  initialEmail: 'ana@example.test',
                ),
          (_) async => throw http.ClientException('network'),
        );
        await tester.tap(
          find.text(
            recovery ? 'Solicitar recuperación' : 'Reenviar verificación',
          ),
        );
        await tester.pumpAndSettle();
        expect(find.textContaining('No fue posible'), findsOneWidget);
        expect(
          tester.widget<FilledButton>(find.byType(FilledButton)).onPressed,
          isNotNull,
        );
      },
    );
  }

  for (final code in [
    'PASSWORD_RESET_TOKEN_EXPIRED',
    'PASSWORD_RESET_TOKEN_USED',
    'PASSWORD_RESET_TOKEN_INVALID',
    'NETWORK',
  ]) {
    testWidgets('reset represents $code without leaking token', (tester) async {
      var calls = 0;
      await mount(
        tester,
        const ResetPasswordScreen(token: 'reset-secret-test'),
        (_) async {
          calls++;
          if (code == 'NETWORK') throw http.ClientException('network');
          return http.Response(
            jsonEncode({'codigo': code, 'mensaje': 'reset-secret-test'}),
            400,
          );
        },
      );
      await tester.enterText(find.byType(TextFormField).at(0), 'NuevaClave1!');
      await tester.enterText(find.byType(TextFormField).at(1), 'NuevaClave1!');
      await tester.tap(find.text('Restablecer contraseña'));
      await tester.pumpAndSettle();
      expect(calls, 1);
      expect(find.textContaining('reset-secret-test'), findsNothing);
      if (code == 'NETWORK') {
        expect(find.textContaining('No fue posible conectar'), findsOneWidget);
        expect(find.text('Restablecer contraseña'), findsOneWidget);
      } else {
        expect(find.text('Solicitar recuperación'), findsOneWidget);
        expect(find.byType(TextFormField), findsNothing);
      }
    });
  }

  testWidgets('empty reset token does not expose a form or call HTTP', (
    tester,
  ) async {
    var calls = 0;
    await mount(tester, const ResetPasswordScreen(token: ' '), (_) async {
      calls++;
      return http.Response('', 200);
    });
    expect(calls, 0);
    expect(find.byType(TextFormField), findsNothing);
    expect(find.text('Solicitar recuperación'), findsOneWidget);
  });

  testWidgets('empty verification token makes no HTTP request', (tester) async {
    var calls = 0;
    await mount(tester, const VerifyEmailLinkScreen(token: ''), (_) async {
      calls++;
      return http.Response('', 200);
    });
    expect(calls, 0);
    expect(find.textContaining('no contiene un token válido'), findsOneWidget);
  });
}
