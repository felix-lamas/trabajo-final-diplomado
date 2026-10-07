import type { Page, TestInfo } from '@playwright/test';
import { expect } from '@playwright/test';
import { credential } from './env.mjs';
import { requireIsolatedMutation } from './api.js';

export async function loginWeb(testInfo: TestInfo, page: Page, role: 'admin' | 'organizer' | 'user') {
  requireIsolatedMutation(testInfo);
  const account = credential(role);
  if (!account) testInfo.skip(true, `Falta E3_${role.toUpperCase()}_EMAIL/PASSWORD; se omitió el login Angular.`);
  try {
    await page.goto('/auth/login');
  } catch (error) {
    testInfo.skip(true, `Aplicación Angular no disponible en la URL configurada; no se ejecutó el login: ${String(error).slice(0, 240)}`);
  }
  await page.locator('input[type="email"]').fill(account!.email);
  await page.locator('input[type="password"]').fill(account!.password);
  const [response] = await Promise.all([
    page.waitForResponse((candidate) => candidate.url().includes('/api/v1/auth/login') && candidate.request().method() === 'POST'),
    page.locator('form button[type="submit"]').click(),
  ]);
  expect(response.status(), 'login Angular debe ser aceptado por la API real').toBe(200);
  const route = role === 'admin' ? /\/admin(?:\/|$)/ : role === 'organizer' ? /\/organizador(?:\/|$)/ : /\/privado(?:\/|$)/;
  await expect.poll(() => new URL(page.url()).pathname).toMatch(route);
  const token = await page.evaluate(() => localStorage.getItem('token'));
  expect(token, 'Angular guarda la sesión devuelta por el backend').toBeTruthy();
  return token!;
}
