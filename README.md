# Plataforma Web para la Gestión Integral de Eventos Universitarios - UAJMS

Bootstrap oficial del proyecto siguiendo los estándares del **Documento Maestro de Desarrollo (MDD)**.

## Estructura del Repositorio
*   `backend/`: Código fuente de la API REST (Java 21 / Spring Boot 3).
*   `frontend/`: Código fuente de la SPA (Angular 21 / Tailwind CSS).
*   `docker/`: Orquestación de servicios (PostgreSQL, pgAdmin).
*   `database/`: Scripts SQL de inicialización y migraciones.
*   `docs/`: Documentación oficial, MDD y manuales técnicos.
*   `diagramas/`: Archivos .wsd (PlantUML) del diseño técnico.
*   `prompts/`: Guías para generación de código asistida por IA.

## Requisitos Previos
*   Docker & Docker Compose.
*   Java JDK 21.
*   Node.js v20+ / Angular CLI 19+.
*   PostgreSQL 16.

## Guía de Inicio Rápido

### 1. Levantar Servicios (Base de Datos)
```bash
cd docker
docker compose --env-file ../.env up -d
```
pgAdmin queda disponible en `http://localhost:5050`. Sus credenciales se definen exclusivamente en el archivo local `.env`.

### 2. Configuración de Entorno
Copia `.env.example` como `.env` y reemplaza todos los valores de ejemplo por valores locales seguros. El archivo `.env` no debe subirse al repositorio ni compartirse. El backend lo carga desde la raíz del proyecto al ejecutarse desde `backend/`.

`DEMO_PASSWORD` se usa únicamente para los usuarios iniciales de demostración/desarrollo creados por el seeder. Debe configurarse con un valor local y nunca reutilizarse como credencial de producción.

En PowerShell, si ejecutas el backend sin Docker, asegúrate de que el servicio PostgreSQL esté disponible con los valores definidos en `.env`.

### 3. Ejecutar Backend
```bash
cd backend
mvn spring-boot:run
```
Swagger UI: `http://localhost:8080/api/v1/swagger-ui.html`

### 4. Ejecutar Frontend
```bash
cd frontend
npm install
ng serve
```
Acceso: `http://localhost:4200`

---
© 2026 Universidad Autónoma Juan Misael Saracho. Todos los derechos reservados.
