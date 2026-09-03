# Catálogo de evidencias

## Objetivo del documento
Centralizar fuentes rastreables para las conclusiones.

## Información encontrada
| Área | Fuente primaria |
|---|---|
| Identidad/stack | `README.md`, `backend/pom.xml`, `frontend/package.json` |
| Configuración | `backend/src/main/resources/application.yml`, `.env`, `docker/docker-compose.yml` |
| Arquitectura | `backend/src/main/java/bo/uajms/eventos/`, `frontend/src/app/` |
| Seguridad | `core/seguridad/SecurityConfig.java`, `JwtService.java`, `ArchivoSeguroServicio.java` |
| API | `modulos/**/controladores/*.java` y `*Controlador.java` |
| Datos | `modulos/**/entidades/*.java`, `comun/EntidadBase.java` |
| UI | `frontend/src/app/app.routes.ts`, `features/**`, `core/services/**` |
| Diagramas | `docs/design/*.wsd` |
| Documentos/pruebas | `docs/**/*.md`, `postman/*.json` |
| Historia | `.git` mediante `git log`, `git branch` |

## Evidencias
Las rutas de la tabla son la evidencia misma.

## Estado
EXISTENTE.

## Observaciones
Se privilegia código/configuración sobre README o documentación narrativa ante conflictos.

## Inconsistencias
No se incluyen rutas de `backend/target` como evidencia de diseño, porque son compilados.

## Información faltante
NO ENCONTRADO: registros de ejecución, BD accesible y telemetría.
