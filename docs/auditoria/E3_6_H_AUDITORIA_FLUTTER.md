# E3.6-H — Auditoría integral y cierre técnico de Flutter

## 1. Alcance

PROBADO LOCALMENTE — Se inspeccionaron Flutter, contratos OpenAPI del backend local, controllers/DTOs/permisos Spring, configuración Android, dependencias, navegación, seguridad, pruebas y builds. No se agregaron funcionalidades ni se modificó backend, OpenAPI, documentación académica o datos.

## 2. Versiones

VERIFICADO — Flutter 3.44.8, Dart 3.12.2, SM A125M con Android 12/API 31. El proyecto Android compila con JVM target 17 y minSdk 24.

## 3. Arquitectura del cliente

IMPLEMENTADO — UI → interfaces de repositorio → adaptadores `Backend*Repository`/servicios → `ApiService` → Spring REST. `MultiProvider` comparte repositorios y servicios. JWT se almacena con `flutter_secure_storage`; no hay driver PostgreSQL ni SDK/clave Supabase en Flutter. Las imágenes de portada se solicitan mediante `Image.network` a la URL que devuelve el DTO del backend; no hay credenciales asociadas.

## 4. Endpoints auditados

PROBADO LOCALMENTE — Los 29 métodos/rutas consumidos existen en OpenAPI `http://localhost:8080/v3/api-docs` y en los controllers auditados. Las rutas de la tabla son absolutas desde `/api/v1`.

| Método y ruta | Pantalla/cliente Flutter | Backend y autenticación efectiva |
|---|---|---|
| `POST /api/v1/auth/login` | Login → `AuthService.signIn` | LoginRequest; público |
| `POST /api/v1/auth/registro` | Registro → `AuthService.register` | RegistroUsuarioRequest; público |
| `POST /api/v1/auth/verificar-correo` | Verificación → `verifyEmail` | VerificarCorreoRequest; público |
| `POST /api/v1/auth/reenviar-verificacion` | Verificación → `resendVerification` | ReenviarVerificacionRequest; público |
| `POST /api/v1/auth/recuperar-contrasena` | Recuperación → `requestPasswordReset` | RecuperarContrasenaRequest; público |
| `POST /api/v1/auth/restablecer-contrasena` | Recuperación → `resetPassword` | ResetContrasenaRequest; público |
| `POST /api/v1/auth/logout` | Home → `signOut` | JWT; invalida/cierra sesión en backend |
| `GET /api/v1/usuarios/perfil` | AuthGate/perfil → `restoreUser` | JWT; perfil autenticado |
| `PUT /api/v1/usuarios/perfil` | Perfil → `updateProfile` | JWT; ActualizarPerfilRequest |
| `POST /api/v1/usuarios/cambiar-contrasena` | Perfil → `changePassword` | JWT; CambioContrasenaRequest; cliente limpia sesión tras éxito |
| `GET /api/v1/categorias-evento/activas` | Catálogo → `BackendCategoriaRepository` | JWT; permitido a roles ADMINISTRADOR/ORGANIZADOR/USUARIO |
| `GET /api/v1/eventos/publicados` | Catálogo → `fetchPublished` | Público efectivo por SecurityConfig |
| `GET /api/v1/eventos/publicados/buscar` | Búsqueda/filtros → `searchPublished` | Público efectivo; `texto`, `categoriaId`, `tipo`, `modalidad` |
| `GET /api/v1/eventos/{id}` | Detalle → `fetchById` | Público efectivo para eventos publicados; no publicados limitados por reglas backend |
| `POST /api/v1/inscripciones` | Detalle → `create` | JWT, rol USUARIO; CrearInscripcionRequest `{eventoId}` |
| `GET /api/v1/inscripciones/mis-inscripciones` | Mis inscripciones/detalle/historial | JWT, rol USUARIO; datos propios |
| `PATCH /api/v1/inscripciones/{id}/cancelar` | Mis inscripciones → `cancel` | JWT, rol USUARIO; ownership/reglas backend |
| `GET /api/v1/inscripciones/{id}/comprobante` | Gestión de pago → `fetchRegistrationReceipt` | JWT, rol USUARIO; JSON de constancia |
| `GET /api/v1/pagos/mis-pagos` | Gestión de pago/historial → `fetchMine` | JWT, rol USUARIO; datos propios |
| `POST /api/v1/pagos/{id}/comprobante` | Gestión de pago → `uploadReceipt` | JWT, rol USUARIO; multipart `archivo` |
| `GET /api/v1/pagos/{id}/comprobante` | Gestión de pago → `downloadReceipt` | JWT y ownership backend; bytes |
| `GET /api/v1/eventos/{id}/qr-pago` | Gestión de pago → `downloadEventPaymentQr` | Público efectivo para evento publicado; imagen binaria |
| `GET /api/v1/eventos/{eventoId}/sesiones` | Sesiones de asistencia → `fetchSessions` | JWT; backend autoriza roles y alcance |
| `POST /api/v1/asistencias` | Scanner → `register` | JWT, rol USUARIO; token requerido y latitud/longitud/precisión opcionales |
| `GET /api/v1/asistencias/mis-asistencias` | Historial → `fetchMine` | JWT, rol USUARIO; datos propios |
| `GET /api/v1/certificados/mis-certificados` | Mis certificados/historial | JWT, rol USUARIO; exclusivamente propios |
| `GET /api/v1/certificados/{id}` | Detalle → `fetchById` | JWT; ownership comprobado por backend |
| `GET /api/v1/certificados/{id}/descargar` | Gestor PDF → `downloadPdf` | JWT; ownership comprobado por backend; PDF binario |
| `GET /api/v1/certificados/verificar/{codigo}` | Verificador público → `verifyPublic` | Público efectivo; Flutter omite Bearer |

**Discrepancia OpenAPI/backend — MEDIO:** OpenAPI publicado en local agrega `bearerAuth` a operaciones marcadas con `@SecurityRequirements` y permitidas por `SecurityConfig` como públicas (catálogo, detalle público, QR de pago de evento publicado y verificación de certificado). La petición real de catálogo y verificación pública desde Flutter funcionó sin Bearer. Flutter sigue el permiso efectivo del backend; no se modificó backend/OpenAPI.

No se encontraron prefijos `/v1` duplicados, métodos/rutas obsoletos ni endpoints Flutter ausentes en OpenAPI. Los endpoints de administración (crear/publicar eventos, generar QR de asistencia, validar pagos, emitir certificados y aprobar roles) no son llamados por el cliente.

## 5. Matriz por dominio

| Dominio | Implementado | Contrato real | Tests | Validación física | Observaciones |
|---|---|---|---|---|---|
| USUARIO | IMPLEMENTADO | PROBADO LOCALMENTE | PROBADO LOCALMENTE | PROBADO LOCALMENTE | Sesión restaurada en A125M; login, registro y recuperación no se reejecutaron |
| CATEGORÍA | IMPLEMENTADO | PROBADO LOCALMENTE | PROBADO LOCALMENTE | PENDIENTE DE PRUEBA | Catálogo usa categorías activas; no se repitió interacción de filtro |
| EVENTO | IMPLEMENTADO | PROBADO LOCALMENTE | PROBADO LOCALMENTE | PROBADO LOCALMENTE | Catálogo y detalle cargaron en A125M |
| INSCRIPCIÓN | IMPLEMENTADO | PROBADO LOCALMENTE | PROBADO LOCALMENTE | PROBADO LOCALMENTE | Lista propia y estados cargaron; no se creó ni canceló ninguna |
| PAGO | IMPLEMENTADO | PROBADO LOCALMENTE | PROBADO LOCALMENTE | PROBADO LOCALMENTE | Monto/estado de pago preexistente visible; sin upload ni modificación |
| SESIÓN | IMPLEMENTADO | PROBADO LOCALMENTE | PROBADO LOCALMENTE | PENDIENTE DE PRUEBA | E3.6-F.1 continúa pendiente de sesiones reales; no se generaron sesiones |
| ASISTENCIA | IMPLEMENTADO | PROBADO LOCALMENTE | PROBADO LOCALMENTE | PENDIENTE DE PRUEBA | Sin nuevo escaneo ni cambios; AT-01…AT-07 previos permanecen pendientes |
| CERTIFICADO | IMPLEMENTADO | PROBADO LOCALMENTE | PROBADO LOCALMENTE | PROBADO LOCALMENTE | Lista real vacía y G-07 sin registro; detalle/PDF pendientes por falta de certificado |

DTOs revisados: `AuthSession`, `AuthUser`, `CategoriaEvento`, `Evento`, `Inscripcion`, `Pago`, `SesionEvento`, `Asistencia`, `Certificado` y `VerificacionCertificado`. Los requests del OpenAPI coinciden con los campos enviados: login `correoElectronico/contrasena`; registro `confirmacionContrasena`; recuperación/reset con los nombres especificados por DTO; inscripción `eventoId`; asistencia `token` y coordenadas/precisión opcionales; cambio de perfil/contraseña con DTO real. No se halló incompatibilidad de campos que requiera corrección.

## 6. Autenticación

IMPLEMENTADO — Login, registro, verificación, reenvío, recuperación, perfil, cambio de contraseña, restauración, logout y respuesta global 401 pasan por `AuthService`/`SessionController`/`ApiService`. La sesión se valida de nuevo mediante `GET /usuarios/perfil`; cambio de contraseña y logout limpian almacenamiento local. Las solicitudes protegidas usan Authorization Bearer.

PROBADO LOCALMENTE — Búsqueda estática de `print`, `debugPrint`, logger y literales con forma de JWT/secretos no encontró resultados en el código Flutter. No se encontraron archivos `.env`, keystore ni `key.properties` con credenciales Flutter/Android en el árbol auditado. No se imprimen contraseñas ni tokens.

## 7. Roles

VERIFICADO — Las acciones visibles de participante se condicionan a `USUARIO`. Para otros roles, Home muestra el aviso de canal móvil participante y no agrega paneles de organizador/administrador. No hay llamadas administrativas en los repositorios.

## 8. Eventos e inscripciones

IMPLEMENTADO — La búsqueda y filtros usan los cuatro query params reales del backend; categorías salen de `/categorias-evento/activas`. Detalle representa campos de `EventoResponse`/`EventoDetalleResponse`. Flutter deshabilita intentos obvios según estado local del evento, pero inscripción, capacidad, duplicidad, cancelación y estado definitivo los decide backend.

PROBADO LOCALMENTE — En A125M cargaron catálogo publicado, detalle y Mis inscripciones desde backend. El smoke test fue de lectura: no se pulsó Inscribirme ni cancelar.

## 9. Pagos

IMPLEMENTADO — El monto y estado salen de `PagoResponse`; no hay gateway, cálculo local de monto ni credenciales de pago. Multipart usa el campo `archivo` y los límites/tipos auditados para el backend. Comprobante de pago, QR de pago y constancia de inscripción mantienen rutas y formatos separados. El QR se obtiene por Spring, no desde Storage en Flutter.

PROBADO LOCALMENTE — En A125M se consultó la pantalla de un pago demo ya existente, donde se mostraron el monto devuelto por backend y estado APROBADO. No se subió, abrió, guardó ni alteró comprobante. La fila previamente descrita en E3.6-E.1 como contaminada apareció solo en Historial/Mis inscripciones; no se abrió ni modificó.

## 10. Asistencia

IMPLEMENTADO — Flutter obtiene sesiones por evento, escanea el valor QR completo y envía `token`; GPS solo se captura cuando la sesión lo requiere. No calcula distancia, elegibilidad ni duplicidad y no envía `userId`. El servidor decide resultado, ownership y estado.

PENDIENTE DE PRUEBA — Se preserva E3.6-F.1: no había sesiones legítimas disponibles y no se fabricaron QR ni asistencias.

## 11. Certificados e historial

IMPLEMENTADO — PDF usa bytes, MIME, Content-Disposition y validación `%PDF-`; no convierte JSON a PDF. Verificación pública usa el contrato público. Historial combina inscripciones, pagos, asistencias y certificados propios sin endpoint agregado inventado y conserva la distinción entre tipos.

PROBADO LOCALMENTE — En A125M Mis certificados presentó vacío real. Historial mostró inscripciones y pagos diferenciados; no se observaron asistencias ni certificados. G-01/G-06/G-07 también constan como probados físicamente en E3.6-G.1. Detalle y descarga PDF permanecen pendientes por ausencia de certificado real.

## 12. Seguridad

VERIFICADO — No hay acceso Flutter→PostgreSQL ni credenciales Supabase. Consultas propias no reciben `userId` de UI; el servidor aplica ownership en recursos identificados por id. Ningún token QR se registra por código. Los documentos privados se descargan desde endpoints backend autenticados.

## 13. API_BASE_URL

IMPLEMENTADO — `lib/config/app_config.dart` es la única fuente de configuración. Por defecto usa `http://10.0.2.2:8080/api/v1` en Android/emulador y localhost para otras plataformas. El A125M usa `API_BASE_URL` de LAN suministrada en build, sin guardar la IP en código. Release rechaza HTTP en runtime y el build de auditoría recibió `https://trabajo-final-diplomado.onrender.com/api/v1`.

**Riesgo medio:** `EventoResponse.imagenPortada` es una URL recibida desde backend y se carga con `Image.network`; backend valida esquemas HTTP y HTTPS, pero el manifest de release no habilita cleartext. Las imágenes con URL HTTP pueden no verse en Android release. No se habilitó HTTP globalmente para release; queda pendiente confirmar que datos de producción usen HTTPS.

## 14. Dependencias

VERIFICADO — Las dependencias directas de `pubspec.yaml` tienen uso: provider, http/http_parser, secure storage, file_picker, open_filex, path_provider, geolocator y mobile_scanner; `flutter_launcher_icons` está asociado a su configuración de recursos. No se encontró duplicación evidente ni se actualizaron versiones en masa.

**Aviso medio de toolchain:** Gradle reportó que la aplicación/plugins `file_picker` y `package_info_plus` aún aplican Kotlin Gradle Plugin de forma que futuras versiones de Flutter podrían dejar de compilar. También hubo warnings de opciones Java 8 obsoletas/deprecadas en dependencias Android. Los builds actuales sí terminaron.

## 15. Navegación y UI/UX

IMPLEMENTADO — Home expone eventos, inscripciones, certificados e historial para USUARIO. Detalle conecta a pago/asistencia según estado real; historial navega a inscripción, pago asociado o certificado cuando tiene referencia propia. Pantallas usan carga, vacío, error/reintento y estados de botón.

**Riesgo medio:** `ApiService` no define timeout para peticiones HTTP. Si la conexión queda abierta sin respuesta, algunos loaders pueden permanecer hasta que cierre el socket o responda servidor. No se cambió globalmente porque aplicar un timeout a POST/upload puede dejar resultado remoto indeterminado y requiere política de retry por operación.

## 16. Tests

PROBADO LOCALMENTE — `flutter test`: 82/82 aprobados. Se añadió una prueba dirigida para que el detalle vuelva a consultar el estado del certificado después de descargar el PDF. La suite cubre auth/sesión, contratos y errores de dominios, multipart/archivos, scanner mediante mocks, historial, certificado binario y pantallas de pago. Los tests no sustituyen QR/GPS/PDF físico ni escenarios backend sin datos elegibles.

## 17. Analyze

PROBADO LOCALMENTE — `flutter analyze`: `No issues found`.

## 18. Builds

PROBADO LOCALMENTE — `flutter build apk --debug --dart-define=API_BASE_URL=http://192.168.100.131:8080/api/v1` terminó correctamente; APK en `flutter/VidiaApp/build/app/outputs/flutter-apk/app-debug.apk` (aprox. 214.8 MB). Fue instalado en SM A125M.

PROBADO LOCALMENTE — `flutter build apk --release --dart-define=API_BASE_URL=https://trabajo-final-diplomado.onrender.com/api/v1` terminó correctamente; APK en `flutter/VidiaApp/build/app/outputs/flutter-apk/app-release.apk` (69,718,304 bytes / 66.5 MiB). No fue publicado ni instalado.

**Hallazgo alto de firma:** no existe `android/key.properties`; `android/app/build.gradle.kts` asigna la firma debug al tipo release cuando falta. El artefacto sirve como verificación de compilación/configuración HTTPS, pero no como release distribuible. No se cambió porque la firma de producción requiere un keystore/alias seguro que no existe en el entorno y no debe inventarse.

## 19. Producción

PENDIENTE DE PRODUCCIÓN — Se intentaron `GET https://trabajo-final-diplomado.onrender.com/v3/api-docs` y `GET https://trabajo-final-diplomado.onrender.com/api/v1/eventos/publicados`; ambos expiraron por timeout del entorno. El navegador de verificación tampoco pudo acceder. No se afirma funcionamiento de Render.

## 20. Smoke test Android

PROBADO LOCALMENTE — 2026-10-06, SM A125M Android 12, pantalla visible y desbloqueada: APK debug final instalado; sesión participante restaurada; Home, catálogo, Mis certificados (vacío real) e Historial cargaron desde backend local. Historial distinguió visualmente pagos e inscripciones y mostró sus estados. También se inspeccionó el detalle de evento y la pantalla de pago ya existente en el smoke de lectura. No hubo POST de inscripción, cancelación, upload, generación de certificado, sesión o asistencia.

Las pantallas mostraron eventos/datos de demostración preexistentes devueltos por backend; esta auditoría no los creó ni alteró. El registro de pago previamente contaminado de E3.6-E.1 apareció como fila de actividad y no se abrió ni modificó.

## 21. Problemas, correcciones, pendientes y estado final

### Problemas por severidad

- **CRÍTICO:** ninguno detectado.
- **ALTO:** firma debug como fallback para APK release cuando no hay `key.properties`; requiere configurar firma de distribución por canal seguro antes de cualquier publicación.
- **MEDIO:** OpenAPI expone `bearerAuth` en rutas que el backend permite públicas; peticiones Flutter funcionan según SecurityConfig, pero el contrato generado puede inducir a error a otros clientes.
- **MEDIO:** falta timeout de red en `ApiService`, con riesgo de carga indefinida.
- **MEDIO:** URLs de imagen HTTP permitidas por backend pueden fallar en Android release porque cleartext está permitido solo en manifest debug.
- **MEDIO:** advertencia futura de migración Kotlin Gradle Plugin por plugins y avisos de opciones Java obsoletas/deprecadas.
- **BAJO:** Render no se pudo alcanzar desde este entorno; la verificación sigue pendiente, no es un fallo confirmado del servicio.
- **INFORMATIVO:** E3.6-E.1 upload/rechazo/reenvío limpio y E3.6-F.1 sesiones/asistencia continúan pendientes por falta de escenarios legítimos; no se reintentaron ni manipularon.

### Correcciones realizadas

VERIFICADO — Se corrigió un defecto puntual en Flutter: tras una descarga o previsualización PDF exitosa, el detalle consulta de nuevo el certificado para reflejar el estado devuelto por backend (por ejemplo, `DESCARGADO`). La descarga conserva su resultado incluso si el refresco falla. Se añadió una prueba widget dirigida. Backend y OpenAPI no se modificaron. No se hizo commit ni push.

Archivos tocados en E3.6-H:

- `flutter/VidiaApp/lib/screens/certificate_detail_screen.dart` — corrección del refresco del estado tras descargar/abrir el PDF (archivo Flutter preexistente en el working tree).
- `flutter/VidiaApp/test/certificate_detail_screen_test.dart` — prueba dirigida nueva.
- `docs/auditoria/E3_6_H_AUDITORIA_FLUTTER.md` — este informe.

### Estado final

VERIFICADO — Los endpoints Flutter auditados existen en backend/OpenAPI local; `flutter analyze` no tiene issues, los 82 tests pasan y los builds debug/release compilan con el código final. El smoke de lectura Android observó restauración, catálogo, detalle, pagos, certificados vacío e historial. El nuevo refresco de estado está cubierto por test automatizado, no por interacción física, porque la cuenta no tiene un certificado real para descargar.

PENDIENTE DE PRODUCCIÓN — Render no respondió antes del timeout.

PENDIENTE DE PRUEBA — Certificado real/PDF, escenarios de pago controlado y asistencia con sesión real siguen sujetos a los pendientes E3.6-E.1, E3.6-F.1 y E3.6-G.1.
