class Certificado {
  const Certificado({
    required this.id,
    required this.evento,
    required this.tipoCertificado,
    required this.codigoCertificado,
    required this.estado,
    this.nombreCompleto,
    this.ru,
    this.ci,
    this.cargaHoraria,
    this.horasAcademicas,
    this.porcentajeAsistencia,
    this.fechaEmision,
    this.urlVerificacion,
    this.archivoPdfUrl,
  });

  final String id;
  final String evento;
  final String tipoCertificado;
  final String codigoCertificado;
  final String estado;
  final String? nombreCompleto;
  final String? ru;
  final String? ci;
  final int? cargaHoraria;
  final int? horasAcademicas;
  final double? porcentajeAsistencia;
  final DateTime? fechaEmision;
  final String? urlVerificacion;
  final String? archivoPdfUrl;

  factory Certificado.fromJson(Map<String, dynamic> json) {
    final id = json['id']?.toString() ?? '';
    final evento = json['evento']?.toString() ?? '';
    final codigo = json['codigoCertificado']?.toString() ?? '';
    final estado = json['estado']?.toString() ?? '';
    final tipo = json['tipoCertificado']?.toString() ?? '';
    if (id.isEmpty ||
        evento.isEmpty ||
        codigo.isEmpty ||
        estado.isEmpty ||
        tipo.isEmpty) {
      throw const FormatException('El certificado recibido no es válido.');
    }
    return Certificado(
      id: id,
      nombreCompleto: _optional(json['nombreCompleto']),
      ru: _optional(json['ru']),
      ci: _optional(json['ci']),
      evento: evento,
      cargaHoraria: (json['cargaHoraria'] as num?)?.toInt(),
      tipoCertificado: tipo,
      horasAcademicas: (json['horasAcademicas'] as num?)?.toInt(),
      porcentajeAsistencia: (json['porcentajeAsistencia'] as num?)?.toDouble(),
      codigoCertificado: codigo,
      fechaEmision: DateTime.tryParse(json['fechaEmision']?.toString() ?? ''),
      urlVerificacion: _optional(json['urlVerificacion']),
      estado: estado,
      archivoPdfUrl: _optional(json['archivoPdfUrl']),
    );
  }

  static String? _optional(Object? value) {
    final text = value?.toString().trim();
    return text == null || text.isEmpty ? null : text;
  }
}

class VerificacionCertificado {
  const VerificacionCertificado({
    required this.valido,
    required this.mensaje,
    required this.institucion,
    required this.codigoCertificado,
    required this.estado,
    this.nombreCompleto,
    this.evento,
    this.tipoCertificado,
    this.horasAcademicas,
    this.porcentajeAsistencia,
    this.fechaEmision,
  });

  final bool valido;
  final String mensaje;
  final String institucion;
  final String codigoCertificado;
  final String estado;
  final String? nombreCompleto;
  final String? evento;
  final String? tipoCertificado;
  final int? horasAcademicas;
  final double? porcentajeAsistencia;
  final DateTime? fechaEmision;

  factory VerificacionCertificado.fromJson(Map<String, dynamic> json) {
    final valido = json['valido'];
    final codigo = json['codigoCertificado']?.toString() ?? '';
    if (valido is! bool || codigo.isEmpty) {
      throw const FormatException('La verificación recibida no es válida.');
    }
    return VerificacionCertificado(
      valido: valido,
      mensaje: json['mensaje']?.toString() ?? '',
      institucion: json['institucion']?.toString() ?? '',
      nombreCompleto: _optional(json['nombreCompleto']),
      evento: _optional(json['evento']),
      tipoCertificado: _optional(json['tipoCertificado']),
      horasAcademicas: (json['horasAcademicas'] as num?)?.toInt(),
      porcentajeAsistencia: (json['porcentajeAsistencia'] as num?)?.toDouble(),
      fechaEmision: DateTime.tryParse(json['fechaEmision']?.toString() ?? ''),
      codigoCertificado: codigo,
      estado: json['estado']?.toString() ?? '',
    );
  }

  static String? _optional(Object? value) {
    final text = value?.toString().trim();
    return text == null || text.isEmpty ? null : text;
  }
}

class CertificadoPdf {
  const CertificadoPdf({
    required this.bytes,
    required this.contentType,
    this.contentDisposition,
  });

  final List<int> bytes;
  final String? contentType;
  final String? contentDisposition;
}
