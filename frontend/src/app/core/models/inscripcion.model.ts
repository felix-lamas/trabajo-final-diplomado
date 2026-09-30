export enum EstadoInscripcion {
  PENDIENTE_PAGO = 'PENDIENTE_PAGO',
  PENDIENTE_VALIDACION = 'PENDIENTE_VALIDACION',
  CONFIRMADA = 'CONFIRMADA',
  CANCELADA = 'CANCELADA'
}

export interface Inscripcion {
  id: string;
  eventoId: string;
  eventoTitulo: string;
  fechaInscripcion: string;
  estado: EstadoInscripcion;
}

export interface DetalleInscripcion extends Inscripcion {
  usuarioId: string;
  usuarioNombre: string;
  codigoInscripcion: string;
  observacion?: string | null;
  modalidadEvento: string;
  ubicacionEvento?: string | null;
}

export interface ComprobanteInscripcion {
  inscripcionId: string;
  codigoInscripcion: string;
  eventoId: string;
  eventoTitulo: string;
  participante: string;
  ci: string;
  ru?: string | null;
  monto: number;
  fechaInscripcion: string;
  estadoInscripcion: EstadoInscripcion;
  estadoPago: string;
  codigoVerificacion: string;
}

export interface CrearInscripcionRequest {
  eventoId: string;
}
