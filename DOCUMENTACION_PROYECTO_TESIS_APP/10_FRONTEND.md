# Interfaz web

## Objetivo del documento
Inventariar rutas y componentes Angular existentes.

## Información encontrada
| Área/rutas | Pantallas verificables | Estado |
|---|---|---|
| Auth | login, registro, recuperar/restablecer contraseña | EXISTENTE |
| Público | landing, catálogo, detalle, inscripción | EXISTENTE |
| Privado | dashboard, perfil, inscripciones, pagos, credenciales | EXISTENTE |
| Administración | dashboard, facultades, carreras, categorías, eventos, validar pagos | EXISTENTE |
| Operación | escaneo QR, historial y lista de asistencia | EXISTENTE |
| Otros | certificados, encuestas, reportes/dashboard | EXISTENTE |

`authGuard`, `roleGuard` y `jwtInterceptor` protegen/navegan en el cliente. Las rutas administrativas requieren `ADMINISTRADOR`; asistencias permite administrador/personal de control; reportes administrador/organizador. Servicios bajo `core/services` representan el acceso HTTP por dominio.

## Evidencias
`frontend/src/app/app.routes.ts`; módulos en `features/**`; `core/guards/*`, `core/interceptors/jwt.interceptor.ts`.

## Estado
EXISTENTE en código fuente.

## Observaciones
Las rutas se cargan de forma diferida mediante `loadChildren`/`loadComponent`; Material/Tailwind se declaran como dependencias.

## Inconsistencias
La guardia de frontend para asistencias no incluye ORGANIZADOR aunque el backend sí lo autoriza; la interfaz puede ocultar acceso permitido por API.

## Información faltante
NO VERIFICABLE: responsive real, accesibilidad, comportamiento visual, manejo de errores y cobertura e2e sin ejecutar la SPA.
