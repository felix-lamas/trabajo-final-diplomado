# E3.6-E.1 — Validación final

## 1. Objetivo

Validar en Android, con datos locales, la integración Flutter de pagos y comprobantes sin cambiar código funcional, backend, OpenAPI ni datos directamente.

**PENDIENTE DE PRUEBA** — No fue posible completar el upload controlado: el único pago del participante en `PENDIENTE_PAGO` está asociado al registro previamente contaminado, que se excluyó de esta prueba.

## 2. Entorno

- **PROBADO LOCALMENTE** — Backend local accesible en `http://192.168.100.131:8080/api/v1`; catálogo público respondió HTTP 200.
- **PROBADO LOCALMENTE** — Dispositivo SM A125M, serie `R58R240RCWH`, con Vidia instalada y sesión participante activa.
- **PROBADO LOCALMENTE** — Se consultaron Mis inscripciones y Gestión de pago desde Flutter con el usuario autenticado.
- **VERIFICADO** — No se modificó backend, OpenAPI, monografía ni configuración de producción. No hubo commit ni push.

## 3. Archivo de prueba

- **VERIFICADO** — Se preparó `PRUEBA_TECNICA_NO_ACREDITA_PAGO.pdf`, PDF de 702 bytes con el encabezado `%PDF-1.4` y texto que indica que no acredita un pago real.
- **VERIFICADO** — El archivo se copió a `/sdcard/Download/PRUEBA_TECNICA_NO_ACREDITA_PAGO.pdf`; el SHA-256 en el teléfono coincidió con el archivo local.
- **PENDIENTE DE PRUEBA** — El archivo no se seleccionó ni se subió, porque no se encontró un pago limpio elegible. No se usaron comprobantes reales ni datos bancarios.

## 4. Cuenta de prueba

- **PROBADO LOCALMENTE** — Cuenta participante demo `usuario@demo.local`; la sesión ya estaba activa en Vidia. No se registran contraseñas, JWT ni secretos.

## 5. Evento utilizado

- **PROBADO LOCALMENTE** — “Curso de Gestión de Proyectos”, evento publicado con monto backend de Bs. 50.00. La inscripción propia aparece `CONFIRMADA` y el pago `APROBADO`.
- **PENDIENTE DE PRUEBA** — No se encontró otra inscripción pagada propia limpia en `PENDIENTE_PAGO` y sin comprobante. “Jornada Cultural en Preparacion” conserva el estado previo `PENDIENTE_VALIDACION` y está contaminado por la selección accidental reportada en E3.6-E; solo se observó en la lista para excluirlo y no se usó en operaciones de pago.

## 6. Upload controlado

- **PENDIENTE DE PRUEBA** — No se envió el PDF de prueba. El selector y el POST multipart no se ejecutaron en esta validación, para evitar repetir la incidencia sobre un registro no elegible.
- **NO APLICA** — No hubo transacción financiera real.

## 7. Estado PENDIENTE_VALIDACION

- **PROBADO LOCALMENTE** — Mis inscripciones muestra el pago previamente contaminado como `Pendiente Validacion`.
- **PENDIENTE DE PRUEBA** — Este resultado no acredita el upload del PDF de esta fase ni la identidad/contenido del archivo almacenado. No se hicieron nuevas operaciones sobre ese pago.

## 8. Descarga

- **PROBADO LOCALMENTE** — Desde Gestión de pago del registro propio `APROBADO`, Flutter consultó el comprobante binario y abrió la previsualización como imagen en Android.
- **PENDIENTE DE PRUEBA** — No se guardó una copia en almacenamiento compartido ni se abrió un PDF en el dispositivo. La previsualización real observada correspondió a imagen.

## 9. Constancia de inscripción

- **PROBADO LOCALMENTE** — Flutter consultó la constancia de “Curso de Gestión de Proyectos” y presentó una vista JSON diferenciada titulada “Constancia de inscripción”. La UI aclara que no es factura ni certificado.
- **VERIFICADO** — No se trató la respuesta como PDF. No se reproducen en este informe códigos ni datos personales de la constancia.

## 10. APROBADO

- **PROBADO LOCALMENTE** — El backend devolvió el estado real `APROBADO` y monto Bs. 50.00 para la inscripción confirmada de “Curso de Gestión de Proyectos”; Flutter mostró “Pago aprobado” y la fecha de resolución.
- **PENDIENTE DE PRUEBA** — No se ejecutó una decisión nueva desde la interfaz administrativa; la prueba fue de consulta participante de un estado existente.

## 11. RECHAZADO

- **PENDIENTE DE PRUEBA** — No se encontró un pago rechazado propio limpio y no se fabricó un rechazo ni se modificaron datos.

## 12. Reenvío

- **PENDIENTE DE PRUEBA** — No se probó el reenvío en Android porque no había un pago rechazado con inscripción propia nuevamente `PENDIENTE_PAGO`.

## 13. Tests

- **VERIFICADO** — `flutter analyze`: `No issues found`.
- **VERIFICADO** — `flutter test`: 45/45 aprobados, 0 fallidos.
- **VERIFICADO** — Los tests existentes comprueban extensión/MIME no permitido, archivo vacío y límite de 5 MiB; no se repitieron esas pruebas en Android.

## 14. Pendientes

- **PENDIENTE DE PRUEBA** — Conseguir un escenario local limpio: evento pagado publicado y una inscripción propia `PENDIENTE_PAGO` sin comprobante.
- **PENDIENTE DE PRUEBA** — Seleccionar el PDF técnico desde Descargas, verificar nombre/tipo/tamaño en Flutter, subirlo y confirmar la respuesta real `PENDIENTE_VALIDACION`.
- **PENDIENTE DE PRUEBA** — Guardar/abrir un PDF de comprobante desde Android.
- **PENDIENTE DE PRUEBA** — Probar un rechazo real y reenvío posterior si existe escenario administrativo de prueba.
- **NO VERIFICADO** — Aprobar/rechazar pagos desde un flujo administrativo; no se usó ni se modificó esa interfaz.
- **NO APLICA** — Pago real, uso de cuenta bancaria real y modificación directa de base de datos.

## 15. Estado final

**PENDIENTE DE PRUEBA** — Se verificaron localmente consulta participante de un pago aprobado, previsualización de comprobante de imagen y constancia JSON en el SM A125M. El archivo PDF de prueba está identificado y copiado al teléfono con hash coincidente, pero no se subió por falta de un pago limpio elegible. Flutter analyze y los 45 tests pasan. No se modificó backend, monografía ni producción; no hubo commit ni push.
