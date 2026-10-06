import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:vidia/repositories/backend_categoria_repository.dart';
import 'package:vidia/services/api_service.dart';

import 'support/memory_token_store.dart';

void main() {
  test('categorías activas usan el endpoint protegido real', () async {
    final store = MemoryTokenStore()..token = 'jwt-de-prueba';
    late http.Request captured;
    final repository = BackendCategoriaRepository(ApiService(
      baseUrl: 'https://api.example.test/api/v1',
      tokenStore: store,
      client: MockClient((request) async {
        captured = request;
        return http.Response(
            jsonEncode([
              {
                'id': '30000000-0000-0000-0000-000000000001',
                'nombre': 'Taller',
                'descripcion': 'Capacitacion',
                'estado': 'ACTIVO',
              }
            ]),
            200);
      }),
    ));

    final categories = await repository.fetchActive();

    expect(captured.method, 'GET');
    expect(captured.url.path, '/api/v1/categorias-evento/activas');
    expect(captured.headers['Authorization'], 'Bearer jwt-de-prueba');
    expect(categories.single.nombre, 'Taller');
  });
}
