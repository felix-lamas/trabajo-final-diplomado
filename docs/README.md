# Plataforma Web para la Gestión Integral de Eventos Universitarios - UAJMS

## Descripción
Plataforma centralizada para la administración de eventos académicos, científicos y culturales de la Universidad Autónoma Juan Misael Saracho (Tarija, Bolivia).

## Tecnologías Principales
*   **Backend:** Java 21, Spring Boot 3.3, Spring Security (JWT), Spring Data JPA.
*   **Frontend:** Angular 21, Angular Material, Tailwind CSS.
*   **Base de Datos:** PostgreSQL 16.
*   **Documentación:** OpenAPI 3 / Swagger.

## Estructura del Proyecto

### Backend (`/src/main/java/bo/uajms/eventos`)
*   `core/`: Configuraciones transversales (Seguridad, Excepciones, Swagger).
*   `modulos/`: Contextos de negocio (Eventos, Usuarios, Inscripciones, etc.).
*   `comun/`: Clases base y utilidades compartidas.

### Frontend (`/src/app`)
*   `core/`: Servicios globales, interceptores y guardias.
*   `shared/`: Componentes y utilidades reutilizables.
*   `features/`: Módulos de funcionalidad (Lazy Loading).
*   `layouts/`: Estructuras de página (Público, Privado, Admin).

## Configuración y Ejecución

### Requisitos
*   JDK 21
*   Node.js 20+
*   PostgreSQL 16

### Pasos Iniciales
1. Clonar el repositorio.
2. Configurar la base de datos en `src/main/resources/application.yml`.
3. Ejecutar backend: `mvn spring-boot:run`.
4. Ejecutar frontend: `npm install` y luego `ng serve`.

---
© 2026 UAJMS - Dirección de Tecnologías de Información
