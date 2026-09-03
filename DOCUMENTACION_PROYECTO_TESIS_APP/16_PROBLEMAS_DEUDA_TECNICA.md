# Problemas y deuda técnica

## Objetivo del documento
Registrar riesgos observables, no fallos hipotéticos.

## Información encontrada
| Tipo | Hallazgo | Evidencia | Estado |
|---|---|---|---|
| Seguridad | secretos/credenciales de desarrollo en `.env` y defaults | `.env`, `application.yml` | EXISTENTE |
| Configuración | `ddl-auto:update` y SQL visible | `application.yml` | EXISTENTE |
| API | prefijos inconsistentes `/api`/`/api/v1` | controladores | EXISTENTE |
| UI/API | permisos de asistencias difieren | `app.routes.ts` vs `AsistenciaController` | EXISTENTE |
| Datos | scripts SQL no localizados | `database/scripts/` | EXISTENTE |
| Calidad | baja evidencia de tests fuente | documento 15 | EXISTENTE |

## Evidencias
Véanse los archivos indicados.

## Estado
EXISTENTE como riesgo/deuda; impacto en ejecución NO VERIFICABLE.

## Observaciones
No se declara un error bloqueante porque el sistema no fue ejecutado durante la auditoría.

## Inconsistencias
La documentación previa puede describir más de lo verificable en SQL/tests; no se la presenta como implementación confirmada.

## Información faltante
NO VERIFICABLE: defectos de producción, rendimiento y compatibilidad real.
