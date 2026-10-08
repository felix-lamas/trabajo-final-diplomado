import 'dart:async';
import 'dart:convert';

import 'package:app_links/app_links.dart';
import 'package:crypto/crypto.dart';

enum AuthLinkAction { verifyEmail, resetPassword }

/// Temporary credentials: never stringify, log or persist this object.
class AuthLink {
  const AuthLink(this.action, this.token);
  static const host = 'trabajo-final-diplomado-web.onrender.com';
  final AuthLinkAction action;
  final String token;

  String get path => action == AuthLinkAction.verifyEmail
      ? '/auth/verificar-correo'
      : '/auth/restablecer-contrasena';

  static AuthLink? parse(Uri uri, {bool internal = false}) {
    if (internal) {
      if (uri.hasScheme || uri.hasAuthority) return null;
    } else if (uri.scheme != 'https' ||
        uri.host != host ||
        uri.userInfo.isNotEmpty ||
        uri.port != 443) {
      return null;
    }
    if (uri.hasFragment) return null;
    final action = switch (uri.path) {
      '/auth/verificar-correo' => AuthLinkAction.verifyEmail,
      '/auth/restablecer-contrasena' => AuthLinkAction.resetPassword,
      _ => null,
    };
    try {
      final tokens = uri.queryParametersAll['token'];
      if (action == null || tokens == null || tokens.length != 1) return null;
      final token = tokens.single;
      if (token.trim().isEmpty) return null;
      return AuthLink(action, token);
    } on FormatException {
      return null;
    }
  }
}

abstract interface class AuthLinkSource {
  Future<Uri?> initialLink();
  Stream<Uri> get links;
}

class PlatformAuthLinkSource implements AuthLinkSource {
  PlatformAuthLinkSource() : _links = AppLinks();
  final AppLinks _links;
  @override
  Future<Uri?> initialLink() => _links.getInitialLink();
  @override
  Stream<Uri> get links => _links.uriLinkStream;
}

/// Buffers links until Navigator/session/lifecycle are ready. Deduplication
/// retains only fingerprints in process memory, never the raw URLs or tokens.
class AuthLinkCoordinator {
  AuthLinkCoordinator({required this._source, required this.onLink});
  final AuthLinkSource _source;
  final void Function(AuthLink) onLink;
  final Set<String> _seen = {};
  final List<AuthLink> _pending = [];
  StreamSubscription<Uri>? _subscription;
  bool _ready = false;
  bool _started = false;
  bool _disposed = false;

  Future<void> start() async {
    if (_started || _disposed) return;
    _started = true;
    _subscription = _source.links.listen(
      accept,
      onError: (Object _) {
        // Platform exceptions can contain URLs. Do not expose them.
      },
    );
    try {
      final initial = await _source.initialLink();
      if (initial != null && !_disposed) accept(initial);
    } catch (_) {
      // A later stream event can still deliver a link.
    }
  }

  bool accept(Uri uri) {
    if (_disposed) return false;
    final link = AuthLink.parse(uri);
    if (link == null) return false;
    final fingerprint = sha256
        .convert(utf8.encode('${link.path}\n${link.token}'))
        .toString();
    if (!_seen.add(fingerprint)) return false;
    _pending.add(link);
    _flush();
    return true;
  }

  void setReady(bool ready) {
    if (_disposed) return;
    _ready = ready;
    _flush();
  }

  void _flush() {
    while (_ready && _pending.isNotEmpty && !_disposed) {
      onLink(_pending.removeAt(0));
    }
  }

  Future<void> dispose() async {
    _disposed = true;
    _pending.clear();
    _seen.clear();
    await _subscription?.cancel();
  }
}
