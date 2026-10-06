import 'dart:typed_data';

import '../models/certificado.dart';
import '../repositories/certificado_repository.dart';
import 'certificado_file_service.dart';

class CertificadoDocumentManager {
  const CertificadoDocumentManager({
    required CertificadoRepository repository,
    required CertificadoFileService files,
  })  : _repository = repository,
        _files = files;

  final CertificadoRepository _repository;
  final CertificadoFileService _files;

  Future<String?> download(String id) async {
    final pdf = await _repository.downloadPdf(id);
    final name = certificateFileName(id, pdf.contentDisposition);
    _validatePdf(pdf);
    return _files.savePdf(fileName: name, bytes: Uint8List.fromList(pdf.bytes));
  }

  Future<bool> preview(String id) async {
    final pdf = await _repository.downloadPdf(id);
    final name = certificateFileName(id, pdf.contentDisposition);
    _validatePdf(pdf);
    return _files.openPdf(fileName: name, bytes: Uint8List.fromList(pdf.bytes));
  }

  void _validatePdf(CertificadoPdf pdf) {
    if (pdf.contentType?.toLowerCase() != 'application/pdf' ||
        pdf.bytes.length < 5 ||
        String.fromCharCodes(pdf.bytes.take(5)) != '%PDF-') {
      throw const FormatException(
        'El servidor no devolvió un documento PDF válido.',
      );
    }
  }
}

String certificateFileName(String id, String? contentDisposition) {
  final disposition = contentDisposition ?? '';
  final encoded = RegExp(r"filename\*=UTF-8''([^;]+)", caseSensitive: false)
      .firstMatch(disposition)
      ?.group(1);
  final plain = RegExp(r'filename="?([^";]+)', caseSensitive: false)
      .firstMatch(disposition)
      ?.group(1);
  var name = encoded == null ? plain : Uri.decodeComponent(encoded);
  name = name?.trim() ?? '';
  name = name.split(RegExp(r'[/\\]')).last;
  name = name.replaceAll(RegExp(r'[\x00-\x1F<>:"|?*]'), '_');
  if (name.isEmpty) name = 'certificado-$id.pdf';
  if (!name.toLowerCase().endsWith('.pdf')) name = '$name.pdf';
  return name;
}
