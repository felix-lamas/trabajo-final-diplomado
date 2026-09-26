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
    final encodedUser = await _tokenStore.readUser();
    if (token == null || token.isEmpty || encodedUser == null) return null;
    try {
      return AuthUser.decode(encodedUser);
    } on FormatException {
      await _tokenStore.clear();
      return null;
    }
  }

  Future<void> signOut() => _tokenStore.clear();
}
