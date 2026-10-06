import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';
import 'package:vidia/models/asistencia.dart';
import 'package:vidia/models/certificado.dart';
import 'package:vidia/models/inscripcion.dart';
import 'package:vidia/models/pago.dart';
import 'package:vidia/models/sesion_evento.dart';
import 'package:vidia/repositories/asistencia_repository.dart';
import 'package:vidia/repositories/certificado_repository.dart';
import 'package:vidia/repositories/inscripcion_repository.dart';
import 'package:vidia/repositories/pago_repository.dart';
import 'package:vidia/screens/history_screen.dart';
import 'package:vidia/screens/my_certificates_screen.dart';
import 'package:vidia/screens/public_certificate_verification_screen.dart';

const _certificate = Certificado(
  id: 'cert-1',
  evento: 'Evento certificado',
  tipoCertificado: 'CURRICULAR',
  codigoCertificado: 'UAJMS-ABC123',
  estado: 'GENERADO',
  fechaEmision: null,
);

void main() {
  testWidgets('Mis certificados muestra lista y estado real del backend',
      (tester) async {
    await tester.pumpWidget(_withCertificateRepository(
      const _FakeCertificados([_certificate]),
      const MyCertificatesScreen(),
    ));
    await tester.pumpAndSettle();

    expect(find.text('Mis certificados'), findsOneWidget);
    expect(find.text('Evento certificado'), findsOneWidget);
    expect(find.textContaining('UAJMS-ABC123'), findsOneWidget);
    expect(find.text('GENERADO'), findsOneWidget);
    expect(find.byTooltip('Verificar certificado'), findsOneWidget);
  });

  testWidgets('Mis certificados presenta el estado vacío del backend',
      (tester) async {
    await tester.pumpWidget(_withCertificateRepository(
      const _FakeCertificados([]),
      const MyCertificatesScreen(),
    ));
    await tester.pumpAndSettle();

    expect(find.text('Sin certificados todavía.'), findsOneWidget);
  });

  testWidgets('historial integra inscripción, pago, asistencia y certificado',
      (tester) async {
    await tester.pumpWidget(MultiProvider(
      providers: [
        Provider<InscripcionRepository>.value(value: _FakeInscripciones()),
        Provider<PagoRepository>.value(value: _FakePagos()),
        Provider<CertificadoRepository>.value(
          value: const _FakeCertificados([_certificate]),
        ),
        Provider<AsistenciaRepository>.value(value: _FakeAsistencias()),
      ],
      child: const MaterialApp(home: HistoryScreen()),
    ));
    await tester.pumpAndSettle();

    expect(find.text('Historial'), findsOneWidget);
    expect(find.text('Curso Demo'), findsWidgets);
    expect(find.text('Inscripción'), findsOneWidget);
    expect(find.text('Pago · Bs. 45.00'), findsOneWidget);
    expect(find.text('Asistencia · Sesión Demo'), findsOneWidget);
    expect(find.text('Certificado · Curricular'), findsOneWidget);
    expect(find.text('APROBADO'), findsOneWidget);
  });

  testWidgets('historial sin recursos presenta estado vacío', (tester) async {
    await tester.pumpWidget(MultiProvider(
      providers: [
        Provider<InscripcionRepository>.value(
            value: _FakeInscripciones(empty: true)),
        Provider<PagoRepository>.value(value: _FakePagos(empty: true)),
        Provider<CertificadoRepository>.value(
          value: const _FakeCertificados([]),
        ),
        Provider<AsistenciaRepository>.value(
            value: _FakeAsistencias(empty: true)),
      ],
      child: const MaterialApp(home: HistoryScreen()),
    ));
    await tester.pumpAndSettle();

    expect(find.text('Sin actividad registrada.'), findsOneWidget);
  });

  testWidgets('verificación pública muestra la respuesta no válida del backend',
      (tester) async {
    final repository = const _FakeCertificados(
      [],
      verification: VerificacionCertificado(
        valido: false,
        mensaje: 'Certificado no registrado',
        institucion: 'Universidad Demo',
        codigoCertificado: 'UAJMS-UNKNOWN',
        estado: 'NO_REGISTRADO',
      ),
    );
    await tester.pumpWidget(_withCertificateRepository(
      repository,
      const PublicCertificateVerificationScreen(
        initialCode: 'UAJMS-UNKNOWN',
      ),
    ));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Verificar'));
    await tester.pumpAndSettle();

    expect(find.text('Certificado no válido'), findsOneWidget);
    expect(find.text('NO_REGISTRADO'), findsOneWidget);
  });
}

Widget _withCertificateRepository(
  CertificadoRepository repository,
  Widget child,
) =>
    Provider<CertificadoRepository>.value(
      value: repository,
      child: MaterialApp(home: child),
    );

class _FakeCertificados implements CertificadoRepository {
  const _FakeCertificados(this.certificates, {this.verification});

  final List<Certificado> certificates;
  final VerificacionCertificado? verification;

  @override
  Future<List<Certificado>> fetchMine() async => certificates;

  @override
  Future<Certificado> fetchById(String id) async => certificates.single;

  @override
  Future<CertificadoPdf> downloadPdf(String id) => throw UnimplementedError();

  @override
  Future<VerificacionCertificado> verifyPublic(String code) async =>
      verification!;
}

class _FakeInscripciones implements InscripcionRepository {
  _FakeInscripciones({this.empty = false});
  final bool empty;
  static const registration = Inscripcion(
    id: 'ins-1',
    eventoId: 'event-1',
    eventoTitulo: 'Curso Demo',
    fechaInscripcion: null,
    estado: 'CONFIRMADA',
  );

  @override
  Future<Inscripcion> create(String eventoId) => throw UnimplementedError();

  @override
  Future<List<Inscripcion>> fetchMine() async =>
      empty ? const [] : const [registration];

  @override
  Future<void> cancel(String id) => throw UnimplementedError();
}

class _FakePagos implements PagoRepository {
  _FakePagos({this.empty = false});
  final bool empty;

  @override
  Future<List<Pago>> fetchMine() async => empty
      ? const []
      : const [
          Pago(
            id: 'pay-1',
            inscripcionId: 'ins-1',
            eventoTitulo: 'Curso Demo',
            monto: 45,
            estado: 'APROBADO',
          ),
        ];

  @override
  Future<Pago> uploadReceipt({
    required String pagoId,
    required String fileName,
    required String contentType,
    required Uint8List bytes,
  }) =>
      throw UnimplementedError();

  @override
  Future<ArchivoDescargado> downloadReceipt(String pagoId) =>
      throw UnimplementedError();

  @override
  Future<ComprobanteInscripcion> fetchRegistrationReceipt(
          String inscripcionId) =>
      throw UnimplementedError();

  @override
  Future<ArchivoDescargado> downloadEventPaymentQr(String eventoId) =>
      throw UnimplementedError();
}

class _FakeAsistencias implements AsistenciaRepository {
  _FakeAsistencias({this.empty = false});
  final bool empty;

  @override
  Future<List<Asistencia>> fetchMine() async => empty
      ? const []
      : const [
          Asistencia(
            id: 'att-1',
            sesionEventoId: 'session-1',
            evento: 'Curso Demo',
            sesion: 'Sesión Demo',
            fechaHoraRegistro: null,
            resultadoValidacion: 'VALIDADA',
          ),
        ];

  @override
  Future<List<SesionEvento>> fetchSessions(String eventoId) =>
      throw UnimplementedError();

  @override
  Future<Asistencia> register({
    required String token,
    double? latitude,
    double? longitude,
    double? precision,
  }) =>
      throw UnimplementedError();
}
