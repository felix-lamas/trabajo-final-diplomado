import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';

const publicAuthUrls = new Set([
  `${environment.apiUrl}/auth/login`,
  `${environment.apiUrl}/auth/registro`,
  `${environment.apiUrl}/auth/verificar-correo`,
  `${environment.apiUrl}/auth/reenviar-verificacion`,
  `${environment.apiUrl}/auth/recuperar-contrasena`,
  `${environment.apiUrl}/auth/restablecer-contrasena`
]);

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const snackBar = inject(MatSnackBar);
  const isCanonicalApiRequest = req.url === environment.apiUrl || req.url.startsWith(`${environment.apiUrl}/`);
  const isPublicCertificateVerification = req.url.startsWith(`${environment.apiUrl}/certificados/verificar/`);

  if (!isCanonicalApiRequest || publicAuthUrls.has(req.url) || isPublicCertificateVerification) {
    return next(req);
  }

  const token = authService.getToken();
  const request = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        const isLogout = req.url === `${environment.apiUrl}/auth/logout`;
        authService.clearLocalSession(true);

        if (!isLogout) {
          const code = error.error?.codigo as string | undefined;
          const message = code === 'AUTH_INVALID_SESSION'
            ? 'Su sesion fue revocada. Inicie sesion nuevamente.'
            : 'La sesion expiro o no es valida. Inicie sesion nuevamente.';
          snackBar.open(message, 'Cerrar', { duration: 5000 });
          void router.navigate(['/auth/login']);
        }
      } else if (error.status === 403) {
        snackBar.open(
          error.error?.mensaje || 'No tiene permisos para realizar esta operacion.',
          'Cerrar',
          { duration: 5000 }
        );
      }
      return throwError(() => error);
    })
  );
};
