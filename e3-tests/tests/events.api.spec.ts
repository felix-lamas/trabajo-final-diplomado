import { expect, request as playwrightRequest, test } from '@playwright/test';
import { apiRequest, bearer, loginApi, requireIsolatedMutation, scenarioId } from '../support/api.js';
import { apiV1, mutationTargetAllowed } from '../support/env.mjs';
import { recordPageScreenshot } from '../support/evidence.js';
import { uniqueEventTitle } from '../support/test-data.js';
import { loginWeb } from '../support/web.js';

let createdEventId: string | undefined;
let createdEventTitle: string | undefined;
let organizerToken: string | undefined;
let adminToken: string | undefined;
let freeEventRegisteredId: string | undefined;

test.afterAll(async () => {
  // El borrador fue creado por CP-04 en esta ejecución; solo se elimina ese ID si el backend aún permite eliminar BORRADOR.
  if (!createdEventId || !organizerToken || !mutationTargetAllowed()) return;
  const context = await playwrightRequest.newContext();
  try {
    const detail = await context.get(apiV1(`/eventos/${createdEventId}`), { headers: bearer(organizerToken) });
    if (!detail.ok()) return;
    const event = await detail.json();
    if (event?.estado === 'BORRADOR') {
      await context.delete(apiV1(`/eventos/${createdEventId}`), { headers: bearer(organizerToken) });
    } else if (event?.estado === 'EN_REVISION' && adminToken) {
      await context.patch(apiV1(`/eventos/${createdEventId}/rechazar`), {
        headers: bearer(adminToken), data: { motivo: 'Limpieza del evento temporal creado por las pruebas E3.' },
      });
    } else if (event?.estado === 'PUBLICADO' && adminToken) {
      await context.patch(apiV1(`/eventos/${createdEventId}/cancelar`), {
        headers: bearer(adminToken), data: { motivo: 'Limpieza del evento temporal creado por las pruebas E3.' },
      });
    }
  } finally {
    await context.dispose();
  }
});

test.describe.serial('Flujos de eventos E3', () => {
  test('CP-04 — ORGANIZADOR aprobado crea evento de prueba en BORRADOR', async ({ request, page }, testInfo) => {
    requireIsolatedMutation(testInfo);
    organizerToken = await loginWeb(testInfo, page, 'organizer');
    const categories = await apiRequest(testInfo, request, 'get', '/categorias-evento/activas', { headers: bearer(organizerToken) });
    expect(categories.response.status()).toBe(200);
    const category = Array.isArray(categories.body) ? categories.body[0] : null;
    if (!category?.id) testInfo.skip(true, 'No hay una categoría activa para completar el DTO; no se inventó una categoría.');

    createdEventTitle = uniqueEventTitle();
    const start = new Date(Date.now() + 45 * 24 * 60 * 60 * 1000).toISOString().slice(0, 10);
    const end = new Date(Date.now() + 46 * 24 * 60 * 60 * 1000).toISOString().slice(0, 10);
    const result = await apiRequest(testInfo, request, 'post', '/eventos', {
      headers: bearer(organizerToken),
      data: {
        titulo: createdEventTitle,
        descripcion: 'Escenario automatizado identificado como dato E3 de prueba.',
        objetivos: 'Validar el contrato de creación de eventos del backend.',
        categoriaId: category.id,
        modalidad: 'VIRTUAL',
        tipoInscripcion: 'GRATUITO',
        costo: 0,
        fechaInicio: start,
        fechaFin: end,
        horaInicio: '09:00:00',
        horaFin: '10:00:00',
        enlaceVirtual: 'https://example.invalid/e3-test',
        requiereInscripcion: true,
        cupoLimitado: false,
        cupoMaximo: null,
        emiteCertificado: false,
        tipoCertificado: null,
        horasAcademicas: null,
        publicoObjetivo: 'EXTERNO',
        emailContacto: 'e3-tests@example.invalid',
      },
    });
    expect(result.response.status()).toBe(201);
    expect(result.body?.estado).toBe('BORRADOR');
    expect(result.body?.titulo).toBe(createdEventTitle);
    createdEventId = result.body?.id;
    expect(createdEventId).toBeTruthy();

    await page.goto('/organizador/eventos');
    await expect(page.getByText(createdEventTitle, { exact: true })).toBeVisible();
    await recordPageScreenshot(testInfo, page);
  });

  test('CP-05 — ADMINISTRADOR publica evento y este aparece en catálogo público', async ({ request, page }, testInfo) => {
    requireIsolatedMutation(testInfo);
    if (!createdEventId || !organizerToken) testInfo.skip(true, 'CP-04 no creó un evento en esta ejecución; no se utilizó un evento existente.');
    if (!process.env.E3_ADMIN_EMAIL || !process.env.E3_ADMIN_PASSWORD) testInfo.skip(true, 'Faltan credenciales E3_ADMIN; no se moverá el evento a EN_REVISION sin actor de publicación.');
    const admin = await loginApi(testInfo, request, 'admin');
    adminToken = admin.token;
    const review = await apiRequest(testInfo, request, 'patch', `/eventos/${createdEventId}/enviar-revision`, { headers: bearer(organizerToken!) });
    expect(review.response.status()).toBe(200);
    const publish = await apiRequest(testInfo, request, 'patch', `/eventos/${createdEventId}/publicar`, { headers: bearer(admin.token) });
    expect(publish.response.status()).toBe(200);
    const catalog = await apiRequest(testInfo, request, 'get', '/eventos/publicados');
    expect(catalog.response.status()).toBe(200);
    const event = catalog.body.find((item: { id: string }) => item.id === createdEventId);
    expect(event?.estado).toBe('PUBLICADO');

    await page.goto(`/eventos/${createdEventId}`);
    await expect(page.getByRole('heading', { level: 1, name: createdEventTitle!, exact: true })).toBeVisible();
    await recordPageScreenshot(testInfo, page);
  });

  test('CP-06 — inscripción gratuita produce estado confirmado', async ({ request }, testInfo) => {
    requireIsolatedMutation(testInfo);
    if (!createdEventId) testInfo.skip(true, 'CP-04 no creó un evento gratuito E3 en esta ejecución; no se utilizó un evento ajeno.');
    const eventId = createdEventId!;
    const user = await loginApi(testInfo, request, 'user');
    const event = await apiRequest(testInfo, request, 'get', `/eventos/${eventId}`);
    expect(event.response.status()).toBe(200);
    expect(event.body?.tipoInscripcion).toBe('GRATUITO');
    const own = await apiRequest(testInfo, request, 'get', '/inscripciones/mis-inscripciones', { headers: bearer(user.token) });
    expect(own.response.status()).toBe(200);
    if (own.body.some((item: { eventoId?: string }) => item.eventoId === eventId)) {
      testInfo.skip(true, 'El usuario demo ya tenía inscripción en este evento; no se alteró ni se contó como nueva ejecución.');
    }
    const registration = await apiRequest(testInfo, request, 'post', '/inscripciones', {
      headers: bearer(user.token), data: { eventoId: eventId },
    });
    expect(registration.response.status()).toBe(201);
    expect(registration.body?.estado).toBe('CONFIRMADA');
    freeEventRegisteredId = eventId;
  });

  test('CP-06 — inscripción pagada produce PENDIENTE_PAGO y monto del backend', async ({ request }, testInfo) => {
    requireIsolatedMutation(testInfo);
    const eventId = scenarioId(testInfo, 'E3_PAID_EVENT_ID');
    const user = await loginApi(testInfo, request, 'user');
    const event = await apiRequest(testInfo, request, 'get', `/eventos/${eventId}`);
    expect(event.response.status()).toBe(200);
    expect(event.body?.tipoInscripcion).toBe('PAGO');
    const own = await apiRequest(testInfo, request, 'get', '/inscripciones/mis-inscripciones', { headers: bearer(user.token) });
    expect(own.response.status()).toBe(200);
    if (own.body.some((item: { eventoId?: string }) => item.eventoId === eventId)) {
      testInfo.skip(true, 'El usuario demo ya tiene inscripción en el evento; no se generó una segunda operación pagada.');
    }
    const registration = await apiRequest(testInfo, request, 'post', '/inscripciones', { headers: bearer(user.token), data: { eventoId: eventId } });
    expect(registration.response.status()).toBe(201);
    expect(registration.body?.estado).toBe('PENDIENTE_PAGO');
    const payments = await apiRequest(testInfo, request, 'get', '/pagos/mis-pagos', { headers: bearer(user.token) });
    expect(payments.response.status()).toBe(200);
    const payment = payments.body.find((item: { inscripcionId?: string }) => item.inscripcionId === registration.body?.id);
    expect(payment?.estado).toBe('PENDIENTE_PAGO');
    expect(Number(payment?.monto)).toBe(Number(event.body?.costo));
  });

  test('CP-07 — segunda inscripción del mismo usuario es rechazada', async ({ request }, testInfo) => {
    requireIsolatedMutation(testInfo);
    const eventId = freeEventRegisteredId ?? scenarioId(testInfo, 'E3_DUPLICATE_EVENT_ID');
    const user = await loginApi(testInfo, request, 'user');
    const own = await apiRequest(testInfo, request, 'get', '/inscripciones/mis-inscripciones', { headers: bearer(user.token) });
    expect(own.response.status()).toBe(200);
    const already = own.body.some((item: { eventoId?: string }) => item.eventoId === eventId);
    if (!already) {
      const first = await apiRequest(testInfo, request, 'post', '/inscripciones', { headers: bearer(user.token), data: { eventoId: eventId } });
      expect(first.response.status()).toBe(201);
    }
    const duplicate = await apiRequest(testInfo, request, 'post', '/inscripciones', { headers: bearer(user.token), data: { eventoId: eventId } });
    expect(duplicate.response.status()).toBe(409);
    expect((await apiRequest(testInfo, request, 'get', '/inscripciones/mis-inscripciones', { headers: bearer(user.token) })).body.filter((item: { eventoId?: string }) => item.eventoId === eventId)).toHaveLength(1);
  });
});
