export interface ValidarQrResponse {
  credencialId: string;
  inscripcionId: string;
  fotografiaUrl: string;
  nombreCompleto: string;
  documentoIdentidad: string;
  carrera: string;
  facultad: string;
  evento: string;
  estadoPago: string;
  estadoInscripcion: string;
  estadoQr: string;
  codigoParticipante: string;
  puedeIngresar: boolean;
  mensajeValidacion: string;
}

export interface ControlAccesoResponse {
  id: string;
  nombreParticipante: string;
  evento: string;
  fechaHoraIngreso: string;
  estadoIngreso: 'AUTORIZADO' | 'DENEGADO' | 'REINTENTO';
  usuarioControl: string;
  observacion: string;
}
