# 07. Modelo conceptual y persistencia

## Entidades conceptuales

1. Usuario
2. CategoriaEvento
3. Evento
4. Inscripcion
5. Pago
6. SesionEvento
7. Asistencia
8. Certificado

Relaciones conceptuales principales: categoría→evento; usuario→evento como organizador; evento→sesión/inscripción/certificado; usuario→inscripción/asistencia/certificado; inscripción→pago; sesión→asistencia.

## Estructuras técnicas

`Rol`, `Permiso`, `UsuarioRol`, `RolPermiso`, `SesionUsuario`, `TokenVerificacionCorreo`, `TokenRecuperacion` y `QrAsistenciaTemporal` soportan autorización, sesión/tokens y QR temporal. No son roles adicionales ni funcionalidades de dominio separadas. QR temporal es técnico asociado a sesión.

## Reconciliación pendiente con esquema físico

La monografía actual afirma 14 entidades/14 tablas; código actual contiene ocho entidades de dominio y estructuras técnicas. No afirmar que hay 14 o 16 tablas físicas hasta inspeccionar PostgreSQL de cada entorno. Solo se localizó `V1__agregar_qr_pago_storage_key_eventos.sql`; Hibernate mantiene `ddl-auto:update` por configuración base. Se requiere inventario real de tablas/constraints y estrategia de migración antes de describir conteos.

`Evento` conserva `qr_pago_storage_key` como referencia privada, no DTO público. `qrPagoUrl` de respuesta es la URL controlada por Vidia. `Pago` guarda metadatos/referencia de comprobante privado; no exponer paths físicos ni keys.
