# PLAN MAESTRO DE FINALIZACIÓN --- PLATAFORMA DE GESTIÓN DE EVENTOS UAJMS

> **Plan de trabajo histórico (E3.4), no fuente de estado verificado.** Las fechas, hitos, entregables y menciones de producción de este plan requieren comprobación actual. Para la base funcional alineada consulte `docs/07_Requerimientos_Funcionales.md`; para estado de implementación, pruebas e infraestructura consulte `docs/plan_maestro/01_estado_actual.md`, `12_pruebas.md`, `13_despliegue.md` y `docs/auditoria/E3_4_ALINEACION_DOCUMENTAL.md`.

**Producto:** Vidia\
**Fecha:** 28 de septiembre de 2026\
**Objetivo:** Finalización integral backend + Angular + Flutter +
Swagger + UX/UI + despliegue.

## 1. Regla principal

Se trabajará entidad por entidad y flujo por flujo. Si una
implementación existente no representa correctamente el negocio, **se
reemplaza**; no se adapta a la fuerza para conservar código.

Cada cambio debe: - reflejar el negocio; - actualizar backend, API y
Swagger; - actualizar Angular y Flutter cuando corresponda; - incluir
pruebas; - generar un commit independiente; - no hacer push sin
autorización.

## 2. Arquitectura objetivo

-   Backend: Java 21 + Spring Boot 3.3.0 + PostgreSQL 16.
-   Web: Angular 21.
-   Móvil: Flutter 3.44.8 / Dart 3.12.2.
-   API REST: `/api/v1`.
-   Arquitectura: monolito modular profesional.
-   Producción:
    -   Backend: `https://trabajo-final-diplomado.onrender.com`
    -   Web: `https://trabajo-final-diplomado-web.onrender.com`
    -   API Flutter:
        `https://trabajo-final-diplomado.onrender.com/api/v1`

## 3. Roles oficiales

### ADMINISTRADOR

Administra usuarios/categorías, aprueba solicitudes de organizador,
revisa/publica/rechaza eventos y supervisa inscripciones, pagos,
asistencia y certificados.

### ORGANIZADOR

Una vez aprobado, crea y administra sus propios eventos, los envía a
revisión, gestiona inscripciones/pagos/sesiones/asistencia/certificados
de sus eventos.

### USUARIO

Participante UAJMS o externo. Consulta eventos, se inscribe, paga cuando
corresponde, registra asistencia, consulta certificados e historial.

## 4. Aprobación de organizadores

Flujo:

`USUARIO → solicitud PENDIENTE → ADMINISTRADOR aprueba → ORGANIZADOR`

Datos demo actuales:

  Cuenta                      Rol             Solicitud
  --------------------------- --------------- -----------
  `admin@demo.local`          ADMINISTRADOR   ---
  `organizador1@demo.local`   ORGANIZADOR     APROBADA
  `organizador2@demo.local`   USUARIO         PENDIENTE
  `usuario@demo.local`        USUARIO         NINGUNA

La implementación actual materializa la aprobación mediante el cambio de
rol de USUARIO a ORGANIZADOR.

## 5. Flujo de eventos

`ORGANIZADOR APROBADO → BORRADOR → EN_REVISIÓN → ADMINISTRADOR → PUBLICADO / RECHAZADO`

También:

`PUBLICADO → FINALIZADO`\
`PUBLICADO → CANCELADO`

El organizador no publica directamente.

## 6. Flujo de inscripción

### Gratuito

`USUARIO → evento PUBLICADO → inscripción → CONFIRMADA`

### Pagado

`USUARIO → PENDIENTE_PAGO → comprobante → PENDIENTE_VALIDACION → ORGANIZADOR → APROBADO/RECHAZADO`

No se implementará pasarela de pagos.

## 7. Flujo de asistencia

`SESION → QR temporal → Flutter escanea → GPS → backend valida JWT + inscripción + sesión + QR + tiempo + precisión + distancia + duplicado → ASISTENCIA`

Reglas conocidas: - QR temporal de 2 minutos. - QR compartido por la
sesión. - Una asistencia por inscripción/sesión. - Precisión GPS máxima
actual: 30 m. - No almacenar coordenadas exactas del participante. -
Radio máximo: 500 m. - Zona horaria: `America/La_Paz`.

## 8. Flujo de certificados

Solo se genera con evento FINALIZADO e inscripción CONFIRMADA. En
eventos pagados se exige pago APROBADO. Los certificados curriculares
requieren asistencia mínima de 80 % y horas académicas. Cada certificado
tiene código único, PDF y verificación pública.

## 9. Entidades conceptuales

1.  USUARIO
2.  EVENTO
3.  CATEGORIA
4.  INSCRIPCION
5.  PAGO
6.  SESION_EVENTO
7.  ASISTENCIA
8.  CERTIFICADO

Entidades técnicas: - rol - permiso - usuario_rol - rol_permiso -
token_recuperacion - qr_asistencia

No se deben convertir entidades técnicas en entidades conceptuales solo
para cumplir cantidades.

## 10. Orden de ejecución

1.  Auditoría integral.
2.  USUARIO/autenticación/roles.
3.  CATEGORIA.
4.  EVENTO.
5.  INSCRIPCION.
6.  PAGO.
7.  SESION_EVENTO.
8.  ASISTENCIA + QR + GPS.
9.  CERTIFICADO.
10. API + Swagger.
11. Angular.
12. Flutter.
13. UX/UI integral.
14. E2E.
15. Despliegue.
16. Auditoría final y documentación.

## 11. Ciclo obligatorio por entidad

`AUDITORÍA → NEGOCIO → BD → ENTITY → REPOSITORY → SERVICE → DTO → CONTROLLER → SEGURIDAD → VALIDACIONES → TESTS → SWAGGER → ANGULAR → FLUTTER → UX/UI → PRUEBA → COMMIT`

Si la estructura actual es incorrecta, se reemplaza.

## 12. Swagger obligatorio

Swagger público objetivo:

`https://trabajo-final-diplomado.onrender.com/api/v1/swagger-ui.html`

OpenAPI:

`https://trabajo-final-diplomado.onrender.com/api/v1/api-docs`

Cada endpoint modificado debe documentar método, ruta, descripción,
autenticación, rol, parámetros, request, response, errores
400/401/403/404/409/500 según corresponda y ejemplos.

Además, Codex debe inventariar **todos los endpoints reales de cada
controlador** y marcar CONSERVAR / MODIFICAR / ELIMINAR / CREAR /
LEGACY.

## 13. APIs principales conocidas

### Auth

`POST /api/v1/auth/login`

### Usuarios

`GET /api/v1/usuarios/perfil`\
`POST /api/v1/usuarios`\
`GET /api/v1/usuarios`\
`GET /api/v1/usuarios/{id}`\
`PATCH /api/v1/usuarios/cambiar-contrasena`\
`POST /api/v1/usuarios/solicitud-organizador`\
`GET /api/v1/usuarios/solicitudes-organizador`\
`PATCH /api/v1/usuarios/solicitudes-organizador/{usuarioId}/aprobar`\
`PATCH /api/v1/usuarios/solicitudes-organizador/{usuarioId}/rechazar`

### Categorías

`GET /api/v1/categorias-evento/activas`\
`POST /api/v1/categorias-evento`\
`PUT /api/v1/categorias-evento/{id}`\
`DELETE /api/v1/categorias-evento/{id}`

### Eventos

`GET /api/v1/eventos`\
`POST /api/v1/eventos`\
`GET /api/v1/eventos/{id}`\
`GET /api/v1/eventos/publicados`\
`GET /api/v1/eventos/publicados/buscar`\
`GET /api/v1/eventos/revision`\
`PATCH /api/v1/eventos/{id}/enviar-revision`\
`PATCH /api/v1/eventos/{id}/publicar`\
`PATCH /api/v1/eventos/{id}/rechazar`\
`PATCH /api/v1/eventos/{id}/volver-borrador`\
`PATCH /api/v1/eventos/{id}/cancelar`\
`PATCH /api/v1/eventos/{id}/finalizar`

### Inscripciones

`POST /api/v1/inscripciones`\
`GET /api/v1/inscripciones/{id}`\
`GET /api/v1/inscripciones/mis-inscripciones`\
`GET /api/v1/inscripciones/evento/{eventoId}`\
`PATCH /api/v1/inscripciones/{id}/cancelar`

### Pagos

`POST /api/v1/pagos`\
`POST /api/v1/pagos/{id}/comprobante`\
`GET /api/v1/pagos/{id}`\
`GET /api/v1/pagos/{id}/comprobante`\
`GET /api/v1/pagos/mis-pagos`\
`GET /api/v1/pagos/pendientes`\
`GET /api/v1/pagos`\
`PATCH /api/v1/pagos/{id}/validar`\
`PATCH /api/v1/pagos/{id}/rechazar`

### Sesiones

`POST /api/v1/eventos/{eventoId}/sesiones`\
`GET /api/v1/eventos/{eventoId}/sesiones`\
`GET /api/v1/sesiones/{id}`\
`PUT /api/v1/sesiones/{id}`\
`PATCH /api/v1/sesiones/{id}/estado`\
`GET /api/v1/sesiones/{sesionId}/qr`\
`POST /api/v1/sesiones/{sesionId}/qr/generar`

### Asistencia

`POST /api/v1/asistencias`\
`GET /api/v1/asistencias/evento/{eventoId}`\
`GET /api/v1/asistencias/mis-asistencias`

### Certificados

`POST /api/v1/certificados/generar/{inscripcionId}`\
`GET /api/v1/certificados/{id}`\
`GET /api/v1/certificados/mis-certificados`\
`GET /api/v1/eventos/{eventoId}/certificados`\
`GET /api/v1/certificados/{id}/descargar`\
`GET /api/v1/certificados/verificar/{codigo}`

Antes de modificar cualquier API, Codex debe verificar las rutas reales
del código.

## 14. Angular --- defecto crítico a corregir

Existe una falla donde los datos parecen aparecer únicamente al
redimensionar la ventana, abrir/cerrar un menú u otra acción que provoca
un refresco.

Esto debe eliminarse completamente.

Comportamiento obligatorio:

`Entrar a página → iniciar carga → HTTP → actualizar estado → datos visibles`

No se debe solucionar con un `detectChanges()` arbitrario. Codex debe
encontrar la causa real revisando lifecycle, Signals, Observables, async
pipe, Change Detection, componentes standalone, `@if/@for`,
subscriptions, navegación, caches y errores silenciosos.

Toda pantalla debe tener estados: - LOADING - SUCCESS con datos -
EMPTY - ERROR + reintentar

## 15. UX/UI 2026

Web y Vidia deben compartir una identidad visual coherente:

-   responsive real;
-   mobile-first en experiencia de usuario;
-   jerarquía visual clara;
-   accesibilidad;
-   contraste;
-   feedback inmediato;
-   prevención de doble envío;
-   formularios con validación;
-   navegación consistente;
-   skeleton/loading;
-   empty state;
-   error state;
-   tarjetas de eventos;
-   filtros claros;
-   CTA principal;
-   confirmación de acciones destructivas.

Breakpoints de prueba web: - 1440 px - 1024 px - 768 px - 390 px

Componentes a estandarizar:

`AppShell, Header, Sidebar, BottomNavigation, Button, Input, Select, Modal, Dialog, Toast, Alert, Badge, StatusBadge, Card, EventCard, Loading, Skeleton, EmptyState, ErrorState, ConfirmDialog, Pagination, SearchBar, FilterBar`

No duplicar componentes equivalentes.

## 16. Seguridad

Mantener: - JWT; - BCrypt; - autorización por rol; - ownership; - 401
para autenticación inválida; - 403 para rol insuficiente; - 404 para
recursos fuera de alcance cuando corresponda; - validación; - protección
de archivos; - secretos por variables de entorno; - cero credenciales en
Git.

## 17. Base de datos

PostgreSQL 16.

Antes de Flyway: - verificar esquema físico; - reconciliar JPA; -
revisar FK, índices, unique y checks; - revisar histórico/soft delete; -
crear backup/dump; - recién después definir migraciones.

No crear V1 a ciegas.

## 18. Tests

Por entidad: - reglas de negocio; - validaciones; - transiciones; -
ownership; - roles; - duplicados; - integridad; - 401/403/404/409; -
API; - integración PostgreSQL cuando corresponda; - E2E al cierre.

## 19. Flujo E2 obligatorio

1.  `organizador1@demo.local` inicia sesión.
2.  Crea evento gratuito.
3.  Guarda BORRADOR.
4.  Envía EN_REVISIÓN.
5.  `admin@demo.local` inicia sesión.
6.  Ve eventos en revisión.
7.  Publica.
8.  `usuario@demo.local` inicia sesión.
9.  Ve el evento publicado.
10. Abre detalle.
11. Se inscribe.
12. La inscripción queda CONFIRMADA.

Prueba adicional:

`organizador2@demo.local → USUARIO + PENDIENTE → 403 → admin aprueba → ORGANIZADOR → puede crear`

## 20. Datos demo

Se prepararon 6 eventos:

  Evento                                     Estado
  ------------------------------------------ -------------
  Congreso de Innovación Tecnológica UAJMS   PUBLICADO
  Taller de Desarrollo Web                   PUBLICADO
  Jornada de Emprendimiento Universitario    PUBLICADO
  Seminario de Inteligencia Artificial       EN_REVISIÓN
  Curso de Gestión de Proyectos              EN_REVISIÓN
  Conferencia de Innovación y Tecnología     EN_REVISIÓN

Todos pertenecen a `organizador1@demo.local`, son gratuitos, requieren
inscripción, no emiten certificado y utilizan fechas futuras.

## 21. Commits

Usar commits independientes por fase:

-   `feat(usuario): ...`
-   `feat(categoria): ...`
-   `feat(evento): ...`
-   `feat(inscripcion): ...`
-   `feat(pago): ...`
-   `feat(sesion): ...`
-   `feat(asistencia): ...`
-   `feat(certificado): ...`
-   `feat(api): ...`
-   `fix(web): ...`
-   `feat(web): ...`
-   `feat(mobile): ...`
-   `style(ui): ...`
-   `test(e2e): ...`
-   `docs: ...`

No mezclar fases no relacionadas.

## 22. Reporte obligatorio de Codex

Después de cada fase:

``` text
FASE:
ENTIDAD:

ARCHIVOS CREADOS:
ARCHIVOS MODIFICADOS:
ARCHIVOS ELIMINADOS:
CAMBIOS BD:

ENDPOINTS:
SWAGGER:
SEGURIDAD:

TESTS:
TOTAL:
NUEVOS:
FALLOS:
ERRORES:

BUILD BACKEND:
BUILD ANGULAR:
BUILD FLUTTER:

VERIFICACIÓN FUNCIONAL:

COMMIT:
PUSH:

PENDIENTES:
```

## 23. FASE 0 --- AUDITORÍA DE CIERRE

Antes de modificar otra entidad, Codex debe inspeccionar:

`/backend`\
`/frontend`\
`/flutter/VidiaApp`

Debe entregar:

1.  estado real;
2.  entidades;
3.  controladores;
4.  servicios;
5.  repositorios;
6.  DTOs;
7.  endpoints;
8.  Swagger;
9.  Angular;
10. Flutter;
11. errores;
12. funcionalidades incompletas;
13. duplicaciones;
14. legacy;
15. incompatibilidades;
16. UX/UI;
17. carga inicial Angular;
18. diferencias backend/web;
19. diferencias backend/Flutter;
20. orden exacto de corrección.

La FASE 0 **NO modifica código**.

Debe terminar con:

  ---------------------------------------------------------------------------------
  Entidad/flujo   Backend    Swagger    Angular    Flutter    Estado     Acción
  --------------- ---------- ---------- ---------- ---------- ---------- ----------
  Usuario                                                                

  Categoría                                                              

  Evento                                                                 

  Inscripción                                                            

  Pago                                                                   

  Sesión                                                                 

  Asistencia                                                             

  Certificado                                                            
  ---------------------------------------------------------------------------------

## 24. Criterio de finalización

Una funcionalidad no se considera terminada solamente porque exista en
código.

Debe: 1. ejecutarse; 2. persistir correctamente; 3. tener API; 4. estar
documentada en Swagger; 5. tener seguridad; 6. funcionar desde la
interfaz correspondiente; 7. manejar loading/empty/error; 8. funcionar
en producción; 9. tener pruebas; 10. tener commit identificable.

**Principio final:** primero negocio, después entidad, API y Swagger,
después web/móvil, después UX/UI, pruebas y producción. Si el código
existente impide cumplir correctamente el negocio, se reemplaza en lugar
de forzarlo.
