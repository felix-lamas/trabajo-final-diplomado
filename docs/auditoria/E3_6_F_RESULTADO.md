# E3.6-F — Resultado

## 1. Objetivo

Implementar en Flutter el flujo participante para consultar sesiones propias, escanear el QR temporal, capturar GPS cuando la sesión lo requiere, enviar los datos al backend y presentar la respuesta. Estado de implementación: **IMPLEMENTADO**. La ejecución funcional en Android con QR y sesión reales continúa **PENDIENTE DE PRUEBA**.

## 2. Backend auditado

Se revisaron los controllers, DTOs y servicios Spring Boot de sesiones, QR de asistencia y asistencia, además del contrato OpenAPI expuesto por el backend local.

## 3. Endpoints reales

| Operación | Método y ruta | Acceso/contrato | Respuesta y errores relevantes |
|---|---|---|---|
| Listar sesiones del evento | `GET /api/v1/eventos/{eventoId}/sesiones` | Bearer; roles ADMINISTRADOR, ORGANIZADOR o USUARIO. El participante requiere acceso válido/inscripción confirmada según la regla del backend. | `200`, lista `SesionEventoResponse`; `401`, `403`, `404`. |
| Consultar sesión | `GET /api/v1/sesiones/{id}` | Bearer; los roles anteriores, con alcance/visibilidad aplicada por backend. Flutter no necesita invocarlo en el recorrido implementado. | Response `SesionEventoResponse`; `404` si no existe o no es visible. |
| Emitir QR temporal | `POST /api/v1/sesiones/{sesionId}/qr/generar` | Bearer; únicamente ADMINISTRADOR/ORGANIZADOR. No se invoca desde la app participante. | `201`; token entregado solo al generar. Vigencia máxima de dos minutos, limitada al fin de la sesión. La rotación revoca el QR previo. |
| Consultar metadatos de QR activo | `GET /api/v1/sesiones/{sesionId}/qr` | Bearer; únicamente ADMINISTRADOR/ORGANIZADOR. No se invoca desde Flutter participante y no vuelve a exponer el token. | `200`, `400`, `401`, `403`, `404`. |
| Registrar asistencia propia | `POST /api/v1/asistencias` | Bearer; rol USUARIO. JSON con `token` requerido (máximo 200 caracteres), `latitud`, `longitud` y `precision` opcionales en DTO y obligatorios por regla de servicio cuando la sesión exige GPS. No se envía `sessionId`, `eventoId` ni `userId`. | `201 AsistenciaResponse`; `400` para QR/sesión/inscripción/GPS inválidos; `401`, `403`; `409 ATTENDANCE_DUPLICATED`. |
| Consultar asistencias propias | `GET /api/v1/asistencias/mis-asistencias` | Bearer; rol USUARIO. Devuelve solo registros del usuario autenticado. | `200`, `401`, `403`. |

El backend identifica sesión por el token y usuario por el JWT. Valida publicación, ventana temporal, estado de sesión, inscripción confirmada, expiración/revocación del QR, precisión GPS (máximo 30 m cuando aplica), radio y duplicidad. Flutter no calcula ni decide estos resultados. Los códigos de negocio incluyen `QR_EXPIRED`, `QR_REVOKED`, `QR_INVALID`, `INSCRIPTION_REQUIRED`, `INSCRIPTION_NOT_CONFIRMED`, `ATTENDANCE_GPS_INVALID`, `ATTENDANCE_GPS_ACCURACY_INVALID`, `ATTENDANCE_OUTSIDE_RADIUS`, `ATTENDANCE_SESSION_INACTIVE`, `ATTENDANCE_SESSION_NOT_REQUIRED`, `ATTENDANCE_OUTSIDE_WINDOW`, `EVENT_NOT_PUBLISHED` y `ATTENDANCE_DUPLICATED`.

## 4. Archivos modificados

Implementación E3.6-F en Flutter:

- `flutter/VidiaApp/lib/models/sesion_evento.dart`
- `flutter/VidiaApp/lib/models/asistencia.dart`
- `flutter/VidiaApp/lib/repositories/asistencia_repository.dart`
- `flutter/VidiaApp/lib/services/attendance_location_source.dart`
- `flutter/VidiaApp/lib/services/asistencia_error_message.dart`
- `flutter/VidiaApp/lib/services/asistencia_flow.dart`
- `flutter/VidiaApp/lib/screens/attendance_sessions_screen.dart`
- `flutter/VidiaApp/lib/screens/attendance_scanner_screen.dart`
- `flutter/VidiaApp/lib/main.dart`
- `flutter/VidiaApp/lib/screens/event_detail_screen.dart`
- `flutter/VidiaApp/android/app/src/main/AndroidManifest.xml`
- `flutter/VidiaApp/pubspec.yaml`
- `flutter/VidiaApp/pubspec.lock`
- `flutter/VidiaApp/test/asistencia_repository_test.dart`
- `flutter/VidiaApp/test/attendance_scanner_screen_test.dart`

También hay cambios previos de E3.6-B/C/D/E y otras fases en el working tree; no se atribuyen a E3.6-F ni se revirtieron.

## 5. Dependencias

- `mobile_scanner ^7.4.2`: escáner QR.
- `geolocator 14.0.2`: permisos/servicios y posición GPS.

`geolocator` se fijó en 14.0.2 para resolver la restricción transitiva de `win32` con `file_picker 10.3.7`; `flutter pub get` terminó correctamente. No se agregó otra dependencia para permisos.

## 6. Permisos Android

Se declararon `CAMERA`, `ACCESS_COARSE_LOCATION` y `ACCESS_FINE_LOCATION`. No se solicita ubicación en segundo plano. La pantalla explica el uso de cámara antes de activarla y solicita confirmación/explica el uso de ubicación para sesiones con coordenadas; luego Geolocator consulta/solicita el permiso del sistema. Permisos configurados: **IMPLEMENTADO**. Aceptación/denegación visible en dispositivo: **PENDIENTE DE PRUEBA**.

## 7. Flujo implementado

Desde el detalle de evento con inscripción propia `CONFIRMADA`, la persona abre “Asistencia y sesiones”. Flutter carga la lista de sesiones y sus propias asistencias; muestra sesión registrada, sesiones sin requisito, no disponibles, vacío, error y reintento. Para una sesión activa que requiere asistencia se abre el escáner. El contenido QR se conserva íntegro y se envía como `token`; se evita el doble envío, se detiene la cámara durante el procesamiento y se muestran el resultado o el error. Se captura latitud, longitud y precisión para eventos presenciales, y para sesiones virtuales cuando la sesión devuelve coordenadas. Una sesión virtual sin coordenadas envía únicamente el token, conforme a la regla backend.

## 8. Seguridad

Las llamadas protegidas usan el ApiService existente y su Bearer token almacenado por el mecanismo seguro actual. No se agrega `userId`, no se usa endpoint de emisión de QR de organizador, no se conecta directamente a PostgreSQL ni a Supabase y no se registran tokens QR/JWT. `401` sigue el mecanismo global de expiración de sesión. La UI solo presenta las asistencias de `/mis-asistencias`; el backend conserva la autoridad de ownership, geocerca y duplicidad.

## 9. Tests

`flutter test`: **PROBADO LOCALMENTE**, 61/61 aprobados, 0 fallidos. Se añadieron tests de parseo de sesión/asistencia, rutas y payload real, Bearer, códigos 401/403/409/400, flujo GPS/virtual/presencial sin coordenadas de sesión, traducción de errores y widget scanner con dependencias simuladas. Las pruebas de widget no acreditan funcionamiento físico de cámara/GPS.

## 10. flutter analyze

`flutter analyze`: **PROBADO LOCALMENTE**, sin issues (6 de octubre de 2026).

## 11. APK

`flutter build apk --debug --dart-define=API_BASE_URL=http://192.168.100.131:8080/api/v1`: **PROBADO LOCALMENTE**, compilación exitosa. Artefacto: `flutter/VidiaApp/build/app/outputs/flutter-apk/app-debug.apk` (debug; no es release). El APK final fue instalado por ADB en el SM A125M (`Performing Streamed Install: Success`). El build mostró avisos de compatibilidad futura del plugin Kotlin y avisos de API/deprecación de dependencias; no bloquearon la compilación.

## 12. Validación física

Dispositivo conectado: SM A125M (`R58R240RCWH`). La pantalla quedó en bloqueo/Keyguard durante la comprobación automatizada. El intento de volcar la jerarquía devolvió `null root node`; no se pudo observar Vidia ni verificar el flujo autenticado. No se afirma que login, cámara, permisos, GPS, lectura QR ni envío backend funcionen físicamente. Estado: **PENDIENTE DE PRUEBA**.

La configuración de compilación apunta a la dirección LAN indicada para backend local; no se cambió `API_BASE_URL` en código. La disponibilidad de una sesión vigente y un QR emitido desde el flujo de organizador tampoco se pudo comprobar en Android.

## 13. Casos AT-01…AT-07

| Caso | Resultado | Evidencia/razón |
|---|---|---|
| AT-01 QR válido e inscripción confirmada | PENDIENTE DE PRUEBA | No se observó el app desbloqueada ni se dispuso de una sesión/QR real validado en el recorrido Android. |
| AT-02 reescaneo/duplicado | PENDIENTE DE PRUEBA | No se fabricó asistencia ni duplicado; el mapeo `409 ATTENDANCE_DUPLICATED` sí se cubre en test automatizado. |
| AT-03 QR expirado | PENDIENTE DE PRUEBA | Rechazo backend `QR_EXPIRED` cubierto en test automatizado; sin QR real expirado para prueba física. |
| AT-04 ubicación fuera del radio | PENDIENTE DE PRUEBA | Mensaje y código backend cubiertos en test; no se modificó ubicación ni sesión para fabricar el escenario. |
| AT-05 permiso de ubicación denegado | PENDIENTE DE PRUEBA | Captura de permisos y mensaje implementados; no se pudo completar interacción Android. |
| AT-06 permiso de cámara denegado | PENDIENTE DE PRUEBA | Manejo del error de permiso implementado; no se pudo completar interacción Android. |
| AT-07 usuario sin inscripción confirmada | PENDIENTE DE PRUEBA | La regla real `INSCRIPTION_REQUIRED`/`INSCRIPTION_NOT_CONFIRMED` está mapeada; no se usó una cuenta/escenario artificial. |

## 14. Pendientes

- Desbloquear y operar Vidia en el SM A125M o un Android disponible.
- Confirmar sesión vigente y QR temporal real mostrado desde la interfaz de organizador.
- Ejecutar y observar AT-01 y, solo con escenario natural, AT-02…AT-07.
- Verificar permisos de cámara/GPS y comportamiento en sesión física con ubicación requerida.

## 15. Riesgos

- La vigencia máxima del QR es de dos minutos; una prueba manual debe coordinarse con su emisión real y con la ventana de la sesión.
- Sesiones del participante solo son visibles según reglas backend y la inscripción debe estar confirmada. Una lista vacía o un 404 no debe interpretarse como fallo de la app sin revisar esa autorización.
- Compilación debug validada; no se realizó release ni publicación.

## 16. Estado final

- Integración Flutter y contratos auditados: **IMPLEMENTADO**.
- Tests y análisis estático: **PROBADO LOCALMENTE**.
- APK debug: **PROBADO LOCALMENTE**.
- Ejecución física completa QR/GPS/backend: **PENDIENTE DE PRUEBA**.
- Backend, OpenAPI, monografía, producción, Render y base de datos: sin modificaciones intencionales.
- Commit/push: no realizados.
