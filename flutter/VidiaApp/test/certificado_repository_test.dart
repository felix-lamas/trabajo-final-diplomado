import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:vidia/models/certificado.dart';
import 'package:vidia/repositories/certificado_repository.dart';
import 'package:vidia/services/api_exception.dart';
import 'package:vidia/services/api_service.dart';

import 'support/memory_token_store.dart';

const _certificateId = '50000000-0000-0000-0000-000000000001';
const _code = 'UAJMS-ABC123';

Map<String, dynamic> _certificateJson() => {
      'id': _certificateId,
      'nombreCompleto': 'Participante Demo',
      'ru': 'RU-TEST',
      'ci': 'CI-TEST',
      'evento': 'Evento finalizado',
      'cargaHoraria': 30,
      'tipoCertificado': 'CURRICULAR',
      'horasAcademicas': 24,
      'porcentajeAsistencia': 87.5,
      'codigoCertificado': _code,
      'fechaEmision': '2026-10-06T10:00:00',
      'urlVerificacion': 'https://demo.local/verificar/$_code',
      'estado': 'GENERADO',
      'archivoPdfUrl': '/api/v1/certificados/$_certificateId/descargar',
    };

Map<String, dynamic> _verificationJson({bool valid = true}) => {
      'valido': valid,
      'mensaje': valid
          ? 'Certificado válido emitido por la UAJMS'
          : 'Certificado no registrado',
      'institucion': 'Universidad Demo',
      'nombreCompleto': valid ? 'Participante Demo' : null,
      'evento': valid ? 'Evento finalizado' : null,
      'tipoCertificado': valid ? 'CURRICULAR' : null,
      'horasAcademicas': valid ? 24 : null,
      'porcentajeAsistencia': valid ? 87.5 : null,
      'fechaEmision': valid ? '2026-10-06T10:00:00' : null,
      'codigoCertificado': _code,
      'estado': valid ? 'GENERADO' : 'NO_REGISTRADO',
    };

void main() {
  test('modelo refleja DTO y campos opcionales/números del certificado', () {
    final value = Certificado.fromJson(_certificateJson());

    expect(value.id, _certificateId);
    expect(value.evento, 'Evento finalizado');
    expect(value.tipoCertificado, 'CURRICULAR');
    expect(value.horasAcademicas, 24);
    expect(value.porcentajeAsistencia, 87.5);
    expect(value.fechaEmision, DateTime(2026, 10, 6, 10));
    expect(
        value.archivoPdfUrl, '/api/v1/certificados/$_certificateId/descargar');
  });

  test('consulta solo certificados del usuario autenticado', () async {
    final store = MemoryTokenStore()..token = 'test-bearer';
    late http.Request request;
    final repository = BackendCertificadoRepository(ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient((value) async {
        request = value;
        return http.Response(jsonEncode([_certificateJson()]), 200);
      }),
    ));

    final certificates = await repository.fetchMine();

    expect(request.method, 'GET');
    expect(request.url.path, '/api/v1/certificados/mis-certificados');
    expect(request.url.queryParameters, isEmpty);
    expect(request.headers['Authorization'], 'Bearer test-bearer');
    expect(certificates.single.id, _certificateId);
  });

  test('lista vacía se representa como lista vacía', () async {
    final repository = BackendCertificadoRepository(ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: MemoryTokenStore()..token = 'test-bearer',
      client: MockClient((_) async => http.Response('[]', 200)),
    ));

    expect(await repository.fetchMine(), isEmpty);
  });

  test('consulta individual usa endpoint real y autorización', () async {
    final store = MemoryTokenStore()..token = 'test-bearer';
    final repository = BackendCertificadoRepository(ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient((request) async {
        expect(request.url.path, '/api/v1/certificados/$_certificateId');
        expect(request.headers['Authorization'], 'Bearer test-bearer');
        return http.Response(jsonEncode(_certificateJson()), 200);
      }),
    ));

    expect(
        (await repository.fetchById(_certificateId)).codigoCertificado, _code);
  });

  test('descarga PDF binario sin parsearlo como JSON y conserva headers',
      () async {
    final bytes = <int>[37, 80, 68, 70, 45, 49, 46, 55, 0, 255, 13, 10];
    final repository = BackendCertificadoRepository(ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: MemoryTokenStore()..token = 'test-bearer',
      client: MockClient((request) async {
        expect(
            request.url.path, '/api/v1/certificados/$_certificateId/descargar');
        expect(request.headers['Authorization'], 'Bearer test-bearer');
        return http.Response.bytes(bytes, 200, headers: {
          'content-type': 'application/pdf',
          'content-disposition':
              'attachment; filename=certificado-$_certificateId.pdf',
        });
      }),
    ));

    final pdf = await repository.downloadPdf(_certificateId);

    expect(pdf.contentType, 'application/pdf');
    expect(pdf.contentDisposition, contains('certificado-$_certificateId.pdf'));
    expect(pdf.bytes, bytes);
  });

  test('verificación pública no envía Bearer y parsea resultado real',
      () async {
    final repository = BackendCertificadoRepository(ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: MemoryTokenStore()..token = 'must-not-be-sent',
      client: MockClient((request) async {
        expect(request.method, 'GET');
        expect(request.url.path, '/api/v1/certificados/verificar/$_code');
        expect(request.headers.containsKey('Authorization'), isFalse);
        return http.Response(
          jsonEncode(_verificationJson()),
          200,
          headers: {'content-type': 'application/json; charset=utf-8'},
        );
      }),
    ));

    final value = await repository.verifyPublic(_code);

    expect(value.valido, isTrue);
    expect(value.estado, 'GENERADO');
    expect(value.porcentajeAsistencia, 87.5);
  });

  test('el backend devuelve válido=false para código no registrado', () async {
    final repository = BackendCertificadoRepository(ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: MemoryTokenStore(),
      client: MockClient((request) async {
        expect(request.headers.containsKey('Authorization'), isFalse);
        return http.Response(jsonEncode(_verificationJson(valid: false)), 200);
      }),
    ));

    final value = await repository.verifyPublic(_code);

    expect(value.valido, isFalse);
    expect(value.estado, 'NO_REGISTRADO');
  });

  for (final status in [403, 404]) {
    test('consulta propia conserva error HTTP $status', () async {
      final repository = BackendCertificadoRepository(ApiService(
        baseUrl: 'https://api.example.test/api/v1',
        tokenStore: MemoryTokenStore()..token = 'test-bearer',
        client: MockClient((_) async => http.Response('{}', status)),
      ));

      await expectLater(
        repository.fetchById(_certificateId),
        throwsA(isA<ApiException>().having(
          (error) => error.statusCode,
          'statusCode',
          status,
        )),
      );
    });
  }

  test('401 limpia sesión usando el manejo global existente', () async {
    final store = MemoryTokenStore()..token = 'test-bearer';
    var expired = 0;
    final api = ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient((_) async => http.Response('{}', 401)),
    )..onUnauthorized = () async => expired++;

    await expectLater(
      BackendCertificadoRepository(api).fetchMine(),
      throwsA(isA<ApiException>().having(
        (error) => error.statusCode,
        'statusCode',
        401,
      )),
    );
    expect(store.token, isNull);
    expect(expired, 1);
  });

  test('error de red queda como ApiException sin exponer excepción interna',
      () async {
    final repository = BackendCertificadoRepository(ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: MemoryTokenStore()..token = 'test-bearer',
      client:
          MockClient((_) async => throw http.ClientException('network down')),
    ));

    await expectLater(
      repository.fetchMine(),
      throwsA(isA<ApiException>().having(
        (error) => error.statusCode,
        'statusCode',
        0,
      )),
    );
  });
}
