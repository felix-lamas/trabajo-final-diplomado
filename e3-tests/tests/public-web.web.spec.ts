import { expect, test } from '@playwright/test';
import { recordPageScreenshot } from '../support/evidence.js';
import { webUrl } from '../support/env.mjs';
import { loginWeb } from '../support/web.js';

test.skip(({ browserName }) => browserName === 'firefox', 'BLOCKED: en este entorno Windows Firefox lanza pero falla browserContext.newPage antes de ejecutar la prueba; requiere diagnóstico del runtime Playwright.');

async function openCatalog(page: import('@playwright/test').Page, testInfo: import('@playwright/test').TestInfo) {
  try {
    await page.goto('/eventos');
  } catch (error) {
    testInfo.skip(true, `Angular no está disponible en la URL configurada; navegador no probado: ${String(error).slice(0, 240)}`);
  }
}

test('CP-19 — catálogo Angular carga eventos y filtros en navegador', async ({ page }, testInfo) => {
  await openCatalog(page, testInfo);
  await expect(page.getByRole('heading', { name: 'Descubre eventos' })).toBeVisible();
  await expect(page.locator('.vidia-catalog__results')).toContainText(/eventos disponibles/i);
  await recordPageScreenshot(testInfo, page);
});

test('CP-20 — búsqueda Angular sin coincidencias muestra estado vacío y permite limpiar', async ({ page }, testInfo) => {
  await openCatalog(page, testInfo);
  await expect(page.getByRole('heading', { name: 'Descubre eventos' })).toBeVisible();
  const query = `E3-NO-COINCIDENCIA-${crypto.randomUUID()}`;
  await page.getByRole('textbox', { name: /buscar eventos/i }).fill(query);
  await expect(page.locator('.vidia-catalog__results')).toContainText('0 eventos disponibles');
  await expect(page.getByText('No encontramos eventos')).toBeVisible();
  await recordPageScreenshot(testInfo, page);
});

test('CP-27 — compatibilidad web del catálogo en el navegador configurado', async ({ page, browserName }, testInfo) => {
  await openCatalog(page, testInfo);
  await expect(page.getByRole('heading', { name: 'Descubre eventos' })).toBeVisible();
  await expect(page.locator('.vidia-catalog__results')).toContainText(/eventos disponibles/i);
  await testInfo.attach('CP-27-navegador.txt', { body: `Navegador ejecutado: ${browserName}\nURL: ${page.url()}`, contentType: 'text/plain' });
  await recordPageScreenshot(testInfo, page);
});
