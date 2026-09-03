# Inventario del proyecto

## Objetivo del documento
Registrar los componentes físicamente localizados y sus ubicaciones.

## Información encontrada
| Elemento | Ubicación | Descripción | Estado |
|---|---|---|---|
| Backend | `backend/src/main/java` | API REST Java/Spring Boot organizada por módulos | Encontrado |
| Configuración backend | `backend/pom.xml`, `backend/src/main/resources/application.yml` | dependencias, BD, multipart, JWT y OpenAPI | Encontrado |
| Frontend | `frontend/src/app` | SPA Angular con rutas, módulos, servicios y guardas | Encontrado |
| Configuración frontend | `frontend/package.json`, `frontend/angular.json` | Angular, Material, Tailwind, scripts | Encontrado |
| Base de datos | `database/scripts/` | carpeta de scripts, sin archivos localizados | Parcial |
| Docker | `docker/docker-compose.yml` | PostgreSQL 16 y pgAdmin | Encontrado |
| Documentación | `docs/`, `documentacion/PORTADA.docx` | MDD, pruebas, release y diseño | Encontrado |
| Diagramas fuente | `docs/design/*.wsd` | 14 archivos PlantUML/WSd | Encontrado |
| Colecciones API | `postman/*.postman_collection.json` | recuperación y encuestas | Encontrado |
| Automatización CI/CD | `.github/` | carpeta localizada; workflow no localizado | No verificable |

Se localizaron 370 archivos rastreables por `rg --files` excluyendo dependencias/compilados y 493 directorios totales; el segundo conteo incluye directorios generados, por lo que no representa solamente código fuente.

## Evidencias
Inventario de archivos de la auditoría; rutas de la tabla.

## Estado
EXISTENTE.

## Observaciones
`docs/` y `documentacion/` son fuentes documentales, no implementación.

## Inconsistencias
`database/scripts` está vacío en la exploración; el modelo ejecutable reside en entidades JPA.

## Información faltante
NO ENCONTRADO EN EL PROYECTO: migraciones SQL, Dockerfile, compose de aplicación, pipeline CI/CD visible, OpenAPI estático.
