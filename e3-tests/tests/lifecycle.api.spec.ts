import { expect, test } from '@playwright/test';
import { apiRequest, bearer, loginApi, requireIsolatedMutation, scenarioId } from '../support/api.js';
import { apiV1 } from '../support/env.mjs';
import { recordNote } from '../support/evidence.js';
import { testPdfBuffer } from '../support/test-data.js';

test('CP-08 — upload multipart de comprobante técnico propio actualiza estado real', async ({ request }, testInfo) => {
  requireIsolatedMutation(testInfo);
  const paymentId = scenarioId(testInfo, 'E3_TEST_PAYMENT_ID');
  const user = await loginApi(testInfo, request, 'user');
  const own = await apiRequest(testInfo, request, 'get', '/pagos/mis-pagos', { headers: bearer(user.token) });
  expect(own.response.status()).toBe(200);
  const payment = own.body.find((item: { id: string }) => item.id === paymentId);
  if (!payment) testInfo.skip(true, 'El identificador no aparece en Mis pagos del usuario de prueba; no se intentó un recurso ajeno.');
  if (!['PENDIENTE_PAGO', 'RECHAZADO'].includes(payment.estado) || payment.comprobante) {
    testInfo.skip(true, `El pago de prueba no admite un comprobante nuevo en estado ${payment.estado}; no se sobrescribió evidencia existente.`);
  }
  const response = await request.post(apiV1(`/pagos/${paymentId}/comprobante`), {
    headers: bearer(user.token),
    multipart: { archivo: { name: 'PRUEBA_TECNICA_NO_ACREDITA_PAGO.pdf', mimeType: 'application/pdf', buffer: testPdfBuffer() } },
  });
  let body: any = null;
  try { body = await response.json(); } catch { /* sin cuerpo JSON */ }
  await recordNote(testInfo, { endpoint: `/pagos/${paymentId}/comprobante`, method: 'POST multipart', status: response.status(), body, fileName: 'PRUEBA_TECNICA_NO_ACREDITA_PAGO.pdf', filePurpose: 'Prueba técnica; no representa ni acredita una transacción real.' });
  expect(response.status()).toBe(200);
  expect(body?.estado).toBe('PENDIENTE_VALIDACION');
  const refreshed = await apiRequest(testInfo, request, 'get', '/pagos/mis-pagos', { headers: bearer(user.token) });
  expect(refreshed.body.find((item: { id: string }) => item.id === paymentId)?.estado).toBe('PENDIENTE_VALIDACION');
});

test('CP-17 — MIME de comprobante incompatible es rechazado', async ({ request }, testInfo) => {
  requireIsolatedMutation(testInfo);
  const paymentId = scenarioId(testInfo, 'E3_INVALID_FILE_PAYMENT_ID');
  const user = await loginApi(testInfo, request, 'user');
  const own = await apiRequest(testInfo, request, 'get', '/pagos/mis-pagos', { headers: bearer(user.token) });
  expect(own.response.status()).toBe(200);
  if (!own.body.some((item: { id: string; estado: string }) => item.id === paymentId && item.estado === 'PENDIENTE_PAGO')) {
    testInfo.skip(true, 'Se requiere un pago propio de prueba en PENDIENTE_PAGO; no se modificó otro recurso.');
  }
  const response = await request.post(apiV1(`/pagos/${paymentId}/comprobante`), {
    headers: bearer(user.token),
    multipart: { archivo: { name: 'e3-archivo-invalido.png', mimeType: 'image/png', buffer: Buffer.from('contenido de texto, no una imagen PNG') } },
  });
  let body: any = null;
  try { body = await response.json(); } catch { /* sin cuerpo JSON */ }
  await recordNote(testInfo, { endpoint: `/pagos/${paymentId}/comprobante`, method: 'POST multipart', status: response.status(), body, file: 'PNG declarado con contenido de texto de prueba.' });
  expect(response.status()).toBe(400);
});

test('CP-17 — tamaño mayor al máximo documentado se rechaza', async ({ request }, testInfo) => {
  requireIsolatedMutation(testInfo);
  const paymentId = scenarioId(testInfo, 'E3_INVALID_FILE_PAYMENT_ID');
  const user = await loginApi(testInfo, request, 'user');
  const own = await apiRequest(testInfo, request, 'get', '/pagos/mis-pagos', { headers: bearer(user.token) });
  expect(own.response.status()).toBe(200);
  if (!own.body.some((item: { id: string; estado: string }) => item.id === paymentId && item.estado === 'PENDIENTE_PAGO')) {
    testInfo.skip(true, 'Se requiere un pago propio de prueba pendiente para comprobar el límite sin cambiar datos existentes.');
  }
  const response = await request.post(apiV1(`/pagos/${paymentId}/comprobante`), {
    headers: bearer(user.token),
    multipart: { archivo: { name: 'e3-limite-5-mib.pdf', mimeType: 'application/pdf', buffer: Buffer.alloc(5 * 1024 * 1024 + 1, 0x41) } },
  });
  await recordNote(testInfo, { endpoint: `/pagos/${paymentId}/comprobante`, method: 'POST multipart', status: response.status(), fileBytes: 5 * 1024 * 1024 + 1, expected: 'rechazo por límite de archivo' });
  expect(response.status()).toBe(400);
});

test('CP-09 — actor autorizado valida un pago propio en PENDIENTE_VALIDACION', async ({ request }, testInfo) => {
  requireIsolatedMutation(testInfo);
  const paymentId = scenarioId(testInfo, 'E3_TEST_PAYMENT_ID');
  const role = process.env.E3_ORGANIZER_EMAIL && process.env.E3_ORGANIZER_PASSWORD ? 'organizer' : 'admin';
  const reviewer = await loginApi(testInfo, request, role);
  const pending = await apiRequest(testInfo, request, 'get', '/pagos/pendientes', { headers: bearer(reviewer.token) });
  expect(pending.response.status()).toBe(200);
  if (!pending.body.some((item: { id: string; estado: string; comprobante?: unknown }) => item.id === paymentId && item.estado === 'PENDIENTE_VALIDACION' && item.comprobante)) {
    testInfo.skip(true, 'El pago no figura como pendiente con comprobante en el alcance del revisor; no se inventó una transición.');
  }
  const result = await apiRequest(testInfo, request, 'patch', `/pagos/${paymentId}/validar`, { headers: bearer(reviewer.token), data: { observacion: 'Validación técnica de cuenta de prueba E3.' } });
  expect(result.response.status()).toBe(200);
  expect(result.body?.estado).toBe('APROBADO');
});

test('CP-10 — sesión propia emite QR temporal con vigencia del contrato', async ({ request }, testInfo) => {
  requireIsolatedMutation(testInfo);
  const eventId = scenarioId(testInfo, 'E3_QR_EVENT_ID');
  const organizer = await loginApi(testInfo, request, 'organizer');
  const event = await apiRequest(testInfo, request, 'get', `/eventos/${eventId}`, { headers: bearer(organizer.token) });
  expect(event.response.status()).toBe(200);
  const session = await apiRequest(testInfo, request, 'post', `/eventos/${eventId}/sesiones`, {
    headers: bearer(organizer.token),
    data: {
      nombre: `[E3-TEST] Sesión ${new Date().toISOString()}`,
      descripcion: 'Sesión temporal creada por escenario automatizado E3.',
      fecha: event.body.fechaInicio,
      horaInicio: event.body.horaInicio,
      horaFin: event.body.horaFin,
      requiereAsistencia: true,
      latitud: null,
      longitud: null,
      radioMetros: 100,
      activa: true,
    },
  });
  expect(session.response.status()).toBe(201);
  const sessionId = session.body?.id;
  expect(sessionId).toBeTruthy();
  const qr = await apiRequest(testInfo, request, 'post', `/sesiones/${sessionId}/qr/generar`, { headers: bearer(organizer.token) });
  expect(qr.response.status()).toBe(201);
  expect(typeof qr.body?.token).toBe('string');
  const issuedAt = Date.parse(qr.body.emitidoEn);
  const expiresAt = Date.parse(qr.body.expiraEn);
  expect(Number.isFinite(issuedAt) && Number.isFinite(expiresAt)).toBe(true);
  expect(expiresAt).toBeGreaterThan(Date.now());
  expect(expiresAt - issuedAt).toBeLessThanOrEqual(121_000);
  expect(qr.body?.sesionId).toBe(sessionId);
  await recordNote(testInfo, { sessionId, qrId: qr.body.id, issuedAt: qr.body.emitidoEn, expiresAt: qr.body.expiraEn, token: '[REDACTED; never persisted]' });
});

test('CP-11 — asistencia QR/GPS necesita ejecución Flutter en dispositivo', async ({}, testInfo) => {
  testInfo.skip(true, 'NEEDS_DEVICE_EVIDENCE: cámara y GPS se validan mediante flutter test integration_test en dispositivo/emulador real; Playwright no simula estos sensores.');
});

test('CP-12 — certificado se genera solo para inscripción elegible configurada', async ({ request }, testInfo) => {
  requireIsolatedMutation(testInfo);
  const inscriptionId = scenarioId(testInfo, 'E3_ELIGIBLE_INSCRIPTION_ID');
  const role = process.env.E3_ORGANIZER_EMAIL && process.env.E3_ORGANIZER_PASSWORD ? 'organizer' : 'admin';
  const issuer = await loginApi(testInfo, request, role);
  const result = await apiRequest(testInfo, request, 'post', `/certificados/generar/${inscriptionId}`, { headers: bearer(issuer.token) });
  expect(result.response.status()).toBe(200);
  expect(result.body?.codigoCertificado).toBeTruthy();
  await recordNote(testInfo, { certificateId: result.body.id, code: '[código de verificación excluido del informe automático]', type: result.body.tipoCertificado, event: result.body.eventoTitulo });
});

test('CP-13 — código válido de prueba se verifica públicamente', async ({ request }, testInfo) => {
  const code = process.env.E3_CERTIFICATE_CODE?.trim();
  if (!code) testInfo.skip(true, 'Falta E3_CERTIFICATE_CODE de un certificado legítimo de prueba; no se generó un certificado para esta consulta.');
  const result = await apiRequest(testInfo, request, 'get', `/certificados/verificar/${encodeURIComponent(code!)}`);
  expect(result.response.status()).toBe(200);
  expect(result.body?.valido).toBe(true);
});

test('CP-21 — rechazo y reenvío solo con pago propio en estado compatible', async ({ request }, testInfo) => {
  requireIsolatedMutation(testInfo);
  const paymentId = scenarioId(testInfo, 'E3_TEST_PAYMENT_ID');
  const user = await loginApi(testInfo, request, 'user');
  const own = await apiRequest(testInfo, request, 'get', '/pagos/mis-pagos', { headers: bearer(user.token) });
  expect(own.response.status()).toBe(200);
  const payment = own.body.find((item: { id: string }) => item.id === paymentId);
  if (!payment || payment.estado !== 'PENDIENTE_VALIDACION' || !payment.comprobante) {
    testInfo.skip(true, 'Se requiere pago propio de prueba con comprobante en PENDIENTE_VALIDACION; no se fabricó rechazo.');
  }
  const reviewerRole = process.env.E3_ORGANIZER_EMAIL && process.env.E3_ORGANIZER_PASSWORD ? 'organizer' : 'admin';
  const reviewer = await loginApi(testInfo, request, reviewerRole);
  const rejected = await apiRequest(testInfo, request, 'patch', `/pagos/${paymentId}/rechazar`, {
    headers: bearer(reviewer.token), data: { observacion: 'Rechazo de prueba E3; el archivo adjunto es técnico y no acredita pago.' },
  });
  expect(rejected.response.status()).toBe(200);
  expect(rejected.body?.estado).toBe('RECHAZADO');
  const resend = await request.post(apiV1(`/pagos/${paymentId}/comprobante`), {
    headers: bearer(user.token),
    multipart: { archivo: { name: 'PRUEBA_TECNICA_NO_ACREDITA_PAGO.pdf', mimeType: 'application/pdf', buffer: testPdfBuffer() } },
  });
  let resendBody: any = null;
  try { resendBody = await resend.json(); } catch { /* sin JSON */ }
  await recordNote(testInfo, { endpoint: `/pagos/${paymentId}/comprobante`, method: 'POST multipart', status: resend.status(), body: resendBody, purpose: 'Reenvío de archivo técnico sin pago real.' });
  expect(resend.status()).toBe(200);
  expect(resendBody?.estado).toBe('PENDIENTE_VALIDACION');
});

test('CP-21 — acceso de USUARIO a pago ajeno se deniega', async ({ request }, testInfo) => {
  const foreignId = scenarioId(testInfo, 'E3_OTHER_USER_PAYMENT_ID');
  const user = await loginApi(testInfo, request, 'user');
  const result = await apiRequest(testInfo, request, 'get', `/pagos/${foreignId}`, { headers: bearer(user.token) });
  expect([403, 404]).toContain(result.response.status());
});

test('CP-23 — certificado no elegible es rechazado por el backend', async ({ request }, testInfo) => {
  requireIsolatedMutation(testInfo);
  const inscriptionId = scenarioId(testInfo, 'E3_INELIGIBLE_INSCRIPTION_ID');
  const role = process.env.E3_ORGANIZER_EMAIL && process.env.E3_ORGANIZER_PASSWORD ? 'organizer' : 'admin';
  const issuer = await loginApi(testInfo, request, role);
  const result = await apiRequest(testInfo, request, 'post', `/certificados/generar/${inscriptionId}`, { headers: bearer(issuer.token) });
  expect(result.response.status()).toBe(400);
});

test('CP-22 — escenarios QR expirado, GPS, duplicado y sensor requieren dispositivo/datos legítimos', async ({}, testInfo) => {
  testInfo.skip(true, 'NEEDS_DEVICE_EVIDENCE: no hay sesión/QR/inscripción real declarada para fabricar expiración, distancia ni duplicidad.');
});
