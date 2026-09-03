import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../../environments/environment.dev';
import { AuthService } from '../services/auth.service';

const authPaths = [
  '/auth/login',
  '/auth/registro',
  '/auth/recuperar-contrasena',
  '/auth/restablecer-contrasena',
  '/auth/resetear-contrasena'
];

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const apiBase = environment.apiUrl.replace(/\/v1$/, '');

  if (!req.url.startsWith(apiBase)) {
    return next(req);
  }

  if (authPaths.some((path) => req.url.includes(path))) {
    return next(req);
  }

  const token = inject(AuthService).getToken();
  if (!token) {
    return next(req);
  }

  return next(
    req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    })
  );
};
