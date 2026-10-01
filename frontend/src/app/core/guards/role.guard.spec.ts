import { TestBed } from '@angular/core/testing';
import { Route, Router, UrlTree, provideRouter } from '@angular/router';
import { Observable, firstValueFrom, isObservable, of, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { roleGuard } from './role.guard';

describe('roleGuard with refreshed roles', () => {
  let router: Router;
  let currentRoles: string[];
  let refreshedRoles: string[];
  let auth: any;

  beforeEach(() => {
    currentRoles = ['USUARIO'];
    refreshedRoles = ['USUARIO'];
    auth = {
      isAuthenticated: vi.fn(() => true),
      hasAnyRole: vi.fn((roles: string[]) => roles.some((role) => currentRoles.includes(role))),
      refrescarPerfil: vi.fn(() => {
        currentRoles = refreshedRoles;
        return of({ roles: refreshedRoles });
      })
    };
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: AuthService, useValue: auth }]
    });
    router = TestBed.inject(Router);
  });

  it('USUARIO permanece bloqueado de rutas ORGANIZADOR', async () => {
    const result = await execute({ data: { roles: ['ORGANIZADOR'] } });

    expect(result).toBeInstanceOf(UrlTree);
    expect(router.serializeUrl(result as UrlTree)).toBe('/privado/dashboard');
  });

  it('ORGANIZADOR puede acceder a rutas ORGANIZADOR', async () => {
    currentRoles = ['ORGANIZADOR'];
    refreshedRoles = ['ORGANIZADOR'];

    expect(await execute({ data: { roles: ['ORGANIZADOR'] } })).toBe(true);
  });

  it('ADMINISTRADOR puede acceder a rutas administrativas', async () => {
    currentRoles = ['ADMINISTRADOR'];
    refreshedRoles = ['ADMINISTRADOR'];

    expect(await execute({ data: { roles: ['ADMINISTRADOR'] } })).toBe(true);
  });

  it('refresco de perfil reconoce inmediatamente USUARIO convertido en ORGANIZADOR', async () => {
    currentRoles = ['USUARIO'];
    refreshedRoles = ['ORGANIZADOR'];

    expect(await execute({ data: { roles: ['ORGANIZADOR'] } })).toBe(true);
    expect(auth.refrescarPerfil).toHaveBeenCalledOnce();
  });

  it('no usa roles cacheados cuando falla el refresco y conserva sesión local', async () => {
    currentRoles = ['ADMINISTRADOR'];
    auth.refrescarPerfil.mockReturnValue(throwError(() => new Error('offline')));

    const result = await execute({ data: { roles: ['ADMINISTRADOR'] } });

    expect(result).toBeInstanceOf(UrlTree);
    expect(router.serializeUrl(result as UrlTree)).toBe('/eventos');
    expect(auth.isAuthenticated).not.toHaveReturnedWith(false);
  });

  async function execute(route: Route): Promise<boolean | UrlTree> {
    const result = TestBed.runInInjectionContext(() => roleGuard(route, []));
    if (isObservable(result)) {
      return firstValueFrom(result as Observable<boolean | UrlTree>);
    }
    return await Promise.resolve(result as boolean | UrlTree);
  }
});
