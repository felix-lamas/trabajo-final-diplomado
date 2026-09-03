# Resumen del proyecto

## Objetivo del documento
Sintetizar la finalidad y el estado verificable de la plataforma.

## Información encontrada
**EXISTENTE:** el proyecto se denomina *Plataforma Eventos UAJMS* (`backend/pom.xml`) y se describe como plataforma web para gestión integral de eventos universitarios (`README.md`). Implementa una SPA Angular que consume una API REST Spring Boot, con persistencia JPA/PostgreSQL. Los módulos de código incluyen autenticación/usuarios, facultades, carreras, categorías, eventos, inscripciones, pagos, credenciales/QR, control de acceso, asistencias, certificados, encuestas y reportes.

**PARCIAL:** existe evidencia de UI y endpoints para esos módulos, pero no se ejecutaron pruebas integrales ni se verificó una base de datos poblada. El almacenamiento de comprobantes es local configurable (`uploads`).

**NO VERIFICADO:** adopción institucional, usuarios reales, despliegue productivo, métricas y resultados operativos.

## Evidencias
`README.md`; `backend/pom.xml`; `backend/src/main/java/bo/uajms/eventos/modulos/`; `frontend/src/app/features/`.

## Estado
EXISTENTE, con alcance operativo no verificado.

## Observaciones
El repositorio contiene artefactos compilados en `backend/target/`; no se consideran fuente primaria frente a `src/main`.

## Inconsistencias
El README declara Angular 21, pero su requisito menciona Angular CLI 19+.

## Información faltante
NO ENCONTRADO EN EL PROYECTO: indicadores de uso, manual de aceptación final y evidencia de ambiente productivo.
