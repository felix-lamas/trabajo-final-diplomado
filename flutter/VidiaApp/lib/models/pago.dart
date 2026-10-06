class Pago {
  const Pago({
    required this.id,
    required this.inscripcionId,
    required this.eventoTitulo,
    required this.monto,
    required this.estado,
    this.fechaPago,
    this.observacion,
    this.motivoRechazo,
    this.fechaResolucion,
    this.intentosComprobante,
    this.comprobante,
  });

  final String id;
  final String inscripcionId;
  final String eventoTitulo;
  final double monto;
  final String estado;
  final DateTime? fechaPago;
  final String? observacion;
  final String? motivoRechazo;
  final DateTime? fechaResolucion;
  final int? intentosComprobante;
  final PagoComprobante? comprobante;

  bool get puedePresentarComprobante =>
      estado == 'PENDIENTE_PAGO' || estado == 'RECHAZADO';

  factory Pago.fromJson(Map<String, dynamic> json) {
    final id = json['id']?.toString() ?? '';
    final inscripcionId = json['inscripcionId']?.toString() ?? '';
    final eventoTitulo = json['eventoTitulo']?.toString() ?? '';
    final monto = json['monto'];
    final estado = json['estado']?.toString() ?? '';
    if (id.isEmpty || inscripcionId.isEmpty || monto is! num || estado.isEmpty) {
      throw const FormatException('El pago recibido no es vÃ¡lido.');
    }
    final receipt = json['comprobante'];
    return Pago(
      id: id,
      inscripcionId: inscripcionId,
      eventoTitulo: eventoTitulo,
      monto: monto.toDouble(),
      estado: estado,
      fechaPago: _date(json['fechaPago']),
      observacion: _optional(json['observacion']),
      motivoRechazo: _optional(json['motivoRechazo']),
      fechaResolucion: _date(json['fechaResolucion']),
      intentosComprobante: (json['intentosComprobante'] as num?)?.toInt(),
      comprobante: receipt is Map<String, dynamic>
          ? PagoComprobante.fromJson(receipt)
          : null,
    );
  }

  static DateTime? _date(dynamic value) =>
      value == null ? null : DateTime.tryParse(value.toString());

  static String? _optional(dynamic value) {
    final text = value?.toString().trim();
    return text == null || text.isEmpty ? null : text;
  }
}

class PagoComprobante {
  const PagoComprobante({
    required this.id,
    required this.nombreArchivo,
    required this.tipoContenido,
    required this.disponible,
    this.fechaCarga,
  });

  final String id;
  final String nombreArchivo;
  final String tipoContenido;
  final DateTime? fechaCarga;
  final bool disponible;

  factory PagoComprobante.fromJson(Map<String, dynamic> json) {
    final id = json['id']?.toString() ?? '';
    final nombre = json['nombreArchivo']?.toString() ?? '';
    final mime = json['tipoContenido']?.toString() ?? '';
    if (id.isEmpty || nombre.isEmpty || mime.isEmpty) {
      throw const FormatException('Los datos del comprobante no son vÃ¡lidos.');
    }
    return PagoComprobante(
      id: id,
      nombreArchivo: nombre,
      tipoContenido: mime,
      fechaCarga: json['fechaCarga'] == null
          ? null
          : DateTime.tryParse(json['fechaCarga'].toString()),
      disponible: json['disponible'] as bool? ?? false,
    );
  }
}

class ComprobanteInscripcion {
  const ComprobanteInscripcion({
    required this.inscripcionId,
    required this.codigoInscripcion,
    required this.eventoId,
    required this.eventoTitulo,
    required this.monto,
    required this.estadoInscripcion,
    required this.estadoPago,
    required this.codigoVerificacion,
    this.fechaInscripcion,
  });

  final String inscripcionId;
  final String codigoInscripcion;
  final String eventoId;
  final String eventoTitulo;
  final double monto;
  final String estadoInscripcion;
  final String estadoPago;
  final String codigoVerificacion;
  final DateTime? fechaInscripcion;

  factory ComprobanteInscripcion.fromJson(Map<String, dynamic> json) {
    final monto = json['monto'];
    final id = json['inscripcionId']?.toString() ?? '';
    final eventoId = json['eventoId']?.toString() ?? '';
    if (monto is! num || id.isEmpty || eventoId.isEmpty) {
      throw const FormatException(
          'La constancia de inscripciÃ³n recibida no es vÃ¡lida.');
    }
    return ComprobanteInscripcion(
      inscripcionId: id,
      codigoInscripcion: json['codigoInscripcion']?.toString() ?? '',
      eventoId: eventoId,
      eventoTitulo: json['eventoTitulo']?.toString() ?? '',
      monto: monto.toDouble(),
      fechaInscripcion: json['fechaInscripcion'] == null
          ? null
          : DateTime.tryParse(json['fechaInscripcion'].toString()),
      estadoInscripcion: json['estadoInscripcion']?.toString() ?? '',
      estadoPago: json['estadoPago']?.toString() ?? '',
      codigoVerificacion: json['codigoVerificacion']?.toString() ?? '',
    );
  }
}
