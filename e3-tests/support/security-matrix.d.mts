export const SECURITY_MATRIX: Array<{
  method: 'GET';
  path: string;
  allowed: Array<'admin' | 'organizer' | 'user'>;
  source: string;
}>;
