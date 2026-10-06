class CategoriaEvento {
  const CategoriaEvento({required this.id, required this.nombre});

  final String id;
  final String nombre;

  factory CategoriaEvento.fromJson(Map<String, dynamic> json) {
    final id = json['id']?.toString() ?? '';
    final nombre = json['nombre']?.toString() ?? '';
    if (id.isEmpty || nombre.isEmpty) {
      throw const FormatException('La categoría recibida no es válida.');
    }
    return CategoriaEvento(id: id, nombre: nombre);
  }
}
