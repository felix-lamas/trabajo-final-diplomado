import '../models/evento.dart';
import '../services/api_service.dart';
import 'evento_repository.dart';

class BackendEventoRepository implements EventoRepository {
  BackendEventoRepository(this._api);

  final ApiService _api;

  @override
  Future<List<Evento>> fetchPublished() async {
    final response = await _api.get(
      '/eventos/publicados',
      authenticated: false,
    );
    return (response as List<dynamic>)
        .map((item) => Evento.fromJson(item as Map<String, dynamic>))
        .toList(growable: false);
  }

  @override
  Future<List<Evento>> searchPublished({
    String? text,
    String? categoryId,
    String? type,
    String? modality,
  }) async {
    final query = <String, String>{};
    if (text != null && text.trim().isNotEmpty) query['texto'] = text.trim();
    if (categoryId != null) query['categoriaId'] = categoryId;
    if (type != null) query['tipo'] = type;
    if (modality != null) query['modalidad'] = modality;
    final path = Uri(path: '/eventos/publicados/buscar', queryParameters: query)
        .toString();
    final response = await _api.get(path, authenticated: false);
    return (response as List<dynamic>)
        .map((item) => Evento.fromJson(item as Map<String, dynamic>))
        .toList(growable: false);
  }

  @override
  Future<Evento> fetchById(String id) async {
    final response = await _api.get('/eventos/$id', authenticated: false);
    return Evento.fromJson(response as Map<String, dynamic>);
  }
}
