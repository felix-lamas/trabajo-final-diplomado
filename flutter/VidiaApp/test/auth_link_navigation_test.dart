import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:provider/provider.dart';
import 'package:vidia/app.dart';
import 'package:vidia/controllers/session_controller.dart';
import 'package:vidia/screens/auth_link_screens.dart';
import 'package:vidia/services/api_service.dart';
import 'package:vidia/services/auth_link_coordinator.dart';
import 'package:vidia/services/auth_service.dart';

import 'support/memory_token_store.dart';

class NavigationLinkSource implements AuthLinkSource {
  NavigationLinkSource(this.initial);
  final Uri? initial;
  final events = StreamController<Uri>.broadcast(sync: true);
  @override
  Future<Uri?> initialLink() async => initial;
  @override
  Stream<Uri> get links => events.stream;
}

void main() {
  for (final scenario in ['closed', 'open', 'background']) {
    testWidgets('real Navigator handles link with app $scenario', (
      tester,
    ) async {
      final uri = Uri.https(AuthLink.host, '/auth/verificar-correo', {
        'token': 'test-link-secret',
      });
      final source = NavigationLinkSource(scenario == 'closed' ? uri : null);
      var calls = 0;
      final store = MemoryTokenStore();
      final auth = AuthService(
        ApiService(
          baseUrl: 'https://api.example.test/api/v1',
          tokenStore: store,
          client: MockClient((request) async {
            calls++;
            return http.Response('', 200);
          }),
        ),
        store,
      );
      final session = SessionController(auth);
      final coordinator = AuthLinkCoordinator(
        source: source,
        onLink: (link) {
          appNavigatorKey.currentState!.pushAndRemoveUntil<void>(
            authLinkRoute(link),
            (route) => route.isFirst,
          );
        },
      );
      await coordinator.start();
      tester.binding.handleAppLifecycleStateChanged(AppLifecycleState.resumed);
      await tester.pumpWidget(
        MultiProvider(
          providers: [
            Provider<AuthService>.value(value: auth),
            ChangeNotifierProvider<SessionController>.value(value: session),
          ],
          child: VidiaApp(authLinks: coordinator),
        ),
      );
      await tester.pump();
      expect(calls, 0); // Initial links wait for session restoration.
      await session.restore();
      await tester.pumpAndSettle();
      if (scenario == 'background') {
        tester.binding.handleAppLifecycleStateChanged(
          AppLifecycleState.inactive,
        );
        tester.binding.handleAppLifecycleStateChanged(AppLifecycleState.hidden);
        tester.binding.handleAppLifecycleStateChanged(AppLifecycleState.paused);
        source.events.add(uri);
        await tester.pump();
        expect(calls, 0);
        tester.binding.handleAppLifecycleStateChanged(AppLifecycleState.hidden);
        tester.binding.handleAppLifecycleStateChanged(
          AppLifecycleState.inactive,
        );
        tester.binding.handleAppLifecycleStateChanged(
          AppLifecycleState.resumed,
        );
      } else if (scenario == 'open') {
        source.events.add(uri);
      }
      await tester.pumpAndSettle();
      expect(calls, 1);
      expect(find.textContaining('Correo verificado'), findsOneWidget);
      expect(
        ModalRoute.of(
          tester.element(find.byType(VerifyEmailLinkScreen)),
        )!.settings.name,
        '/auth/verificar-correo',
      );
      source.events.add(uri);
      await tester.pumpAndSettle();
      expect(calls, 1);
      await tester.pumpWidget(const SizedBox());
      await source.events.close();
      session.dispose();
    });
  }

  testWidgets(
    'internal reset URI opens password screen without HTTP or credential in route name',
    (tester) async {
      final store = MemoryTokenStore();
      var calls = 0;
      final auth = AuthService(
        ApiService(
          baseUrl: 'https://api.example.test/api/v1',
          tokenStore: store,
          client: MockClient((_) async {
            calls++;
            return http.Response('', 200);
          }),
        ),
        store,
      );
      final session = SessionController(auth);
      await session.restore();
      await tester.pumpWidget(
        MultiProvider(
          providers: [
            Provider<AuthService>.value(value: auth),
            ChangeNotifierProvider<SessionController>.value(value: session),
          ],
          child: const VidiaApp(),
        ),
      );
      await tester.pumpAndSettle();
      unawaited(
        appNavigatorKey.currentState!.pushNamed(
          '/auth/restablecer-contrasena?token=internal-test-secret',
        ),
      );
      await tester.pumpAndSettle();
      expect(find.byType(ResetPasswordScreen), findsOneWidget);
      expect(find.byType(TextFormField), findsNWidgets(2));
      expect(calls, 0);
      expect(
        ModalRoute.of(
          tester.element(find.byType(ResetPasswordScreen)),
        )!.settings.name,
        '/auth/restablecer-contrasena',
      );
      expect(find.textContaining('internal-test-secret'), findsNothing);
      await tester.pumpWidget(const SizedBox());
      session.dispose();
    },
  );

  testWidgets('EMAIL_NOT_VERIFIED opens resend screen with login email', (
    tester,
  ) async {
    final store = MemoryTokenStore();
    final auth = AuthService(
      ApiService(
        baseUrl: 'https://api.example.test/api/v1',
        tokenStore: store,
        client: MockClient(
          (_) async =>
              http.Response(jsonEncode({'codigo': 'EMAIL_NOT_VERIFIED'}), 400),
        ),
      ),
      store,
    );
    final session = SessionController(auth);
    await session.restore();
    await tester.pumpWidget(
      MultiProvider(
        providers: [
          Provider<AuthService>.value(value: auth),
          ChangeNotifierProvider<SessionController>.value(value: session),
        ],
        child: const VidiaApp(),
      ),
    );
    await tester.pumpAndSettle();
    await tester.enterText(
      find.byType(TextFormField).at(0),
      'ana@example.test',
    );
    await tester.enterText(find.byType(TextFormField).at(1), 'test-password');
    await tester.ensureVisible(find.text('Ingresar'));
    await tester.tap(find.text('Ingresar'));
    await tester.pumpAndSettle();
    expect(find.text('Revisa tu correo'), findsOneWidget);
    expect(find.text('Correo destino: ana@example.test'), findsOneWidget);
    expect(find.byType(TextFormField), findsOneWidget);
    expect(find.text('Reenviar verificación'), findsOneWidget);
    expect(find.text('Volver al login'), findsOneWidget);
    await tester.pumpWidget(const SizedBox());
    session.dispose();
  });
}
