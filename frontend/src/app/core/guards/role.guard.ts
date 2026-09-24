import { inject } from '@angular/core';
import { CanMatchFn, Route, Router, UrlSegment, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanMatchFn = (route: Route, _segments: UrlSegment[]): boolean | UrlTree => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const roles = (route.data?.['roles'] as string[] | undefined) ?? [];

  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/auth/login']);
  }

  if (roles.length === 0 || authService.hasAnyRole(roles)) {
    return true;
  }

  if (authService.hasAnyRole(['ORGANIZADOR'])) {
    return router.createUrlTree(['/organizador/eventos']);
  }
  return router.createUrlTree(['/eventos']);
};
