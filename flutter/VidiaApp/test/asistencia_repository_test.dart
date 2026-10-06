import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:vidia/models/asistencia.dart';
import 'package:vidia/models/sesion_evento.dart';
import 'package:vidia/repositories/asistencia_repository.dart';
import 'package:vidia/services/api_exception.dart';
import 'package:vidia/services/api_service.dart';
import 'package:vidia/services/asistencia_error_message.dart';
import 'package:vidia/services/asistencia_flow.dart';
import 'package:vidia/services/attendance_location_source.dart';

import 'support/memory_token_store.dart';

const _sessionJson = {
  'id': '30000000-0000-0000-0000-000000000001',
  'eventoId': '10000000-0000-0000-0000-000000000001',
  'nombre': 'Sesión de apertura',
  'descripcion': 'Sesión de prueba',
  'fecha': '2026-10-06',
  'horaInicio': '09:30:00',
  'horaFin': '10:30:00',
  'requiereAsistencia': true,
  'latitud': -21.535,
  'longitud': -64.729,
  'radioMetros': 100,
  'activa': true,
  'historica': false,
};

Map<String, dynamic> _attendanceJson() => {
      'id': '40000000-0000-0000-0000-000000000001',
      'nombreParticipante': 'Demo Participante',
      'documentoIdentidad': 'NO_MOSTRAR',
      'evento': 'Evento de prueba',
      'sesionEventoId': _sessionJson['id'],
      'sesion': 'Sesión de apertura',
      'fechaHoraRegistro': '2026-10-06T09:45:00',
      'distanciaMetros': 12.5,
      'precisionGpsMetros': 4.0,
      'resultadoValidacion': 'VALIDADA',
      'observacion': null,
    };

void main() {
  test('modelo de sesión interpreta fechas, horas y GPS del contrato', () {
    final session = SesionEvento.fromJson(_sessionJson);

    expect(session.id, _sessionJson['id']);
    expect(session.eventoId, _sessionJson['eventoId']);
    expect(session.horaInicio, '09:30');
    expect(session.requiereGps, isTrue);
    expect(session.radioMetros, 100);
    expect(session.fecha, DateTime(2026, 10, 6));
  });

  test('respuesta de asistencia parsea solo campos móviles de la respuesta',
      () {
    final attendance = Asistencia.fromJson(_attendanceJson());

    expect(attendance.sesionEventoId, _sessionJson['id']);
    expect(attendance.evento, 'Evento de prueba');
    expect(attendance.resultadoValidacion, 'VALIDADA');
    expect(attendance.fechaHoraRegistro, DateTime(2026, 10, 6, 9, 45));
    expect(attendance.distanciaMetros, 12.5);
  });

  test('lista sesiones usa GET autenticado del evento', () async {
    final store = MemoryTokenStore()..token = 'jwt-test';
    late http.Request captured;
    final repository = BackendAsistenciaRepository(ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient((request) async {
        captured = request;
        return http.Response(
          jsonEncode([_sessionJson]),
          200,
          headers: {'content-type': 'application/json; charset=utf-8'},
        );
      }),
    ));

    final sessions = await repository.fetchSessions(
      _sessionJson['eventoId']!.toString(),
    );

    expect(captured.method, 'GET');
    expect(captured.url.path,
        '/api/v1/eventos/${_sessionJson['eventoId']}/sesiones');
    expect(captured.headers['Authorization'], 'Bearer jwt-test');
    expect(sessions.single.nombre, 'Sesión de apertura');
  });

  test('envía al POST el token sin sessionId ni usuarioId', () async {
    final store = MemoryTokenStore()..token = 'jwt-test';
    late http.Request captured;
    final repository = BackendAsistenciaRepository(ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient((request) async {
        captured = request;
        return http.Response(
          jsonEncode(_attendanceJson()),
          201,
          headers: {'content-type': 'application/json; charset=utf-8'},
        );
      }),
    ));

    final attendance = await repository.register(token: 'qr-raw-token');

    expect(captured.method, 'POST');
    expect(captured.url.path, '/api/v1/asistencias');
    expect(captured.headers['Authorization'], 'Bearer jwt-test');
    expect(jsonDecode(captured.body), {'token': 'qr-raw-token'});
    expect(attendance.id, _attendanceJson()['id']);
  });

  test('envía latitud, longitud y precisión con los nombres reales', () async {
    final store = MemoryTokenStore()..token = 'jwt-test';
    late http.Request captured;
    final repository = BackendAsistenciaRepository(ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient((request) async {
        captured = request;
        return http.Response(
          jsonEncode(_attendanceJson()),
          201,
          headers: {'content-type': 'application/json; charset=utf-8'},
        );
      }),
    ));

    await repository.register(
      token: 'raw',
      latitude: -21.535,
      longitude: -64.729,
      precision: 4.0,
    );

    expect(jsonDecode(captured.body), {
      'token': 'raw',
      'latitud': -21.535,
      'longitud': -64.729,
      'precision': 4.0,
    });
  });

  test('consulta únicamente mis asistencias con JWT', () async {
    final store = MemoryTokenStore()..token = 'jwt-test';
    late http.Request captured;
    final repository = BackendAsistenciaRepository(ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient((request) async {
        captured = request;
        return http.Response(
          jsonEncode([_attendanceJson()]),
          200,
          headers: {'content-type': 'application/json; charset=utf-8'},
        );
      }),
    ));

    final records = await repository.fetchMine();

    expect(captured.method, 'GET');
    expect(captured.url.path, '/api/v1/asistencias/mis-asistencias');
    expect(captured.headers['Authorization'], 'Bearer jwt-test');
    expect(records.single.sesion, 'Sesión de apertura');
  });

  for (final (status, code) in <(int, String)>[
    (401, 'AUTH_INVALID_SESSION'),
    (403, 'ACCESS_DENIED'),
    (409, 'ATTENDANCE_DUPLICATED'),
    (400, 'QR_EXPIRED'),
    (400, 'ATTENDANCE_OUTSIDE_RADIUS'),
  ]) {
    test('conserva código de error backend $status $code', () async {
      final store = MemoryTokenStore()..token = 'jwt-test';
      final repository = BackendAsistenciaRepository(ApiService(
        baseUrl: 'https://api.example.test/api/v1',
        tokenStore: store,
        client: MockClient((_) async => http.Response(
              jsonEncode({'codigo': code, 'mensaje': 'mensaje backend'}),
              status,
            )),
      ));

      await expectLater(
        repository.register(token: 'qr'),
        throwsA(isA<ApiException>()
            .having((error) => error.statusCode, 'statusCode', status)
            .having((error) => error.code, 'code', code)),
      );
      if (status == 401) expect(store.token, isNull);
    });
  }

  test('flujo virtual sin GPS no solicita ubicación', () async {
    final repository = _RecordingRepository();
    final location = _RecordingLocation();
    final flow = AsistenciaFlow(
      repository: repository,
      locationSource: location,
    );

    await flow.submit(
      session: SesionEvento.fromJson({
        ..._sessionJson,
        'latitud': null,
        'longitud': null,
      }),
      rawToken: 'token-original',
      requiresGps: false,
    );

    expect(location.calls, 0);
    expect(repository.token, 'token-original');
    expect(repository.latitude, isNull);
    expect(repository.precision, isNull);
  });

  test('flujo GPS obtiene fix y envía precisión recibida del dispositivo',
      () async {
    final repository = _RecordingRepository();
    final location = _RecordingLocation();
    final flow = AsistenciaFlow(
      repository: repository,
      locationSource: location,
    );

    await flow.submit(
      session: SesionEvento.fromJson(_sessionJson),
      rawToken: 'token-original',
      requiresGps: true,
    );

    expect(location.calls, 1);
    expect(repository.latitude, -21.535);
    expect(repository.longitude, -64.729);
    expect(repository.precision, 5.5);
  });

  test('evento presencial exige captura GPS aunque no haya coordenadas de sesión',
      () async {
    final repository = _RecordingRepository();
    final location = _RecordingLocation();
    final flow = AsistenciaFlow(
      repository: repository,
      locationSource: location,
    );

    await flow.submit(
      session: SesionEvento.fromJson({
        ..._sessionJson,
        'latitud': null,
        'longitud': null,
      }),
      rawToken: 'token-original',
      requiresGps: true,
    );

    expect(location.calls, 1);
    expect(repository.latitude, -21.535);
    expect(repository.longitude, -64.729);
    expect(repository.precision, 5.5);
  });

  test('errores backend se traducen a mensajes de participante', () {
    expect(
      asistenciaErrorMessage(const ApiException(
          statusCode: 409, message: 'x', code: 'ATTENDANCE_DUPLICATED')),
      'Tu asistencia ya fue registrada para esta sesión.',
    );
    expect(
      asistenciaErrorMessage(const ApiException(
          statusCode: 400, message: 'x', code: 'QR_EXPIRED')),
      'El código de asistencia ha expirado. Solicita un nuevo código.',
    );
    expect(
      asistenciaErrorMessage(const ApiException(
          statusCode: 400, message: 'x', code: 'ATTENDANCE_OUTSIDE_RADIUS')),
      'No te encuentras dentro del área permitida para registrar asistencia.',
    );
    expect(
      asistenciaErrorMessage(const ApiException(
          statusCode: 403, message: 'x', code: 'ACCESS_DENIED')),
      'No tienes autorización para registrar esta asistencia.',
    );
  });
}

class _RecordingRepository implements AsistenciaRepository {
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
    this.token = token;
    this.latitude = latitude;
    this.longitude = longitude;
    this.precision = precision;
    return Asistencia.fromJson(_attendanceJson());
  }
}

class _RecordingLocation implements AttendanceLocationSource {
  int calls = 0;

  @override
  Future<AttendancePosition> currentPosition() async {
    calls++;
    return const AttendancePosition(
      latitude: -21.535,
      longitude: -64.729,
      accuracy: 5.5,
    );
  }
}
