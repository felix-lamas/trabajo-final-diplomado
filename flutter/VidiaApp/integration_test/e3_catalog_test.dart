import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';

import 'package:vidia/main.dart' as app;

const _testEmail = String.fromEnvironment('E3_USER_EMAIL');
const _testPassword = String.fromEnvironment('E3_USER_PASSWORD');

Future<void> _pumpUntil(
  WidgetTester tester,
  Finder finder, {
  Duration timeout = const Duration(seconds: 40),
}) async {
  final deadline = DateTime.now().add(timeout);
  while (finder.evaluate().isEmpty && DateTime.now().isBefore(deadline)) {
    await tester.pump(const Duration(milliseconds: 250));
    await Future<void>.delayed(const Duration(milliseconds: 100));
  }
  expect(finder, findsWidgets, reason: 'El estado solicitado no apareció durante el tiempo límite.');
}

Future<void> _pumpUntilAny(WidgetTester tester, List<Finder> finders) async {
  final deadline = DateTime.now().add(const Duration(seconds: 45));
  bool found() => finders.any((finder) => finder.evaluate().isNotEmpty);
  while (!found() && DateTime.now().isBefore(deadline)) {
    await tester.pump(const Duration(milliseconds: 250));
    await Future<void>.delayed(const Duration(milliseconds: 100));
  }
  expect(found(), isTrue, reason: 'No apareció login ni sesión restaurada.');
}

void main() {
  final binding = IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('CP-14 — participante ve el catálogo recibido desde el backend', (tester) async {
    if (_testEmail.isEmpty || _testPassword.isEmpty) {
      fail('Faltan E3_USER_EMAIL y E3_USER_PASSWORD de cuenta demo pasados por el runner seguro.');
    }

    await app.main();
    final loginButton = find.text('Ingresar');
    final profileButton = find.byTooltip('Mi perfil');
    await _pumpUntilAny(tester, [loginButton, profileButton]);
    if (loginButton.evaluate().isNotEmpty) {
      final fields = find.byType(TextFormField);
      expect(fields, findsNWidgets(2));
      await tester.enterText(fields.at(0), _testEmail);
      await tester.enterText(fields.at(1), _testPassword);
      await tester.tap(loginButton);
      await _pumpUntil(tester, find.text('Eventos publicados'), timeout: const Duration(seconds: 45));
    } else {
      // No cerramos una sesión preexistente. Solo se acepta si pertenece a la cuenta demo configurada.
      await tester.tap(find.byTooltip('Mi perfil'));
      await _pumpUntil(tester, find.text('Mi perfil'));
      expect(find.text(_testEmail), findsOneWidget, reason: 'La sesión del dispositivo pertenece a otra cuenta; no se cerró ni alteró.');
      await tester.pageBack();
      await _pumpUntil(tester, find.text('Eventos publicados'));
    }

    await _pumpUntil(tester, find.text('Eventos publicados'));
    await tester.tap(find.text('Eventos publicados').first);
    await _pumpUntil(tester, find.text('Buscar eventos'));
    final emptyMessage = find.text('No hay eventos publicados por el momento.');
    final loadError = find.text('No se pudieron cargar los eventos.');
    await _pumpUntilAny(tester, [emptyMessage, find.byType(Card), loadError]);
    expect(find.text('No se pudieron cargar los eventos.'), findsNothing);
    expect(find.byType(CircularProgressIndicator), findsNothing);
    final emptyCatalog = find.text('No hay eventos publicados por el momento.').evaluate().isNotEmpty;
    final eventCards = find.byType(Card).evaluate().isNotEmpty;
    expect(emptyCatalog || eventCards, isTrue, reason: 'La UI debe mostrar lista o estado vacío del catálogo real.');
    await binding.takeScreenshot('CP-14-flutter-catalogo');
  });
}
