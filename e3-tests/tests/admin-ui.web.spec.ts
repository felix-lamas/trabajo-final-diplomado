import { expect, test } from '@playwright/test';
import { recordPageScreenshot } from '../support/evidence.js';
import { webUrl } from '../support/env.mjs';
import { loginWeb } from '../support/web.js';

test('CP-15 — Angular presenta datos administrativos recibidos de API', async ({ page }, testInfo) => {
  await loginWeb(testInfo, page, 'admin');
  const executiveResponse = page.waitForResponse((response) => response.url().includes('/api/v1/dashboard/ejecutivo') && response.request().method() === 'GET');
  await page.goto(webUrl('/admin'));
  const response = await executiveResponse;
  expect(response.status()).toBe(200);
  await expect(page.getByText('Resumen institucional')).toBeVisible();
  // El panel de pagos puede incluir nombres de personas; se oculta solo en la captura de evidencia.
  await page.addStyleTag({ content: '.admin-dashboard__columns { display: none !important; }' });
  await recordPageScreenshot(testInfo, page);
});
