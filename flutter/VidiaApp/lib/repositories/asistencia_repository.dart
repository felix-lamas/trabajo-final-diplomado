import '../models/asistencia.dart';
import '../models/sesion_evento.dart';
import '../services/api_service.dart';

abstract interface class AsistenciaRepository {
  Future<List<SesionEvento>> fetchSessions(String eventoId);
  Future<List<Asistencia>> fetchMine();
  Future<Asistencia> register({
    required String token,
    double? latitude,
    double? longitude,
    double? precision,
  });
}

class BackendAsistenciaRepository implements AsistenciaRepository {
  BackendAsistenciaRepository(this._api);

  final ApiService _api;

  @override
  Future<List<SesionEvento>> fetchSessions(String eventoId) async {
    final response = await _api.get('/eventos/$eventoId/sesiones');
    return (response as List<dynamic>)
        .map((item) => SesionEvento.fromJson(item as Map<String, dynamic>))
        .toList(growable: false);
  }

  @override
  Future<List<Asistencia>> fetchMine() async {
    final response = await _api.get('/asistencias/mis-asistencias');
    return (response as List<dynamic>)
        .map((item) => Asistencia.fromJson(item as Map<String, dynamic>))
        .toList(growable: false);
  }

  @override
  Future<Asistencia> register({
    required String token,
    double? latitude,
    double? longitude,
    double? precision,
  }) async {
    final body = <String, Object>{'token': token};
    if (latitude != null) body['latitud'] = latitude;
    if (longitude != null) body['longitud'] = longitude;
    if (precision != null) body['precision'] = precision;
    final response = await _api.post('/asistencias', body: body);
    return Asistencia.fromJson(response as Map<String, dynamic>);
  }
}
