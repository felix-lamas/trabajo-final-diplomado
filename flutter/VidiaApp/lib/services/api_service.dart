import 'dart:convert';
import 'dart:typed_data';

import 'package:http/http.dart' as http;
import 'package:http_parser/http_parser.dart';

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

  Future<dynamic> put(
    String path, {
    Object? body,
    bool authenticated = true,
  }) =>
      _send('PUT', path, body: body, authenticated: authenticated);

  Future<dynamic> patch(String path, {Object? body}) =>
      _send('PATCH', path, body: body, authenticated: true);

  Future<dynamic> postMultipart(
    String path, {
    required String fieldName,
    required String fileName,
    required String contentType,
    required Uint8List bytes,
  }) async {
    final token = await _tokenStore.readToken();
    if (token == null || token.isEmpty) {
      throw const ApiException(
        statusCode: 401,
        message: 'SesiÃ³n no vÃ¡lida o credenciales incorrectas.',
      );
    }
    final request = http.MultipartRequest('POST', Uri.parse('$_baseUrl$path'))
      ..headers['Accept'] = 'application/json'
      ..headers['Authorization'] = 'Bearer $token'
      ..files.add(http.MultipartFile.fromBytes(
        fieldName,
        bytes,
        filename: fileName,
        contentType: MediaType.parse(contentType),
      ));

    late http.Response response;
    try {
      response = await http.Response.fromStream(await _client.send(request));
    } on Exception {
      throw const ApiException(
        statusCode: 0,
        message:
            'No fue posible conectar con el servidor. IntÃ©ntalo nuevamente.',
      );
    }
    await _throwIfFailed(response, authenticated: true);
    return _decodeSuccess(response);
  }

  Future<ApiBinaryResponse> getBinary(
    String path, {
    bool authenticated = true,
  }) async {
    final headers = <String, String>{'Accept': '*/*'};
    if (authenticated) {
      final token = await _tokenStore.readToken();
      if (token == null || token.isEmpty) {
        throw const ApiException(
          statusCode: 401,
          message: 'SesiÃ³n no vÃ¡lida o credenciales incorrectas.',
        );
      }
      headers['Authorization'] = 'Bearer $token';
    }
    late http.Response response;
    try {
      response = await _client.get(Uri.parse('$_baseUrl$path'), headers: headers);
    } on Exception {
      throw const ApiException(
        statusCode: 0,
        message:
            'No fue posible conectar con el servidor. IntÃ©ntalo nuevamente.',
      );
    }
    await _throwIfFailed(response, authenticated: authenticated);
    return ApiBinaryResponse(
      bytes: response.bodyBytes,
      contentType: response.headers['content-type']?.split(';').first.trim(),
      contentDisposition: response.headers['content-disposition'],
    );
  }

  Future<void> _throwIfFailed(
    http.Response response, {
    required bool authenticated,
  }) async {
    if (response.statusCode >= 200 && response.statusCode < 300) return;
    if (response.statusCode == 401 && authenticated) {
      await _tokenStore.clear();
      await _handleUnauthorized();
    }
    throw ApiException(
      statusCode: response.statusCode,
      message: _messageFor(response, authenticated: authenticated),
      code: _backendCode(response),
    );
  }

  dynamic _decodeSuccess(http.Response response) {
    if (response.body.trim().isEmpty) return null;
    try {
      return jsonDecode(utf8.decode(response.bodyBytes));
    } on FormatException {
      throw const ApiException(
        statusCode: 500,
        message: 'El servidor devolviÃ³ una respuesta no vÃ¡lida.',
      );
    }
  }

  Future<void>? _unauthorizedHandling;

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
      final requestBody = body == null ? null : jsonEncode(body);
      response = switch (method) {
        'GET' => await _client.get(uri, headers: headers),
        'PATCH' =>
          await _client.patch(uri, headers: headers, body: requestBody),
        'PUT' => await _client.put(uri, headers: headers, body: requestBody),
        _ => await _client.post(uri, headers: headers, body: requestBody),
      };
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
      await _handleUnauthorized();
    }
    throw ApiException(
      statusCode: response.statusCode,
      message: _messageFor(response, authenticated: authenticated),
      code: _backendCode(response),
    );
  }

  Future<void> _handleUnauthorized() {
    final active = _unauthorizedHandling;
    if (active != null) return active;
    final handling = onUnauthorized?.call() ?? Future<void>.value();
    _unauthorizedHandling = handling;
    return handling.whenComplete(() {
      if (identical(_unauthorizedHandling, handling)) {
        _unauthorizedHandling = null;
      }
    });
  }

  String _messageFor(http.Response response, {required bool authenticated}) {
    if (response.statusCode == 401) {
      return authenticated
          ? 'Tu sesión expiró o fue revocada. Inicia sesión nuevamente.'
          : 'Correo electrónico o contraseña incorrectos.';
    }
    if (response.statusCode == 403) {
      return 'No tienes permisos para realizar esta operación.';
    }
    if (response.statusCode == 404) return 'Recurso no encontrado.';
    if (response.statusCode == 409 &&
        _backendCode(response) == 'PAYMENT_INVALID_STATE') {
      return 'El estado actual del pago no admite esta operacion.';
    }
    if (response.statusCode == 413) {
      return 'El archivo supera el tamano maximo permitido.';
    }
    if (response.statusCode == 409) {
      return switch (_backendCode(response)) {
        'INSCRIPTION_DUPLICATED' => 'Ya tienes una inscripción en este evento.',
        'INSCRIPTION_CAPACITY_FULL' =>
          'El evento ya no tiene cupos disponibles.',
        _ => 'Ya estás inscrito en este evento.',
      };
    }
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

  String? _backendCode(http.Response response) {
    try {
      final decoded = jsonDecode(utf8.decode(response.bodyBytes));
      if (decoded is Map<String, dynamic>) {
        final code = decoded['codigo']?.toString().trim();
        if (code != null && code.isNotEmpty) return code;
      }
    } on FormatException {
      return null;
    }
    return null;
  }
}

class ApiBinaryResponse {
  const ApiBinaryResponse({
    required this.bytes,
    this.contentType,
    this.contentDisposition,
  });

  final Uint8List bytes;
  final String? contentType;
  final String? contentDisposition;
}
