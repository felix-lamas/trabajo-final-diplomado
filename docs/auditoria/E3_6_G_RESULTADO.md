# E3.6-G — Resultado

## 1. Objetivo

IMPLEMENTADO — Se añadieron a Flutter las consultas de certificados propios, detalle, descarga binaria PDF, verificación pública por código y un historial de actividad construido con endpoints existentes.

## 2. Auditoría backend

VERIFICADO — Se revisaron controllers, servicios, DTOs, entidades, autorización y OpenAPI local. La elegibilidad y emisión son responsabilidad exclusiva del backend; Flutter no calcula si corresponde emitir un certificado.

La regla observada en `CertificadoService` exige evento `FINALIZADO`, inscripción `CONFIRMADA`, pago `APROBADO` cuando el evento es pagado, sesiones requeridas y asistencia registrada. Para `CURRICULAR` también requiere horas académicas mayores que cero y asistencia de al menos 80%. Para `NO_CURRICULAR` no se observó ese mínimo adicional. La emisión está restringida a ADMINISTRADOR/ORGANIZADOR; Flutter participante no emite certificados.

## 3. Endpoints reales

| Uso | Método y ruta | Acceso / respuesta observada |
|---|---|---|
| Mis certificados | `GET /api/v1/certificados/mis-certificados` | Bearer; lista propia `CertificadoResponse` |
| Detalle propio | `GET /api/v1/certificados/{id}` | Bearer; ownership validado en backend; recurso no visible/no propio responde 404 |
| Descargar | `GET /api/v1/certificados/{id}/descargar` | Bearer; bytes PDF `application/pdf`, `Content-Disposition` con nombre del archivo |
| Verificación pública | `GET /api/v1/certificados/verificar/{codigo}` | Público, sin Bearer; JSON de verificación. Código inexistente responde `valido=false`, `NO_REGISTRADO` |
| Inscripciones propias | `GET /api/v1/inscripciones/mis-inscripciones` | Bearer; fuente de entradas de inscripción del historial |
| Pagos propios | `GET /api/v1/pagos/mis-pagos` | Bearer; fuente de entradas de pago del historial |
| Asistencias propias | `GET /api/v1/asistencias/mis-asistencias` | Bearer; fuente de entradas de asistencia del historial |

VERIFICADO — No existe endpoint agregado de historial en el backend. No se inventó uno. La verificación pública devuelve datos JSON y es independiente de los QR de asistencia y de pago.

## 4. Reglas reales del certificado

VERIFICADO — Los campos de certificado reflejan el DTO real: `id`, `nombreCompleto`, `ru`, `ci`, `evento`, `cargaHoraria`, `tipoCertificado`, `horasAcademicas`, `porcentajeAsistencia`, `codigoCertificado`, `fechaEmision`, `urlVerificacion`, `estado` y `archivoPdfUrl`. Los tipos observados son `CURRICULAR`/`NO_CURRICULAR`; estados `GENERADO`/`DESCARGADO`/`ANULADO`.

## 5. Archivos Flutter modificados

IMPLEMENTADO — Archivos añadidos:

- `lib/models/certificado.dart`
- `lib/repositories/certificado_repository.dart`
- `lib/screens/my_certificates_screen.dart`
- `lib/screens/certificate_detail_screen.dart`
- `lib/screens/public_certificate_verification_screen.dart`
- `lib/screens/history_screen.dart`
- `lib/services/certificado_document_manager.dart`
- `lib/services/certificado_file_service.dart`
- `test/certificado_repository_test.dart`
- `test/certificado_document_manager_test.dart`
- `test/certificate_history_screen_test.dart`

IMPLEMENTADO — Se conectaron proveedores en `lib/main.dart`, las entradas de navegación en `lib/screens/home_screen.dart`, y `Content-Disposition` en la respuesta binaria de `lib/services/api_service.dart`. Se agregaron `open_filex` y `path_provider` a `pubspec.yaml`/`pubspec.lock`; `file_picker` ya estaba presente y se reutiliza para guardar el PDF.

## 6. Dependencias

IMPLEMENTADO — Se reutiliza `file_picker 10.3.7` para el selector nativo de destino y se añadieron `open_filex ^4.7.0` y `path_provider ^2.1.6` para abrir un PDF temporal en Android. `flutter pub get` terminó correctamente. No se agregó dependencia de almacenamiento remoto.

## 7. Pantalla Mis certificados

IMPLEMENTADO — Consulta exclusivamente `mis-certificados`, presenta evento, tipo, fecha, horas, código y estado cuando esos valores vienen en la respuesta. Tiene carga, lista, vacío, error y reintento; abre el detalle y ofrece acceso al verificador público.

## 8. Detalle

IMPLEMENTADO — Vuelve a consultar el certificado por su id y muestra los campos de detalle disponibles. Distingue el certificado de la constancia de inscripción y del comprobante de pago. La descarga/consulta de detalle depende de ownership validado por backend.

## 9. Descarga binaria

IMPLEMENTADO — La respuesta se consume como bytes y conserva `Content-Type` y `Content-Disposition`. Antes de guardar/abrir se exige MIME `application/pdf` y firma `%PDF-`; JSON u otros contenidos se rechazan. El nombre se toma del header y se sanitiza. La descarga usa el selector de guardado del sistema; la previsualización escribe temporalmente en el directorio de caché y solicita abrir el PDF mediante Android. No se genera ni transforma un PDF desde JSON.

## 10. Verificación pública

IMPLEMENTADO — El verificador envía el código al endpoint público sin Bearer y muestra la respuesta real, incluso `valido=false`/`NO_REGISTRADO`. No afirma autenticidad a partir de una respuesta local.

## 11. Historial

IMPLEMENTADO — Al no existir endpoint agregado, combina las listas propias de inscripciones, pagos, asistencias y certificados. Se muestran como tipos separados y se ordenan por fecha cuando está disponible. Los pagos enlazan a la inscripción propia correspondiente, certificados al detalle y las inscripciones al evento. No se etiqueta una constancia, pago o asistencia como certificado.

## 12. Seguridad

VERIFICADO — Consultas privadas usan el mecanismo Bearer y almacenamiento seguro ya existente; la verificación pública omite autenticación porque el contrato es público. Flutter no envía un `userId` arbitrario, no conecta a Supabase y no contiene claves privilegiadas. Ownership y elegibilidad permanecen en backend. No se registran JWT ni códigos en logs.

## 13. Tests

PROBADO LOCALMENTE — `flutter test`: 81/81 aprobados. Incluye parseo/DTO, lista propia y vacía, detalle por endpoint propio, errores 401/403/404/red, verificación sin Authorization, descarga binaria PDF y conservación de bytes/headers, rechazo de contenido que no es PDF, historial de cuatro fuentes y estados de pantalla. Los datos de UI son fixtures aislados de tests; no se insertaron en backend.

## 14. Analyze

PROBADO LOCALMENTE — `flutter analyze`: `No issues found`.

## 15. APK

PROBADO LOCALMENTE — `flutter build apk --debug --dart-define=API_BASE_URL=http://192.168.100.131:8080/api/v1` terminó correctamente. Artefacto: `flutter/VidiaApp/build/app/outputs/flutter-apk/app-debug.apk` (214,829,546 bytes). Gradle mostró una advertencia de compatibilidad futura relacionada con Kotlin Gradle Plugin aplicado por plugins; no impidió compilar.

## 16. Validación Android

PENDIENTE DE PRUEBA — `flutter devices` encontró el SM A125M (Android 12), y el APK debug fue instalado. La consulta posterior mostró `mWakefulness=Dozing` y Keyguard `showing=true`; la captura de pantalla obtenida fue negra y no permite observar Vidia. No se afirma que se haya recorrido la interfaz. El backend local respondió HTTP 200 en `/v3/api-docs` y en el catálogo publicado; la URL LAN configurada también respondió HTTP 200 en OpenAPI.

## 17. Casos G-01…G-07

| Caso | Estado | Evidencia |
|---|---|---|
| G-01 Abrir Mis certificados | PENDIENTE DE PRUEBA | APK instalado; teléfono quedó bloqueado/en Dozing antes de observar la pantalla |
| G-02 Consultar certificado propio | PENDIENTE DE PRUEBA | No se pudo iniciar/observar una sesión participante en el dispositivo |
| G-03 Abrir detalle | PENDIENTE DE PRUEBA | Requiere certificado propio real y pantalla observable; no se fabricó certificado |
| G-04 Descargar certificado | PENDIENTE DE PRUEBA | No se encontró evidencia de certificado natural accesible desde una sesión Android |
| G-05 Abrir/previsualizar PDF | PENDIENTE DE PRUEBA | No se descargó ni creó certificado para la prueba |
| G-06 Consultar historial | PENDIENTE DE PRUEBA | Sin interacción observable en Android; composición cubierta por tests automatizados |
| G-07 Verificación pública | PENDIENTE DE PRUEBA | No se usó un código real de certificado en dispositivo; verificador y respuesta no válida cubiertos por tests |

## 18. Pendientes

PENDIENTE DE PRUEBA — Desbloquear y mantener visible el SM A125M, iniciar sesión con cuenta demo participante y observar G-01/G-06. G-02…G-05 requieren además un certificado legítimo ya emitido para esa cuenta; G-07 requiere un código real o una consulta de código inexistente ejecutada desde Android. No crear datos ni cambiar estados para obtenerlos.

## 19. Riesgos

PENDIENTE DE PRUEBA — La apertura de PDF depende de que Android tenga una aplicación compatible. El APK debug generado pesa aproximadamente 205 MiB. La advertencia actual de Gradle/Kotlin plugin señala una compatibilidad futura de plugins; el build presente sí concluyó.

## 20. Estado final

IMPLEMENTADO — Contratos, modelos, consultas propias, verificación pública, manejo binario de PDF, historial y tests Flutter implementados.

PROBADO LOCALMENTE — 81/81 tests, `flutter analyze` sin issues, build APK debug e instalación del APK.

PENDIENTE DE PRUEBA — Interacción física Android y flujo con certificado real. E3.6-E.1 y E3.6-F.1 siguen fuera de esta fase y no se modificaron.

VERIFICADO — No se modificaron fuentes del backend, OpenAPI, monografía, Render ni datos de PostgreSQL/Supabase durante E3.6-G. No se realizó commit ni push.
