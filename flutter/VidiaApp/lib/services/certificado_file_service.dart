import 'dart:io';
import 'dart:typed_data';

import 'package:file_picker/file_picker.dart';
import 'package:open_filex/open_filex.dart';
import 'package:path_provider/path_provider.dart';

abstract interface class CertificadoFileService {
  Future<String?> savePdf({
    required String fileName,
    required Uint8List bytes,
  });

  Future<bool> openPdf({
    required String fileName,
    required Uint8List bytes,
  });
}

class PlatformCertificadoFileService implements CertificadoFileService {
  @override
  Future<String?> savePdf({
    required String fileName,
    required Uint8List bytes,
  }) =>
      FilePicker.platform.saveFile(
        dialogTitle: 'Guardar certificado',
        fileName: fileName,
        type: FileType.custom,
        allowedExtensions: const ['pdf'],
        bytes: bytes,
      );

  @override
  Future<bool> openPdf({
    required String fileName,
    required Uint8List bytes,
  }) async {
    final cache = await getTemporaryDirectory();
    final directory =
        Directory('${cache.path}${Platform.pathSeparator}certificados');
    await directory.create(recursive: true);
    final file = File('${directory.path}${Platform.pathSeparator}$fileName');
    await file.writeAsBytes(bytes, flush: true);
    final result = await OpenFilex.open(file.path, type: 'application/pdf');
    return result.type == ResultType.done;
  }
}
