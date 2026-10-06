# E3.6-C — Resultado

Fecha de verificación: 2026-10-05. Alcance: cliente Flutter de autenticación y sesión. El backend fue tratado como fuente de verdad; no se modificó.

## 1. Resumen

Se adaptó Flutter al contrato real de autenticación de Spring Boot: login, almacenamiento seguro, validación de sesión mediante perfil, cierre de sesión con revocación, invalidación global por HTTP 401, registro, verificación, reenvío de verificación, recuperación/restablecimiento de contraseña, perfil, cambio de contraseña y presentación de los tres roles. Se agregó manejo visual de una solicitud de organizador pendiente sin elevar el rol local.

Validación: `flutter analyze` sin issues; `flutter test` con 21 pruebas aprobadas; APK Android debug compilado. OpenAPI local y catálogo público respondieron HTTP 200. La integración desde Android Emulator queda **PENDIENTE POR ADB NO AUTORIZADO** (`emulator-5554`); el endpoint OpenAPI Render no respondió dentro del timeout, por lo que producción queda **NO VERIFICADA**. La fase no se declara completamente aceptada hasta realizar esas pruebas de integración.

## 2. Auditoría inicial

Se inspeccionaron los DTO, controladores, servicios, seguridad de sesión y el cliente Flutter antes de implementar.

Hallazgos previos en Flutter:

- Ya existían `ApiConfig` con `--dart-define=API_BASE_URL`, `ApiService`, `AuthService`, `SecureTokenStore` basado en `flutter_secure_storage`, `SessionController`, login y pantalla principal.
- El login ya llamaba al endpoint real y guardaba el JWT en almacenamiento seguro.
- La restauración confiaba únicamente en token y perfil cacheados, sin consultar al servidor.
- El logout solo borraba almacenamiento local; no revocaba la sesión en backend.
- Existía manejo parcial de HTTP 401, pero sin protección explícita contra múltiples redirecciones simultáneas.
- No existían pantallas móviles de registro/verificación/recuperación/perfil ni manejo de navegación por rol.
- `flutter_secure_storage` ya estaba en `pubspec.yaml`; no se agregaron dependencias.

## 3. Contrato backend utilizado

El contrato se contrastó con `http://localhost:8080/v3/api-docs` (HTTP 200, 115776 bytes) y el código de `AutenticacionControlador`, `UsuarioControlador`, `AutenticacionServicio`, `SesionUsuarioServicio` y sus DTO.

- Login: `POST /api/v1/auth/login`, JSON `{correoElectronico, contrasena}`. Respuesta `{token, usuario}`. Credenciales incorrectas: 401 `AUTH_INVALID_CREDENTIALS`; correo no verificado: 400 `EMAIL_NOT_VERIFIED`.
- Registro: `POST /api/v1/auth/registro`. Campos: `nombres`, `apellidos`, `correoElectronico`, `ci`, `ru`, `celular`, `contrasena`, `confirmacionContrasena`, `tipoUsuario`. `tipoUsuario` acepta `INTERNO` o `EXTERNO`; RU es obligatorio para `INTERNO` y debe omitirse/vaciarse para `EXTERNO`. La cuenta se crea como `USUARIO`, con correo sin verificar y sin emitir JWT.
- Verificación: `POST /api/v1/auth/verificar-correo`, `{token}`. Token de un solo uso con vigencia de 24 horas; invalidez, expiración o reutilización responden 400 con código funcional.
- Reenvío: `POST /api/v1/auth/reenviar-verificacion`, `{correoElectronico}`; 200 no enumerativo.
- Recuperación: `POST /api/v1/auth/recuperar-contrasena`, `{correoElectronico}`; respuesta 200 no revela si existe la cuenta.
- Restablecimiento: `POST /api/v1/auth/restablecer-contrasena`, `{token, nuevaContrasena, confirmacion}`; token de un solo uso, vigencia de 30 minutos; token inválido/usado/expirado o contraseña no válida: 400.
- Logout: `POST /api/v1/auth/logout`, autenticado, sin cuerpo; 204. El identificador `jti` del JWT corresponde a una sesión persistida y revocable. El filtro rechaza sesiones revocadas/expiradas con 401.
- Perfil: `GET /api/v1/usuarios/perfil`; devuelve `id`, nombres, apellidos, correo, `correoVerificado`, CI, RU, celular, `tipoUsuario`, `estadoSolicitudOrganizador` y `roles`.
- Cambio de contraseña: `POST /api/v1/usuarios/cambiar-contrasena`, `{contrasenaActual, nuevaContrasena, confirmacion}`; 200 y revocación de todas las sesiones, incluido el JWT actual.
- Estado de organizador: el perfil informa `NINGUNA`, `PENDIENTE`, `APROBADA` o `RECHAZADA`. Solicitar organizador es `POST /api/v1/usuarios/solicitud-organizador` para `USUARIO`; las operaciones de revisión son exclusivas de administrador y no se incorporan como interfaz de gestión en esta fase.

Los errores siguen `ErrorRespuesta`: `codigo`, `mensaje`, `detalles`, `timestamp`, `ruta`. Los códigos de error se preservan en `ApiException`; el cliente presenta el mensaje seguro y usa `EMAIL_NOT_VERIFIED` para dirigir al flujo de verificación.

## 4. Matriz backend vs Flutter

| Funcionalidad | Backend | Flutter actual después de E3.6-C | Diferencia / estado |
|---|---|---|---|
| Login | `POST /auth/login`; token + perfil; 400/401 | Envía DTO real, persiste JWT seguro y distingue correo sin verificar/credenciales incorrectas | COMPATIBLE; integración móvil pendiente |
| Registro | `POST /auth/registro`; `INTERNO`/`EXTERNO`; rol inicial USUARIO; correo no verificado | Formulario envía campos del DTO; no inicia sesión automáticamente | COMPATIBLE |
| Verificación | `POST /auth/verificar-correo`, token recibido por email | Formulario de ingreso manual del token | Compatible con API; enlace profundo móvil no implementado |
| Reenvío verificación | `POST /auth/reenviar-verificacion`, correo | Acción no enumerativa y mensaje genérico | COMPATIBLE |
| Recuperación | `POST /auth/recuperar-contrasena`, correo; 200 genérico | Solicitud presenta mensaje no enumerativo | COMPATIBLE |
| Restablecimiento | `POST /auth/restablecer-contrasena`, token y dos campos de contraseña | Formulario token/nueva contraseña/confirmación | COMPATIBLE; token se ingresa manualmente |
| Perfil | `GET /usuarios/perfil` autenticado | Carga real, presentación de datos y refresco | COMPATIBLE |
| Cambio de contraseña | `POST /usuarios/cambiar-contrasena`; revoca sesiones | Envía DTO real y elimina sesión local tras éxito | COMPATIBLE |
| Logout | `POST /auth/logout` autenticado; 204/revocación | Intenta revocar servidor y borra local incluso si falla conectividad | COMPATIBLE |
| Sesión revocada | Filtro backend responde 401 | Limpia sesión, coalesce 401 concurrentes y reinicia navegación | COMPATIBLE en unit tests; integración pendiente |
| Roles | Tres roles oficiales; autorización servidor | Lee roles del perfil; interfaz móvil de participante solo para USUARIO | COMPATIBLE; frontend no se trata como seguridad |
| Solicitud organizador | Perfil incluye estado; solicitud solo para USUARIO | Muestra estado pendiente sin asignar ORGANIZADOR ni habilitar sus funciones | COMPATIBLE |

## 5. Archivos modificados

Archivos Flutter modificados en E3.6-C:

- `flutter/VidiaApp/lib/app.dart`
- `flutter/VidiaApp/lib/controllers/session_controller.dart`
- `flutter/VidiaApp/lib/main.dart`
- `flutter/VidiaApp/lib/models/auth_user.dart`
- `flutter/VidiaApp/lib/screens/auth_gate.dart`
- `flutter/VidiaApp/lib/screens/home_screen.dart`
- `flutter/VidiaApp/lib/screens/login_screen.dart`
- `flutter/VidiaApp/lib/services/api_exception.dart`
- `flutter/VidiaApp/lib/services/api_service.dart`
- `flutter/VidiaApp/lib/services/auth_service.dart`
- `flutter/VidiaApp/test/api_error_test.dart`
- `flutter/VidiaApp/test/auth_service_test.dart`
- `flutter/VidiaApp/test/session_controller_test.dart` (nuevo)
- `flutter/VidiaApp/lib/screens/auth_flow_screens.dart` (nuevo)
- `flutter/VidiaApp/lib/screens/profile_screen.dart` (nuevo)
- `docs/auditoria/E3_6_C_RESULTADO.md` (este informe)

No se tocaron backend, monografía, configuración Render, `pubspec.yaml`, servicios de eventos/inscripciones ni la configuración E3.6-B de ambientes. `README.md`, `app_config.dart` y `android/app/src/debug/AndroidManifest.xml` aparecen modificados en el working tree por E3.6-B anterior; no se editaron en E3.6-C y se conservaron.

## 6. Login

El login mantiene `POST /auth/login` con los nombres exactos de campos. Nunca almacena contraseña ni escribe JWT en logs. El JWT se pasa a `SecureTokenStore`; se guarda mediante `flutter_secure_storage`. Los errores muestran credenciales incorrectas o sesión expirada según contexto, y el código real `EMAIL_NOT_VERIFIED` dirige a la pantalla de verificación/recuperación.

## 7. Persistencia segura

Se reutilizó `SecureTokenStore` existente, sustentado por `flutter_secure_storage` ya declarado. El token y el perfil no sensible serializado se guardan allí; no se utiliza `SharedPreferences`. El perfil cacheado solo sirve para persistencia, nunca para validar autenticación. No se introdujeron secretos ni almacenamiento de contraseña.

## 8. Restauración de sesión

Al iniciar la app, `AuthGate` muestra carga mientras `SessionController.restore()` comprueba el token y consulta `/usuarios/perfil`. Respuesta 200 restaura perfil/roles desde backend y actualiza caché. Un 401 elimina sesión. Ante error de conexión, la app no muestra contenido autenticado basado en caché: conserva el token para reintento y ofrece reintentar o cerrar sesión localmente.

## 9. Logout

Logout llama al endpoint real de revocación y limpia siempre la sesión local incluso si la red falla. Si el servidor no confirma el cierre, la interfaz comunica que solo pudo cerrar la sesión local. No se inventó un endpoint alternativo.

## 10. Manejo 401

`ApiService` centraliza la expiración de solicitudes autenticadas, elimina el almacenamiento y comparte una única operación de invalidación cuando varios 401 llegan concurrentemente. El estado autenticado se limpia y la navegación se reinicia hacia `AuthGate`; 401 del login público no dispara el logout global.

## 11. Registro

El formulario recoge los campos requeridos por backend. `INTERNO` exige RU; para `EXTERNO` se envía RU vacío, valor permitido por la validación real. Se hace validación preliminar equivalente a campos/formato/fortaleza y se conserva el error del backend. El mensaje posterior refleja que el registro no significa correo verificado ni inicio de sesión.

## 12. Verificación de correo

Se consumen los endpoints reales de verificación y reenvío. El token se ingresa manualmente desde el correo/enlace, no se crea ni valida localmente. Backend define vencimiento y uso único. La app no lo persiste. **Pendiente:** probar entrega real de correo y UX de enlace profundo móvil; el backend actual configura enlaces hacia la web.

## 13. Recuperación de contraseña

La pantalla permite solicitar recuperación y restablecer con token, nueva contraseña y confirmación, usando los cuerpos exactos. La respuesta de solicitud es genérica para no revelar existencia de correo. La aplicación no guarda el token ni contraseñas.

## 14. Perfil

La pantalla consulta perfil actual al backend y presenta solo campos de `PerfilResponse`. Permite cambio de contraseña con los tres campos reales. Tras el éxito borra el token porque el servidor revocó todas las sesiones y vuelve al login.

## 15. Roles

Flutter reconoce exactamente `ADMINISTRADOR`, `ORGANIZADOR` y `USUARIO`. El canal de eventos/inscripciones se presenta únicamente para la sesión que incluye `USUARIO`; para ADMINISTRADOR u ORGANIZADOR se explica que esas funciones móviles no están dentro de este cliente. Esta separación es UX; los permisos siguen siendo del backend.

## 16. Usuario con solicitud de organizador

El perfil backend devuelve el estado. Si es `PENDIENTE`, Flutter lo muestra y mantiene el rol `USUARIO`; no fabrica el rol ORGANIZADOR ni habilita acciones de ese rol. No se agregó revisión/aprobación administrativa.

## 17. Navegación

No autenticado: login, registro y verificación/recuperación. Autenticado: interfaz ajustada según el rol que devolvió backend, acceso a perfil y logout. Restauración sin conectividad no se convierte en sesión autenticada. No se añadieron guards como sustituto de autorización servidor.

## 18. Tests

`flutter analyze`: **OK**, `No issues found`.

`flutter test`: **OK**, 21 pruebas aprobadas, 0 fallidas. Cubren login exitoso/incorrecto/no verificado, persistencia a través del `TokenStore`, registro DTO y rechazo, campos de verificación/recuperación/restablecimiento, perfil y solicitud pendiente, tres roles, sesión restaurada, fallo de red sin autenticar desde caché, logout remoto/local, cambio de contraseña, 401 centralizado y 401 concurrentes.

Son pruebas unitarias/mocks; no equivalen a integración real con SMTP, PostgreSQL, FlutterSecureStorage del dispositivo, Android Emulator ni Render.

## 19. Validación local

- `GET http://localhost:8080/v3/api-docs`: **HTTP 200**, 115776 bytes.
- `GET http://localhost:8080/api/v1/eventos/publicados`: **HTTP 200**, 5834 bytes.
- Son consultas desde el host; no constituyen prueba desde Flutter/Android.
- `flutter devices` mostró un teléfono Android 12 y un AVD Pixel 6a iniciado, pero el emulador `emulator-5554` quedó **not authorized**. No se instaló ni ejecutó la aplicación en Android.
- No se crearon cuentas locales ni se dispararon correos reales para evitar modificar datos sin una cuenta/destino de prueba acordados.
- APK debug compilado con `--dart-define=API_BASE_URL=http://10.0.2.2:8080/api/v1`: **OK**. Advertencia existente de Kotlin Gradle Plugin respecto a futuras versiones de Flutter; no bloquea el build.
- Prueba Flutter → backend local: **PENDIENTE DE PRUEBA DE INTEGRACIÓN**, por falta de ADB autorizado/AVD operativo y cuenta de prueba verificada.

## 20. Pendientes

- Autorizar ADB/emulador y probar registro/verificación/login/logout/restauración/perfil contra backend local con cuenta de prueba autorizada.
- Probar el envío/recepción de email con configuración local segura y verificar el token sin exponerlo en logs.
- Probar cambio de contraseña y revocación con usuario de prueba local.
- Repetir consultas no destructivas a OpenAPI/catálogo Render y después flujos con cuenta de prueba autorizada.
- Evaluar enlaces profundos móviles para los links de verificación/reset que el backend actualmente envía a la web.
- Confirmar con pruebas de integración que `flutter_secure_storage` persiste y borra correctamente en Android real/emulador.

## 21. Riesgos

- La app aún requiere evidencia de integración; un test con `MockClient` solo demuestra el contrato enviado/interpretado en el cliente.
- Si Android Emulator sigue sin autorización, `10.0.2.2` no se ha probado desde la app. El teléfono físico requeriría URL LAN del equipo, no `10.0.2.2`.
- El entorno no pudo conectarse a Render: intento de `https://trabajo-final-diplomado.onrender.com/v3/api-docs` terminó por timeout, incluso en el intento fuera del sandbox. Producción no está verificada.
- No se hizo flujo real de email; registro y recuperación no se ejecutaron contra base de datos.
- Build emite advertencia de migración futura de Kotlin Gradle Plugin; no se cambió por estar fuera de esta fase.

## 22. Estado final

| Criterio | Estado |
|---|---|
| Login y DTO real | OK (unit test) |
| JWT en almacenamiento seguro | OK (SecureTokenStore/flutter_secure_storage); prueba de dispositivo pendiente |
| Restauración validada por backend | OK (unit test); integración local pendiente |
| HTTP 401 y redirección | OK (tests, incluido caso concurrente); integración pendiente |
| Logout alineado a revocación | OK (unit test); backend real pendiente |
| Registro/verificación/recuperación | OK (contratos y unit tests); correo/backend integrado pendiente |
| Perfil/cambio contraseña/roles/estado organizador | OK (contrato/código y unit tests); integración pendiente |
| Analyze / test | OK: 21 tests aprobados |
| Build Android debug | OK |
| Flutter → backend local desde emulador | PENDIENTE POR ADB NO AUTORIZADO |
| Render | NO VERIFICADO (timeout de red) |
| Backend, monografía y configuración de producción modificados | NO; ninguno se modificó |
| Commit / push | No realizados |

**Conclusión:** la implementación cliente de E3.6-C está aplicada y pasa análisis, suite unitaria y build debug. La fase no se declara plenamente verificada/aceptada: quedan pruebas de integración local y producción pendientes por las limitaciones de ADB y conectividad. No se modificó backend, monografía, Render ni secretos.
