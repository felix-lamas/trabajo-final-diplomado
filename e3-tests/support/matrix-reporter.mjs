import fs from 'node:fs';
import path from 'node:path';
import { CASES } from './cases.mjs';
import { E3_DOCS_ROOT as docsRoot, REPO_ROOT as root, writeMatrix } from './matrix-utils.mjs';

function latestJson(prefix) {
  const directory = path.join(docsRoot, 'resultados');
  if (!fs.existsSync(directory)) return null;
  const file = fs.readdirSync(directory).filter((name) => name.startsWith(prefix) && name.endsWith('.json')).sort().at(-1);
  if (!file) return null;
  try { return { path: path.join(directory, file), body: JSON.parse(fs.readFileSync(path.join(directory, file), 'utf8')) }; }
  catch { return null; }
}

function externalRun(id) {
  const source = id === 'CP-14' ? latestJson('flutter-cp-14-')
    : id === 'CP-24' ? latestJson('CP-24-rendimiento-')
      : id === 'CP-28' ? latestJson('CP-28-disponibilidad-')
        : null;
  if (!source) return null;
  const body = source.body;
  const status = body.state ?? body.status;
  if (!['PASS', 'FAIL', 'BLOCKED', 'NOT_RUN'].includes(status)) return null;
  return {
    title: id === 'CP-14' ? 'Flutter integration_test CP-14' : id === 'CP-24' ? 'Carga de 20 usuarios concurrentes' : 'Monitoreo de disponibilidad',
    status,
    durationMs: body.durationMs ?? null,
    browser: id === 'CP-14' ? 'Android / Flutter integration_test' : 'node fetch',
    error: body.error ?? null,
    blockedReason: body.reason ?? null,
    evidence: [path.relative(root, source.path).replaceAll('\\', '/')],
  };
}

function safe(text) {
  let value = String(text ?? '');
  for (const name of ['E3_ADMIN_PASSWORD', 'E3_ORGANIZER_PASSWORD', 'E3_USER_PASSWORD', 'E3_ADMIN_EMAIL', 'E3_ORGANIZER_EMAIL', 'E3_USER_EMAIL']) {
    const secret = process.env[name];
    if (secret) value = value.split(secret).join('[REDACTED]');
  }
  return value.replace(/Bearer\s+[^\s"']+/gi, 'Bearer [REDACTED]').replace(/eyJ[a-zA-Z0-9_-]{10,}\.[a-zA-Z0-9_-]+\.[a-zA-Z0-9_-]+/g, '[REDACTED_JWT]');
}

function envName() {
  return process.env.E3_ENVIRONMENT || (process.env.E3_API_URL?.includes('onrender.com') ? 'production' : 'local-or-custom');
}

function statusFor(result) {
  if (result.status === 'passed') return 'PASS';
  if (result.status === 'failed' || result.status === 'timedOut') return 'FAIL';
  return 'BLOCKED';
}

export default class E3MatrixReporter {
  results = new Map();

  onTestEnd(test, result) {
    const id = test.title.match(/CP-\d{2}/)?.[0];
    if (!id) return;
    for (const attachment of result.attachments ?? []) {
      if (!attachment.path) continue;
      const kind = attachment.name.toLowerCase().includes('trace') || attachment.path.endsWith('.zip')
        ? 'traces'
        : attachment.name.toLowerCase().includes('video') || attachment.path.endsWith('.webm')
          ? 'videos'
          : attachment.name.toLowerCase().includes('screenshot') || attachment.path.endsWith('.png')
            ? 'capturas'
            : null;
      if (!kind) continue;
      const targetDir = path.join(docsRoot, kind);
      fs.mkdirSync(targetDir, { recursive: true });
      const project = test.parent?.project()?.name ?? 'browser';
      const extension = path.extname(attachment.path);
      const slug = test.title.replace(/CP-\d{2}/, '').toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '').slice(0, 45) || 'evidencia';
      const target = path.join(targetDir, `${id}-${slug}-${project}${extension}`);
      fs.copyFileSync(attachment.path, target);
    }
    const entry = this.results.get(id) ?? [];
    const annotations = result.annotations ?? [];
    const attachments = (result.attachments ?? []).map((item) => item.path ?? item.name).filter(Boolean);
    const error = result.error?.message ? safe(result.error.message).slice(0, 1200) : null;
    entry.push({
      title: test.title,
      status: statusFor(result),
      durationMs: result.duration,
      browser: test.parent?.project()?.name ?? 'unknown',
      error,
      blockedReason: result.status === 'skipped'
        ? safe(annotations.find((annotation) => annotation.type === 'skip')?.description ?? 'Prerrequisito o evidencia no disponible.')
        : null,
      evidence: attachments.map((item) => path.relative(root, item).replaceAll('\\', '/')),
    });
    this.results.set(id, entry);
  }

  async onEnd() {
    fs.mkdirSync(path.join(docsRoot, 'resultados'), { recursive: true });
    const cases = CASES.map((testCase) => {
      const runs = this.results.get(testCase.id) ?? [];
      const separateRun = externalRun(testCase.id);
      if (separateRun) runs.push(separateRun);
      if (testCase.id === 'CP-01') runs.push({
        title: 'Registro y verificacion de correo mediante bandeja externa',
        status: 'BLOCKED',
        durationMs: null,
        browser: 'evidencia manual/correo',
        error: null,
        blockedReason: 'No hay una bandeja de correo de prueba accesible; el seed marca la cuenta demo como verificada, por lo que no se afirmo haber probado el flujo de correo.',
        evidence: [],
      });
      let state = 'NOT_RUN';
      if (runs.some((run) => run.status === 'FAIL')) state = 'FAIL';
      else if (runs.some((run) => run.status === 'BLOCKED') || (runs.length > 0 && runs.every((run) => run.status === 'PASS') === false)) state = 'BLOCKED';
      else if (runs.length > 0) state = 'PASS';
      return { ...testCase, state, runs };
    });
    const report = {
      generatedAt: new Date().toISOString(),
      environment: envName(),
      webUrl: process.env.E3_WEB_URL || process.env.BASE_WEB_URL || 'http://localhost:4200',
      apiUrl: process.env.E3_API_URL || process.env.BASE_API_URL || 'http://localhost:8080',
      cases,
    };
    fs.writeFileSync(path.join(docsRoot, 'resultados', 'matriz-cp-e3.json'), `${JSON.stringify(report, null, 2)}\n`, 'utf8');
    writeMatrix(report);
  }
}
