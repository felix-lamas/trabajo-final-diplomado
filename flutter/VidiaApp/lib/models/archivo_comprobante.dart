import 'dart:typed_data';

class ArchivoComprobante {
  const ArchivoComprobante({
    required this.nombre,
    required this.bytes,
    required this.contentType,
  });

  static const maxBytes = 5 * 1024 * 1024;

  final String nombre;
  final Uint8List bytes;
  final String contentType;

  factory ArchivoComprobante.validar({
    required String nombre,
    required Uint8List bytes,
  }) {
    final extension = nombre.split('.').last.toLowerCase();
    final contentType = switch (extension) {
      'jpg' || 'jpeg' => 'image/jpeg',
      'png' => 'image/png',
      'pdf' => 'application/pdf',
      _ => throw const ArchivoComprobanteException(
          ArchivoComprobanteError.tipoNoPermitido),
    };
    if (bytes.isEmpty) {
      throw const ArchivoComprobanteException(
          ArchivoComprobanteError.archivoVacio);
    }
    if (bytes.length > maxBytes) {
      throw const ArchivoComprobanteException(
          ArchivoComprobanteError.demasiadoGrande);
    }
    return ArchivoComprobante(
      nombre: nombre,
      bytes: bytes,
      contentType: contentType,
    );
  }
}

enum ArchivoComprobanteError { tipoNoPermitido, demasiadoGrande, archivoVacio }

class ArchivoComprobanteException implements Exception {
  const ArchivoComprobanteException(this.error);

  final ArchivoComprobanteError error;
}
