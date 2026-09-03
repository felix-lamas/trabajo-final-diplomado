# Auditoría inicial del proyecto

Fecha de auditoría: septiembre de 2026.

## Estado general

El proyecto es una plataforma web para la gestión de eventos universitarios UAJMS. Está organizado como monorepositorio con backend Spring Boot, frontend Angular, PostgreSQL mediante Docker Compose y documentación técnica.

## Tecnologías verificadas

- Java 21 y Spring Boot 3.3.0 para la API REST.
- Angular 21, TypeScript y Angular Material para la SPA.
- PostgreSQL 16 para persistencia.
- Spring Security, JWT y BCrypt para autenticación y autorización.
- JPA/Hibernate, OpenAPI, ZXing, iText y Apache Tika.

## Funcionalidades verificadas

- Autenticación, registro, recuperación de contraseña, perfiles y roles.
- Gestión de facultades, carreras, categorías y eventos.
- Inscripciones, pagos y comprobantes.
- Credenciales, códigos QR, control de acceso y asistencias.
- Certificados, encuestas, dashboards y reportes.

## Hallazgos iniciales

1. Se detectaron archivos de configuración sensible versionados: `.env`, `application.yml` y `docker/docker-compose.yml`.
2. `backend/target/` estaba versionado, aunque contiene artefactos compilados.
3. No existen scripts SQL ni migraciones en `database/scripts/`; el esquema depende de entidades JPA.
4. Se detectó una prueba frontend mínima y no se detectaron pruebas automatizadas backend.
5. No se detectaron CI/CD, Dockerfiles de aplicación ni despliegue productivo verificable.
6. El historial Git contiene tres commits y una implementación amplia concentrada en el último.

## Recomendación inicial

Antes de publicar un repositorio oficial deben sanearse secretos y artefactos versionados, definir migraciones de base de datos, reforzar las pruebas y preparar configuración de despliegue. Este documento registra la auditoría inicial y no sustituye la validación funcional posterior.
