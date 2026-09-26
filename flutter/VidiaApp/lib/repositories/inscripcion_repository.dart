import '../models/inscripcion.dart';

abstract interface class InscripcionRepository {
  Future<Inscripcion> create(String eventoId);
  Future<List<Inscripcion>> fetchMine();
}
