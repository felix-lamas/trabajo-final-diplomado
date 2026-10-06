import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:vidia/models/certificado.dart';
import 'package:vidia/repositories/certificado_repository.dart';
import 'package:vidia/services/certificado_document_manager.dart';
import 'package:vidia/services/certificado_file_service.dart';

const _id = '50000000-0000-0000-0000-000000000001';
final _pdf = Uint8List.fromList('%PDF-1.7\nbody'.codeUnits);

void main() {
  test('usa nombre recibido en Content-Disposition al guardar bytes PDF',
      () async {
    final files = _FakeFiles();
    final manager = CertificadoDocumentManager(
      repository: _FakeCertificates(
        CertificadoPdf(
          bytes: _pdf,
          contentType: 'application/pdf',
          contentDisposition: 'attachment; filename="mi-certificado.pdf"',
        ),
      ),
      files: files,
    );

    final saved = await manager.download(_id);

    expect(saved, '/downloads/mi-certificado.pdf');
    expect(files.savedName, 'mi-certificado.pdf');
    expect(files.savedBytes, _pdf);
  });

  test('abre PDF real con Content-Disposition UTF-8', () async {
    final files = _FakeFiles()..openResult = true;
    final manager = CertificadoDocumentManager(
      repository: _FakeCertificates(
        CertificadoPdf(
          bytes: _pdf,
          contentType: 'application/pdf',
          contentDisposition:
              "attachment; filename*=UTF-8''certificado%20UAJMS.pdf",
        ),
      ),
      files: files,
    );

    expect(await manager.preview(_id), isTrue);
    expect(files.openedName, 'certificado UAJMS.pdf');
    expect(files.openedBytes, _pdf);
  });

  test('rechaza response JSON o bytes que no tengan firma PDF', () async {
    final files = _FakeFiles();
    final manager = CertificadoDocumentManager(
      repository: _FakeCertificates(
        const CertificadoPdf(
          bytes: [123, 34, 105, 100, 34, 58, 49, 125],
          contentType: 'application/json',
        ),
      ),
      files: files,
    );

    await expectLater(manager.download(_id), throwsA(isA<FormatException>()));
    expect(files.savedBytes, isNull);
  });

  test('deriva nombre seguro y fuerza extensión PDF', () {
    expect(certificateFileName(_id, null), 'certificado-$_id.pdf');
    expect(
      certificateFileName(_id, 'attachment; filename="../privado/certificado"'),
      'certificado.pdf',
    );
  });
}

class _FakeCertificates implements CertificadoRepository {
  _FakeCertificates(this.pdf);
  final CertificadoPdf pdf;

  @override
  Future<List<Certificado>> fetchMine() async => const [];

  @override
  Future<Certificado> fetchById(String id) => throw UnimplementedError();

  @override
  Future<CertificadoPdf> downloadPdf(String id) async => pdf;

  @override
  Future<VerificacionCertificado> verifyPublic(String code) =>
      throw UnimplementedError();
}

class _FakeFiles implements CertificadoFileService {
  String? savedName;
  String? openedName;
  Uint8List? savedBytes;
  Uint8List? openedBytes;
  bool openResult = false;

  @override
  Future<String?> savePdf(
      {required String fileName, required Uint8List bytes}) async {
    savedName = fileName;
    savedBytes = bytes;
    return '/downloads/$fileName';
  }

  @override
  Future<bool> openPdf(
      {required String fileName, required Uint8List bytes}) async {
    openedName = fileName;
    openedBytes = bytes;
    return openResult;
  }
}
