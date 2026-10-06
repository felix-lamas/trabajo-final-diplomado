import '../models/auth_session.dart';
import '../models/auth_user.dart';
import 'api_service.dart';
import 'token_store.dart';

class AuthService {
  AuthService(this._api, this._tokenStore);

  final ApiService _api;
  final TokenStore _tokenStore;

  Future<AuthSession> signIn({
    required String email,
    required String password,
  }) async {
    final response = await _api.post(
      '/auth/login',
      authenticated: false,
      body: {
        'correoElectronico': email.trim(),
        'contrasena': password,
      },
    );
    final session = AuthSession.fromJson(response as Map<String, dynamic>);
    await _tokenStore.saveSession(
      token: session.token,
      user: session.user.encode(),
    );
    return session;
  }

  Future<AuthUser?> restoreUser() async {
    final token = await _tokenStore.readToken();
    if (token == null || token.isEmpty) return null;
    final response = await _api.get('/usuarios/perfil');
    final user = AuthUser.fromJson(response as Map<String, dynamic>);
    await _tokenStore.saveSession(token: token, user: user.encode());
    return user;
  }

  Future<Map<String, dynamic>> register(Map<String, dynamic> body) async =>
      (await _api.post('/auth/registro', authenticated: false, body: body))
          as Map<String, dynamic>;

  Future<void> verifyEmail(String token) async {
    await _api.post('/auth/verificar-correo',
        authenticated: false, body: {'token': token});
  }

  Future<void> resendVerification(String email) async {
    await _api.post('/auth/reenviar-verificacion',
        authenticated: false, body: {'correoElectronico': email.trim()});
  }

  Future<void> requestPasswordReset(String email) async {
    await _api.post('/auth/recuperar-contrasena',
        authenticated: false, body: {'correoElectronico': email.trim()});
  }

  Future<void> resetPassword({
    required String token,
    required String password,
    required String confirmation,
  }) async {
    await _api
        .post('/auth/restablecer-contrasena', authenticated: false, body: {
      'token': token,
      'nuevaContrasena': password,
      'confirmacion': confirmation,
    });
  }

  Future<void> changePassword({
    required String currentPassword,
    required String newPassword,
    required String confirmation,
  }) async {
    await _api.post('/usuarios/cambiar-contrasena', body: {
      'contrasenaActual': currentPassword,
      'nuevaContrasena': newPassword,
      'confirmacion': confirmation,
    });
    // The backend revokes every active session as part of this operation.
    await _tokenStore.clear();
  }

  Future<void> updateProfile({
    required String names,
    required String surnames,
    required String phone,
  }) async {
    await _api.put('/usuarios/perfil', body: {
      'nombres': names,
      'apellidos': surnames,
      'celular': phone,
    });
  }

  Future<void> signOut() async {
    try {
      await _api.post('/auth/logout');
    } finally {
      await _tokenStore.clear();
    }
  }

  Future<void> clearLocalSession() => _tokenStore.clear();
}
