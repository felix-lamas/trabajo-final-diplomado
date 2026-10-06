import 'package:flutter/foundation.dart';

abstract final class ApiConfig {
  static const _configuredBaseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: '',
  );

  static String get baseUrl {
    final configured = _configuredBaseUrl.trim();
    final localHost = kIsWeb || defaultTargetPlatform != TargetPlatform.android
        ? 'localhost'
        : '10.0.2.2';
    final value =
        (configured.isEmpty ? 'http://$localHost:8080/api/v1' : configured)
            .replaceFirst(RegExp(r'/+$'), '');
    final uri = Uri.tryParse(value);

    if (uri == null ||
        !uri.hasAuthority ||
        uri.path != '/api/v1' ||
        (uri.scheme != 'http' && uri.scheme != 'https')) {
      throw StateError(
        'API_BASE_URL debe ser una URL HTTP(S) que termine en /api/v1.',
      );
    }
    if (kReleaseMode && uri.scheme != 'https') {
      throw StateError('Las compilaciones release requieren una API HTTPS.');
    }
    return value;
  }
}
