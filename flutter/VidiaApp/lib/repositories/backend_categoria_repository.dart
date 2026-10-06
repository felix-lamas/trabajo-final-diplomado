import '../models/categoria_evento.dart';
import '../services/api_service.dart';
import 'categoria_repository.dart';

class BackendCategoriaRepository implements CategoriaRepository {
  BackendCategoriaRepository(this._api);

  final ApiService _api;

  @override
  Future<List<CategoriaEvento>> fetchActive() async {
    final response = await _api.get('/categorias-evento/activas');
    return (response as List<dynamic>)
        .map((item) => CategoriaEvento.fromJson(item as Map<String, dynamic>))
        .toList(growable: false);
  }
}
