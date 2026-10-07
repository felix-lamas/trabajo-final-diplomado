import { expect, test } from '@playwright/test';
import fs from 'node:fs/promises';
import path from 'node:path';
import { apiRequest, bearer, loginApi } from '../support/api.js';
import { uniqueTestIdentity } from '../support/test-data.js';
import { apiV1, credential, mutationTargetAllowed, REPO_ROOT } from '../support/env.mjs';
import { requireIsolatedMutation } from '../support/api.js';

test('CP-01 — login, perfil protegido, logout y revocación de sesión', async ({ request }, testInfo) => {
  if (!mutationTargetAllowed()) testInfo.skip(true, 'El login revoca la sesión anterior y logout escribe; requiere API privada/local aislada y confirmación explícita.');
  const { token } = await loginApi(testInfo, request, 'user');
  const profile = await apiRequest(testInfo, request, 'get', '/usuarios/perfil', { headers: bearer(token) });
  expect(profile.response.status()).toBe(200);
  const logout = await apiRequest(testInfo, request, 'post', '/auth/logout', { headers: bearer(token) });
  expect(logout.response.status()).toBe(204);
  const revoked = await apiRequest(testInfo, request, 'get', '/usuarios/perfil', { headers: bearer(token) });
  expect(revoked.response.status()).toBe(401);
  await testInfo.attach('CP-01-sesion-revocada.txt', { body: 'La API aceptó perfil antes de logout, revocó la sesión y rechazó el mismo JWT después.', contentType: 'text/plain' });
});

test('CP-02 — endpoint protegido devuelve 401 sin Authorization', async ({ request, page }, testInfo) => {
  const result = await apiRequest(testInfo, request, 'get', '/usuarios/perfil');
  expect(result.response.status()).toBe(401);
  expect(result.body?.codigo).toBeTruthy();

  // Captura la respuesta real del endpoint en Chromium; no es una maqueta del estado HTTP.
  const browserResponse = await page.goto(apiV1('/usuarios/perfil'));
  expect(browserResponse?.status()).toBe(401);
  const screenshotPath = path.join(REPO_ROOT, 'docs/pruebas/e3/capturas/CP-02-401.png');
  await fs.mkdir(path.dirname(screenshotPath), { recursive: true });
  await page.screenshot({ path: screenshotPath, fullPage: true });
  await testInfo.attach('CP-02-401.png', { path: screenshotPath, contentType: 'image/png' });
});

test('CP-03 — USUARIO autenticado recibe 403 en consulta exclusiva de administración', async ({ request }, testInfo) => {
  const { token } = await loginApi(testInfo, request, 'user');
  const result = await apiRequest(testInfo, request, 'get', '/usuarios/solicitudes-organizador', { headers: bearer(token) });
  expect(result.response.status()).toBe(403);
});

test('CP-16 — credenciales inválidas y JWT inválido se rechazan', async ({ request }, testInfo) => {
  requireIsolatedMutation(testInfo);
  const identity = uniqueTestIdentity();
  const login = await apiRequest(testInfo, request, 'post', '/auth/login', {
    data: { correoElectronico: identity.email, contrasena: `invalid-${identity.ru}` },
  });
  expect(login.response.status()).toBe(401);
  const protectedResponse = await apiRequest(testInfo, request, 'get', '/usuarios/perfil', {
    headers: { Authorization: 'Bearer e3-invalid-token-not-a-jwt' },
  });
  expect(protectedResponse.response.status()).toBe(401);
});

test('CP-18 — USUARIO no publica ni rechaza eventos', async ({ request }, testInfo) => {
  const { token } = await loginApi(testInfo, request, 'user');
  const headers = bearer(token);
  const id = '00000000-0000-4000-8000-000000000001';
  const publish = await apiRequest(testInfo, request, 'patch', `/eventos/${id}/publicar`, { headers });
  expect(publish.response.status()).toBe(403);
  const reject = await apiRequest(testInfo, request, 'patch', `/eventos/${id}/rechazar`, {
    headers,
    data: { motivo: 'Solicitud de prueba de autorización; no debe ejecutarse.' },
  });
  expect(reject.response.status()).toBe(403);
});

test('CP-18 — transición de publicación incompatible devuelve conflicto real', async ({ request }, testInfo) => {
  requireIsolatedMutation(testInfo);
  const id = process.env.E3_STATE_CONFLICT_EVENT_ID?.trim();
  if (!id) testInfo.skip(true, 'Falta E3_STATE_CONFLICT_EVENT_ID de un evento local no EN_REVISION; no se crea ni cambia un evento para fabricar el conflicto.');
  const admin = await loginApi(testInfo, request, 'admin');
  const event = await apiRequest(testInfo, request, 'get', `/eventos/${id}`, { headers: bearer(admin.token) });
  expect(event.response.status()).toBe(200);
  if (event.body?.estado === 'EN_REVISION') testInfo.skip(true, 'El evento está EN_REVISION y publicar podría cambiarlo; se evita una transición válida.');
  const publish = await apiRequest(testInfo, request, 'patch', `/eventos/${id}/publicar`, { headers: bearer(admin.token) });
  expect(publish.response.status()).toBe(409);
});

test('CP-17 — datos incompletos de evento son rechazados por validación real', async ({ request }, testInfo) => {
  const { token } = await loginApi(testInfo, request, 'organizer');
  const result = await apiRequest(testInfo, request, 'post', '/eventos', {
    headers: bearer(token),
    data: { titulo: '', modalidad: 'NO_ES_MODALIDAD' },
  });
  expect(result.response.status()).toBe(400);
});

test('CP-19 — catálogo y búsqueda pública usan rutas reales y solo datos publicados', async ({ request }, testInfo) => {
  const catalog = await apiRequest(testInfo, request, 'get', '/eventos/publicados');
  expect(catalog.response.status()).toBe(200);
  expect(Array.isArray(catalog.body)).toBe(true);
  expect(catalog.body.every((event: { estado?: string }) => event.estado === 'PUBLICADO')).toBe(true);
  const searchPath = '/eventos/publicados/buscar?texto=E3-NO-COINCIDENCIA&tipo=GRATUITO&modalidad=VIRTUAL';
  const search = await apiRequest(testInfo, request, 'get', searchPath);
  expect(search.response.status()).toBe(200);
  expect(Array.isArray(search.body)).toBe(true);
  expect(search.body.every((event: { estado?: string }) => event.estado === 'PUBLICADO')).toBe(true);
});

test('CP-20 — búsqueda vacía y detalle inexistente se responden según contrato', async ({ request }, testInfo) => {
  const search = await apiRequest(testInfo, request, 'get', `/eventos/publicados/buscar?texto=${encodeURIComponent(`E3-${crypto.randomUUID()}`)}`);
  expect(search.response.status()).toBe(200);
  expect(search.body).toEqual([]);
  const missing = await apiRequest(testInfo, request, 'get', `/eventos/${crypto.randomUUID()}`);
  expect(missing.response.status()).toBe(404);
});

test('CP-23 — verificación pública distingue un código no registrado sin JWT', async ({ request }, testInfo) => {
  const code = `E3-NO-REGISTRADO-${crypto.randomUUID()}`;
  const result = await apiRequest(testInfo, request, 'get', `/certificados/verificar/${encodeURIComponent(code)}`);
  expect(result.response.status()).toBe(200);
  expect(result.body?.valido).toBe(false);
  expect(result.body?.estado).toBe('NO_REGISTRADO');
});

test('CP-16 — login correcto emite JWT solo para credenciales de cuenta de prueba', async ({ request }, testInfo) => {
  if (!credential('user')) testInfo.skip(true, 'Faltan credenciales de cuenta demo; no se puede validar login correcto.');
  const { token } = await loginApi(testInfo, request, 'user');
  const profile = await apiRequest(testInfo, request, 'get', '/usuarios/perfil', { headers: bearer(token) });
  expect(profile.response.status()).toBe(200);
});
