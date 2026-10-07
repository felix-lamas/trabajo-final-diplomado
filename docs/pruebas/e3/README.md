# Infraestructura de pruebas automatizadas E3 — Vidia

## Alcance y fuentes

La tabla de CP-01…CP-28 se extrajo de la Tabla 24 del apartado 2.8 de `docs/monografia/Lamas-monografia-F.docx`. Las rutas y permisos se auditaron en los controladores, DTO, `SecurityConfig`, anotaciones `@PreAuthorize`, seed demo y OpenAPI generado por Springdoc. Las pruebas no modifican lógica funcional de backend, Angular ni Flutter; Flutter contiene únicamente un integration test de QA.

Rutas comprobadas en código: `POST /api/v1/auth/login`, `POST /api/v1/auth/logout`, `GET /api/v1/usuarios/perfil`, `GET /api/v1/usuarios/solicitudes-organizador`, `GET /api/v1/eventos/publicados`, `GET /api/v1/eventos/publicados/buscar`, `GET /api/v1/eventos/{id}`, `POST /api/v1/eventos`, `PATCH /api/v1/eventos/{id}/enviar-revision`, `PATCH /api/v1/eventos/{id}/publicar`, `POST /api/v1/inscripciones`, `GET /api/v1/inscripciones/mis-inscripciones`, `POST /api/v1/pagos/{id}/comprobante` (multipart `archivo`), `GET /api/v1/pagos/mis-pagos`, `GET /api/v1/pagos/pendientes`, `PATCH /api/v1/pagos/{id}/validar`, `PATCH /api/v1/pagos/{id}/rechazar`, `POST /api/v1/eventos/{eventoId}/sesiones`, `POST /api/v1/sesiones/{sesionId}/qr/generar`, `POST /api/v1/certificados/generar/{inscripcionId}`, `GET /api/v1/certificados/verificar/{codigo}`, `GET /api/v1/dashboard/ejecutivo`, `GET /api/v1/salud` y `GET /v3/api-docs`.

Roles del backend: `ADMINISTRADOR`, `ORGANIZADOR`, `USUARIO`. Las credenciales demo de desarrollo proceden del seed y de `DEMO_PASSWORD` local; este paquete no guarda contraseñas ni asume valores. Las sesiones protegidas del backend se crean/revocan en login/logout. Por eso, todos los escenarios que autentican requieren destino local/privado, `E3_ALLOW_MUTATIONS=true` y `E3_ISOLATED_TEST_ENV=true`; la suite bloquea escrituras y login contra Render.

## Requisitos

- Java 21, Maven, PostgreSQL local, Node.js 24 recomendado y npm.
- Angular: dependencias instaladas en `frontend/`.
- Flutter/Dart del proyecto y un emulador o Android conectado para `CP-14`.
- Navegadores de Playwright instalados para cada proyecto Chromium/Firefox/WebKit.
- API local normalmente en `http://localhost:8080`; Web Angular en `http://localhost:4200`.

## Configuración

1. Copiar `.env.e3.example` como `.env.e3.local` en la raíz del repositorio.
2. Completar `E3_ADMIN_EMAIL/PASSWORD`, `E3_ORGANIZER_EMAIL/PASSWORD`, `E3_USER_EMAIL/PASSWORD` solo con cuentas de prueba.
3. Para CP que escriben, configurar explícitamente `E3_ALLOW_MUTATIONS=true` y `E3_ISOLATED_TEST_ENV=true`. El destino API debe ser localhost, loopback, LAN privada o dominio `.local`; `*.onrender.com` está bloqueado. Usar una base de datos local aislada, sin datos institucionales reales.
4. Proporcionar IDs reales de fixtures solo si ya existen en el entorno de prueba: `E3_FREE_EVENT_ID`, `E3_PAID_EVENT_ID`, `E3_DUPLICATE_EVENT_ID`, `E3_STATE_CONFLICT_EVENT_ID` (debe estar fuera de `EN_REVISION`), `E3_TEST_PAYMENT_ID`, `E3_INVALID_FILE_PAYMENT_ID`, `E3_OTHER_USER_PAYMENT_ID`, `E3_QR_EVENT_ID`, `E3_ELIGIBLE_INSCRIPTION_ID`, `E3_INELIGIBLE_INSCRIPTION_ID`, `E3_CERTIFICATE_CODE`. La suite no inventa esos IDs ni crea certificados/asistencias/pagos.

Los defaults Playwright son Web `http://localhost:4200`, API `http://localhost:8080`. `E3_WEB_URL` y `E3_API_URL` tienen prioridad; `BASE_WEB_URL` y `BASE_API_URL` son alternativas. La URL API puede ser el host raíz o terminar en `/api/v1`. No usar `E3_*_URL` de producción con escenarios mutantes.

## Instalación y comandos

```powershell
npm install
npm run test:e3:install-browsers
npm run test:e3:types
npm run test:e3
npm run test:e3:report
```

Suites existentes y ejecución Flutter:

```powershell
npm run test:backend
npm run test:angular
npm run test:flutter:unit
npm run test:e3:flutter
```

Para Angular el comando ejecuta la suite ya definida por `frontend/package.json`. El backend sigue usando Maven. Flutter unitario no demuestra cámara/GPS físico; `integration_test` CP-14 requiere `E3_ANDROID_DEVICE_ID`, `E3_FLUTTER_API_BASE_URL` (alcanzable desde Android, terminado en `/api/v1`) y cuenta demo configurada. El runner crea un JSON temporal en el directorio del sistema, lo elimina al terminar y no imprime sus valores.

### Rendimiento CP-24

Es una carga controlada de solo lectura (20 requests concurrentes por muestra a `/salud` y `/eventos/publicados`). Solo admite host local/privado y exige `E3_PERFORMANCE_ACK=I_CONFIRM_LOCAL_LOAD`. `E3_PERFORMANCE_SAMPLES` controla cuántas oleadas se ejecutan. No se ejecuta automáticamente contra producción.

```powershell
$env:E3_PERFORMANCE_ACK = 'I_CONFIRM_LOCAL_LOAD'
$env:E3_PERFORMANCE_SAMPLES = '3'
npm run test:e3:performance
```

### Disponibilidad CP-28

La medición lee `/api/v1/salud`; requiere período explícito `E3_AVAILABILITY_PERIOD_HOURS` e intervalo `E3_AVAILABILITY_INTERVAL_SECONDS`. No se debe declarar 95 % antes de completar el período; al interrumpirla registra `BLOCKED`. El monitoreo solo inicia cuando se invoca el comando.

```powershell
$env:E3_AVAILABILITY_PERIOD_HOURS = '24'
$env:E3_AVAILABILITY_INTERVAL_SECONDS = '60'
npm run test:e3:availability
```

## Reportes y evidencia

`npm run test:e3` genera Playwright HTML, JUnit XML, JSON, matriz CP automática, JSON de evidencia HTTP, screenshots de UI y artifacts. El HTML se abre con `npm run test:e3:report`.

- `reporte/html/`: reporte Playwright.
- `resultados/junit.xml`, `resultados/playwright.json`: resultados interoperables.
- `resultados/matriz-cp-e3.json` y `MATRIZ_CP_E3.md`: estados CP-01…CP-28; scripts Flutter, carga y disponibilidad sincronizan sus resultados.
- `resultados/evidencia/`: status/body sanitizado, endpoint, entorno, navegador y adjuntos.
- `capturas/`: screenshots reales prefijados con `CP-01`…`CP-28`.
- `traces/`, `videos/`: copia de trace/video de Playwright cuando falla.

Por seguridad, el proyecto API no graba traces/videos de solicitudes que puedan incluir credenciales; conserva evidencia JSON con cuerpos redactados. El proyecto de login administrativo Angular tampoco graba trace/video. Los flujos web públicos usan screenshots, trace y video de fallo. Firefox se configura, pero en este Windows su `browserContext.newPage()` falla antes del test; se registra `BLOCKED` en vez de atribuirlo al producto.

El reporter no conserva JWT, contraseña, Authorization, token temporal QR ni correos en bodies. Login se conserva solo en memoria. Playwright trace/video pueden contener información visual de la página; por eso los casos web son públicos o se omiten paneles con nombres en capturas administrativas. Mantener los artifacts de una cuenta real fuera de Git.

Estados: `PASS` exige respuesta/resultado real esperado; `FAIL` significa que la operación ejecutada contradijo la expectativa; `BLOCKED` indica precondición, entorno, cuenta, fixture o dispositivo faltante; `NOT_RUN` indica que no hubo resultado en la ejecución. Una fila incompleta no se transforma en PASS.

## Casos y límites

- CP-01: login/perfil/logout/revocación local; entrega de correo/verificación externa requiere evidencia manual de bandeja. La suite no dice que Brevo se probó.
- CP-02, CP-03, CP-16, CP-18, CP-19, CP-20, CP-23: se automatizan contra endpoints reales; CP-03/18 y login válido requieren cuenta/entorno aislado.
- CP-04/05: crean un evento único `[E3-TEST]`, lo envían a revisión y publican con actor real autorizado. El `afterAll` solo actúa sobre el UUID creado por CP-04: elimina si sigue BORRADOR, rechaza si quedó EN_REVISION y cancela si quedó PUBLICADO. No borra eventos previos ni usa SQL. Nunca se ejecuta en Render.
- CP-06/07: requieren eventos publicados específicos; guardan inscripción real. La suite no elimina inscripciones y debe usarse una cuenta/DB aislada.
- CP-08/17: el archivo válido generado se llama `PRUEBA_TECNICA_NO_ACREDITA_PAGO.pdf` y su contenido indica que no demuestra transacción. Upload cambia el estado real; usa solo un pago propio local de prueba. CP-09/21 modifican el pago vía endpoints reales y requieren fixtures con estado compatible.
- CP-10: crea una sesión y QR temporal en evento propio. El QR se valida en memoria; el token nunca se persiste en evidencia. El backend no ofrece eliminación de sesión en la ruta auditada, por lo que la sesión de prueba queda en DB local.
- CP-11/22: quedan `BLOCKED/NEEDS_DEVICE_EVIDENCE` si no existe QR/sesión/inscripción natural y Android físico para cámara/GPS.
- CP-12/13/23: solo se genera certificado con inscripción configurada elegible; verificación inválida es pública y automática; certificado real no disponible queda bloqueado. No se fabrican asistencias/porcentajes/certificados.
- CP-14: integration test Flutter prueba UI participante + API en dispositivo/emulador, sin sustituir sensor/cámara ni afirmar asistencia.
- CP-15: login Angular de administrador y lectura real del dashboard; screenshot oculta el panel que puede listar nombres de participantes/pagos.
- CP-24: solo lectura local/privada, 20 concurrentes, resultado calculado de mediciones.
- CP-25: matriz revisada desde `@PreAuthorize` de los controllers y confirma método/ruta en `/v3/api-docs`. El caso queda bloqueado si falta alguna credencial o aislamiento.
- CP-26: estudio de usabilidad es manual, plantilla en `PROTOCOLO_CP26_USABILIDAD.csv`; Playwright no simula participantes humanos.
- CP-27: Playwright ejecuta catálogo en Chromium, Firefox y WebKit; Edge/Android se registran mediante checklist, no se infieren.
- CP-28: la disponibilidad solo se calcula al completar período/intervalo configurados.

## Limpieza y seguridad

No hay DELETE masivo ni consulta directa a base de datos. El único cleanup automático es el borrador con ID recién recibido por CP-04. El backend no expone una operación de borrado seguro para inscripciones, pagos, sesiones o certificados; por tanto, usar un entorno temporal aislado y no reutilizar fixtures después de una transición irreversible. No subir `.env.e3.local`, tokens, secretos, archivos reales ni evidencias con personas reales. No ejecutar mutaciones contra producción.
