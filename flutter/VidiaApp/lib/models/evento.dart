class Evento {
  const Evento({
    required this.id,
    required this.titulo,
    required this.descripcion,
    required this.objetivos,
    required this.categoriaNombre,
    required this.modalidad,
    required this.tipoInscripcion,
    required this.costo,
    required this.fechaInicio,
    required this.fechaFin,
    required this.horaInicio,
    required this.horaFin,
    required this.estado,
    required this.requiereInscripcion,
    required this.cupoLimitado,
    this.cupoMaximo,
    this.cupoDisponible,
    this.imagenPortada,
    this.ubicacion,
    this.direccion,
    this.enlaceVirtual,
    this.emiteCertificado = false,
    this.tipoCertificado,
    this.horasAcademicas,
    this.organizadorNombre,
  });

  final String id;
  final String titulo;
  final String descripcion;
  final String objetivos;
  final String categoriaNombre;
  final String modalidad;
  final String tipoInscripcion;
  final double costo;
  final DateTime? fechaInicio;
  final DateTime? fechaFin;
  final String horaInicio;
  final String horaFin;
  final String estado;
  final bool requiereInscripcion;
  final bool cupoLimitado;
  final int? cupoMaximo;
  final int? cupoDisponible;
  final String? imagenPortada;
  final String? ubicacion;
  final String? direccion;
  final String? enlaceVirtual;
  final bool emiteCertificado;
  final String? tipoCertificado;
  final int? horasAcademicas;
  final String? organizadorNombre;

  bool get esGratuito => tipoInscripcion == 'GRATUITO';
  bool get sinCupo => cupoLimitado && (cupoDisponible ?? 0) <= 0;

  factory Evento.fromJson(Map<String, dynamic> json) {
    final id = json['id']?.toString() ?? '';
    final titulo = json['titulo']?.toString() ?? '';
    if (id.isEmpty || titulo.isEmpty) {
      throw const FormatException(
          'El evento recibido no contiene id o título.');
    }
    return Evento(
      id: id,
      titulo: titulo,
      descripcion: json['descripcion']?.toString() ?? '',
      objetivos: json['objetivos']?.toString() ?? '',
      categoriaNombre: json['categoriaNombre']?.toString() ?? 'Sin categoría',
      modalidad: json['modalidad']?.toString() ?? '',
      tipoInscripcion: json['tipoInscripcion']?.toString() ?? '',
      costo: (json['costo'] as num?)?.toDouble() ?? 0,
      fechaInicio: _date(json['fechaInicio']),
      fechaFin: _date(json['fechaFin']),
      horaInicio: _time(json['horaInicio']),
      horaFin: _time(json['horaFin']),
      estado: json['estado']?.toString() ?? '',
      requiereInscripcion: json['requiereInscripcion'] as bool? ?? true,
      cupoLimitado: json['cupoLimitado'] as bool? ?? false,
      cupoMaximo: (json['cupoMaximo'] as num?)?.toInt(),
      cupoDisponible: (json['cupoDisponible'] as num?)?.toInt(),
      imagenPortada: _optional(json['imagenPortada']),
      ubicacion: _optional(json['ubicacion']),
      direccion: _optional(json['direccion']),
      enlaceVirtual: _optional(json['enlaceVirtual']),
      emiteCertificado: json['emiteCertificado'] as bool? ?? false,
      tipoCertificado: _optional(json['tipoCertificado']),
      horasAcademicas: (json['horasAcademicas'] as num?)?.toInt(),
      organizadorNombre: _optional(json['organizadorNombre']),
    );
  }

  static DateTime? _date(dynamic value) =>
      value == null ? null : DateTime.tryParse(value.toString());

  static String _time(dynamic value) {
    if (value == null) return '';
    if (value is List && value.length >= 2) {
      return '${value[0].toString().padLeft(2, '0')}:${value[1].toString().padLeft(2, '0')}';
    }
    final text = value.toString();
    return text.length >= 5 ? text.substring(0, 5) : text;
  }

  static String? _optional(dynamic value) {
    final text = value?.toString().trim();
    return text == null || text.isEmpty ? null : text;
  }
}

String formatEventDate(DateTime? date) {
  if (date == null) return 'Fecha por confirmar';
  return '${date.day.toString().padLeft(2, '0')}/'
      '${date.month.toString().padLeft(2, '0')}/${date.year}';
}

String formatEventPrice(Evento event) =>
    event.esGratuito ? 'Gratuito' : 'Bs. ${event.costo.toStringAsFixed(2)}';
