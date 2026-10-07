import type { APIRequestContext, APIResponse, TestInfo } from '@playwright/test';
import { expect } from '@playwright/test';
import { apiV1, credential, mutationTargetAllowed } from './env.mjs';
import { recordHttp, recordNote } from './evidence.js';

export async function apiRequest(
  testInfo: TestInfo,
  request: APIRequestContext,
  method: 'get' | 'post' | 'patch' | 'put' | 'delete',
  route: string,
  options: Parameters<APIRequestContext[typeof method]>[1] = {},
): Promise<{ response: APIResponse; body: any }> {
  const url = apiV1(route);
  const startedAt = performance.now();
  let response: APIResponse;
  try {
    response = await request[method](url, options as never);
  } catch (error) {
    await recordNote(testInfo, { endpoint: route, outcome: 'BLOCKED', reason: `API no disponible o inaccesible: ${String(error).slice(0, 500)}` });
    testInfo.skip(true, `API no disponible para ${route}; no se registró un resultado de negocio.`);
  }
  let body: any = null;
  const contentType = response!.headers()['content-type'] ?? '';
  if (contentType.includes('json')) {
    try { body = await response!.json(); } catch { body = null; }
  } else if (response!.status() >= 400) {
    try { body = (await response!.text()).slice(0, 1500); } catch { body = null; }
  }
  await recordHttp(testInfo, response!, route, method.toUpperCase(), Number((performance.now() - startedAt).toFixed(2)), body);
  return { response: response!, body };
}

export async function loginApi(testInfo: TestInfo, request: APIRequestContext, role: 'admin' | 'organizer' | 'user') {
  requireIsolatedMutation(testInfo);
  const account = credential(role);
  if (!account) testInfo.skip(true, `Falta E3_${role.toUpperCase()}_EMAIL/PASSWORD en .env.e3.local; no se intentó autenticar.`);
  const { response, body } = await apiRequest(testInfo, request, 'post', '/auth/login', {
    data: { correoElectronico: account!.email, contrasena: account!.password },
  });
  expect(response.status(), 'login devuelve el HTTP del contrato real').toBe(200);
  expect(typeof body?.token).toBe('string');
  return { token: body.token as string, account: account! };
}

export function bearer(token: string) {
  return { Authorization: `Bearer ${token}` };
}

export function requireIsolatedMutation(testInfo: TestInfo) {
  if (!mutationTargetAllowed()) {
    testInfo.skip(true, 'Escenario con escritura bloqueado: requiere E3_ALLOW_MUTATIONS=true, E3_ISOLATED_TEST_ENV=true y API local/privada. Producción está bloqueada.');
  }
}

export function scenarioId(testInfo: TestInfo, variable: string) {
  const value = process.env[variable]?.trim();
  if (!value) testInfo.skip(true, `Falta el fixture real ${variable}; no se fabricó ni se buscó un identificador ajeno.`);
  return value!;
}

export async function jsonBody(response: APIResponse) {
  try { return await response.json(); } catch { return null; }
}
