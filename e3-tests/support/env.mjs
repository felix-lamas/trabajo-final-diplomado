import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

export const REPO_ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..');

export function loadE3Env() {
  const file = path.join(REPO_ROOT, '.env.e3.local');
  if (!fs.existsSync(file)) return;
  for (const rawLine of fs.readFileSync(file, 'utf8').split(/\r?\n/)) {
    const line = rawLine.trim();
    if (!line || line.startsWith('#')) continue;
    const separator = line.indexOf('=');
    if (separator < 1) continue;
    const key = line.slice(0, separator).trim();
    let value = line.slice(separator + 1).trim();
    if ((value.startsWith('"') && value.endsWith('"')) || (value.startsWith("'") && value.endsWith("'"))) {
      value = value.slice(1, -1);
    }
    if (!process.env[key]) process.env[key] = value;
  }
}

loadE3Env();

export const WEB_URL = cleanUrl(process.env.E3_WEB_URL || process.env.BASE_WEB_URL || 'http://localhost:4200');
export const API_URL = cleanUrl(process.env.E3_API_URL || process.env.BASE_API_URL || 'http://localhost:8080');

function cleanUrl(value) {
  const url = new URL(value);
  url.username = '';
  url.password = '';
  url.hash = '';
  return url.toString().replace(/\/$/, '');
}

export function apiV1(route = '') {
  const base = new URL(API_URL);
  const prefix = base.pathname.replace(/\/$/, '').endsWith('/api/v1')
    ? base.pathname.replace(/\/$/, '')
    : `${base.pathname.replace(/\/$/, '')}/api/v1`;
  const suffix = route.replace(/^\//, '');
  return new URL(`${prefix}/${suffix}`, base.origin).toString().replace(/\/$/, route ? '' : '/');
}

export function webUrl(route = '') {
  return new URL(route.replace(/^\//, ''), `${WEB_URL}/`).toString();
}

export function credential(role) {
  const suffix = role.toUpperCase();
  const email = process.env[`E3_${suffix}_EMAIL`];
  const password = process.env[`E3_${suffix}_PASSWORD`];
  return email && password ? { email, password } : null;
}

export function e3Environment() {
  return process.env.E3_ENVIRONMENT || (new URL(API_URL).hostname === 'trabajo-final-diplomado.onrender.com' ? 'production' : 'local-or-custom');
}

export function mutationTargetAllowed() {
  if (process.env.E3_ALLOW_MUTATIONS !== 'true' || process.env.E3_ISOLATED_TEST_ENV !== 'true') return false;
  return isLocalOrPrivateTarget();
}

export function isLocalOrPrivateTarget() {
  const host = new URL(API_URL).hostname.toLowerCase();
  if (host === 'trabajo-final-diplomado.onrender.com' || host.endsWith('.onrender.com')) return false;
  const privateIpv4 = /^(10\.|127\.|192\.168\.|172\.(1[6-9]|2\d|3[01])\.)/.test(host);
  return host === 'localhost' || host === '127.0.0.1' || host === '::1' || host.endsWith('.local') || privateIpv4;
}

export function requiredScenario(name) {
  const value = process.env[name];
  return value?.trim() ? value.trim() : null;
}
