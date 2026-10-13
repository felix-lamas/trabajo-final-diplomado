# Revisión del README y manuales de Vidia

Fecha: **13/10/2026**, America/La_Paz. Fuente técnica: árbol local actual. HEAD `f6891ef3ffb5a89a31e323d37a7ae9d2f734725c`, rama main, con cambios previos sin publicar. No se modificaron código de aplicación, configuración, migraciones ni documentos Word/PDF.

## Entregables

Actualizados: `README.md` y `flutter/VidiaApp/README.md`.

Creados: tres manuales Markdown en `docs/manuales/`, nueve imágenes en `docs/images/readme/`, su índice/procedencia, este informe y `README_COMPROBACIONES_20261013.json`. Los documentos Word del Anexo A/B y los manuales históricos se conservaron. Las guías Markdown permiten lectura directa en ambos repositorios y separan instalación, producción y uso; no se copiaron íntegramente los anexos académicos.

## Fuentes inspeccionadas

- `backend/pom.xml`; `frontend/package.json`, `package-lock.json`, `angular.json`, entornos; `flutter/VidiaApp/pubspec.yaml`, `pubspec.lock`, configuración API y Gradle Android.
- `backend/src/main/resources/application.yml`, `application-prod.yml` y migraciones V1–V5; `.env.example`, `render.yaml`, `backend/Dockerfile`, `docker/docker-compose.yml`.
- Java bajo `backend/src/main/java/bo/uajms/eventos/`: `core/seguridad` (SecurityConfig, JwtService, JwtAuthenticationFilter), `core/configuracion/DatosInicialesSeed`; controladores/servicios/repositorios de los nueve módulos.
- `InscripcionService`, `EventoRepository`, `QrAsistenciaService`, `AsistenciaService`, `CertificadoService`, `CertificadoDocumentoService`, `DashboardController`, `ReporteArchivoService`, `ArchivoSeguroServicio`, `SupabaseS3StorageService`, `FilesystemStorageService`, `BrevoApiCorreoProveedor`, `SesionUsuarioServicio`.
- Angular rutas, servicios de inscripción/autenticación y QR en `sesiones-evento.component.ts`; Flutter `ApiConfig`, pantallas, `AsistenciaFlow`, `GeolocatorAttendanceLocationSource`, repositorios y coordinación de enlaces.
- Pruebas relacionadas y reportes del Anexo B, manuales `docs/release_1_0/`, requisitos/trazabilidad y monografía `Monografia_Vidia_Final.docx`, apartado 1.5.
- Manifest/capturas del Anexo A; configuración de ignorados, nombres de archivos sensibles versionados, licencia y workflows.

## Discrepancias corregidas o delimitadas

| Antes / afirmación | Evidencia actual | Tratamiento |
|---|---|---|
| Flutter pagos, asistencia, certificados e historial «pendientes de implementación» | Pantallas, repositorios y pruebas Flutter; Anexo A con consultas reales | Corregido a implementado, con validación completa pendiente |
| Correo presentado solo como SMTP | `BrevoApiCorreoProveedor.enviar`: HTTPS/JSON; proveedor default brevo-api | Brevo API y alternativa SMTP diferenciados |
| Copiar .env y arrancar sin adaptar | Ejemplo selecciona SMTP/Supabase; credenciales incompletas | Explicar variables mínimas, proveedores y fallos de correo |
| Swagger `/api/v1/swagger-ui.html` | Config `/swagger-ui.html`; interfaz `/swagger-ui/index.html`; OpenAPI `/v3/api-docs` | Corregir rutas |
| Node «20 o superior» | engines CLI: ^20.19.0, ^22.12.0 o >=24.0.0 | Rangos exactos; Node local 24.16.0 |
| Versión Angular única implícita | Core 21.2.17, CLI 21.2.16 en lock e instalación actual | Distinguir paquete/CLI y declaración/resolución |
| Release Android no publicada | Página v1.0.0 y APK HEAD 200 | Publicación confirmada; firma/instalación no repetidas |
| Release usa firma debug si falta key.properties | Gradle exige key.properties y credenciales/keystore | Corregido; no generar firma nueva |
| App Links requiere publicar descriptor ausente | Archivo en `frontend/public/.well-known/assetlinks.json`; GET público JSON 200 | Publicación confirmada; asociación/firma/dispositivo pendientes |
| PostgreSQL productivo = 16 por Compose | Solo descriptor local postgres:16-alpine | Versión productiva no verificada |
| Todos los servicios descritos como automatizados | render.yaml declara solo web; backend tiene Dockerfile | Separar descriptor web de configuración externa backend/base |
| Roles equivalentes en todos los endpoints | Inscripción/asistencia exigen USUARIO; creación/envío exige ORGANIZADOR; publicación ADMINISTRADOR | Responsabilidades y límites precisos |
| QR personal o global consumido una vez | QR compartido por sesión, máximo 2 min; unicidad inscripción/sesión | No confundir token QR y JWT ni afirmar antifraude absoluto |
| Certificados siempre para todo inscrito | Evento finalizado, inscripción confirmada, pago aprobado y reglas de asistencia | Explicar elegibilidad, incluido curricular 80% |
| Tika interpretado como protección antimalware | Detección MIME y validaciones, no integración antivirus identificada | No atribuir análisis antimalware |
| Docker DB_USER tratado como cuenta limitada | Compose POSTGRES_USER inicializa usuario administrativo | Separar inicialización y cuenta de aplicación |
| Todos los resultados históricos tratados como actuales | Anexo B del 12/10 y cortes anteriores con fechas/alcances distintos | Referencias fechadas; suites no repetidas en esta tarea |

`application-prod.yml` declara Supabase, pero variables/argumentos pueden modificar la selección efectiva. No se añadió ni se afirma un guard global que prohíba local por perfil. V3 puede omitir agregar restricciones históricas cuando encuentra duplicados; no se certifica el catálogo productivo de constraints.

## Comprobaciones efectivamente ejecutadas

Comandos de lectura, con resultado satisfactorio: `java -version`, `mvn -version`, `node --version`, `npm --version`, `flutter --version`, Angular CLI local `node node_modules/@angular/cli/bin/ng.js version`, `git rev-parse HEAD`, `git status --short`, consulta de remotos y `git ls-remote --heads origin main` / `git ls-remote --heads gitlab main`.

Versiones observadas: Temurin 21.0.11, Maven 3.10.0, Node 24.16.0, npm 11.6.3, Flutter 3.44.8, Dart 3.12.2, Angular 21.2.17, CLI 21.2.16. El Anexo B conserva resultados de otro entorno/copia; su versión CLI indicada no se sustituye silenciosamente por esta lectura.

Los dos remotos main respondieron con el mismo SHA que HEAD. No se hizo fetch, commit o push, ni comparación exhaustiva de todas las ramas. El árbol local contiene modificaciones; no se declara sincronización completa.

Consultas HTTPS GET públicas: GitHub/GitLab, web raíz y catálogo, verificación pública, Swagger, OpenAPI, salud y release: **200**. Backend raíz y perfil sin autenticación: **401**. OpenAPI expuso 77 rutas; salud respondió `{"estado":"UP"}`. APK: **HEAD 200**, sin descargar ni instalar. App Links: JSON público 200 con el paquete esperado, sin comprobar huella de la APK instalada. Fechas, métodos y alcance: [registro JSON](README_COMPROBACIONES_20261013.json). Las URL finales firmadas de descarga se omitieron del registro publicado.

No se ejecutaron compilaciones, suites, migraciones, pruebas de carga, autenticaciones ni escrituras de negocio en esta tarea. Las compilaciones/pruebas del 12/10 se reutilizan como evidencia histórica explícita, no como ejecución nueva.

## Capturas y privacidad

Nueve capturas existentes del 12/10 fueron inspeccionadas y reutilizadas. Se recortó detalle Android antes de correo/teléfonos, y certificado para excluir el código público. No se alteraron resultados ni se fabricaron interfaces. El pago mostrado es una consulta ficticia, sin documento real. [Índice](../images/readme/README.md) y [procedencia/hashes](../images/readme/procedencia.json).

No se incorporaron contraseñas, JWT, cookies ni QR temporales. No se encontraron archivos `.env`, `key.properties` o keystore entre los nombres versionados inspeccionados. Esto no es una revisión exhaustiva de secretos en todo contenido/historial Git. `.env` y firma Android resultaron ignorados; **backend/uploads/readme-check.pdf no resultó ignorado**. Se documentó el riesgo; `.gitignore` no se cambió. Antes de publicar deben excluirse cargas privadas y revisarse los archivos preparados.

No se encontró licencia general, CONTRIBUTING ni workflow CI versionado; no se agregaron licencias ni insignias de CI/cobertura inventadas.

## Pendientes antes de publicar

1. Revisar diff y archivos preparados, licencias del proyecto/dependencias y ausencia de datos privados.
2. Revisar presentación de Markdown/Mermaid en GitHub y GitLab al publicar: no se hizo push ni se renderizó allí la versión nueva.
3. Confirmar panel Render, variables efectivas, commit/versión productivos, TLS/versión PostgreSQL y cabeceras del hosting.
4. Validar las incidencias observadas el 12/10: consulta de sesiones Android y enlaces de certificados dirigidos a localhost. No se dan por corregidas con documentación.
5. Ejecutar QR/GPS físico, entrega de correo y flujos completos de archivos/inscripción/certificación en entorno autorizado.
6. Verificar firma/instalación de APK y asociación App Links en dispositivo, recuperación institucional y monitoreo.
7. Acordar bootstrap administrativo de producción y política de respaldos/secretos; procedimientos descritos no son acciones ejecutadas.

Se comprobaron 58 enlaces relativos en siete documentos Markdown, los delimitadores de bloques de código y la decodificación de las nueve imágenes, sin errores encontrados. Se revisaron visualmente los recortes de Android y verificación de certificados. La comparación de hashes de 1450 archivos originales confirmó que solo cambiaron `README.md` y `flutter/VidiaApp/README.md`; no falta ningún original. El código, las configuraciones, las migraciones y los documentos Word/PDF conservaron su contenido previo a esta tarea. Los resultados están registrados en el JSON de esta revisión. La presentación final de Markdown/Mermaid en GitHub y GitLab queda pendiente de revisión tras la publicación.
