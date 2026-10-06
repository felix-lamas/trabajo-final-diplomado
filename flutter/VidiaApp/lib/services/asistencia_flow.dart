import '../models/asistencia.dart';
import '../models/sesion_evento.dart';
import '../repositories/asistencia_repository.dart';
import 'attendance_location_source.dart';

class AsistenciaFlow {
  const AsistenciaFlow({
    required AsistenciaRepository repository,
    required AttendanceLocationSource locationSource,
  })  : _repository = repository,
        _locationSource = locationSource;

  final AsistenciaRepository _repository;
  final AttendanceLocationSource _locationSource;

  Future<Asistencia> submit({
    required SesionEvento session,
    required String rawToken,
    required bool requiresGps,
  }) async {
    if (!requiresGps) {
      return _repository.register(token: rawToken);
    }

    final position = await _locationSource.currentPosition();
    return _repository.register(
      token: rawToken,
      latitude: position.latitude,
      longitude: position.longitude,
      precision: position.accuracy,
    );
  }
}
