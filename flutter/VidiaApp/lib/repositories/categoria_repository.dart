import '../models/categoria_evento.dart';

abstract interface class CategoriaRepository {
  Future<List<CategoriaEvento>> fetchActive();
}
