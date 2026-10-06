import 'dart:convert';

class AuthUser {
  const AuthUser({
    required this.id,
    required this.nombres,
    required this.apellidos,
    required this.correoElectronico,
    required this.roles,
    this.correoVerificado = true,
    this.tipoUsuario,
    this.estadoSolicitudOrganizador = 'NINGUNA',
    this.ci,
    this.ru,
    this.celular,
  });

  final String id;
  final String nombres;
  final String apellidos;
  final String correoElectronico;
  final List<String> roles;
  final bool correoVerificado;
  final String? tipoUsuario;
  final String estadoSolicitudOrganizador;
  final String? ci;
  final String? ru;
  final String? celular;

  static const rolesOficiales = {'ADMINISTRADOR', 'ORGANIZADOR', 'USUARIO'};

  bool get isAdministrador => roles.contains('ADMINISTRADOR');
  bool get isOrganizador => roles.contains('ORGANIZADOR');
  bool get isUsuario => roles.contains('USUARIO');
  bool get organizadorPendiente =>
      isUsuario && estadoSolicitudOrganizador == 'PENDIENTE';

  String get nombreCompleto => '$nombres $apellidos'.trim();

  factory AuthUser.fromJson(Map<String, dynamic> json) => AuthUser(
        id: json['id']?.toString() ?? '',
        nombres: json['nombres']?.toString() ?? '',
        apellidos: json['apellidos']?.toString() ?? '',
        correoElectronico: json['correoElectronico']?.toString() ?? '',
        roles: (json['roles'] as List<dynamic>? ?? const [])
            .map((role) => role.toString())
            .toList(growable: false),
        correoVerificado: json['correoVerificado'] as bool? ?? true,
        tipoUsuario: json['tipoUsuario']?.toString(),
        estadoSolicitudOrganizador:
            json['estadoSolicitudOrganizador']?.toString() ?? 'NINGUNA',
        ci: json['ci']?.toString(),
        ru: json['ru']?.toString(),
        celular: json['celular']?.toString(),
      );

  Map<String, dynamic> toJson() => {
        'id': id,
        'nombres': nombres,
        'apellidos': apellidos,
        'correoElectronico': correoElectronico,
        'roles': roles,
        'correoVerificado': correoVerificado,
        'tipoUsuario': tipoUsuario,
        'estadoSolicitudOrganizador': estadoSolicitudOrganizador,
        'ci': ci,
        'ru': ru,
        'celular': celular,
      };

  String encode() => jsonEncode(toJson());

  static AuthUser decode(String value) =>
      AuthUser.fromJson(jsonDecode(value) as Map<String, dynamic>);
}
