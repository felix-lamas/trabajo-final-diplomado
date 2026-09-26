import 'auth_user.dart';

class AuthSession {
  const AuthSession({required this.token, required this.user});

  final String token;
  final AuthUser user;

  factory AuthSession.fromJson(Map<String, dynamic> json) {
    final token = json['token']?.toString() ?? '';
    final userJson = json['usuario'];
    if (token.isEmpty || userJson is! Map<String, dynamic>) {
      throw const FormatException(
        'La respuesta de inicio de sesión no es válida.',
      );
    }
    return AuthSession(token: token, user: AuthUser.fromJson(userJson));
  }
}
