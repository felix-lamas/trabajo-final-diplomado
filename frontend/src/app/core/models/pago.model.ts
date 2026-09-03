export enum EstadoPago {
  PENDIENTE = 'PENDIENTE',
  VALIDADO = 'VALIDADO',
  RECHAZADO = 'RECHAZADO'
}

export interface ComprobantePago {
  id: string;
  urlArchivo: string;
  nombreArchivo: string;
  tipoContenido: string;
}

export interface Pago {
  id: string;
  inscripcionId: string;
  eventoTitulo: string;
  usuarioNombre: string;
  monto: number;
  fechaPago: Date;
  estado: EstadoPago;
  observacion?: string;
  comprobante?: ComprobantePago;
}

export interface RegistrarPagoRequest {
  inscripcionId: string;
  monto: number;
  observacion?: string;
}

export interface ValidarPagoRequest {
  observacion?: string;
}
