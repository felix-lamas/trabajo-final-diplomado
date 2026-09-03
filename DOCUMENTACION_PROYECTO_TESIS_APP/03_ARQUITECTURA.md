# Arquitectura real

## Objetivo del documento
Determinar la arquitectura a partir de código y configuración.

## Información encontrada
**EXISTENTE:** arquitectura cliente-servidor y monolito modular por capas. Angular 21 actúa como cliente SPA; los servicios TypeScript llaman a la API; Spring Boot contiene controladores REST, servicios de negocio, repositorios JPA, entidades, DTO y mappers; PostgreSQL es el almacén configurado. Los módulos comparten el mismo proceso y esquema, por lo que **no hay evidencia de microservicios**.

Flujo principal: `componente Angular → servicio HttpClient/interceptor JWT → controlador REST → servicio → repositorio JPA → PostgreSQL → DTO/HTTP`. Los módulos se agrupan en `backend/.../modulos/<modulo>/{controladores,servicios,repositorios,entidades,dtos,mappers}`. Existe núcleo transversal `core/seguridad`, `core/excepciones`, `core/configuracion` y `comun/EntidadBase`.

## Evidencias
`frontend/src/app/core/services/`; `frontend/src/app/core/interceptors/jwt.interceptor.ts`; `backend/src/main/java/bo/uajms/eventos/modulos/`; `SecurityConfig.java`; `application.yml`.

## Estado
EXISTENTE.

## Observaciones
La separación por capas es verificable por las dependencias de controladores a servicios y servicios a repositorios. No se verificó una arquitectura hexagonal/Clean: las entidades JPA forman parte directa de los módulos.

## Inconsistencias
Algunos controladores publican rutas duplicadas `/api` y `/api/v1`, mientras el frontend usa `api/v1`.

## Información faltante
NO ENCONTRADO EN EL PROYECTO: gateway, broker de mensajes, caché distribuida, servicio de archivos externo o diagrama de despliegue ejecutado.
