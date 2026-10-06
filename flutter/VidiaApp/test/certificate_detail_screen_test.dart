import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';
import 'package:vidia/models/certificado.dart';
import 'package:vidia/repositories/certificado_repository.dart';
import 'package:vidia/screens/certificate_detail_screen.dart';
import 'package:vidia/services/certificado_document_manager.dart';
import 'package:vidia/services/certificado_file_service.dart';

void main() {
  testWidgets('actualiza el estado después de descargar el PDF',
      (tester) async {
    final repository = _CertificateRepository();
    final documents = CertificadoDocumentManager(
      repository: repository,
      files: _FakeFiles(),
    );

    await tester.pumpWidget(MultiProvider(
      providers: [
        Provider<CertificadoRepository>.value(value: repository),
        Provider<CertificadoDocumentManager>.value(value: documents),
      ],
      child: const MaterialApp(
        home: CertificateDetailScreen(certificadoId: 'cert-1'),
      ),
    ));
    await tester.pumpAndSettle();

    expect(find.text('GENERADO'), findsNWidgets(2));
    await tester.tap(find.text('Descargar PDF'));
    await tester.pumpAndSettle();

    expect(repository.fetchCount, 2);
    expect(find.text('DESCARGADO'), findsNWidgets(2));
  });
}

class _CertificateRepository implements CertificadoRepository {
  int fetchCount = 0;

  Certificado _certificate(String state) => Certificado(
        id: 'cert-1',
        evento: 'Evento demo',
        tipoCertificado: 'NO_CURRICULAR',
        codigoCertificado: 'CODE-DEMO',
        estado: state,
      );

  @override
  Future<Certificado> fetchById(String id) async {
    fetchCount++;
    return _certificate(fetchCount == 1 ? 'GENERADO' : 'DESCARGADO');
  }

  @override
  Future<List<Certificado>> fetchMine() async => const [];

  @override
  Future<CertificadoPdf> downloadPdf(String id) async => const CertificadoPdf(
        bytes: [37, 80, 68, 70, 45, 49, 46, 55],
        contentType: 'application/pdf',
        contentDisposition: 'attachment; filename="certificado.pdf"',
      );

  @override
  Future<VerificacionCertificado> verifyPublic(String code) =>
      throw UnimplementedError();
}

class _FakeFiles implements CertificadoFileService {
  @override
  Future<bool> openPdf({
    required String fileName,
    required Uint8List bytes,
  }) async =>
      true;

  @override
  Future<String?> savePdf({
    required String fileName,
    required Uint8List bytes,
  }) async =>
      '/tmp/$fileName';
}
