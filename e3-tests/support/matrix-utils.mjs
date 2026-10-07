import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { CASES } from './cases.mjs';

export const REPO_ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..');
export const E3_DOCS_ROOT = path.join(REPO_ROOT, 'docs', 'pruebas', 'e3');
export const MATRIX_JSON = path.join(E3_DOCS_ROOT, 'resultados', 'matriz-cp-e3.json');

export function markdownFor(report) {
  const rows = report.cases.map((item) => {
    const detail = (item.runs ?? []).map((run) => `${run.status}${run.blockedReason ? `: ${run.blockedReason}` : ''}${run.error ? `: ${run.error}` : ''}`).join('<br>') || 'Sin ejecución registrada.';
    const evidence = (item.runs ?? []).flatMap((run) => run.evidence ?? []).map((file) => `\`${file}\``).join('<br>') || '—';
    return `| ${item.id} | ${item.requirement} | ${item.scenario} | ${report.environment} | ${detail.replaceAll('|', '\\|')} | ${item.state} | ${evidence} | ${report.generatedAt} |`;
  });
  return [
    '# Matriz de evidencia E3 — CP-01 a CP-28', '',
    `Generada: ${report.generatedAt} · Entorno: ${report.environment} · Web: ${report.webUrl} · API: ${report.apiUrl}`, '',
    'Los estados proceden de ejecuciones registradas. `NOT_RUN` significa que no hubo ejecución; `BLOCKED` indica una precondición o evidencia externa faltante; `PASS` requiere resultado observado; `FAIL` conserva una discrepancia real.', '',
    '| ID | RF/RNF | Escenario | Entorno | Resultado/error | Estado | Artefacto | Fecha |',
    '|---|---|---|---|---|---|---|---|', ...rows, '',
  ].join('\n');
}

export function writeMatrix(report) {
  fs.mkdirSync(path.dirname(MATRIX_JSON), { recursive: true });
  fs.writeFileSync(MATRIX_JSON, `${JSON.stringify(report, null, 2)}\n`, 'utf8');
  fs.writeFileSync(path.join(E3_DOCS_ROOT, 'MATRIZ_CP_E3.md'), markdownFor(report), 'utf8');
}

export function loadOrCreateMatrix() {
  if (fs.existsSync(MATRIX_JSON)) return JSON.parse(fs.readFileSync(MATRIX_JSON, 'utf8'));
  const now = new Date().toISOString();
  return {
    generatedAt: now,
    environment: process.env.E3_ENVIRONMENT || 'local-or-custom',
    webUrl: process.env.E3_WEB_URL || process.env.BASE_WEB_URL || 'http://localhost:4200',
    apiUrl: process.env.E3_API_URL || process.env.BASE_API_URL || 'http://localhost:8080',
    cases: CASES.map((item) => ({ ...item, state: 'NOT_RUN', runs: [] })),
  };
}

export function recordExternalCase(caseId, run, state) {
  const report = loadOrCreateMatrix();
  const item = report.cases.find((entry) => entry.id === caseId);
  if (!item) throw new Error(`Caso E3 desconocido: ${caseId}`);
  item.state = state;
  item.runs = [{ ...run, status: state }];
  report.generatedAt = new Date().toISOString();
  writeMatrix(report);
}
