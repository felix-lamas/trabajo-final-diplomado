import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { spawn } from 'node:child_process';
import { API_URL, REPO_ROOT, credential, isLocalOrPrivateTarget, loadE3Env } from '../support/env.mjs';
import { recordExternalCase } from '../support/matrix-utils.mjs';

loadE3Env();
const device = process.env.E3_ANDROID_DEVICE_ID?.trim();
const account = credential('user');
const apiBase = process.env.E3_FLUTTER_API_BASE_URL?.trim();
const isoStamp = new Date().toISOString().replaceAll(':', '-');
const resultDir = path.join(REPO_ROOT, 'docs/pruebas/e3/resultados');
fs.mkdirSync(resultDir, { recursive: true });

function save(status, details) {
  const result = { case: 'CP-14', timestamp: new Date().toISOString(), environment: process.env.E3_ENVIRONMENT || 'local-or-custom', device: device || null, apiBaseUrl: apiBase || null, status, ...details };
  const resultPath = path.join(resultDir, `flutter-cp-14-${isoStamp}.json`);
  fs.writeFileSync(resultPath, `${JSON.stringify(result, null, 2)}\n`, 'utf8');
  recordExternalCase('CP-14', { title: 'Flutter integration_test CP-14', durationMs: details.durationMs ?? null, browser: 'Android / Flutter integration_test', evidence: [path.relative(REPO_ROOT, resultPath).replaceAll('\\', '/')], error: details.error ?? null, blockedReason: details.reason ?? null }, status);
  return resultPath;
}

const missing = [];
if (!device) missing.push('E3_ANDROID_DEVICE_ID');
if (!account) missing.push('E3_USER_EMAIL/E3_USER_PASSWORD');
if (!apiBase) missing.push('E3_FLUTTER_API_BASE_URL con /api/v1 accesible desde Android');
if (process.env.E3_ISOLATED_TEST_ENV !== 'true') missing.push('E3_ISOLATED_TEST_ENV=true (login crea/revoca una sesión local)');
if (!isLocalOrPrivateTarget()) missing.push('E3_API_URL local/privada; no se permite Render/producción');

if (missing.length) {
  const resultPath = save('BLOCKED', { reason: `Precondiciones faltantes: ${missing.join(', ')}` });
  console.log(`CP-14 BLOCKED. Ver ${path.relative(REPO_ROOT, resultPath)}`);
  process.exitCode = 2;
} else {
  const parsed = new URL(apiBase);
  if (!parsed.pathname.replace(/\/$/, '').endsWith('/api/v1')) {
    const resultPath = save('BLOCKED', { reason: 'E3_FLUTTER_API_BASE_URL debe terminar exactamente en /api/v1.' });
    console.log(`CP-14 BLOCKED. Ver ${path.relative(REPO_ROOT, resultPath)}`);
    process.exitCode = 2;
  } else {
    const tempFile = path.join(os.tmpdir(), `vidia-e3-flutter-${process.pid}-${Date.now()}.json`);
    const defines = { API_BASE_URL: apiBase, E3_USER_EMAIL: account.email, E3_USER_PASSWORD: account.password };
    fs.writeFileSync(tempFile, JSON.stringify(defines), { encoding: 'utf8', mode: 0o600, flag: 'wx' });
    const start = Date.now();
    const command = process.platform === 'win32' ? 'flutter.bat' : 'flutter';
    const child = spawn(command, ['test', 'integration_test/e3_catalog_test.dart', '-d', device, `--dart-define-from-file=${tempFile}`], {
      cwd: path.join(REPO_ROOT, 'flutter/VidiaApp'),
      windowsHide: true,
      shell: process.platform === 'win32',
      env: process.env,
    });
    let output = '';
    child.stdout.on('data', (chunk) => { output += chunk.toString(); });
    child.stderr.on('data', (chunk) => { output += chunk.toString(); });
    child.on('error', (error) => { output += String(error); });
    child.on('close', (code) => {
      fs.rmSync(tempFile, { force: true });
      const redacted = [account.email, account.password].filter(Boolean).reduce((text, secret) => text.split(secret).join('[REDACTED]'), output)
        .replace(/Bearer\s+[^\s"']+/gi, 'Bearer [REDACTED]')
        .replace(/eyJ[a-zA-Z0-9_-]{10,}\.[a-zA-Z0-9_-]+\.[a-zA-Z0-9_-]+/g, '[REDACTED_JWT]');
      const state = code === 0 ? 'PASS' : 'FAIL';
      const resultPath = save(state, { durationMs: Date.now() - start, exitCode: code, output: redacted.slice(-8000), screenshot: code === 0 ? 'Harness takeScreenshot: CP-14-flutter-catalogo' : null });
      console.log(redacted);
      console.log(`CP-14 ${state}. Ver ${path.relative(REPO_ROOT, resultPath)}`);
      if (code !== 0) process.exitCode = code ?? 1;
    });
  }
}
