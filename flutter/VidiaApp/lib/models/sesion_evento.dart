class SesionEvento {
  const SesionEvento({
    required this.id,
    required this.eventoId,
    required this.nombre,
    required this.fecha,
    required this.horaInicio,
    required this.horaFin,
    required this.requiereAsistencia,
    required this.activa,
    required this.historica,
    this.descripcion,
    this.latitud,
    this.longitud,
    this.radioMetros,
  });

  final String id;
  final String eventoId;
  final String nombre;
  final DateTime? fecha;
  final String horaInicio;
  final String horaFin;
  final bool? requiereAsistencia;
  final bool? activa;
  final bool? historica;
  final String? descripcion;
  final double? latitud;
  final double? longitud;
  final int? radioMetros;

  bool get requiereGps => latitud != null || longitud != null;

  factory SesionEvento.fromJson(Map<String, dynamic> json) {
    final id = json['id']?.toString() ?? '';
    final eventoId = json['eventoId']?.toString() ?? '';
    final nombre = json['nombre']?.toString() ?? '';
    if (id.isEmpty || eventoId.isEmpty || nombre.isEmpty) {
      throw const FormatException('La sesión recibida no es válida.');
    }
    return SesionEvento(
      id: id,
      eventoId: eventoId,
      nombre: nombre,
      descripcion: _optional(json['descripcion']),
      fecha: DateTime.tryParse(json['fecha']?.toString() ?? ''),
      horaInicio: _time(json['horaInicio']),
      horaFin: _time(json['horaFin']),
      requiereAsistencia: json['requiereAsistencia'] as bool?,
      latitud: (json['latitud'] as num?)?.toDouble(),
      longitud: (json['longitud'] as num?)?.toDouble(),
      radioMetros: (json['radioMetros'] as num?)?.toInt(),
      activa: json['activa'] as bool?,
      historica: json['historica'] as bool?,
    );
  }

  static String _time(Object? value) {
    if (value is List && value.length >= 2) {
      return '${value[0].toString().padLeft(2, '0')}:${value[1].toString().padLeft(2, '0')}';
    }
    final text = value?.toString() ?? '';
    return text.length >= 5 ? text.substring(0, 5) : text;
  }

  static String? _optional(Object? value) {
    final text = value?.toString().trim();
    return text == null || text.isEmpty ? null : text;
  }
}
