export enum EstadoPago {
  PENDIENTE_PAGO = 'PENDIENTE_PAGO',
  PENDIENTE_VALIDACION = 'PENDIENTE_VALIDACION',
  APROBADO = 'APROBADO',
  RECHAZADO = 'RECHAZADO'
}

export interface ComprobantePago {
  id: string;
  nombreArchivo: string;
  tipoContenido: string;
  fechaCarga: string;
  disponible: boolean;
}

export interface Pago {
  id: string;
  inscripcionId: string;
  eventoTitulo: string;
  usuarioNombre: string;
  monto: number;
  fechaPago: string;
  estado: EstadoPago;
  observacion?: string;
  motivoRechazo?: string;
  fechaResolucion?: string;
  intentosComprobante: number;
  comprobante?: ComprobantePago;
}

export interface RegistrarPagoRequest {
  inscripcionId: string;
  observacion?: string;
}

export interface ValidarPagoRequest {
  observacion?: string;
}
