import { HttpErrorResponse } from '@angular/common/http';

interface ApiErrorBody {
  mensaje?: string;
  detalles?: string[];
}

export function apiErrorMessage(error: unknown, fallback: string): string {
  if (!(error instanceof HttpErrorResponse)) {
    return fallback;
  }

  const body = error.error as ApiErrorBody | null;
  const detail = body?.detalles?.filter(Boolean).join('. ');
  if (detail) {
    return detail;
  }

  if (body?.mensaje) {
    return body.mensaje;
  }

  switch (error.status) {
    case 400: return 'La solicitud contiene datos invalidos.';
    case 401: return 'La sesion no es valida. Inicie sesion nuevamente.';
    case 403: return 'No tiene permisos para realizar esta operacion.';
    case 404: return 'No se encontro el recurso solicitado.';
    case 409: return 'La operacion entra en conflicto con un registro existente.';
    default: return fallback;
  }
}
