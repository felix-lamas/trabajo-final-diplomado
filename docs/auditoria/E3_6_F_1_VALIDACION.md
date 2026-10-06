# E3.6-F.1 — Validación física Android de asistencia QR + GPS

## 1. Objetivo

Validar en Android el flujo de asistencia QR + GPS de E3.6-F sin implementar funcionalidad nueva. Estado del recorrido funcional completo: **PENDIENTE DE PRUEBA**, porque la cuenta no dispone de una sesión de asistencia.

## 2. Dispositivo

- Dispositivo: Samsung SM A125M (`R58R240RCWH`), Android 12 / API 31, Android arm64.
- `flutter devices --machine`: identificó el teléfono como conectado y compatible.
- ADB: dispositivo en estado `device`; durante la navegación, pantalla despierta y actividad Vidia en primer plano.
- APK debug E3.6-F instalado. No se hizo una nueva compilación ni se modificó código en esta validación.
- Fecha/hora de validación: 2026-10-06, aproximadamente 12:15 (America/La_Paz).

## 3. Backend

Backend local Spring Boot, consultado en `localhost:8080` desde el equipo:

- `GET /v3/api-docs`: HTTP 200, `application/json`.
- `GET /api/v1/eventos/publicados`: HTTP 200, `application/json`.
- El catálogo publicado también cargó en la app instalada en el SM A125M mediante su configuración existente de API. No se cambió ni se hardcodeó `API_BASE_URL`.

## 4. Cuenta demo utilizada

Se utilizó la sesión participante que ya estaba abierta en Vidia y que la aplicación presenta como “Usuario”. No se mostraron ni registraron email, contraseña, JWT ni otros identificadores de cuenta. Las inscripciones revisadas fueron las que la app mostró para esa sesión.

## 5. Evento

Se recorrieron tres inscripciones que Flutter mostraba como `Confirmada` y cuyos eventos se mostraban `PUBLICADO`:

- **Taller de Desarrollo Web** — modalidad `VIRTUAL`, gratuito, fechas 14–15/11/2026.
- **Curso de Gestión de Proyectos** — modalidad `PRESENCIAL`, Bs. 50.00, fechas 29–30/12/2026.
- **Congreso de Innovación Tecnológica UAJMS** — modalidad `PRESENCIAL`, gratuito, fechas 30–31/10/2026.

En los tres casos se abrió “Asistencia y sesiones”. La pantalla mostró el estado vacío “Todavía no hay sesiones de asistencia para [evento]”. No se encontró otra inscripción confirmada con sesión.

## 6. Sesión

Sesión utilizada: **NO APLICA**. No había sesión disponible en los eventos confirmados inspeccionados. Por tanto, no existían sesión activa ni QR vigente que pudiera generarse desde el flujo autorizado de organizador. No se crearon ni alteraron sesiones, eventos, inscripciones o asistencias.

## 7. AT-01

**PENDIENTE DE PRUEBA.** Se validó físicamente el login ya restaurado, catálogo, detalle y navegación a “Asistencia y sesiones”; el catálogo mostró eventos del backend y el detalle mostró la inscripción `Confirmada`. No se pudo llegar al scanner porque la lista real de sesiones estaba vacía. No se afirmó lectura de cámara, captura GPS, solicitud de asistencia ni respuesta exitosa del backend.

## 8. AT-02

**PENDIENTE DE PRUEBA.** No se intentó duplicar: AT-01 no tuvo una asistencia exitosa y no se fabricó un registro. El `409 ATTENDANCE_DUPLICATED` permanece cubierto por los tests automatizados de E3.6-F, no por esta prueba física.

## 9. AT-03

**PENDIENTE DE PRUEBA.** No hubo QR real que pudiera expirar naturalmente. No se manipuló la base de datos ni se fabricó un QR vencido.

## 10. AT-04

**PENDIENTE DE PRUEBA.** No hubo sesión presencial con QR válido ni escenario natural fuera del radio. No se alteraron coordenadas ni ubicación para fabricar el caso.

## 11. AT-05

**PENDIENTE DE PRUEBA.** No se revocó el permiso GPS: sin una sesión accesible no se puede llegar al flujo que lo necesita, y una prueba de permisos sin ese flujo no validaría el comportamiento de asistencia.

## 12. AT-06

**PENDIENTE DE PRUEBA.** No se revocó el permiso de cámara ni se abrió el scanner, pues no había una sesión desde la cual acceder legítimamente.

## 13. AT-07

**PENDIENTE DE PRUEBA.** No se encontró en la cuenta/eventos inspeccionados una sesión accesible sin inscripción confirmada que permitiera intentar asistencia. No se fabricó un usuario o estado de inscripción.

## 14. Sesiones virtuales

**PENDIENTE DE PRUEBA.** El evento virtual confirmado “Taller de Desarrollo Web” no tenía sesiones. No fue posible observar virtual sin coordenadas ni virtual con coordenadas.

## 15. Analyze y tests

- `flutter analyze`: **PROBADO LOCALMENTE**, sin issues.
- `flutter test`: **PROBADO LOCALMENTE**, 61/61 aprobados, 0 fallidos.

No se agregaron ni modificaron tests.

## 16. Problemas encontrados

No se observó un defecto Flutter ni un error HTTP de asistencia. El impedimento fue de datos/entorno: la cuenta demo no tenía sesiones asociadas a los eventos confirmados revisados. La app cargó el catálogo, detalle, inscripciones y la pantalla vacía de sesiones sin presentar un error técnico.

## 17. Evidencia disponible

- Salida de `flutter devices --machine` y `adb devices -l`: SM A125M conectado.
- Estado Android: pantalla despierta y actividad `bo.edu.uajms.vidia/.MainActivity` visible.
- OpenAPI local y catálogo publicado: HTTP 200.
- Jerarquía accesible Android observada para catálogo, “Mis inscripciones”, detalles y pantallas vacías de asistencia. No se guardó una captura local, por lo que no se adjunta imagen.
- No se registraron tokens, secretos, contraseñas ni datos personales innecesarios.

## 18. Pendientes

- Disponer, mediante el flujo habitual autorizado de organización, de un evento publicado con una sesión accesible para la cuenta demo.
- Generar el QR desde la interfaz autorizada de organizador dentro de su vigencia máxima de dos minutos.
- Ejecutar AT-01 en Android observando cámara, ubicación cuando corresponda, solicitud HTTP y resultado Flutter.
- Ejecutar AT-02 solo después de un AT-01 exitoso; probar el resto únicamente si aparece un escenario natural y legítimo.

## 19. Estado final

- Navegación física por catálogo, inscripción confirmada y consulta de sesiones: **PROBADO LOCALMENTE**.
- Backend local/OpenAPI/catálogo: **PROBADO LOCALMENTE**.
- Analyze y suite automatizada: **PROBADO LOCALMENTE**.
- AT-01 a AT-07 y escenarios virtuales: **PENDIENTE DE PRUEBA** por falta de sesión real.
- Implementación E3.6-F: sin cambios en esta validación.
- Backend, OpenAPI, monografía, Render, PostgreSQL y Supabase: sin cambios intencionales.
- Commit/push: no realizados.
