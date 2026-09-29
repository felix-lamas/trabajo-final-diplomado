# Contrato OpenAPI canonico de Vidia

## Alcance

La API documentada oficialmente usa exclusivamente el prefijo `/api/v1`. Swagger UI es publico como documentacion; esto no modifica la autenticacion JWT, los roles ni el ownership de las operaciones funcionales.

OpenAPI local: `http://localhost:8080/api/v1/api-docs`

Swagger UI local: `http://localhost:8080/api/v1/swagger-ui.html`

Produccion: `https://trabajo-final-diplomado.onrender.com/api/v1`

## Normalizacion de aliases

| Alias | Decision | Motivo |
|---|---|---|
| `/api/auth/**` | Eliminado | Angular y Flutter consumen `/api/v1/auth/**`; no se encontraron consumidores del alias. |
| `POST /api/v1/auth/resetear-contrasena` | Eliminado | Los clientes usan `restablecer-contrasena`; `resetear-contrasena` solo era un alias historico. |
| `/api/dashboard/**` y `/api/reportes/**` | Compatibilidad temporal, fuera de OpenAPI | Angular elimina actualmente `/v1` en `DashboardService`; retirarlo en esta fase romperia el cliente. El contrato canonico documentado es `/api/v1/**`. |
| `/api/certificados/**` | Eliminado | No se encontraron consumidores; Angular usa `/api/v1/certificados/**`. |
| `/api/v1/verificacion-certificados/{codigo}` | Compatibilidad temporal, oculto en OpenAPI | Angular lo consume actualmente. El contrato canonico es `/api/v1/certificados/verificar/{codigo}`. |
| `/api/verificacion-certificados/{codigo}` | Eliminado | No se encontraron consumidores; solo se conserva temporalmente la variante bajo `/api/v1`. |

Los aliases conservados no deben utilizarse en desarrollos nuevos. Su retirada requiere primero migrar `DashboardService` y `CertificadoService` de Angular en una fase de frontend.

## Controllers y operaciones canonicas

### Autenticacion

- `POST /api/v1/auth/registro`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/recuperar-contrasena`
- `POST /api/v1/auth/restablecer-contrasena`
- `POST /api/v1/auth/verificar-correo`
- `POST /api/v1/auth/reenviar-verificacion`
- `POST /api/v1/auth/logout`

El registro crea una cuenta con `correoVerificado=false`, emite un token aleatorio de verificacion con vigencia de 24 horas y no devuelve JWT. El valor plano se entrega exclusivamente al servicio SMTP; la base conserva SHA-256. La verificacion consume el token una sola vez. El reenvio responde de forma no enumerativa tanto para correos inexistentes, cuentas verificadas y fallos operativos de SMTP; la respuesta confirma procesamiento, no entrega. Los fallos SMTP se controlan internamente sin registrar destinatario, URL ni token.

El login solo admite cuentas verificadas. Cada login bloquea la fila del usuario, revoca sesiones anteriores, crea una unica sesion activa y emite un JWT cuyo `jti` referencia esa sesion. Firma, expiracion y sesion persistida se validan en cada peticion. `POST /api/v1/auth/logout` requiere Bearer JWT, devuelve `204` y revoca la sesion actual; el backend no elimina el token almacenado por el cliente.

### Usuarios

- `GET /api/v1/usuarios`
- `GET /api/v1/usuarios/perfil`
- `PUT /api/v1/usuarios/perfil`
- `POST /api/v1/usuarios/cambiar-contrasena`
- `GET /api/v1/usuarios/{id}`
- `POST /api/v1/usuarios/solicitud-organizador`
- `GET /api/v1/usuarios/solicitudes-organizador`
- `PATCH /api/v1/usuarios/solicitudes-organizador/{usuarioId}/aprobar`
- `PATCH /api/v1/usuarios/solicitudes-organizador/{usuarioId}/rechazar`

El cambio de contrasena conserva `POST` porque es el contrato implementado y no existe un consumidor de un supuesto `PATCH`. El registro canonico permanece en `/auth/registro`; no existe `POST /usuarios` en el backend real.

## Persistencia pendiente de migracion

Esta fase no activa Flyway. Para una migracion productiva futura deben declararse y reconciliarse de forma explicita:

- `usuario.correo_verificado BOOLEAN NOT NULL` con politica definida para cuentas historicas;
- tabla `token_verificacion_correo` con UUID, `token_hash CHAR(64) UNIQUE NOT NULL`, usuario, expiracion, uso y auditoria;
- tabla `sesion_usuario` con UUID usado como `jti`, usuario, expiracion, revocacion y auditoria;
- indices por usuario para tokens y sesiones, y por expiracion para limpieza;
- garantia fisica de una unica sesion no revocada por usuario mediante indice parcial de PostgreSQL, como defensa adicional al bloqueo pesimista transaccional del login.

`spring.jpa.hibernate.ddl-auto=update` sigue siendo solo una facilidad local y no se considera migracion de produccion.

### Categorias

- `GET /api/v1/categorias-evento`
- `GET /api/v1/categorias-evento/activas`
- `GET /api/v1/categorias-evento/{id}`
- `POST /api/v1/categorias-evento`
- `PUT /api/v1/categorias-evento/{id}`
- `DELETE /api/v1/categorias-evento/{id}`

### Eventos

- `GET /api/v1/eventos`
- `GET /api/v1/eventos/publicados`
- `GET /api/v1/eventos/publicados/buscar`
- `GET /api/v1/eventos/revision`
- `GET /api/v1/eventos/{id}`
- `POST /api/v1/eventos`
- `PUT /api/v1/eventos/{id}`
- `DELETE /api/v1/eventos/{id}`
- `PATCH /api/v1/eventos/{id}/publicar`
- `PATCH /api/v1/eventos/{id}/enviar-revision`
- `PATCH /api/v1/eventos/{id}/rechazar`
- `PATCH /api/v1/eventos/{id}/volver-borrador`
- `PATCH /api/v1/eventos/{id}/cancelar`
- `PATCH /api/v1/eventos/{id}/finalizar`
- `GET /api/v1/eventos/categoria/{id}`

### Inscripciones

- `POST /api/v1/inscripciones`
- `GET /api/v1/inscripciones/mis-inscripciones`
- `GET /api/v1/inscripciones/{id}`
- `PATCH /api/v1/inscripciones/{id}/cancelar`
- `GET /api/v1/inscripciones/evento/{eventoId}`

### Pagos

- `POST /api/v1/pagos`
- `POST /api/v1/pagos/{id}/comprobante`
- `GET /api/v1/pagos/mis-pagos`
- `GET /api/v1/pagos/pendientes`
- `GET /api/v1/pagos`
- `GET /api/v1/pagos/{id}`
- `GET /api/v1/pagos/{id}/comprobante`
- `PATCH /api/v1/pagos/{id}/validar`
- `PATCH /api/v1/pagos/{id}/rechazar`

### Sesiones y asistencia

- `POST /api/v1/eventos/{eventoId}/sesiones`
- `GET /api/v1/eventos/{eventoId}/sesiones`
- `GET /api/v1/sesiones/{id}`
- `PUT /api/v1/sesiones/{id}`
- `PATCH /api/v1/sesiones/{id}/estado`
- `GET /api/v1/sesiones/{sesionId}/qr`
- `POST /api/v1/sesiones/{sesionId}/qr/generar`
- `POST /api/v1/asistencias`
- `GET /api/v1/asistencias/evento/{id}`
- `GET /api/v1/asistencias/mis-asistencias`

### Certificados

- `POST /api/v1/certificados/generar/{inscripcionId}`
- `GET /api/v1/certificados/{id}`
- `GET /api/v1/certificados/mis-certificados`
- `GET /api/v1/eventos/{eventoId}/certificados`
- `GET /api/v1/certificados/{id}/descargar`
- `GET /api/v1/certificados/verificar/{codigo}`

### Dashboards y reportes

- `GET /api/v1/dashboard`
- `GET /api/v1/dashboard/ejecutivo`
- `GET /api/v1/dashboard/academico`
- `GET /api/v1/dashboard/operativo`
- `GET /api/v1/reportes/eventos`
- `GET /api/v1/reportes/participantes`
- `GET /api/v1/reportes/pagos`
- `GET /api/v1/reportes/certificados`
- `GET /api/v1/reportes/exportar/pdf`
- `GET /api/v1/reportes/exportar/excel`

## Binarios

- La descarga de certificados genera un PDF real y se documenta como `application/pdf` con schema binario.
- La descarga de comprobantes devuelve el archivo real y conserva su media type validado (JPG, PNG o PDF).
- Los exports PDF y XLSX de reportes siguen siendo implementaciones simuladas: retornan bytes de texto con media type binario, no documentos validos. OpenAPI los identifica expresamente como provisionales; esta fase no implementa nuevos exports.

## Contrato de errores

Los errores gestionados utilizan `ErrorRespuesta` con `codigo`, `mensaje`, `detalles`, `timestamp` y `ruta`. OpenAPI agrega 401/403 solo a operaciones protegidas, 400 a entradas validadas y 500 como fallo inesperado. Los 404 y 409 se declaran unicamente en operaciones cuyo servicio los produce.

## Fuera del backend actual

No existen controllers backend para `control-acceso`, `credenciales`, `codigos-qr` legacy ni `encuestas`. Aunque Angular conserva servicios para esas rutas, no se documentan ni se inventan endpoints en esta fase.
