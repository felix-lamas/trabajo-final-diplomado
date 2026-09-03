# 14. Restricciones Técnicas

El desarrollo debe ceñirse estrictamente a las siguientes definiciones tecnológicas:

## 14.1. Infraestructura y Backend
*   **Lenguaje:** Java 21 (LTS) - Uso obligatorio de Virtual Threads si se requiere alta concurrencia.
*   **Framework:** Spring Boot 3.x.
*   **Seguridad:** Spring Security con Stateless Authentication (JWT).
*   **Base de Datos:** PostgreSQL 16+.
*   **Persistencia:** Spring Data JPA / Hibernate.

## 14.2. Frontend
*   **Framework:** Angular 21.
*   **Lenguaje:** TypeScript 5+.
*   **Estilos:** Tailwind CSS (para diseño rápido y responsivo) y Angular Material (para componentes de formulario y tablas).
*   **Arquitectura:** SPA (Single Page Application) con Lazy Loading por módulos.

## 14.3. Arquitectura de Software
*   **Patrón:** Monolítico Modular. No se permiten dependencias circulares entre módulos.
*   **Comunicación:** REST API con formato JSON.
*   **Documentación de API:** OpenAPI / Swagger (integrado en Spring Boot).

## 14.4. Calidad y Proceso
*   **Control de Versiones:** Git con flujo GitFlow o GitHub Flow.
*   **Entorno:** Los servidores de la universidad operan bajo Linux (Ubuntu/Debian), por lo que la solución debe ser compatible con entornos Unix-like.
