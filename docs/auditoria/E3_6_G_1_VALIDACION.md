# E3.6-G.1 — Validación física Android

## 1. Objetivo

PROBADO LOCALMENTE — Validar en Android Mis certificados, Historial y la verificación pública de código; probar detalle y PDF únicamente si la cuenta participante dispone de un certificado real.

## 2. Dispositivo

PROBADO LOCALMENTE — SM A125M, Android 12 (API 31), conectado por ADB. Vidia se abrió con pantalla despierta y Keyguard no visible. Se observó la UI directamente mediante capturas temporales; estas no se conservan porque mostraban actividad de la cuenta.

## 3. Backend

PROBADO LOCALMENTE — Backend local en `localhost:8080`; `GET /v3/api-docs` y `GET /api/v1/eventos/publicados` respondieron HTTP 200. La app instalada conservó la configuración LAN existente; la respuesta de certificados e historial confirma que el dispositivo accedió a datos autenticados del backend. No se cambió `API_BASE_URL` ni se hardcodeó una dirección nueva.

## 4. Cuenta participante

PROBADO LOCALMENTE — Se utilizó la sesión participante ya restaurada en Vidia (la pantalla mostró el perfil genérico “Usuario”). No se anotaron nombre, RU, CI, contraseña ni tokens. No se verificó independientemente el origen demo de la cuenta.

## 5. G-01 — Mis certificados

PROBADO LOCALMENTE — Se abrió **Vidia → Mis certificados**. La pantalla cargó la respuesta real del backend y mostró el estado vacío “Sin certificados todavía.” La cuenta no presentó certificados; esto no se consideró un fallo.

## 6. G-02 — Certificado propio

PENDIENTE DE PRUEBA — La consulta real de Mis certificados fue vacía, por lo que no existe un certificado propio que abrir. No se generó ni fabricó uno.

## 7. G-03 — Detalle

PENDIENTE DE PRUEBA — Depende de G-02 y requiere un certificado legítimo de la cuenta. No se intentó consultar un id ajeno ni se creó información artificial.

## 8. G-04 — Descarga PDF

PENDIENTE DE PRUEBA — No hay certificado real disponible para descargar. El flujo de bytes, MIME `application/pdf`, firma `%PDF-` y headers quedó cubierto por las pruebas automatizadas de E3.6-G, no por esta validación física.

## 9. G-05 — Apertura PDF

PENDIENTE DE PRUEBA — Sin una descarga de certificado real no se probó la apertura en Android ni la existencia de una aplicación PDF compatible.

## 10. G-06 — Historial

PROBADO LOCALMENTE — Se abrió **Vidia → Historial** con datos reales de la cuenta. Se observaron entradas de inscripción y pago con fechas/estados; se distinguieron visualmente los tipos y el orden temporal descendente. No se observaron entradas de asistencia ni certificados en el listado consultado.

La lista mostró también un pago `PENDIENTE_VALIDACION` que corresponde al escenario previamente identificado en la trazabilidad de E3.6-E.1 como contaminado por una selección accidental de archivo. Solo apareció como fila del historial; no se abrió, descargó ni modificó, y no se usa como evidencia de E3.6-E.1.

## 11. G-07 — Verificación pública

PROBADO LOCALMENTE — Desde Mis certificados se abrió el verificador y se consultó `INVALIDO_G1_20261006`, un código de formato inválido reservado a esta prueba. La respuesta real mostrada fue `valido=false`, “Certificado no registrado” y estado `NO_REGISTRADO`. No se afirmó la validación de un certificado válido. La ausencia de Bearer para el endpoint público también está cubierta por el test del repositorio.

## 12. Tests

PROBADO LOCALMENTE — `flutter test`: 81/81 aprobados.

## 13. Analyze

PROBADO LOCALMENTE — `flutter analyze`: `No issues found`.

## 14. APK

PROBADO LOCALMENTE — Se utilizó el APK debug de E3.6-G ya compilado e instalado: `flutter/VidiaApp/build/app/outputs/flutter-apk/app-debug.apk`. En esta validación no fue necesario recompilar ni cambiar configuración.

## 15. Problemas

VERIFICADO — En la primera inspección el teléfono estaba bloqueado/Dozing y una captura resultó negra; no se contó como prueba. Luego el dispositivo quedó despierto, Keyguard dejó de mostrarse y fue posible observar la UI. No se detectó un defecto Flutter durante G-01, G-06 o G-07.

## 16. Limitaciones

PENDIENTE DE PRUEBA — La cuenta no tiene certificado propio. Por ello no se puede validar físicamente detalle, descarga o visor PDF en esta cuenta. La consulta pública realizada demuestra el camino de código inexistente, no la validez de un certificado real.

## 17. Pendientes

PENDIENTE DE PRUEBA — G-02, G-03, G-04 y G-05 requieren que la cuenta participante obtenga un certificado real mediante las reglas y el proceso autorizados del backend. No se modificaron eventos, inscripciones, pagos, asistencias ni estados para crear ese escenario.

## 18. Estado final

PROBADO LOCALMENTE — G-01, G-06 y G-07 en SM A125M contra backend local; `flutter analyze` sin issues; `flutter test` 81/81.

PENDIENTE DE PRUEBA — G-02 a G-05 por falta de certificado real elegible en la cuenta probada.

VERIFICADO — No se modificaron backend, OpenAPI, monografía, Render, PostgreSQL ni Supabase. No se hizo commit ni push.
