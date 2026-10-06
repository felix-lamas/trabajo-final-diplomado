import 'package:geolocator/geolocator.dart';

class AttendancePosition {
  const AttendancePosition({
    required this.latitude,
    required this.longitude,
    required this.accuracy,
  });

  final double latitude;
  final double longitude;
  final double accuracy;
}

enum AttendanceLocationFailure {
  servicesDisabled,
  permissionDenied,
  permissionDeniedForever,
  unavailable,
}

class AttendanceLocationException implements Exception {
  const AttendanceLocationException(this.failure);

  final AttendanceLocationFailure failure;
}

abstract interface class AttendanceLocationSource {
  Future<AttendancePosition> currentPosition();
}

class GeolocatorAttendanceLocationSource implements AttendanceLocationSource {
  @override
  Future<AttendancePosition> currentPosition() async {
    if (!await Geolocator.isLocationServiceEnabled()) {
      throw const AttendanceLocationException(
        AttendanceLocationFailure.servicesDisabled,
      );
    }

    var permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
    }
    if (permission == LocationPermission.deniedForever) {
      throw const AttendanceLocationException(
        AttendanceLocationFailure.permissionDeniedForever,
      );
    }
    if (permission == LocationPermission.denied) {
      throw const AttendanceLocationException(
        AttendanceLocationFailure.permissionDenied,
      );
    }

    try {
      final position = await Geolocator.getCurrentPosition(
        locationSettings: const LocationSettings(
          accuracy: LocationAccuracy.best,
          timeLimit: Duration(seconds: 20),
        ),
      );
      return AttendancePosition(
        latitude: position.latitude,
        longitude: position.longitude,
        accuracy: position.accuracy,
      );
    } on AttendanceLocationException {
      rethrow;
    } catch (_) {
      throw const AttendanceLocationException(
        AttendanceLocationFailure.unavailable,
      );
    }
  }
}
