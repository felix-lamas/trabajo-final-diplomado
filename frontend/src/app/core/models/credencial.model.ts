export interface Credencial {
  id: string;
  usuarioId: string;
  usuarioNombre: string;
  eventoId: string;
  eventoTitulo: string;
  inscripcionId: string;
  codigoParticipante: string;
  fechaGeneracion: Date;
  estado: string;
}

export interface CodigoQr {
  id: string;
  credencialId: string;
  contenido: string;
  fechaGeneracion: Date;
  activo: boolean;
  qrBase64?: string;
}
