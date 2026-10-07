import fs from 'node:fs/promises';
import path from 'node:path';
import { API_URL, apiV1, e3Environment, isLocalOrPrivateTarget, REPO_ROOT } from '../support/env.mjs';
import { recordExternalCase } from '../support/matrix-utils.mjs';

const outDir = path.join(REPO_ROOT, 'docs/pruebas/e3/resultados');
const stamp = new Date().toISOString().replaceAll(':', '-');
await fs.mkdir(outDir, { recursive: true });

if (!isLocalOrPrivateTarget() || process.env.E3_PERFORMANCE_ACK !== 'I_CONFIRM_LOCAL_LOAD') {
  const resultPath = path.join(outDir, `CP-24-rendimiento-${stamp}.json`);
  const csvPath = path.join(outDir, `CP-24-rendimiento-${stamp}.csv`);
  const htmlPath = path.join(outDir, `CP-24-rendimiento-${stamp}.html`);
  const reason = 'La carga de 20 usuarios solo se ejecuta en API local/privada y requiere E3_PERFORMANCE_ACK=I_CONFIRM_LOCAL_LOAD.';
  await fs.writeFile(resultPath, `${JSON.stringify({ case: 'CP-24', status: 'BLOCKED', timestamp: new Date().toISOString(), apiUrl: API_URL, reason }, null, 2)}\n`);
  await fs.writeFile(csvPath, 'sample,virtualUser,endpoint,status,durationMs,within3s\n');
  await fs.writeFile(htmlPath, `<!doctype html><meta charset="utf-8"><title>CP-24 rendimiento</title><h1>CP-24 — Medición no ejecutada</h1><p>Estado: BLOCKED</p><p>${reason}</p><p>API: ${new URL(API_URL).origin}</p>`);
  recordExternalCase('CP-24', { title: 'Rendimiento concurrente (bloqueado)', status: 'BLOCKED', blockedReason: reason, evidence: [resultPath, csvPath, htmlPath].map((file) => path.relative(REPO_ROOT, file).replaceAll('\\', '/')) }, 'BLOCKED');
  console.log(`CP-24 BLOCKED: ${reason}`);
  process.exitCode = 2;
} else {
  const samples = Number.parseInt(process.env.E3_PERFORMANCE_SAMPLES || '1', 10);
  if (!Number.isInteger(samples) || samples < 1 || samples > 100) {
    const resultPath = path.join(outDir, `CP-24-rendimiento-${stamp}.json`);
    const csvPath = path.join(outDir, `CP-24-rendimiento-${stamp}.csv`);
    const htmlPath = path.join(outDir, `CP-24-rendimiento-${stamp}.html`);
    const reason = 'E3_PERFORMANCE_SAMPLES debe ser un entero entre 1 y 100.';
    await fs.writeFile(resultPath, `${JSON.stringify({ case: 'CP-24', status: 'BLOCKED', timestamp: new Date().toISOString(), apiUrl: API_URL, reason }, null, 2)}\n`);
    await fs.writeFile(csvPath, 'sample,virtualUser,endpoint,status,durationMs,within3s\n');
    await fs.writeFile(htmlPath, `<!doctype html><meta charset="utf-8"><title>CP-24 rendimiento</title><h1>CP-24 — Medición no ejecutada</h1><p>Estado: BLOCKED</p><p>${reason}</p>`);
    recordExternalCase('CP-24', { title: 'Rendimiento concurrente (configuración bloqueada)', status: 'BLOCKED', blockedReason: reason, evidence: [resultPath, csvPath, htmlPath].map((file) => path.relative(REPO_ROOT, file).replaceAll('\\', '/')) }, 'BLOCKED');
    console.log(`CP-24 BLOCKED: ${reason}`);
    process.exitCode = 2;
    process.exit();
  }
  const targets = [apiV1('/salud'), apiV1('/eventos/publicados')];
  const measurements: Array<{ sample: number; virtualUser: number; endpoint: string; status: number; durationMs: number; success: boolean }> = [];
  for (let sample = 1; sample <= samples; sample++) {
    await Promise.all(Array.from({ length: 20 }, async (_, index) => {
      const endpoint = targets[index % targets.length];
      const started = performance.now();
      try {
        const response = await fetch(endpoint, { signal: AbortSignal.timeout(30_000) });
        const durationMs = Number((performance.now() - started).toFixed(2));
        await response.arrayBuffer();
        measurements.push({ sample, virtualUser: index + 1, endpoint, status: response.status, durationMs, success: response.ok && durationMs <= 3000 });
      } catch {
        measurements.push({ sample, virtualUser: index + 1, endpoint, status: 0, durationMs: Number((performance.now() - started).toFixed(2)), success: false });
      }
    }));
  }
  const within = measurements.filter((item) => item.success).length;
  const percentage = measurements.length ? Number((within / measurements.length * 100).toFixed(2)) : 0;
  const sorted = measurements.map((item) => item.durationMs).sort((a, b) => a - b);
  const p95 = sorted[Math.max(0, Math.ceil(sorted.length * 0.95) - 1)] ?? null;
  const state = percentage >= 95 ? 'PASS' : 'FAIL';
  const result = { case: 'CP-24', timestamp: new Date().toISOString(), environment: e3Environment(), apiUrl: API_URL, virtualUsersPerSample: 20, samples, requests: measurements.length, requestsWithin3Seconds: within, percentageWithin3Seconds: percentage, p95Ms: p95, thresholdPercent: 95, thresholdMs: 3000, state, measurements };
  const jsonPath = path.join(outDir, `CP-24-rendimiento-${stamp}.json`);
  const csvPath = path.join(outDir, `CP-24-rendimiento-${stamp}.csv`);
  const htmlPath = path.join(outDir, `CP-24-rendimiento-${stamp}.html`);
  await fs.writeFile(jsonPath, `${JSON.stringify(result, null, 2)}\n`);
  await fs.writeFile(csvPath, ['sample,virtualUser,endpoint,status,durationMs,within3s', ...measurements.map((item) => `${item.sample},${item.virtualUser},"${item.endpoint}",${item.status},${item.durationMs},${item.success}`)].join('\n') + '\n');
  await fs.writeFile(htmlPath, `<!doctype html><meta charset="utf-8"><title>CP-24 rendimiento</title><h1>CP-24 — Medición controlada</h1><p>Entorno: ${e3Environment()}</p><p>Concurrencia: 20 por muestra · Solicitudes: ${measurements.length}</p><p>≤ 3 s: ${within}/${measurements.length} (${percentage}%)</p><p>P95: ${p95} ms · Estado observado: ${state}</p><p>API: ${new URL(API_URL).origin}</p>`);
  recordExternalCase('CP-24', { title: 'Carga de 20 usuarios concurrentes', durationMs: null, browser: 'node fetch', evidence: [jsonPath, csvPath, htmlPath].map((file) => path.relative(REPO_ROOT, file).replaceAll('\\', '/')), summary: `${percentage}% dentro de 3 s; P95 ${p95} ms` }, state);
  console.log(`CP-24 ${state}: ${percentage}% ≤ 3 s; P95 ${p95} ms; ${measurements.length} solicitudes.`);
}
