import 'api_exception.dart';
import 'attendance_location_source.dart';

String asistenciaErrorMessage(Object error) {
  if (error is AttendanceLocationException) {
    return switch (error.failure) {
      AttendanceLocationFailure.servicesDisabled =>
        'Activa la ubicación del teléfono para registrar asistencia.',
      AttendanceLocationFailure.permissionDenied =>
        'Se necesita permiso de ubicación para validar esta sesión.',
      AttendanceLocationFailure.permissionDeniedForever =>
        'Habilita el permiso de ubicación de Vidia en los ajustes del teléfono.',
      AttendanceLocationFailure.unavailable =>
        'No se pudo obtener la ubicación. Comprueba la señal GPS e inténtalo otra vez.',
    };
  }

  if (error is! ApiException) {
    return 'No se pudo registrar la asistencia. Inténtalo nuevamente.';
  }

  return switch (error.code) {
    'ATTENDANCE_DUPLICATED' =>
      'Tu asistencia ya fue registrada para esta sesión.',
    'QR_EXPIRED' =>
      'El código de asistencia ha expirado. Solicita un nuevo código.',
    'QR_REVOKED' =>
      'El código fue reemplazado. Solicita que muestren el código vigente.',
    'QR_INVALID' => 'El código no es válido para registrar asistencia.',
    'ATTENDANCE_OUTSIDE_RADIUS' =>
      'No te encuentras dentro del área permitida para registrar asistencia.',
    'ATTENDANCE_GPS_INVALID' ||
    'ATTENDANCE_GPS_ACCURACY_INVALID' =>
      'No se pudo validar una ubicación precisa. Revisa la ubicación e inténtalo otra vez.',
    'INSCRIPTION_REQUIRED' ||
    'INSCRIPTION_NOT_CONFIRMED' =>
      'Necesitas una inscripción confirmada para registrar asistencia.',
    'ATTENDANCE_SESSION_INACTIVE' ||
    'ATTENDANCE_SESSION_NOT_REQUIRED' ||
    'ATTENDANCE_OUTSIDE_WINDOW' ||
    'EVENT_NOT_PUBLISHED' =>
      'Esta sesión no está disponible para registrar asistencia.',
    _ => switch (error.statusCode) {
        401 => 'Tu sesión expiró. Inicia sesión nuevamente.',
        403 => 'No tienes autorización para registrar esta asistencia.',
        404 => 'La sesión ya no está disponible.',
        409 => 'Tu asistencia ya fue registrada para esta sesión.',
        _ => 'No se pudo registrar la asistencia. Inténtalo nuevamente.',
      },
  };
}
