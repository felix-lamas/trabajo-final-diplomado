import 'package:flutter/foundation.dart';

import '../models/auth_user.dart';
import '../services/auth_service.dart';

class SessionController extends ChangeNotifier {
  SessionController(this._authService);

  final AuthService _authService;
  AuthUser? _user;
  bool _initializing = true;
  bool _busy = false;
  String? _message;

  AuthUser? get user => _user;
  bool get isAuthenticated => _user != null;
  bool get initializing => _initializing;
  bool get busy => _busy;
  String? get message => _message;

  Future<void> restore() async {
    _user = await _authService.restoreUser();
    _initializing = false;
    notifyListeners();
  }

  Future<bool> login({required String email, required String password}) async {
    _busy = true;
    _message = null;
    notifyListeners();
    try {
      final session =
          await _authService.signIn(email: email, password: password);
      _user = session.user;
      return true;
    } catch (error) {
      _message = error.toString();
      return false;
    } finally {
      _busy = false;
      notifyListeners();
    }
  }

  Future<void> logout() async {
    await _authService.signOut();
    _user = null;
    _message = null;
    notifyListeners();
  }

  Future<void> expire() async {
    await _authService.signOut();
    _user = null;
    _message = 'Tu sesión expiró. Inicia sesión nuevamente.';
    notifyListeners();
  }
}
