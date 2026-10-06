import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:vidia/repositories/pago_repository.dart';
import 'package:vidia/services/api_exception.dart';
import 'package:vidia/services/api_service.dart';

import 'support/memory_token_store.dart';

const pagoId = '30000000-0000-0000-0000-000000000001';
const inscripcionId = '20000000-0000-0000-0000-000000000001';

Map<String, dynamic> pagoJson(String estado) => {
      'id': pagoId,
      'inscripcionId': inscripcionId,
      'eventoTitulo': 'Evento demo',
      'monto': 60.0,
      'fechaPago': '2026-10-06T10:00:00',
      'estado': estado,
      'motivoRechazo': estado == 'RECHAZADO' ? 'Archivo ilegible' : null,
      'fechaResolucion': null,
      'intentosComprobante': 0,
      'comprobante': null,
    };

void main() {
  test('consulta pagos propios por el endpoint documentado', () async {
    final store = MemoryTokenStore()..token = 'test-token';
    final repository = BackendPagoRepository(ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient((request) async {
        expect(request.method, 'GET');
        expect(request.url.path, '/api/v1/pagos/mis-pagos');
        expect(request.headers['Authorization'], 'Bearer test-token');
        return http.Response(jsonEncode([pagoJson('PENDIENTE_PAGO')]), 200);
      }),
    ));

    final payments = await repository.fetchMine();

    expect(payments, hasLength(1));
    expect(payments.single.inscripcionId, inscripcionId);
    expect(payments.single.monto, 60);
    expect(payments.single.estado, 'PENDIENTE_PAGO');
  });

  test('sube multipart con archivo, MIME y autorización', () async {
    final store = MemoryTokenStore()..token = 'test-token';
    late http.Request captured;
    final repository = BackendPagoRepository(ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient((request) async {
        captured = request;
        return http.Response(jsonEncode(pagoJson('PENDIENTE_VALIDACION')), 200);
      }),
    ));

    final payment = await repository.uploadReceipt(
      pagoId: pagoId,
      fileName: 'prueba.pdf',
      contentType: 'application/pdf',
      bytes: Uint8List.fromList('%PDF-1.4\n1234'.codeUnits),
    );

    expect(captured.method, 'POST');
    expect(captured.url.path, '/api/v1/pagos/$pagoId/comprobante');
    expect(captured.headers['Authorization'], 'Bearer test-token');
    expect(captured.headers['content-type'],
        startsWith('multipart/form-data; boundary='));
    expect(captured.body, contains('name="archivo"'));
    expect(captured.body, contains('filename="prueba.pdf"'));
    expect(captured.body, contains('content-type: application/pdf'));
    expect(captured.body, contains('%PDF-1.4'));
    expect(payment.estado, 'PENDIENTE_VALIDACION');
  });

  test('consulta y descarga el comprobante binario propio', () async {
    final store = MemoryTokenStore()..token = 'test-token';
    final repository = BackendPagoRepository(ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient((request) async {
        expect(request.method, 'GET');
        expect(request.url.path, '/api/v1/pagos/$pagoId/comprobante');
        expect(request.headers['Authorization'], 'Bearer test-token');
        return http.Response.bytes([37, 80, 68, 70], 200,
            headers: {'content-type': 'application/pdf'});
      }),
    ));

    final file = await repository.downloadReceipt(pagoId);

    expect(file.contentType, 'application/pdf');
    expect(file.bytes, [37, 80, 68, 70]);
  });

  test('consulta la constancia JSON usando el id propio de inscripción',
      () async {
    final repository = BackendPagoRepository(ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: MemoryTokenStore()..token = 'test-token',
      client: MockClient((request) async {
        expect(request.method, 'GET');
        expect(request.url.path,
            '/api/v1/inscripciones/$inscripcionId/comprobante');
        return http.Response(
            jsonEncode({
              'inscripcionId': inscripcionId,
              'codigoInscripcion': 'INS-001',
              'eventoId': '10000000-0000-0000-0000-000000000001',
              'eventoTitulo': 'Evento demo',
              'participante': 'Participante Demo',
              'ci': 'NO SE USA EN UI',
              'ru': null,
              'monto': 60,
              'fechaInscripcion': '2026-10-06T10:00:00',
              'estadoInscripcion': 'PENDIENTE_PAGO',
              'estadoPago': 'PENDIENTE_PAGO',
              'codigoVerificacion': 'INS-001',
            }),
            200);
      }),
    ));

    final receipt = await repository.fetchRegistrationReceipt(inscripcionId);

    expect(receipt.eventoTitulo, 'Evento demo');
    expect(receipt.estadoPago, 'PENDIENTE_PAGO');
    expect(receipt.codigoVerificacion, 'INS-001');
  });

  test('obtiene el QR de pago solo desde el endpoint público Spring', () async {
    final repository = BackendPagoRepository(ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: MemoryTokenStore(),
      client: MockClient((request) async {
        expect(request.url.path,
            '/api/v1/eventos/10000000-0000-0000-0000-000000000001/qr-pago');
        expect(request.headers.containsKey('Authorization'), isFalse);
        return http.Response.bytes([137, 80, 78, 71], 200,
            headers: {'content-type': 'image/png'});
      }),
    ));

    final qr = await repository
        .downloadEventPaymentQr('10000000-0000-0000-0000-000000000001');

    expect(qr.contentType, 'image/png');
    expect(qr.bytes, [137, 80, 78, 71]);
  });

  test('el mismo endpoint acepta reenvío tras estado RECHAZADO', () async {
    late http.Request captured;
    final repository = BackendPagoRepository(ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: MemoryTokenStore()..token = 'test-token',
      client: MockClient((request) async {
        captured = request;
        return http.Response(jsonEncode(pagoJson('PENDIENTE_VALIDACION')), 200);
      }),
    ));

    final updated = await repository.uploadReceipt(
      pagoId: pagoId,
      fileName: 'reenvio.png',
      contentType: 'image/png',
      bytes: Uint8List.fromList([137, 80, 78, 71]),
    );

    expect(captured.method, 'POST');
    expect(captured.url.path, '/api/v1/pagos/$pagoId/comprobante');
    expect(
        latin1.decode(captured.bodyBytes), contains('filename="reenvio.png"'));
    expect(updated.estado, 'PENDIENTE_VALIDACION');
  });

  test('propaga conflicto de estado al intentar subir comprobante', () async {
    final repository = BackendPagoRepository(ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: MemoryTokenStore()..token = 'test-token',
      client: MockClient((_) async => http.Response(
            jsonEncode({
              'codigo': 'PAYMENT_INVALID_STATE',
              'mensaje': 'El pago no admite comprobantes en este estado',
            }),
            409,
          )),
    ));

    await expectLater(
      repository.uploadReceipt(
        pagoId: pagoId,
        fileName: 'test.pdf',
        contentType: 'application/pdf',
        bytes: Uint8List.fromList([37, 80, 68, 70]),
      ),
      throwsA(isA<ApiException>()
          .having((error) => error.statusCode, 'status', 409)
          .having((error) => error.code, 'code', 'PAYMENT_INVALID_STATE')),
    );
  });
}
