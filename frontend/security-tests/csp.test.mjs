import { test } from 'node:test';
import assert from 'node:assert/strict';
import { createServer } from 'node:http';
import { readFile } from 'node:fs/promises';
import { resolve, extname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { chromium } from '@playwright/test';

// Run after npm --prefix frontend run build: node --test frontend/security-tests/csp.test.mjs
// Local built SPA only. API requests are intercepted; no accounts or production writes.
const dist = fileURLToPath(new URL('../dist/frontend/browser/', import.meta.url));
const html = await readFile(resolve(dist, 'index.html'), 'utf8');
const mime = { '.js': 'text/javascript', '.css': 'text/css', '.woff2': 'font/woff2', '.png': 'image/png', '.svg': 'image/svg+xml' };

test('production CSP keeps Angular login functional and blocks injected inline script', async () => {
  assert.match(html, /http-equiv="Content-Security-Policy"/i);
  assert.match(html, /strict-dynamic/);
  assert.match(html, /sha256-/);
  assert.doesNotMatch(html, /unsafe-eval/);
  const server = createServer(async (req, res) => {
    try {
      const url = new URL(req.url, 'http://localhost');
      const path = resolve(dist, '.' + decodeURIComponent(url.pathname));
      if (!path.startsWith(dist)) { res.writeHead(403).end(); return; }
      if (extname(path)) {
        res.setHeader('Content-Type', mime[extname(path)] ?? 'application/octet-stream');
        res.end(await readFile(path));
      } else {
        res.setHeader('Content-Type', 'text/html');
        const injected = url.searchParams.has('inject') ? '<script>window.__untrustedScriptRan = true;</script>' : '';
        res.end(html.replace('</body>', injected + '</body>'));
      }
    } catch { res.writeHead(404).end(); }
  });
  await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
  let browser;
  try {
    browser = await chromium.launch();
    const page = await browser.newPage();
    await page.route('https://trabajo-final-diplomado.onrender.com/**', route => route.fulfill({ contentType: 'application/json', body: '[]' }));
    const violations = [];
    page.on('console', msg => { if (msg.type() === 'error' && /Content Security Policy|Refused to/.test(msg.text())) violations.push(msg.text()); });
    const base = `http://127.0.0.1:${server.address().port}`;
    await page.goto(base + '/auth/login');
    await page.locator('input[type="password"]').waitFor();
    assert.equal(violations.length, 0, 'Normal startup must not violate CSP');
    await page.goto(base + '/auth/login?inject=1');
    await page.locator('input[type="password"]').waitFor();
    assert.equal(await page.evaluate(() => window.__untrustedScriptRan), undefined);
    assert.ok(violations.length > 0, 'Browser must reject untrusted inline script');
  } finally {
    await browser?.close();
    await new Promise(resolve => server.close(resolve));
  }
});
