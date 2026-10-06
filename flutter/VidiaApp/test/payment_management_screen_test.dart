import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'dart:typed_data';
import 'package:provider/provider.dart';
import 'package:vidia/models/evento.dart';
import 'package:vidia/models/inscripcion.dart';
import 'package:vidia/models/pago.dart';
import 'package:vidia/repositories/evento_repository.dart';
import 'package:vidia/repositories/inscripcion_repository.dart';
import 'package:vidia/repositories/pago_repository.dart';
import 'package:vidia/screens/payment_management_screen.dart';

void main() {
  for (final (state, expected) in [
    ('PENDIENTE_PAGO', 'Pendiente de pago'),
    ('PENDIENTE_VALIDACION', 'Pendiente de validación'),
    ('APROBADO', 'Aprobado'),
    ('RECHAZADO', 'Rechazado'),
  ]) {
    testWidgets('muestra el estado real $state y el monto del pago',
        (tester) async {
      await tester.pumpWidget(_app(state));
      await tester.pumpAndSettle();

      expect(find.text(expected), findsOneWidget);
      expect(find.text('Bs. 60.00'), findsOneWidget);
      expect(find.text('Evento pagado demo'), findsOneWidget);

      if (state == 'PENDIENTE_PAGO' || state == 'RECHAZADO') {
        expect(find.text('Seleccionar comprobante'), findsOneWidget);
      } else {
        expect(find.text('Seleccionar comprobante'), findsNothing);
      }
      if (state == 'PENDIENTE_VALIDACION') {
        expect(find.text('Comprobante enviado. Pendiente de validación.'),
            findsOneWidget);
      }
      if (state == 'APROBADO') {
        expect(find.text('Pago aprobado'), findsOneWidget);
      }
      if (state == 'RECHAZADO') {
        expect(find.text('Motivo demo de rechazo'), findsOneWidget);
      }
    });
  }
}

Widget _app(String state) => MultiProvider(
      providers: [
        Provider<PagoRepository>.value(
            value: _FakePagoRepository(_payment(state))),
        Provider<EventoRepository>.value(value: _FakeEventoRepository()),
        Provider<InscripcionRepository>.value(value: _FakeInscripcionRepository()),
      ],
      child: const MaterialApp(
        home: PaymentManagementScreen(
          registration: Inscripcion(
            id: '20000000-0000-0000-0000-000000000001',
            eventoId: '10000000-0000-0000-0000-000000000001',
            eventoTitulo: 'Evento pagado demo',
            fechaInscripcion: null,
            estado: 'PENDIENTE_PAGO',
          ),
        ),
      ),
    );

Pago _payment(String state) => Pago(
      id: '30000000-0000-0000-0000-000000000001',
      inscripcionId: '20000000-0000-0000-0000-000000000001',
      eventoTitulo: 'Evento pagado demo',
      monto: 60,
      estado: state,
      motivoRechazo: state == 'RECHAZADO' ? 'Motivo demo de rechazo' : null,
    );

class _FakePagoRepository implements PagoRepository {
  _FakePagoRepository(this.payment);
  final Pago payment;

  @override
  Future<List<Pago>> fetchMine() async => [payment];

  @override
  Future<Pago> uploadReceipt({
    required String pagoId,
    required String fileName,
    required String contentType,
    required Uint8List bytes,
  }) async => payment;

  @override
  Future<ArchivoDescargado> downloadReceipt(String pagoId) async =>
      ArchivoDescargado(bytes: Uint8List(0), contentType: 'application/pdf');

  @override
  Future<ComprobanteInscripcion> fetchRegistrationReceipt(
          String inscripcionId) async =>
      const ComprobanteInscripcion(
        inscripcionId: '20000000-0000-0000-0000-000000000001',
        codigoInscripcion: 'INS-DEMO',
        eventoId: '10000000-0000-0000-0000-000000000001',
        eventoTitulo: 'Evento pagado demo',
        monto: 60,
        estadoInscripcion: 'PENDIENTE_PAGO',
        estadoPago: 'PENDIENTE_PAGO',
        codigoVerificacion: 'INS-DEMO',
      );

  @override
  Future<ArchivoDescargado> downloadEventPaymentQr(String eventoId) async =>
      ArchivoDescargado(bytes: Uint8List(0), contentType: 'image/png');
}

class _FakeEventoRepository implements EventoRepository {
  @override
  Future<Evento> fetchById(String id) async => const Evento(
        id: '10000000-0000-0000-0000-000000000001',
        titulo: 'Evento pagado demo',
        descripcion: '',
        objetivos: '',
        categoriaNombre: 'Curso',
        modalidad: 'PRESENCIAL',
        tipoInscripcion: 'PAGO',
        costo: 60,
        fechaInicio: null,
        fechaFin: null,
        horaInicio: '',
        horaFin: '',
        estado: 'PUBLICADO',
        requiereInscripcion: true,
        cupoLimitado: false,
        instruccionesPago: 'Instrucciones proporcionadas por backend.',
      );

  @override
  Future<List<Evento>> fetchPublished() async => [];

  @override
  Future<List<Evento>> searchPublished({
    String? text,
    String? categoryId,
    String? type,
    String? modality,
  }) async =>
      [];
}

class _FakeInscripcionRepository implements InscripcionRepository {
  @override
  Future<List<Inscripcion>> fetchMine() async => const [
        Inscripcion(
          id: '20000000-0000-0000-0000-000000000001',
          eventoId: '10000000-0000-0000-0000-000000000001',
          eventoTitulo: 'Evento pagado demo',
          fechaInscripcion: null,
          estado: 'PENDIENTE_PAGO',
        ),
      ];

  @override
  Future<Inscripcion> create(String eventoId) async => throw UnimplementedError();

  @override
  Future<void> cancel(String id) async => throw UnimplementedError();
}
