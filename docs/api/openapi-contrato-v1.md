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

`GET /usuarios/perfil` y `PUT /usuarios/perfil` operan exclusivamente sobre el usuario identificado por el JWT y no reciben un ID. La actualizacion permite solamente `nombres`, `apellidos` y `celular`; email, CI, RU, roles, contrasena, estado de organizador y verificacion de correo son inmutables desde este contrato. `POST /usuarios/cambiar-contrasena` exige la clave actual, una nueva clave segura y diferente, y confirmacion coincidente. Al completarse revoca todas las sesiones, incluido el JWT utilizado en la operacion.

La solicitud de organizador pertenece siempre al usuario autenticado y no acepta `usuarioId`. Una solicitud `RECHAZADA` puede presentarse nuevamente sin espera; `PENDIENTE` y `APROBADA` impiden duplicados. Aprobacion y rechazo bloquean pesimistamente al solicitante y solo admiten una transicion desde `PENDIENTE`. Aprobar reemplaza el rol `USUARIO` por `ORGANIZADOR`; rechazar conserva `USUARIO`. El JWT solo contiene identidad y sesion: el filtro recarga authorities desde base de datos en cada peticion, por lo que el rol aprobado se reconoce con la sesion actual y no se emiten ni revocan tokens por este cambio.

## Persistencia pendiente de migracion

Esta fase no activa Flyway. Para una migracion productiva futura deben declararse y reconciliarse de forma explicita:

- `usuario.correo_verificado BOOLEAN NOT NULL` con politica definida para cuentas historicas;
- tabla `token_verificacion_correo` con UUID, `token_hash CHAR(64) UNIQUE NOT NULL`, usuario, expiracion, uso y auditoria;
- tabla `sesion_usuario` con UUID usado como `jti`, usuario, expiracion, revocacion y auditoria;
- indices por usuario para tokens y sesiones, y por expiracion para limpieza;
- garantia fisica de una unica sesion no revocada por usuario mediante indice parcial de PostgreSQL, como defensa adicional al bloqueo pesimista transaccional del login.

`spring.jpa.hibernate.ddl-auto=update` sigue siendo solo una facilidad local y no se considera migracion de produccion.

### Categorias

Contrato canonico, sin aliases:

| Metodo y ruta | Rol | Resultado | Errores relevantes |
| --- | --- | --- | --- |
| `GET /api/v1/categorias-evento` | `ADMINISTRADOR`, `ORGANIZADOR`, `USUARIO` | `200`, lista de DTOs activos e inactivos | `401`, `403` |
| `GET /api/v1/categorias-evento/activas` | `ADMINISTRADOR`, `ORGANIZADOR`, `USUARIO` | `200`, categorias utilizables por selectores de eventos | `401`, `403` |
| `GET /api/v1/categorias-evento/{id}` | `ADMINISTRADOR`, `ORGANIZADOR`, `USUARIO` | `200`, DTO de categoria | `400`, `401`, `403`, `404` |
| `POST /api/v1/categorias-evento` | `ADMINISTRADOR` | `201`, categoria creada | `400`, `401`, `403`, `409` por nombre duplicado |
| `PUT /api/v1/categorias-evento/{id}` | `ADMINISTRADOR` | `200`, categoria actualizada | `400`, `401`, `403`, `404`, `409` por nombre duplicado |
| `DELETE /api/v1/categorias-evento/{id}` | `ADMINISTRADOR` | `204`, eliminacion logica | `400`, `401`, `403`, `404`, `409` si existen eventos asociados |

El nombre se recorta en sus extremos y debe tener entre 3 y 100 caracteres una vez normalizado. La
unicidad no distingue mayusculas ni espacios externos. La respuesta publica es
`CategoriaEventoResponse` (`id`, `nombre`, `descripcion`, `estado`); nunca se expone la entidad JPA ni
la clave tecnica `nombreNormalizado`. Todos los errores usan `ErrorRespuesta` con `codigo`, `mensaje`,
`detalles`, `timestamp` y `ruta`.

La entidad declara `nombre_normalizado VARCHAR(100) NOT NULL UNIQUE` como defensa fisica ante carreras
concurrentes. El esquema actual sigue dependiendo de `ddl-auto=update`: antes de desplegar sobre una
base con categorias historicas se debe reconciliar duplicados, poblar la columna con
`lower(trim(nombre))` y crear/verificar su restriccion unica mediante el procedimiento de despliegue.
No se incorpora Flyway en esta fase y PostgreSQL no se considera validado por las pruebas unitarias.

### Eventos

| Operacion canonica | Acceso | Resultado | Errores relevantes |
| --- | --- | --- | --- |
| `GET /api/v1/eventos` | JWT: `ADMINISTRADOR`, `ORGANIZADOR`, `USUARIO` | `200`; todos, propios o publicados segun rol | `401`, `403` |
| `GET /api/v1/eventos/publicados` | Publico | `200`; solo `PUBLICADO` | `200` con lista vacia |
| `GET /api/v1/eventos/publicados/buscar` | Publico | `200`; filtros opcionales de texto, categoria, precio y modalidad | `400` por enum/filtro invalido |
| `GET /api/v1/eventos/revision` | `ADMINISTRADOR` | `200`; solo `EN_REVISION` | `401`, `403` |
| `GET /api/v1/eventos/{id}` | Publico o JWT | `200`; publico solo `PUBLICADO`, administrador cualquiera, organizador los propios | `404` inexistente o no visible |
| `POST /api/v1/eventos` | `ORGANIZADOR` aprobado | `201`; crea `BORRADOR` y deriva ownership del JWT | `400`, `401`, `403`, `404` categoria activa inexistente |
| `PUT /api/v1/eventos/{id}` | `ADMINISTRADOR`, `ORGANIZADOR` propietario aprobado | `200`; solo `BORRADOR` o `RECHAZADO` | `400`, `401`, `403`, `404`, `409` estado incompatible |
| `DELETE /api/v1/eventos/{id}` | `ADMINISTRADOR`, `ORGANIZADOR` propietario aprobado | `204`; solo `BORRADOR` | `401`, `403`, `404`, `409` |
| `PATCH /api/v1/eventos/{id}/enviar-revision` | `ORGANIZADOR` propietario aprobado | `200`; `BORRADOR -> EN_REVISION` | `400` incompleto, `401`, `403`, `404`, `409` |
| `PATCH /api/v1/eventos/{id}/publicar` | `ADMINISTRADOR` | `200`; `EN_REVISION -> PUBLICADO` | `400` incompleto, `401`, `403`, `404`, `409` |
| `PATCH /api/v1/eventos/{id}/rechazar` | `ADMINISTRADOR` | `200`; `EN_REVISION -> RECHAZADO`, motivo obligatorio | `400`, `401`, `403`, `404`, `409` |
| `PATCH /api/v1/eventos/{id}/volver-borrador` | `ORGANIZADOR` propietario aprobado | `200`; `RECHAZADO -> BORRADOR` | `401`, `403`, `404`, `409` |
| `PATCH /api/v1/eventos/{id}/cancelar` | `ADMINISTRADOR` | `200`; `PUBLICADO -> CANCELADO`, motivo obligatorio | `400`, `401`, `403`, `404`, `409` |
| `PATCH /api/v1/eventos/{id}/finalizar` | `ADMINISTRADOR` | `200`; `PUBLICADO -> FINALIZADO` tras la fecha/hora fin | `400`, `401`, `403`, `404`, `409` |
| `GET /api/v1/eventos/categoria/{id}` | Publico | `200`; publicados de la categoria | `200` con lista vacia |

Los DTO de escritura no aceptan `organizadorId`, estado ni auditoria. La API impone ownership con el
usuario autenticado y bloqueo pesimista en mutaciones; un organizador ajeno recibe `403`, no puede
publicar, rechazar, cancelar ni finalizar. Los estados validos son exactamente `BORRADOR`, `EN_REVISION`,
`PUBLICADO`, `RECHAZADO`, `CANCELADO` y `FINALIZADO`. Las audiencias son `UAJMS`, `EXTERNO` y `AMBOS`.

El formulario y el servicio validan el rango temporal, los datos fisicos para `PRESENCIAL`, el enlace
HTTP(S) para `VIRTUAL`, capacidad positiva solo cuando es limitada, costo positivo y datos de pago para
`PAGO`, y tipo/horas para certificados curriculares. Estas propiedades solo configuran EVENTO: no crean
inscripciones, pagos, asistencias ni certificados.

Deuda fisica: el esquema sigue gestionado por `ddl-auto`. Un despliegue con datos que contengan el valor
historico `EXTERNA` en `eventos.publico_objetivo` debe convertirlo a `EXTERNO` antes de aplicar el enum
oficial. Esta fase no incorpora Flyway y las pruebas automatizadas no validan PostgreSQL.

### Inscripciones

| Operacion | Autorizacion y alcance | Respuestas relevantes |
|---|---|---|
| `POST /api/v1/inscripciones` | Solo `USUARIO`; el propietario se obtiene del JWT y el request solo admite `eventoId`. | `201`, `400` por evento/configuracion no elegible, `401`, `403`, `404`, `409` por duplicado. |
| `GET /api/v1/inscripciones/mis-inscripciones` | Solo `USUARIO`; devuelve exclusivamente registros propios. | `200`, `401`, `403`. |
| `GET /api/v1/inscripciones/{id}` | `USUARIO` ve la propia; `ORGANIZADOR` solo la perteneciente a un evento propio; `ADMINISTRADOR` posee lectura administrativa. | `200`, `401`, `403`, `404` para recurso inexistente o fuera del alcance. |
| `GET /api/v1/inscripciones/{id}/comprobante` | Solo `USUARIO` propietario. Constancia no tributaria, no factura, proforma ni certificado. | `200`, `401`, `403`, `404`. |
| `PATCH /api/v1/inscripciones/{id}/cancelar` | Solo `USUARIO` propietario y evento `PUBLICADO`; libera cupo bajo bloqueo transaccional. | `200`, `400`, `401`, `403`, `404`, `409`. |
| `GET /api/v1/inscripciones/evento/{eventoId}` | `ORGANIZADOR` solo para evento propio; `ADMINISTRADOR` con lectura administrativa. | `200`, `401`, `403`, `404`. |

Los estados propios de INSCRIPCION son `PENDIENTE_PAGO`, `PENDIENTE_VALIDACION`, `CONFIRMADA` y
`CANCELADA`. Asistencia y rechazo/aprobacion del pago no se representan como estados adicionales de
inscripcion. Un evento gratuito crea directamente una inscripcion `CONFIRMADA` sin `PAGO`; uno pagado
crea `PENDIENTE_PAGO` y un registro de pago con el monto oficial del evento. Al cancelar se bloquea
primero el pago asociado, si existe, para impedir que una carga o validacion concurrente sobrescriba
el estado `CANCELADA`; el pago se conserva como historial y sus mutaciones quedan invalidadas por ese estado.

La combinacion `usuario_id + evento_id` posee restriccion unica. El alta bloquea pesimistamente la fila
del evento antes de comprobar duplicidad y descontar cupo; la cancelacion usa el orden de bloqueo
`PAGO (si existe) -> EVENTO -> INSCRIPCION` antes de devolverlo. Esto evita exceder el ultimo cupo, perder incrementos por
cancelaciones concurrentes en una base que respete `PESSIMISTIC_WRITE`. Las pruebas automatizadas usan
H2/mocks y no constituyen validacion de concurrencia real sobre PostgreSQL.

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
