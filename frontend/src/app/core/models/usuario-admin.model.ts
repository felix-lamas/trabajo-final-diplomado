export type TipoUsuarioAdmin = 'INTERNO' | 'EXTERNO';
export type EstadoSolicitudOrganizadorAdmin = 'NINGUNA' | 'PENDIENTE' | 'APROBADA' | 'RECHAZADA';

export interface UsuarioAdmin {
  id: string;
  nombres: string;
  apellidos: string;
  correoElectronico: string;
  correoVerificado: boolean;
  ci: string | null;
  ru: string | null;
  celular: string | null;
  tipoUsuario: TipoUsuarioAdmin;
  estadoSolicitudOrganizador: EstadoSolicitudOrganizadorAdmin | null;
  roles: string[];
}
