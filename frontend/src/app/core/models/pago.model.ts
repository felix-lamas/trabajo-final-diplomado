export enum EstadoPago {
  PENDIENTE_PAGO = 'PENDIENTE_PAGO',
  PENDIENTE_VALIDACION = 'PENDIENTE_VALIDACION',
  APROBADO = 'APROBADO',
  RECHAZADO = 'RECHAZADO'
}

export interface ComprobantePago {
  id: string;
  nombreArchivo: string;
  tipoContenido: string | null;
  fechaCarga: string | null;
  disponible: boolean | null;
}

export interface Pago {
  id: string;
  inscripcionId: string;
  eventoTitulo: string;
  usuarioNombre: string;
  monto: number;
  fechaPago: string;
  estado: EstadoPago;
  observacion?: string | null;
  motivoRechazo?: string | null;
  fechaResolucion?: string | null;
  intentosComprobante: number;
  comprobante?: ComprobantePago | null;
}

export interface RegistrarPagoRequest {
  inscripcionId: string;
  observacion?: string;
}

export interface ValidarPagoRequest {
  observacion?: string;
}
