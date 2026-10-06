import '../models/certificado.dart';
import '../services/api_service.dart';

abstract interface class CertificadoRepository {
  Future<List<Certificado>> fetchMine();
  Future<Certificado> fetchById(String id);
  Future<CertificadoPdf> downloadPdf(String id);
  Future<VerificacionCertificado> verifyPublic(String code);
}

class BackendCertificadoRepository implements CertificadoRepository {
  BackendCertificadoRepository(this._api);

  final ApiService _api;

  @override
  Future<List<Certificado>> fetchMine() async {
    final response = await _api.get('/certificados/mis-certificados');
    return (response as List<dynamic>)
        .map((item) => Certificado.fromJson(item as Map<String, dynamic>))
        .toList(growable: false);
  }

  @override
  Future<Certificado> fetchById(String id) async {
    final response = await _api.get('/certificados/${Uri.encodeComponent(id)}');
    return Certificado.fromJson(response as Map<String, dynamic>);
  }

  @override
  Future<CertificadoPdf> downloadPdf(String id) async {
    final response = await _api.getBinary(
      '/certificados/${Uri.encodeComponent(id)}/descargar',
    );
    return CertificadoPdf(
      bytes: response.bytes,
      contentType: response.contentType,
      contentDisposition: response.contentDisposition,
    );
  }

  @override
  Future<VerificacionCertificado> verifyPublic(String code) async {
    final response = await _api.get(
      '/certificados/verificar/${Uri.encodeComponent(code.trim())}',
      authenticated: false,
    );
    return VerificacionCertificado.fromJson(response as Map<String, dynamic>);
  }
}
