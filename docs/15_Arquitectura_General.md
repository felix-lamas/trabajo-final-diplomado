# 15. Arquitectura General (Estandarizada)

## 15.1. Estilo: Monolito Modular
*   **Módulo de Seguridad:** Gestión de acceso y roles.
*   **Módulo de Catálogo:** Administración de eventos y categorías.
*   **Módulo de Inscripciones:** Flujo de participantes.
*   **Módulo de Finanzas:** Validación de pagos y comprobantes.
*   **Módulo de Asistencia:** Control de acceso y escaneo QR.
*   **Módulo de Certificación:** Generación y validación de certificados.

## 15.2. Capas Técnicas
1.  **Capa de Interfaz (Angular):** Componentes visuales y lógica de usuario.
2.  **Capa de API (REST):** Endpoints y Controladores.
3.  **Capa de Servicios:** Lógica de negocio y reglas.
4.  **Capa de Datos:** Entidades, Repositorios y PostgreSQL.
