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
});
