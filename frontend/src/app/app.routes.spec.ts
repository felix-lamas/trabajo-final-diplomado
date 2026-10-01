import { Route } from '@angular/router';
import { routes } from './app.routes';

describe('rutas públicas y de certificados propios', () => {
  const shell = routes.find((route) => route.children !== undefined);
  const children = shell?.children ?? [];

  it('restringe Mis certificados al rol USUARIO', () => {
    const certificados = children.find((route) => route.path === 'certificados');

    expect(certificados?.data?.['roles']).toEqual(['USUARIO']);
    expect(certificados?.canMatch).toHaveLength(2);
  });

  it('mantiene la verificación de certificado pública', () => {
    const verificacion = routes.find((route: Route) => route.path === 'verificar-certificado/:codigo');

    expect(verificacion).toBeDefined();
    expect(verificacion?.canMatch).toBeUndefined();
    expect(verificacion?.canActivate).toBeUndefined();
  });
  it('conecta /privado con el panel exclusivo de USUARIO', () => {
    const privado = children.find((route) => route.path === 'privado');
    const dashboard = children.find((route) => route.path === 'privado/dashboard');

    expect(privado?.redirectTo).toBe('/privado/dashboard');
    expect(dashboard?.data?.['roles']).toEqual(['USUARIO']);
    expect(dashboard?.loadComponent).toBeDefined();
  });

  it('mantiene acceso directo publico a /eventos', () => {
    const publico = routes.find((route) => route.loadChildren !== undefined && route.path === '');
    expect(publico?.loadChildren).toBeDefined();
  });
});
