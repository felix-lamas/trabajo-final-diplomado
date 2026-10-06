import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:vidia/models/archivo_comprobante.dart';

void main() {
  test('acepta los MIME/extension permitidos por el backend', () {
    for (final (name, mime) in [
      ('pago.jpg', 'image/jpeg'),
      ('pago.jpeg', 'image/jpeg'),
      ('pago.png', 'image/png'),
      ('pago.pdf', 'application/pdf'),
    ]) {
      final file = ArchivoComprobante.validar(
        nombre: name,
        bytes: Uint8List.fromList([1, 2, 3]),
      );
      expect(file.contentType, mime);
    }
  });

  test('rechaza una extensión no permitida', () {
    expect(
      () => ArchivoComprobante.validar(
        nombre: 'archivo.exe',
        bytes: Uint8List.fromList([1]),
      ),
      throwsA(isA<ArchivoComprobanteException>().having(
        (error) => error.error,
        'error',
        ArchivoComprobanteError.tipoNoPermitido,
      )),
    );
  });

  test('rechaza un archivo vacío', () {
    expect(
      () => ArchivoComprobante.validar(
        nombre: 'archivo.pdf',
        bytes: Uint8List(0),
      ),
      throwsA(isA<ArchivoComprobanteException>().having(
        (error) => error.error,
        'error',
        ArchivoComprobanteError.archivoVacio,
      )),
    );
  });

  test('limita el tamaño al máximo real de 5 MiB', () {
    expect(
      () => ArchivoComprobante.validar(
        nombre: 'archivo.pdf',
        bytes: Uint8List(ArchivoComprobante.maxBytes + 1),
      ),
      throwsA(isA<ArchivoComprobanteException>().having(
        (error) => error.error,
        'error',
        ArchivoComprobanteError.demasiadoGrande,
      )),
    );
  });
}
