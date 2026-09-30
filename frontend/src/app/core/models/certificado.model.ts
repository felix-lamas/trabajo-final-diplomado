export interface CertificadoResponse {
  id: string;
  nombreCompleto: string;
  ru: string | null;
  ci: string;
  evento: string;
  cargaHoraria: number | null;
  tipoCertificado: 'CURRICULAR' | 'NO_CURRICULAR';
  horasAcademicas: number | null;
  porcentajeAsistencia: number | null;
  codigoCertificado: string;
  fechaEmision: string;
  urlVerificacion: string;
  estado: 'GENERADO' | 'DESCARGADO' | 'ANULADO';
  archivoPdfUrl: string;
}

export interface VerificacionCertificadoResponse {
  valido: boolean;
  mensaje: string;
  institucion: string;
  nombreCompleto: string | null;
  evento: string | null;
  tipoCertificado: 'CURRICULAR' | 'NO_CURRICULAR' | null;
  horasAcademicas: number | null;
  porcentajeAsistencia: number | null;
  fechaEmision: string | null;
  codigoCertificado: string;
  estado: string;
}
