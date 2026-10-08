import 'dart:async';

import 'package:flutter_test/flutter_test.dart';
import 'package:vidia/services/auth_link_coordinator.dart';

class FakeAuthLinkSource implements AuthLinkSource {
  FakeAuthLinkSource({this.initial});
  final Uri? initial;
  final events = StreamController<Uri>.broadcast(sync: true);
  @override
  Future<Uri?> initialLink() async => initial;
  @override
  Stream<Uri> get links => events.stream;
}

Uri link(String path, [String token = 'temporary-test-token']) =>
    Uri.https(AuthLink.host, '/auth/$path', {'token': token});

void main() {
  test('accepts both exact HTTPS authentication links and opaque tokens', () {
    final verification = AuthLink.parse(link('verificar-correo', 'a+/='))!;
    expect(verification.action, AuthLinkAction.verifyEmail);
    expect(verification.token, 'a+/=');
    expect(
      AuthLink.parse(link('restablecer-contrasena'))!.action,
      AuthLinkAction.resetPassword,
    );
    expect(
      AuthLink.parse(
        Uri.parse('/auth/restablecer-contrasena?token=test'),
        internal: true,
      ),
      isNotNull,
    );
  });

  test(
    'rejects wrong domain, scheme, port, path and ambiguous or empty token',
    () {
      for (final value in [
        'https://evil.example/auth/verificar-correo?token=test',
        'https://${AuthLink.host}.evil.example/auth/verificar-correo?token=test',
        'http://${AuthLink.host}/auth/verificar-correo?token=test',
        'https://${AuthLink.host}:444/auth/verificar-correo?token=test',
        'https://user@${AuthLink.host}/auth/verificar-correo?token=test',
        'https://${AuthLink.host}/unknown?token=test',
        'https://${AuthLink.host}/auth/verificar-correo/extra?token=test',
        'https://${AuthLink.host}/auth/verificar-correo',
        'https://${AuthLink.host}/auth/verificar-correo?token=',
        'https://${AuthLink.host}/auth/verificar-correo?token=%20',
        'https://${AuthLink.host}/auth/verificar-correo?token=a&token=b',
        'https://${AuthLink.host}/auth/verificar-correo?token=a#fragment',
      ]) {
        expect(AuthLink.parse(Uri.parse(value)), isNull);
      }
    },
  );

  for (final scenario in ['app closed', 'app open', 'app background']) {
    test('$scenario delivers once when navigation is ready', () async {
      final uri = link('verificar-correo');
      final source = FakeAuthLinkSource(
        initial: scenario == 'app closed' ? uri : null,
      );
      final received = <AuthLink>[];
      final coordinator = AuthLinkCoordinator(
        source: source,
        onLink: received.add,
      );
      await coordinator.start();
      if (scenario == 'app open') coordinator.setReady(true);
      if (scenario != 'app closed') source.events.add(uri);
      if (scenario != 'app open') expect(received, isEmpty);
      coordinator.setReady(true);
      expect(received, hasLength(1));
      source.events.add(uri);
      source.events.add(Uri.parse('$uri&tracking=email'));
      expect(received, hasLength(1));
      await coordinator.dispose();
      source.events.add(link('restablecer-contrasena'));
      expect(received, hasLength(1));
      await source.events.close();
    });
  }

  test(
    'initial and stream delivery of the same link is deduplicated',
    () async {
      final uri = link('restablecer-contrasena');
      final source = FakeAuthLinkSource(initial: uri);
      var calls = 0;
      final coordinator = AuthLinkCoordinator(
        source: source,
        onLink: (_) => calls++,
      );
      final starting = coordinator.start();
      source.events.add(uri);
      await starting;
      coordinator.setReady(true);
      expect(calls, 1);
      source.events.addError(Exception('platform error'));
      source.events.add(link('verificar-correo', 'another-test-token'));
      expect(calls, 2);
      await coordinator.dispose();
      await source.events.close();
    },
  );
}
