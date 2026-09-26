class Inscripcion {
  const Inscripcion({
    required this.id,
    required this.eventoId,
    required this.eventoTitulo,
    required this.fechaInscripcion,
    required this.estado,
  });

  final String id;
  final String eventoId;
  final String eventoTitulo;
  final DateTime? fechaInscripcion;
  final String estado;

  factory Inscripcion.fromJson(Map<String, dynamic> json) {
    final id = json['id']?.toString() ?? '';
    final eventoId = json['eventoId']?.toString() ?? '';
    if (id.isEmpty || eventoId.isEmpty) {
      throw const FormatException('La inscripción recibida no es válida.');
    }
    return Inscripcion(
      id: id,
      eventoId: eventoId,
      eventoTitulo: json['eventoTitulo']?.toString() ?? 'Evento',
      fechaInscripcion: DateTime.tryParse(
        json['fechaInscripcion']?.toString() ?? '',
      ),
      estado: json['estado']?.toString() ?? '',
    );
  }
}
