import { inject } from '@angular/core';
import { CanMatchFn, Route, Router, UrlSegment, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { catchError, map, of } from 'rxjs';

export const roleGuard: CanMatchFn = (route: Route, _segments: UrlSegment[]) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const roles = (route.data?.['roles'] as string[] | undefined) ?? [];

  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/auth/login']);
  }

  const resolveAccess = (): boolean | UrlTree => {
    if (!authService.isAuthenticated()) {
      return router.createUrlTree(['/auth/login']);
    }
    if (roles.length === 0 || authService.hasAnyRole(roles)) {
      return true;
    }
    if (authService.hasAnyRole(['ORGANIZADOR'])) {
      return router.createUrlTree(['/organizador/eventos']);
    }
    if (authService.hasAnyRole(['USUARIO'])) {
      return router.createUrlTree(['/privado/dashboard']);
    }
    return router.createUrlTree(['/eventos']);
  };

  return authService.refrescarPerfil().pipe(
    map(() => resolveAccess()),
    catchError(() => of(
      authService.isAuthenticated()
        ? router.createUrlTree(['/eventos'])
        : router.createUrlTree(['/auth/login'])
    ))
  );
};
