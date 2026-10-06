import 'dart:typed_data';

import '../models/pago.dart';
import '../services/api_service.dart';

class ArchivoDescargado {
  const ArchivoDescargado({required this.bytes, required this.contentType});

  final Uint8List bytes;
  final String? contentType;
}

abstract interface class PagoRepository {
  Future<List<Pago>> fetchMine();

  Future<Pago> uploadReceipt({
    required String pagoId,
    required String fileName,
    required String contentType,
    required Uint8List bytes,
  });

  Future<ArchivoDescargado> downloadReceipt(String pagoId);

  Future<ComprobanteInscripcion> fetchRegistrationReceipt(
      String inscripcionId);

  Future<ArchivoDescargado> downloadEventPaymentQr(String eventoId);
}

class BackendPagoRepository implements PagoRepository {
  BackendPagoRepository(this._api);

  final ApiService _api;

  @override
  Future<List<Pago>> fetchMine() async {
    final response = await _api.get('/pagos/mis-pagos');
    return (response as List<dynamic>)
        .map((item) => Pago.fromJson(item as Map<String, dynamic>))
        .toList(growable: false);
  }

  @override
  Future<Pago> uploadReceipt({
    required String pagoId,
    required String fileName,
    required String contentType,
    required Uint8List bytes,
  }) async {
    final response = await _api.postMultipart(
      '/pagos/$pagoId/comprobante',
      fieldName: 'archivo',
      fileName: fileName,
      contentType: contentType,
      bytes: bytes,
    );
    return Pago.fromJson(response as Map<String, dynamic>);
  }

  @override
  Future<ArchivoDescargado> downloadReceipt(String pagoId) async {
    final response = await _api.getBinary('/pagos/$pagoId/comprobante');
    return ArchivoDescargado(
      bytes: response.bytes,
      contentType: response.contentType,
    );
  }

  @override
  Future<ComprobanteInscripcion> fetchRegistrationReceipt(
      String inscripcionId) async {
    final response = await _api.get('/inscripciones/$inscripcionId/comprobante');
    return ComprobanteInscripcion.fromJson(response as Map<String, dynamic>);
  }

  @override
  Future<ArchivoDescargado> downloadEventPaymentQr(String eventoId) async {
    final response = await _api.getBinary(
      '/eventos/$eventoId/qr-pago',
      authenticated: false,
    );
    return ArchivoDescargado(
      bytes: response.bytes,
      contentType: response.contentType,
    );
  }
}
