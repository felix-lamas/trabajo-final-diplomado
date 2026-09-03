export enum EstadoInscripcion {
  PENDIENTE_PAGO = 'PENDIENTE_PAGO',
  PENDIENTE_VALIDACION = 'PENDIENTE_VALIDACION',
  CONFIRMADA = 'CONFIRMADA',
  CANCELADA = 'CANCELADA',
  RECHAZADA = 'RECHAZADA',
  ASISTIO = 'ASISTIO',
  NO_ASISTIO = 'NO_ASISTIO'
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
  observacion?: string;
  modalidalEvento: string;
  ubicacionEvento?: string;
}

export interface CrearInscripcionRequest {
  eventoId: string;
}
