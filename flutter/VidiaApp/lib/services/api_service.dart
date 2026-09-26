import 'dart:convert';

import 'package:http/http.dart' as http;

import 'api_exception.dart';
import 'token_store.dart';

class ApiService {
  ApiService({
    required String baseUrl,
    required TokenStore tokenStore,
    http.Client? client,
  })  : _baseUrl = baseUrl,
        _tokenStore = tokenStore,
        _client = client ?? http.Client();

  final String _baseUrl;
  final TokenStore _tokenStore;
  final http.Client _client;

  Future<void> Function()? onUnauthorized;

  Future<dynamic> get(String path, {bool authenticated = true}) =>
      _send('GET', path, authenticated: authenticated);

  Future<dynamic> post(
    String path, {
    Object? body,
    bool authenticated = true,
  }) =>
      _send('POST', path, body: body, authenticated: authenticated);

  Future<dynamic> _send(
    String method,
    String path, {
    Object? body,
    required bool authenticated,
  }) async {
    final headers = <String, String>{'Accept': 'application/json'};
    if (body != null) headers['Content-Type'] = 'application/json';
    if (authenticated) {
      final token = await _tokenStore.readToken();
      if (token == null || token.isEmpty) {
        throw const ApiException(
          statusCode: 401,
          message: 'Sesión no válida o credenciales incorrectas.',
        );
      }
      headers['Authorization'] = 'Bearer $token';
    }

    final uri = Uri.parse('$_baseUrl$path');
    late http.Response response;
    try {
      response = method == 'GET'
          ? await _client.get(uri, headers: headers)
          : await _client.post(
              uri,
              headers: headers,
              body: body == null ? null : jsonEncode(body),
            );
    } on Exception {
      throw const ApiException(
        statusCode: 0,
        message:
            'No fue posible conectar con el servidor. Inténtalo nuevamente.',
      );
    }

    if (response.statusCode >= 200 && response.statusCode < 300) {
      if (response.body.trim().isEmpty) return null;
      try {
        return jsonDecode(utf8.decode(response.bodyBytes));
      } on FormatException {
        throw const ApiException(
          statusCode: 500,
          message: 'El servidor devolvió una respuesta no válida.',
        );
      }
    }

    if (response.statusCode == 401 && authenticated) {
      await _tokenStore.clear();
      await onUnauthorized?.call();
    }
    throw ApiException(
      statusCode: response.statusCode,
      message: _messageFor(response),
    );
  }

  String _messageFor(http.Response response) {
    if (response.statusCode == 401) {
      return 'Sesión no válida o credenciales incorrectas.';
    }
    if (response.statusCode == 403) {
      return 'No tienes permisos para realizar esta operación.';
    }
    if (response.statusCode == 404) return 'Recurso no encontrado.';
    if (response.statusCode == 409) return 'Ya estás inscrito en este evento.';
    if (response.statusCode == 500) {
      return 'Ocurrió un error en el servidor. Inténtalo más tarde.';
    }
    if (response.statusCode == 400) {
      return _backendMessage(response) ?? 'Los datos enviados no son válidos.';
    }
    return _backendMessage(response) ??
        'No fue posible completar la operación.';
  }

  String? _backendMessage(http.Response response) {
    try {
      final decoded = jsonDecode(utf8.decode(response.bodyBytes));
      if (decoded is Map<String, dynamic>) {
        final message = decoded['mensaje']?.toString().trim();
        if (message != null && message.isNotEmpty) return message;
      }
    } on FormatException {
      return null;
    }
    return null;
  }
}
