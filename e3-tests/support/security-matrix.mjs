export const SECURITY_MATRIX = [
  { method: 'GET', path: '/usuarios', allowed: ['admin'], source: 'UsuarioControlador.listar — @PreAuthorize(hasRole ADMINISTRADOR)' },
  { method: 'GET', path: '/usuarios/solicitudes-organizador', allowed: ['admin'], source: 'UsuarioControlador.listarSolicitudesOrganizador — @PreAuthorize(hasRole ADMINISTRADOR)' },
  { method: 'GET', path: '/eventos/revision', allowed: ['admin'], source: 'EventoController.listarEnRevision — @PreAuthorize(hasRole ADMINISTRADOR)' },
  { method: 'GET', path: '/pagos', allowed: ['admin'], source: 'PagoController.listarTodos — @PreAuthorize(hasRole ADMINISTRADOR)' },
  { method: 'GET', path: '/pagos/pendientes', allowed: ['admin', 'organizer'], source: 'PagoController.listarPendientes — @PreAuthorize(hasAnyRole ADMINISTRADOR, ORGANIZADOR)' },
  { method: 'GET', path: '/inscripciones/mis-inscripciones', allowed: ['user'], source: 'InscripcionController.listarMisInscripciones — @PreAuthorize(hasRole USUARIO)' },
];
