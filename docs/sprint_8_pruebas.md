# Casos de Prueba Postman - Sprint 8: Dashboard, Reportes y Analítica

Esta es la especificación técnica para la colección de pruebas en Postman para validar la integridad de la capa analítica.

## Variables de Colección
*   `{{baseUrl}}`: `http://localhost:8080/api`
*   `{{token_admin}}`: JWT token del usuario con rol `ADMIN`.
*   `{{token_organizador}}`: JWT token del usuario con rol `ORGANIZADOR`.

---

## 1. Dashboards Ejecutivos y Operativos

### Caso 1.1: Dashboard Ejecutivo (KPIs Maestros)
*   **URL:** `{{baseUrl}}/dashboard/ejecutivo`
*   **Respuesta Esperada (200 OK):**
    ```json
    {
      "totalEventos": 25,
      "totalUsuarios": 1540,
      "totalParticipantes": 1540,
      "totalInscripciones": 842,
      "totalCertificados": 120,
      "ingresosGenerados": 45600.50
    }
    ```

### Caso 1.2: Dashboard Académico (Distribución Facultativa)
*   **URL:** `{{baseUrl}}/dashboard/academico`
*   **Respuesta Esperada (200 OK):** Listado de mapas `name/value` para gráficas.

---

## 2. Reportes Maestros de Datos

### Caso 2.1: Reporte de Eventos
*   **URL:** `{{baseUrl}}/reportes/eventos`
*   **Respuesta Esperada (200 OK):**
    ```json
    {
      "tipoReporte": "EVENTOS",
      "totalRegistros": 2,
      "filas": [
        { "titulo": "IA Summit 2026", "estado": "PUBLICADO", "cupos": 200 },
        { "titulo": "Taller Java 21", "estado": "FINALIZADO", "cupos": 50 }
      ]
    }
    ```

---

## 3. Exportación Institucional de Archivos

### Caso 3.1: Exportar PDF de Pagos
*   **URL:** `{{baseUrl}}/reportes/exportar/pdf?tipo=pagos`
*   **Respuesta Esperada (200 OK):** Archivo binario `application/pdf`.
*   **Header Check:** `Content-Disposition: attachment; filename=reporte_pagos.pdf`

### Caso 3.2: Exportar Excel de Participantes
*   **URL:** `{{baseUrl}}/reportes/exportar/excel?tipo=participantes`
*   **Respuesta Esperada (200 OK):** Archivo binario `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`.
