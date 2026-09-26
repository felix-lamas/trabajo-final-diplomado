import '../models/inscripcion.dart';
import '../services/api_service.dart';
import 'inscripcion_repository.dart';

class BackendInscripcionRepository implements InscripcionRepository {
  BackendInscripcionRepository(this._api);

  final ApiService _api;

  @override
  Future<Inscripcion> create(String eventoId) async {
    final response = await _api.post(
      '/inscripciones',
      body: {'eventoId': eventoId},
    );
    return Inscripcion.fromJson(response as Map<String, dynamic>);
  }

  @override
  Future<List<Inscripcion>> fetchMine() async {
    final response = await _api.get('/inscripciones/mis-inscripciones');
    return (response as List<dynamic>)
        .map((item) => Inscripcion.fromJson(item as Map<String, dynamic>))
        .toList(growable: false);
  }
}
