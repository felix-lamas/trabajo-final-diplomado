import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../../environments/environment.dev';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';

const authPaths = [
  '/auth/login',
  '/auth/registro',
  '/auth/recuperar-contrasena',
  '/auth/restablecer-contrasena',
  '/auth/resetear-contrasena'
];

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const apiBase = environment.apiUrl.replace(/\/v1$/, '');
  const authService = inject(AuthService);
  const router = inject(Router);
  const snackBar = inject(MatSnackBar);

  if (!req.url.startsWith(apiBase)) {
    return next(req);
  }

  if (authPaths.some((path) => req.url.includes(path))) {
    return next(req);
  }

  const handleErrors = (response: ReturnType<typeof next>) => response.pipe(
    catchError((error) => {
      if (error.status === 401) {
        authService.logout();
        snackBar.open('La sesion expiro o no es valida. Inicie sesion nuevamente.', 'Cerrar', { duration: 5000 });
        void router.navigate(['/auth/login']);
      } else if (error.status === 403) {
        snackBar.open(error.error?.mensaje || 'No tiene permisos para realizar esta operacion.', 'Cerrar', { duration: 5000 });
      }
      return throwError(() => error);
    })
  );

  const token = authService.getToken();
  if (!token) {
    return handleErrors(next(req));
  }

  return handleErrors(next(
    req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    })
  ));
};
