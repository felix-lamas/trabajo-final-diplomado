import 'dart:convert';

class AuthUser {
  const AuthUser({
    required this.id,
    required this.nombres,
    required this.apellidos,
    required this.correoElectronico,
    required this.roles,
  });

  final String id;
  final String nombres;
  final String apellidos;
  final String correoElectronico;
  final List<String> roles;

  String get nombreCompleto => '$nombres $apellidos'.trim();

  factory AuthUser.fromJson(Map<String, dynamic> json) => AuthUser(
        id: json['id']?.toString() ?? '',
        nombres: json['nombres']?.toString() ?? '',
        apellidos: json['apellidos']?.toString() ?? '',
        correoElectronico: json['correoElectronico']?.toString() ?? '',
        roles: (json['roles'] as List<dynamic>? ?? const [])
            .map((role) => role.toString())
            .toList(growable: false),
      );

  Map<String, dynamic> toJson() => {
        'id': id,
        'nombres': nombres,
        'apellidos': apellidos,
        'correoElectronico': correoElectronico,
        'roles': roles,
      };

  String encode() => jsonEncode(toJson());

  static AuthUser decode(String value) =>
      AuthUser.fromJson(jsonDecode(value) as Map<String, dynamic>);
}
