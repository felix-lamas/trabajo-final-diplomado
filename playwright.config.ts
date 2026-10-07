import { defineConfig } from '@playwright/test';
import { API_URL, WEB_URL, REPO_ROOT } from './e3-tests/support/env.mjs';
import path from 'node:path';

const output = path.join(REPO_ROOT, 'docs/pruebas/e3');

export default defineConfig({
  testDir: path.join(REPO_ROOT, 'e3-tests/tests'),
  testMatch: ['**/*.api.spec.ts', '**/*.web.spec.ts'],
  fullyParallel: false,
  forbidOnly: Boolean(process.env.CI),
  retries: process.env.CI ? 1 : 0,
  workers: 1,
  timeout: 45_000,
  expect: { timeout: 10_000 },
  outputDir: path.join(output, 'resultados/playwright-artifacts'),
  reporter: [
    ['list'],
    ['html', { outputFolder: path.join(output, 'reporte/html'), open: 'never' }],
    ['junit', { outputFile: path.join(output, 'resultados/junit.xml') }],
    ['json', { outputFile: path.join(output, 'resultados/playwright.json') }],
    ['./e3-tests/support/matrix-reporter.mjs'],
  ],
  metadata: {
    environment: process.env.E3_ENVIRONMENT || 'local-or-custom',
    webUrl: WEB_URL,
    apiUrl: API_URL,
    startedAt: new Date().toISOString(),
  },
  use: {
    baseURL: WEB_URL,
    actionTimeout: 10_000,
    navigationTimeout: 20_000,
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
    video: 'retain-on-failure',
    ignoreHTTPSErrors: false,
  },
  projects: [
    // Los proyectos API no guardan traces porque las peticiones de login contienen credenciales.
    { name: 'api', testMatch: '**/*.api.spec.ts', use: { browserName: 'chromium', trace: 'off', video: 'off' } },
    { name: 'chromium', testMatch: '**/public-web.web.spec.ts', use: { browserName: 'chromium' } },
    // Firefox se mantiene configurado, pero su contexto no puede abrir Page en este Windows;
    // los tests lo marcan BLOCKED antes de crear el fixture. Admin Chromium no graba credenciales.
    { name: 'admin-chromium', testMatch: '**/admin-ui.web.spec.ts', use: { browserName: 'chromium', video: 'off', trace: 'off' } },
    { name: 'firefox', testMatch: '**/public-web.web.spec.ts', use: { browserName: 'firefox', video: 'off', trace: 'off' } },
    { name: 'webkit', testMatch: '**/public-web.web.spec.ts', use: { browserName: 'webkit' } },
  ],
});
