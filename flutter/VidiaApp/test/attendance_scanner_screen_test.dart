import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:vidia/models/asistencia.dart';
import 'package:vidia/models/sesion_evento.dart';
import 'package:vidia/repositories/asistencia_repository.dart';
import 'package:vidia/screens/attendance_scanner_screen.dart';
import 'package:vidia/services/attendance_location_source.dart';

void main() {
  testWidgets('escaneo solicita ubicación explicada y muestra respuesta real',
      (tester) async {
    final repository = _FakeAttendanceRepository();
    final location = _FakeLocationSource();
    late ValueChanged<String> scannedToken;

    await tester.pumpWidget(MaterialApp(
      home: AttendanceScannerScreen(
        session: SesionEvento.fromJson(_sessionJson),
        requiresGps: true,
        repository: repository,
        locationSource: location,
        scannerBuilder: (_, onToken) {
          scannedToken = onToken;
          return Center(
            child: TextButton(
              onPressed: () => onToken('raw-temporary-qr-token'),
              child: const Text('Simular lectura QR'),
            ),
          );
        },
      ),
    ));

    await tester.tap(find.text('Activar cámara'));
    await tester.pumpAndSettle();
    expect(find.text('Simular lectura QR'), findsOneWidget);

    scannedToken('raw-temporary-qr-token');
    scannedToken('raw-temporary-qr-token');
    await tester.pump();
    expect(find.text('Ubicación para validar asistencia'), findsOneWidget);

    await tester.tap(find.text('Continuar'));
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 200));
    await tester.pumpAndSettle();

    expect(repository.registerCalls, 1);
    expect(repository.token, 'raw-temporary-qr-token');
    expect(repository.latitude, -21.535);
    expect(repository.longitude, -64.729);
    expect(repository.precision, 6.0);
    expect(location.calls, 1);
    expect(find.text('Asistencia registrada correctamente'), findsOneWidget);
    expect(find.text('Sesión móvil'), findsOneWidget);
  });
}

const _sessionJson = {
  'id': '30000000-0000-0000-0000-000000000001',
  'eventoId': '10000000-0000-0000-0000-000000000001',
  'nombre': 'Sesión móvil',
  'fecha': '2026-10-06',
  'horaInicio': '09:00:00',
  'horaFin': '10:00:00',
  'requiereAsistencia': true,
  'latitud': -21.535,
  'longitud': -64.729,
  'radioMetros': 80,
  'activa': true,
  'historica': false,
};

class _FakeAttendanceRepository implements AsistenciaRepository {
  int registerCalls = 0;
  String? token;
  double? latitude;
  double? longitude;
  double? precision;

  @override
  Future<List<Asistencia>> fetchMine() async => const [];

  @override
  Future<List<SesionEvento>> fetchSessions(String eventoId) async => const [];

  @override
  Future<Asistencia> register({
    required String token,
    double? latitude,
    double? longitude,
    double? precision,
  }) async {
    registerCalls++;
    this.token = token;
    this.latitude = latitude;
    this.longitude = longitude;
    this.precision = precision;
    return Asistencia.fromJson({
      'id': '40000000-0000-0000-0000-000000000001',
      'evento': 'Evento móvil',
      'sesionEventoId': _sessionJson['id'],
      'sesion': 'Sesión móvil',
      'fechaHoraRegistro': '2026-10-06T09:15:00',
      'resultadoValidacion': 'VALIDADA',
    });
  }
}

class _FakeLocationSource implements AttendanceLocationSource {
  int calls = 0;

  @override
  Future<AttendancePosition> currentPosition() async {
    calls++;
    return const AttendancePosition(
      latitude: -21.535,
      longitude: -64.729,
      accuracy: 6,
    );
  }
}
