# DOCUMENTO MAESTRO DE DESARROLLO (MDD)
## Proyecto: Plataforma Web para la Gestión Integral de Eventos Universitarios - UAJMS

**Versión:** 1.0  
**Estado:** Oficial / Fuente Única de Verdad  
**Idioma Oficial:** Español  

---

## 1. Resumen Ejecutivo
La Plataforma de Eventos UAJMS es una solución empresarial diseñada para centralizar, digitalizar y optimizar la gestión de eventos académicos y culturales de la Universidad Autónoma Juan Misael Saracho. El sistema cubre todo el ciclo de vida del evento: desde la creación y gestión de cupos, hasta la validación de pagos, control de acceso mediante códigos QR y la emisión de certificados digitales verificables.

## 2. Arquitectura Oficial
*   **Estilo:** Monolito Modular Profesional.
*   **Backend:** Java 21, Spring Boot 3.3, Spring Security (Stateless/JWT), Spring Data JPA.
*   **Frontend:** Angular 21, Tailwind CSS, Angular Material.
*   **Base de Datos:** PostgreSQL 16 (Normalización 3FN).
*   **Estrategia de Persistencia:** UUID como Clave Primaria (PK), Auditoría vía JSONB, Borrado Lógico (Soft Delete).

## 3. Catálogo Oficial de Módulos (Backend)
1.  **Módulo Seguridad:** Autenticación JWT y autorización basada en permisos.
2.  **Módulo Usuarios:** Gestión de perfiles internos (UAJMS) y externos.
3.  **Módulo Catálogo:** Administración de eventos, categorías y organización académica.
4.  **Módulo Inscripciones:** Gestión de solicitudes y control de cupos.
5.  **Módulo Finanzas:** Validación de pagos y carga de comprobantes.
6.  **Módulo Asistencia:** Generación de QR y registro de marcas de entrada/salida.
7.  **Módulo Certificación:** Motor de generación de PDF y validación pública.
8.  **Módulo Soporte:** Notificaciones y logs de auditoría.

## 4. Catálogo Oficial de Entidades (Base de Datos)
*   `usuario`, `rol`, `permiso`, `usuario_rol`, `rol_permiso`
*   `facultad`, `carrera`
*   `categoria_evento`, `evento`
*   `inscripcion`, `pago`
*   `asistencia`, `certificado`
*   `auditoria`, `notificacion`

## 5. Seguridad: Roles y Permisos
*   **ADMIN:** Gestión institucional total y auditoría.
*   **ORGANIZADOR:** Gestión de eventos y reportes de su facultad.
*   **VALIDADOR_FINANCIERO:** Aprobación de comprobantes de pago.
*   **PERSONAL_CONTROL:** Registro de asistencia vía escaneo QR.
*   **PARTICIPANTE (ESTUDIANTE/EXTERNO):** Inscripción y descarga de certificados.

## 6. Reglas de Negocio Críticas
*   **RN-01 (Unicidad):** Un usuario solo puede tener una inscripción activa por evento.
*   **RN-02 (Validación de Pago):** El acceso al QR y credencial está bloqueado hasta la aprobación del pago.
*   **RN-03 (Asistencia Mínima):** La certificación requiere el 80% de asistencia registrada (configurable).
*   **RN-04 (Inmutabilidad):** Los certificados generados no pueden ser editados; solo anulados y regenerados.

## 7. Convenciones de Nomenclatura
*   **BD:** `snake_case` (ej. `fecha_inicio`).
*   **Backend (Java):** `PascalCase` para clases, `camelCase` para métodos/variables.
*   **Frontend (TS):** `camelCase` para variables, `kebab-case` para nombres de archivos.

## 8. Roadmap de Implementación (MVP)
*   **Sprint 1:** Seguridad, Usuarios y Base Técnica.
*   **Sprint 2:** Catálogo de Eventos e Inscripción base.
*   **Sprint 3:** Gestión de Pagos y Control de Acceso (QR).
*   **Sprint 4:** Motor de Certificación y Reportes Finales.

## 9. Decisiones Arquitectónicas (ADR)
1.  **Uso de UUID:** Para garantizar la no predictibilidad de registros y facilitar la portabilidad de datos.
2.  **Soft Delete:** No se eliminan registros financieros u operativos; se marcan con `fecha_eliminacion`.
3.  **Idioma Español:** Normalización total de código y datos para facilitar el mantenimiento por personal de la UAJMS.

## 10. Checklist de Cumplimiento Técnico
*   [ ] ¿La entidad hereda de `EntidadBase`?
*   [ ] ¿La lógica de negocio reside en la capa de `Servicio`?
*   [ ] ¿El controlador usa exclusivamente `DTOs`?
*   [ ] ¿El código sigue la nomenclatura oficial en español?
*   [ ] ¿Se ha registrado la acción en la tabla de `auditoria`?

---
**Aprobado por:** Arquitecto Principal de Software  
**Fecha de Emisión:** 16 de Junio de 2026
