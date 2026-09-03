# Tecnologías y versiones

## Objetivo del documento
Documentar tecnologías declaradas y su evidencia.

## Información encontrada
| Tecnología | Versión | Función | Evidencia | Estado |
|---|---:|---|---|---|
| Java | 21 | backend | `backend/pom.xml` | EXISTENTE |
| Spring Boot | 3.3.0 | API REST | `pom.xml` | EXISTENTE |
| Spring Security/JPA/Validation/Mail | gestionada por Boot | seguridad, persistencia, validación, correo | `pom.xml` | EXISTENTE |
| PostgreSQL | 16-alpine | BD en contenedor | `docker/docker-compose.yml` | EXISTENTE |
| Hibernate/JPA | gestionada por Boot | ORM | `pom.xml`, entidades | EXISTENTE |
| JJWT | 0.12.5 | JWT | `pom.xml` | EXISTENTE |
| springdoc OpenAPI | 2.5.0 | Swagger/OpenAPI | `pom.xml` | EXISTENTE |
| ZXing | 3.5.3 | QR | `pom.xml` | EXISTENTE |
| iText 7 | 8.0.4 | PDF de credenciales | `pom.xml` | EXISTENTE |
| Apache Tika | 2.9.2 | validación de archivos | `pom.xml` | EXISTENTE |
| Angular | ^21.0.0 | SPA | `frontend/package.json` | EXISTENTE |
| Angular Material/CDK | ^21.0.0 | interfaz | `frontend/package.json` | EXISTENTE |
| Tailwind CSS | ^4.1.12 | estilos | `frontend/package.json` | EXISTENTE |
| TypeScript | ~5.9.3 | frontend | `frontend/package.json` | EXISTENTE |
| Vitest | ^4.0.8 | prueba frontend declarada | `frontend/package.json` | PARCIAL |

## Evidencias
`backend/pom.xml`; `frontend/package.json`; `docker/docker-compose.yml`.

## Estado
EXISTENTE.

## Observaciones
Las versiones con `^` o `~` son rangos declarados, no versiones efectivamente instaladas verificadas.

## Inconsistencias
`README.md` solicita Node 20+/CLI 19+, pero `package.json` corresponde a Angular 21 y npm 11.6.3.

## Información faltante
NO ENCONTRADO EN EL PROYECTO: versión real del servidor de producción o de PostgreSQL fuera de Docker.
