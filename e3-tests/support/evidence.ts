import fs from 'node:fs/promises';
import path from 'node:path';
import type { APIResponse, Page, TestInfo } from '@playwright/test';
import { API_URL, e3Environment, REPO_ROOT } from './env.mjs';

function redactValue(value: unknown, key = ''): unknown {
  if (/password|token|secret|authorization|correo|email|codigoqr|qrcontent|codigoCertificado|codigoVerificacion|nombreCompleto|nombres|apellidos|organizadorNombre|telefono|celular|\bci\b|\bru\b/i.test(key)) return '[REDACTED]';
  if (Array.isArray(value)) return value.map((item) => redactValue(item));
  if (value && typeof value === 'object') {
    return Object.fromEntries(Object.entries(value).map(([childKey, childValue]) => [childKey, redactValue(childValue, childKey)]));
  }
  if (typeof value === 'string') return value.replace(/Bearer\s+[^\s"']+/gi, 'Bearer [REDACTED]').replace(/eyJ[a-zA-Z0-9_-]{10,}\.[a-zA-Z0-9_-]+\.[a-zA-Z0-9_-]+/g, '[REDACTED_JWT]');
  return value;
}

function evidenceName(testInfo: TestInfo, extension: string): string {
  const cp = testInfo.title.match(/CP-\d{2}/)?.[0] ?? 'CP-00';
  const slug = testInfo.title.replace(/CP-\d{2}/, '').toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '').slice(0, 54) || 'evidencia';
  const project = testInfo.project.name.replace(/[^a-z0-9_-]/gi, '-');
  return `${cp}-${slug}-${project}.${extension}`;
}

export async function recordHttp(testInfo: TestInfo, response: APIResponse, endpoint: string, method: string, durationMs: number, body?: unknown) {
  const payload = {
    case: testInfo.title.match(/CP-\d{2}/)?.[0] ?? null,
    title: testInfo.title,
    timestamp: new Date().toISOString(),
    environment: e3Environment(),
    apiBaseUrl: API_URL,
    endpoint,
    method,
    status: response.status(),
    durationMs,
    browser: testInfo.project.use.browserName ?? 'API',
    body: redactValue(body),
  };
  const directory = path.join(REPO_ROOT, 'docs/pruebas/e3/resultados/evidencia');
  await fs.mkdir(directory, { recursive: true });
  const file = path.join(directory, evidenceName(testInfo, 'json'));
  await fs.writeFile(file, `${JSON.stringify(payload, null, 2)}\n`, 'utf8');
  await testInfo.attach(path.basename(file), { path: file, contentType: 'application/json' });
  return payload;
}

export async function recordPageScreenshot(testInfo: TestInfo, page: Page) {
  const directory = path.join(REPO_ROOT, 'docs/pruebas/e3/capturas');
  await fs.mkdir(directory, { recursive: true });
  const file = path.join(directory, evidenceName(testInfo, 'png'));
  await page.screenshot({ path: file, fullPage: true, animations: 'disabled' });
  await testInfo.attach(path.basename(file), { path: file, contentType: 'image/png' });
  return file;
}

export async function recordNote(testInfo: TestInfo, data: Record<string, unknown>) {
  const directory = path.join(REPO_ROOT, 'docs/pruebas/e3/resultados/evidencia');
  await fs.mkdir(directory, { recursive: true });
  const file = path.join(directory, evidenceName(testInfo, 'json'));
  await fs.writeFile(file, `${JSON.stringify(redactValue({
    case: testInfo.title.match(/CP-\d{2}/)?.[0] ?? null,
    title: testInfo.title,
    timestamp: new Date().toISOString(),
    environment: e3Environment(),
    ...data,
  }), null, 2)}\n`, 'utf8');
  await testInfo.attach(path.basename(file), { path: file, contentType: 'application/json' });
}
