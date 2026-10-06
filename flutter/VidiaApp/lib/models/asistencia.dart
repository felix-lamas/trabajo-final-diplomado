class Asistencia {
  const Asistencia({
    required this.id,
    required this.sesionEventoId,
    required this.evento,
    required this.sesion,
    required this.fechaHoraRegistro,
    required this.resultadoValidacion,
    this.distanciaMetros,
    this.precisionGpsMetros,
    this.observacion,
  });

  final String id;
  final String sesionEventoId;
  final String evento;
  final String sesion;
  final DateTime? fechaHoraRegistro;
  final String resultadoValidacion;
  final double? distanciaMetros;
  final double? precisionGpsMetros;
  final String? observacion;

  factory Asistencia.fromJson(Map<String, dynamic> json) {
    final id = json['id']?.toString() ?? '';
    final sesionEventoId = json['sesionEventoId']?.toString() ?? '';
    final evento = json['evento']?.toString() ?? '';
    final sesion = json['sesion']?.toString() ?? '';
    if (id.isEmpty ||
        sesionEventoId.isEmpty ||
        evento.isEmpty ||
        sesion.isEmpty) {
      throw const FormatException('La asistencia recibida no es válida.');
    }
    return Asistencia(
      id: id,
      sesionEventoId: sesionEventoId,
      evento: evento,
      sesion: sesion,
      fechaHoraRegistro:
          DateTime.tryParse(json['fechaHoraRegistro']?.toString() ?? ''),
      resultadoValidacion: json['resultadoValidacion']?.toString() ?? '',
      distanciaMetros: (json['distanciaMetros'] as num?)?.toDouble(),
      precisionGpsMetros: (json['precisionGpsMetros'] as num?)?.toDouble(),
      observacion: _optional(json['observacion']),
    );
  }

  static String? _optional(Object? value) {
    final text = value?.toString().trim();
    return text == null || text.isEmpty ? null : text;
  }
}
