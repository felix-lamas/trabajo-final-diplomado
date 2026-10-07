import { randomBytes, randomUUID } from 'node:crypto';

export function uniqueTestIdentity() {
  const id = randomUUID().replaceAll('-', '').slice(0, 12).toUpperCase();
  return {
    email: `e3-${id.toLowerCase()}@example.invalid`,
    ci: `E3${id.slice(0, 8)}`,
    ru: `E3-${id}`,
    displayName: `E3 TEST ${id}`,
  };
}

export function uniqueEventTitle() {
  return `[E3-TEST] ${new Date().toISOString().replace(/[^0-9TZ]/g, '')}-${randomBytes(3).toString('hex')}`;
}

export function testPdfBuffer() {
  const content = 'BT /F1 16 Tf 48 760 Td (PRUEBA TECNICA - NO ACREDITA PAGO REAL) Tj ET';
  const objects = [
    '<< /Type /Catalog /Pages 2 0 R >>',
    '<< /Type /Pages /Kids [3 0 R] /Count 1 >>',
    '<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 5 0 R >> >> /Contents 4 0 R >>',
    `<< /Length ${Buffer.byteLength(content, 'ascii')} >>\nstream\n${content}\nendstream`,
    '<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>',
  ];
  let pdf = '%PDF-1.4\n';
  const offsets = [0];
  for (let i = 0; i < objects.length; i++) {
    offsets.push(Buffer.byteLength(pdf, 'ascii'));
    pdf += `${i + 1} 0 obj\n${objects[i]}\nendobj\n`;
  }
  const xrefOffset = Buffer.byteLength(pdf, 'ascii');
  pdf += `xref\n0 ${objects.length + 1}\n0000000000 65535 f \n`;
  for (const offset of offsets.slice(1)) pdf += `${String(offset).padStart(10, '0')} 00000 n \n`;
  pdf += `trailer\n<< /Size ${objects.length + 1} /Root 1 0 R >>\nstartxref\n${xrefOffset}\n%%EOF\n`;
  return Buffer.from(pdf, 'ascii');
}
