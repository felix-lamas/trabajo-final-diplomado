import fs from 'node:fs/promises';
import path from 'node:path';
import { API_URL, apiV1, e3Environment, REPO_ROOT } from '../support/env.mjs';
import { recordExternalCase } from '../support/matrix-utils.mjs';

const periodHours = Number(process.env.E3_AVAILABILITY_PERIOD_HOURS);
const intervalSeconds = Math.max(1, Number(process.env.E3_AVAILABILITY_INTERVAL_SECONDS || 60));
const outDir = path.join(REPO_ROOT, 'docs/pruebas/e3/resultados');
const stamp = new Date().toISOString().replaceAll(':', '-');
await fs.mkdir(outDir, { recursive: true });

if (!Number.isFinite(periodHours) || periodHours <= 0) {
  const pathName = path.join(outDir, `CP-28-disponibilidad-${stamp}.json`);
  const csvPath = path.join(outDir, `CP-28-disponibilidad-${stamp}.csv`);
  const reason = 'Definir E3_AVAILABILITY_PERIOD_HOURS > 0 para comenzar una medición con duración explícita.';
  await fs.writeFile(pathName, `${JSON.stringify({ case: 'CP-28', state: 'BLOCKED', timestamp: new Date().toISOString(), reason }, null, 2)}\n`);
  await fs.writeFile(csvPath, 'timestamp,http,latencyMs,success\n');
  recordExternalCase('CP-28', { title: 'Monitoreo de disponibilidad', blockedReason: reason, evidence: [pathName, csvPath].map((file) => path.relative(REPO_ROOT, file).replaceAll('\\', '/')) }, 'BLOCKED');
  console.log(`CP-28 BLOCKED: ${reason}`);
  process.exitCode = 2;
} else {
  const deadline = Date.now() + periodHours * 60 * 60 * 1000;
  let interrupted = false;
  process.on('SIGINT', () => { interrupted = true; });
  const samples: Array<{ timestamp: string; http: number; latencyMs: number; success: boolean }> = [];
  while (!interrupted && Date.now() < deadline) {
    const timestamp = new Date().toISOString();
    const started = performance.now();
    try {
      const response = await fetch(apiV1('/salud'), { signal: AbortSignal.timeout(15_000) });
      let state: any = null;
      try { state = await response.json(); } catch { /* invalid response */ }
      const latencyMs = Number((performance.now() - started).toFixed(2));
      samples.push({ timestamp, http: response.status, latencyMs, success: response.ok && state?.estado === 'UP' });
    } catch {
      samples.push({ timestamp, http: 0, latencyMs: Number((performance.now() - started).toFixed(2)), success: false });
    }
    const remaining = deadline - Date.now();
    if (remaining > 0 && !interrupted) await new Promise((resolve) => setTimeout(resolve, Math.min(intervalSeconds * 1000, remaining)));
  }
  const elapsedHours = (Date.now() - (deadline - periodHours * 60 * 60 * 1000)) / 3_600_000;
  const successes = samples.filter((item) => item.success).length;
  const percentage = samples.length ? Number((successes / samples.length * 100).toFixed(3)) : 0;
  const complete = !interrupted && Date.now() >= deadline;
  const state = !complete ? 'BLOCKED' : percentage >= 95 ? 'PASS' : 'FAIL';
  const result = { case: 'CP-28', timestamp: new Date().toISOString(), environment: e3Environment(), apiUrl: API_URL, requestedHours: periodHours, observedHours: Number(elapsedHours.toFixed(3)), intervalSeconds, total: samples.length, successful: successes, availabilityPercent: percentage, thresholdPercent: 95, complete, state, samples };
  const jsonPath = path.join(outDir, `CP-28-disponibilidad-${stamp}.json`);
  const csvPath = path.join(outDir, `CP-28-disponibilidad-${stamp}.csv`);
  await fs.writeFile(jsonPath, `${JSON.stringify(result, null, 2)}\n`);
  await fs.writeFile(csvPath, ['timestamp,http,latencyMs,success', ...samples.map((item) => `${item.timestamp},${item.http},${item.latencyMs},${item.success}`)].join('\n') + '\n');
  recordExternalCase('CP-28', { title: 'Monitoreo de disponibilidad', durationMs: Math.round(elapsedHours * 3_600_000), evidence: [jsonPath, csvPath].map((file) => path.relative(REPO_ROOT, file).replaceAll('\\', '/')), summary: `${percentage}% (${successes}/${samples.length}), ${elapsedHours.toFixed(3)} h observadas`, blockedReason: complete ? null : 'Medición interrumpida antes de completar el período configurado.' }, state);
  console.log(`CP-28 ${state}: ${percentage}% de respuestas correctas en ${elapsedHours.toFixed(3)} h (${samples.length} muestras).`);
}
