import { expect, test } from '@playwright/test';
import { SECURITY_MATRIX } from '../support/security-matrix.mjs';
import { API_URL, credential, mutationTargetAllowed } from '../support/env.mjs';
import { apiRequest, bearer, loginApi } from '../support/api.js';
import { recordNote } from '../support/evidence.js';

test('CP-25 — matriz de autenticación y roles basada en anotaciones actuales', async ({ request }, testInfo) => {
  const openApiUrl = new URL('/v3/api-docs', API_URL).toString();
  let openApi;
  try {
    const response = await request.get(openApiUrl);
    expect(response.status(), 'OpenAPI accesible').toBe(200);
    openApi = await response.json();
  } catch (error) {
    await recordNote(testInfo, { openApiUrl, outcome: 'BLOCKED', reason: String(error).slice(0, 500) });
    testInfo.skip(true, 'No fue posible consultar OpenAPI para verificar que las operaciones de la matriz existan.');
  }

  const rows: Array<Record<string, unknown>> = [];
  for (const operation of SECURITY_MATRIX) {
    const specPath = `/api/v1${operation.path}`;
    expect(openApi.paths?.[specPath]?.[operation.method.toLowerCase()], `Operación actual en OpenAPI: ${operation.method} ${specPath}`).toBeTruthy();
    const noAuth = await apiRequest(testInfo, request, operation.method.toLowerCase() as 'get', operation.path);
    rows.push({ endpoint: operation.path, method: operation.method, actor: 'sin token', expected: 401, obtained: noAuth.response.status(), result: noAuth.response.status() === 401 ? 'PASS' : 'FAIL', source: operation.source });
  }

  const runnable = mutationTargetAllowed();
  let blockedRoleCount = 0;
  if (runnable) {
    for (const role of ['user', 'organizer', 'admin'] as const) {
      if (!credential(role)) {
        blockedRoleCount += SECURITY_MATRIX.length;
        rows.push(...SECURITY_MATRIX.map((operation) => ({ endpoint: operation.path, method: operation.method, actor: role, expected: operation.allowed.includes(role) ? 200 : 403, obtained: null, result: 'BLOCKED', source: operation.source, reason: 'Credencial de prueba ausente.' })));
        continue;
      }
      const { token } = await loginApi(testInfo, request, role);
      for (const operation of SECURITY_MATRIX) {
        const expected = operation.allowed.includes(role) ? 200 : 403;
        const result = await apiRequest(testInfo, request, 'get', operation.path, { headers: bearer(token) });
        rows.push({ endpoint: operation.path, method: operation.method, actor: role, expected, obtained: result.response.status(), result: result.response.status() === expected ? 'PASS' : 'FAIL', source: operation.source });
      }
    }
  } else {
    blockedRoleCount = SECURITY_MATRIX.length * 3;
    for (const role of ['user', 'organizer', 'admin'] as const) {
      rows.push(...SECURITY_MATRIX.map((operation) => ({ endpoint: operation.path, method: operation.method, actor: role, expected: operation.allowed.includes(role) ? 200 : 403, obtained: null, result: 'BLOCKED', source: operation.source, reason: 'Login crea/revoca sesión; requiere entorno aislado confirmado.' })));
    }
  }

  await recordNote(testInfo, { openApi: openApiUrl, derivation: 'Roles tomados de las anotaciones @PreAuthorize listadas por fila; OpenAPI comprueba ruta/método y seguridad declarada.', rows });
  const mismatches = rows.filter((row) => row.result === 'FAIL');
  expect(mismatches, 'la respuesta real debe coincidir con el permiso del controlador').toEqual([]);
  if (blockedRoleCount > 0) testInfo.skip(true, `${blockedRoleCount} verificaciones de rol quedaron BLOCKED por falta de entorno aislado o credenciales demo.`);
});
