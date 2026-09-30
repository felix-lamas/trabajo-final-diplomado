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
  fechaInscripcion: Date;
  estado: EstadoInscripcion;
}

export interface DetalleInscripcion extends Inscripcion {
  usuarioId: string;
  usuarioNombre: string;
  codigoInscripcion: string;
  observacion?: string;
  modalidadEvento: string;
  ubicacionEvento?: string;
}

export interface ComprobanteInscripcion {
  inscripcionId: string;
  codigoInscripcion: string;
  eventoId: string;
  eventoTitulo: string;
  participante: string;
  ci: string;
  ru?: string;
  monto: number;
  fechaInscripcion: string;
  estadoInscripcion: EstadoInscripcion;
  estadoPago: string;
  codigoVerificacion: string;
}

export interface CrearInscripcionRequest {
  eventoId: string;
}
