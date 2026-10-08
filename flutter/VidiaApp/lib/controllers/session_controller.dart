import 'package:flutter/foundation.dart';

import '../models/auth_user.dart';
import '../services/auth_service.dart';
import '../services/api_exception.dart';

class SessionController extends ChangeNotifier {
  SessionController(this._authService);

  final AuthService _authService;
  AuthUser? _user;
  bool _initializing = true;
  bool _busy = false;
  String? _message;
  String? _restoreError;
  String? _unverifiedEmail;

  AuthUser? get user => _user;
  bool get isAuthenticated => _user != null;
  bool get initializing => _initializing;
  bool get busy => _busy;
  String? get message => _message;
  String? get restoreError => _restoreError;
  String? get unverifiedEmail => _unverifiedEmail;
  Future<void> Function()? onSessionExpired;

  Future<void> restore() async {
    _initializing = true;
    _restoreError = null;
    notifyListeners();
    try {
      _user = await _authService.restoreUser();
    } on ApiException catch (error) {
      _user = null;
      if (error.statusCode != 401) {
        _restoreError = error.message;
      }
    } catch (_) {
      _user = null;
      _restoreError = 'No fue posible validar tu sesión. Inténtalo nuevamente.';
    } finally {
      _initializing = false;
      notifyListeners();
    }
  }

  Future<bool> login({required String email, required String password}) async {
    _busy = true;
    _message = null;
    _unverifiedEmail = null;
    notifyListeners();
    try {
      final session =
          await _authService.signIn(email: email, password: password);
      _user = session.user;
      _restoreError = null;
      return true;
    } catch (error) {
      if (error is ApiException && error.code == 'EMAIL_NOT_VERIFIED') {
        _unverifiedEmail = email.trim();
      }
      _message = error is ApiException && error.code == 'EMAIL_NOT_VERIFIED'
          ? 'Tu correo todavía no está verificado. Abre el enlace recibido o solicita otro correo.'
          : error.toString();
      return false;
    } finally {
      _busy = false;
      notifyListeners();
    }
  }

  void accountPasswordReset() {
    _user = null;
    _restoreError = null;
    _message = 'Contraseña restablecida. Inicia sesión nuevamente.';
    notifyListeners();
  }

  Future<void> logout() async {
    _busy = true;
    notifyListeners();
    String? logoutError;
    try {
      await _authService.signOut();
    } catch (_) {
      logoutError =
          'Se cerró la sesión localmente; no se confirmó en el servidor.';
    }
    _user = null;
    _message = logoutError;
    _busy = false;
    notifyListeners();
  }

  Future<void> expire() async {
    await _authService.clearLocalSession();
    _user = null;
    _message = 'Tu sesión expiró. Inicia sesión nuevamente.';
    await onSessionExpired?.call();
    notifyListeners();
  }

  Future<void> refreshProfile() async {
    _busy = true;
    _message = null;
    notifyListeners();
    try {
      _user = await _authService.restoreUser();
    } catch (error) {
      _message = error.toString();
    } finally {
      _busy = false;
      notifyListeners();
    }
  }

  Future<void> changePassword({
    required String currentPassword,
    required String newPassword,
    required String confirmation,
  }) async {
    await _authService.changePassword(
      currentPassword: currentPassword,
      newPassword: newPassword,
      confirmation: confirmation,
    );
    _user = null;
    _message = 'Contraseña cambiada. Inicia sesión con tu nueva contraseña.';
    notifyListeners();
  }
}
