# Informe ejecutivo

## Objetivo del documento
Presentar la síntesis ejecutiva de la auditoría verificable.

## Información encontrada
El resumen siguiente consolida solamente elementos existentes en el repositorio y separa las propuestas.

## 1. Qué es el proyecto
Plataforma web UAJMS para gestión integral de eventos universitarios: SPA Angular + API REST Spring Boot + PostgreSQL configurado.

## 2. Qué problema intenta resolver
Centralizar la gestión de eventos, participantes, inscripciones, pagos, acceso, credenciales, certificación, encuestas y reportes. Evidencia: README, módulos y documentación de visión.

## 3. Qué está implementado
Autenticación, usuarios, catálogos, eventos, inscripciones, pagos/comprobantes, credenciales/QR, control de acceso, asistencia, certificados, encuestas, dashboard y reportes, a nivel de código.

## 4. Qué está parcialmente implementado
Pruebas automatizadas y scripts de base de datos: documentación existe, pero la evidencia fuente es escasa/ausente. Ejecución integral no fue verificada.

## 5. Qué no está implementado
NO ENCONTRADO: app móvil, push, pago electrónico, migraciones SQL, CI/CD visible y requisitos formales de producción.

## 6. Arquitectura real
Monolito modular cliente-servidor por capas: Angular → REST/Spring → servicios/repositorios JPA → PostgreSQL.

## 7. Tecnologías
Java 21, Spring Boot 3.3.0, JPA/Hibernate, Spring Security, JJWT 0.12.5, PostgreSQL 16 Docker, Angular 21, Material, Tailwind, ZXing, iText, Tika y OpenAPI.

## 8. Base de datos
Modelo JPA con 21 tablas de dominio/seguridad localizadas; scripts SQL no localizados.

## 9. APIs
Controladores REST por módulo, Swagger configurado. Conviven rutas `/api` y `/api/v1`.

## 10. Seguridad
JWT, BCrypt, roles, validación y restricciones de archivos. Riesgos: secretos en `.env`/defaults, `ddl-auto:update`, SQL visible y CORS no explícito.

## 11. Estado del frontend
Rutas/módulos para áreas pública, privada, admin y operación; guardas/interceptor presentes. La ejecución visual/responsive no fue verificada.

## 12. Estado del backend
Código modular con controladores, servicios, repositorios, entidades, DTO y mappers; compilados presentes. No se ejecutó.

## 13. Principales flujos
Registro/login, inscripción, pago, control QR, certificado y encuesta.

## 14. Principales problemas
Rutas no uniformes, exposición de configuración de desarrollo, falta de migraciones localizadas, poca evidencia de pruebas fuente y desalineación de una guarda frontend.

## 15. Información útil para la tesis
Arquitectura, módulos, modelo de datos, API, requisitos/documentos, diagramas y evolución Git son fuentes localizadas; métricas/metodología aplicada requieren investigación adicional.

## 16. Información útil para la aplicación móvil
La API ya ofrece la mayor parte de dominios móviles; móvil/push/offline/refresh son propuestas futuras.

## 17. Información que falta investigar
Usuarios reales, producción, BD física/datos, pruebas ejecutadas, métricas, despliegue y requisitos móviles.

## 18. Recomendaciones
Aplicar las propuestas de `24_RECOMENDACIONES.md` tras validación de interesados.

## Evidencias
`README.md`, `backend/pom.xml`, `frontend/package.json`, `application.yml`, controladores/entidades/backend y rutas Angular.

## Estado
EXISTENTE a nivel de código y configuración; ejecución integral NO VERIFICABLE.

## Observaciones
El informe resume los documentos de auditoría, no sustituye la evidencia fuente.

## Inconsistencias
Rutas API heterogéneas y configuración de desarrollo expuesta como se detalla en los documentos 07, 09 y 16.

## Información faltante
Datos operativos, ejecución de pruebas, despliegue productivo y requisitos móviles formales.
