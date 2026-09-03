export interface CertificadoResponse {
  id: string;
  nombreCompleto: string;
  ci: string;
  evento: string;
  cargaHoraria: number;
  codigoCertificado: string;
  fechaEmision: string;
  urlVerificacion: string;
  estado: 'GENERADO' | 'DESCARGADO' | 'ANULADO';
  archivoPdfUrl: string;
}

export interface VerificacionCertificadoResponse {
  valido: boolean;
  mensaje: string;
  nombreCompleto: string;
  ci: string;
  evento: string;
  cargaHoraria: number;
  fechaEmision: string;
  codigoCertificado: string;
  estado: string;
}
