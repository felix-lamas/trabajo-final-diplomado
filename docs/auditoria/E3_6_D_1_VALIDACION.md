# E3.6-D.1 — Validación final

## 1. Objetivo

Validar en el teléfono el flujo autenticado de catálogo, detalle, inscripción gratuita y pagada, “Mis inscripciones” y cancelación usando el backend local y una cuenta demo participante existente. Esta fase no cambió código funcional.

## 2. Entorno utilizado

- Fecha local: 2026-10-06, zona America/La_Paz. La lista del teléfono registró la inscripción gratuita a las 10:26 y la pagada a las 10:33.
- Backend local: `http://localhost:8080/api/v1`; catálogo público respondió HTTP 200.
- Cliente Android: Vidia debug en dispositivo físico SM A125M (`R58R240RCWH`). La configuración de esta instalación apunta al backend local por LAN.
- Base de datos local: disponibilidad comprobada durante la sesión; no se consultaron ni modificaron filas manualmente.

## 3. Cuenta de prueba

**PROBADO LOCALMENTE** — Se usó la cuenta sembrada de participante `usuario@demo.local` del perfil demo. La autenticación REST confirmó el rol `USUARIO` y el login realizado desde la interfaz Flutter fue exitoso. No se registran credenciales ni tokens.

## 4. Flujo ejecutado

**PROBADO LOCALMENTE** — Login en Vidia, apertura del catálogo público, apertura de detalles publicados, solicitud de inscripción, pantalla de resultado y consulta de “Mis inscripciones”. Los eventos y estados se observaron en el dispositivo y se contrastaron con el catálogo HTTP local.

## 5. Inscripción gratuita

**PROBADO LOCALMENTE** — Evento: “Seminario de Inteligencia Artificial”, publicado, gratuito y con cupos disponibles informados por el detalle. Flutter mostró el resultado devuelto: `CONFIRMADA`. “Mis inscripciones” presentó el mismo evento con estado `Confirmada`. Después se ejerció la cancelación y la lista cambió a `Cancelada`; el flujo informó “Inscripción cancelada”.

## 6. Inscripción pagada

**PROBADO LOCALMENTE** — Evento: “Jornada Cultural en Preparacion”, publicado, costo Bs. 60.00 y 80 cupos disponibles según el detalle. Flutter envió la solicitud de inscripción y presentó `PENDIENTE_PAGO` / “Inscripción pendiente de pago”. “Mis inscripciones” mostró el evento con `Pendiente Pago`. No se inició ni simuló pago, ni se cargó comprobante.

## 7. Duplicidad

**PROBADO LOCALMENTE** — Al abrir el detalle de “Curso de Gestión de Proyectos”, ya inscrito por la cuenta, Flutter mostró “Ya tienes una inscripción” y el estado existente `Confirmada`; no presentó acción para crear otra. No se forzó un POST fuera de la interfaz para fabricar el conflicto. La interpretación del código real 409 permanece cubierta por el test unitario existente.

**PENDIENTE DE PRUEBA** — Respuesta HTTP 409 de duplicidad observada de extremo a extremo en dispositivo. La interfaz bloqueó el intento al detectar la inscripción ya existente.

## 8. Cancelación

**PROBADO LOCALMENTE** — La inscripción `CONFIRMADA` del evento gratuito de prueba permitió abrir “Cancelar inscripción” y confirmar la operación. La app informó éxito y “Mis inscripciones” mostró `Cancelada` para ese evento.

## 9. Tests

**VERIFICADO** — `flutter analyze` ejecutado desde `flutter/VidiaApp`: `No issues found`.

**VERIFICADO** — `flutter test` ejecutado desde `flutter/VidiaApp`: 29/29 aprobados, 0 fallidos, 0 omitidos.

## 10. Evidencia

**PROBADO LOCALMENTE** — Evidencia observada en Vidia sobre SM A125M: login exitoso; catálogo y detalles publicados; resultado gratuito `CONFIRMADA`; resultado pagado `PENDIENTE_PAGO`; listado con ambos estados; cancelación reflejada como `CANCELADA`; bloqueo visual de nueva inscripción en el evento que ya tenía inscripción. El endpoint público local respondió HTTP 200. No se guardan capturas ni datos personales adicionales.

## 11. Pendientes

- **PENDIENTE DE PRUEBA** — Duplicidad vía respuesta HTTP 409 real en dispositivo; la UI bloqueó el envío por inscripción previa y no se saltó esa protección.
- **PENDIENTE DE PRUEBA** — Capacidad agotada; no había un evento publicado sin cupos y no se alteraron datos para crear ese escenario.
- **NO APLICA** — Cancelación de la inscripción pagada; no se solicitó porque el alcance excluye procesos de pago y comprobante.
- **NO VERIFICADO** — Conectividad Flutter con Render; fuera de esta validación local.

## 12. Estado final

**PROBADO LOCALMENTE** — Login participante, flujo gratuito, flujo pagado hasta `PENDIENTE_PAGO`, consulta de “Mis inscripciones”, cancelación, `flutter analyze` y 29/29 tests. La validación completa de E3.6-D.1 permanece **PENDIENTE DE PRUEBA** para el conflicto HTTP 409 en dispositivo y el escenario de capacidad agotada. No se modificaron backend, OpenAPI, monografía ni configuración de producción. No se hizo commit ni push.
