# Casos de Prueba Postman - Sprint 7: Certificados Digitales y Validación Pública

Esta es la especificación técnica para la colección de pruebas en Postman para validar las reglas de negocio de certificación y validación pública.

## Variables de Colección
*   `{{baseUrl}}`: `http://localhost:8080/api`
*   `{{token_organizador}}`: JWT token del usuario con rol `ORGANIZADOR` o `ADMIN`.
*   `{{token_participante}}`: JWT token del usuario con rol `PARTICIPANTE`.

---

## 1. Generar Certificado (`POST /api/certificados/generar/{inscripcionId}`)

### Caso 1.1: Generación Exitosa (Cumple con asistencia y evento FINALIZADO)
*   **URL:** `{{baseUrl}}/certificados/generar/f47ac10b-58cc-4372-a567-0e02b2c3d4e5`
*   **Headers:** `Authorization: Bearer {{token_organizador}}`
*   **Respuesta Esperada (200 OK):**
    ```json
    {
      "id": "c7b2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f",
      "nombreCompleto": "Juan Pérez Miranda",
      "ci": "7432189",
      "evento": "Congreso Internacional de IA 2026",
      "cargaHoraria": 30,
      "codigoCertificado": "UAJMS-CERT-2026-B8E49A2D",
      "fechaEmision": "2026-06-18T11:45:00",
      "urlVerificacion": "http://localhost:4200/publico/verificacion/UAJMS-CERT-2026-B8E49A2D",
      "estado": "GENERADO",
      "archivoPdfUrl": "https://storage.uajms.edu.bo/certificados/UAJMS-CERT-2026-B8E49A2D.pdf"
    }
    ```

### Caso 1.2: Rechazo por Asistencia Insuficiente
*   **URL:** `{{baseUrl}}/certificados/generar/a8b9c0d1-e2f3-4a5b-6c7d-8e9f0a1b2c3d`
*   **Headers:** `Authorization: Bearer {{token_organizador}}`
*   **Respuesta Esperada (400 Bad Request):**
    ```json
    {
      "mensaje": "El participante no cumple con el requisito mínimo de asistencia para recibir la certificación.",
      "codigo": "NEGOCIO_ERROR"
    }
    ```

### Caso 1.3: Rechazo por Evento No Finalizado
*   **URL:** `{{baseUrl}}/certificados/generar/b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e`
*   **Headers:** `Authorization: Bearer {{token_organizador}}`
*   **Respuesta Esperada (400 Bad Request):**
    ```json
    {
      "mensaje": "No se pueden emitir certificados para un evento que no esté FINALIZADO.",
      "codigo": "NEGOCIO_ERROR"
    }
    ```

---

## 2. Descargar Certificado (`GET /api/certificados/{id}/descargar`)

### Caso 2.1: Transición de Estado a DESCARGADO
*   **URL:** `{{baseUrl}}/certificados/c7b2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f/descargar`
*   **Headers:** `Authorization: Bearer {{token_participante}}`
*   **Respuesta Esperada (200 OK):** Retorna el objeto certificado actualizando la propiedad `"estado"` a `"DESCARGADO"`.

---

## 3. Validación Pública (`GET /api/verificacion-certificados/{codigo}`)

### Caso 3.1: Verificación Exitosa de Código Válido (Accesible sin Token)
*   **URL:** `{{baseUrl}}/verificacion-certificados/UAJMS-CERT-2026-B8E49A2D`
*   **Respuesta Esperada (200 OK):**
    ```json
    {
      "valido": true,
      "mensaje": "Certificado VÁLIDO y autenticado por la Universidad Autónoma Juan Misael Saracho.",
      "nombreCompleto": "Juan Pérez Miranda",
      "ci": "7432189",
      "evento": "Congreso Internacional de IA 2026",
      "cargaHoraria": 30,
      "fechaEmision": "2026-06-18T11:45:00",
      "codigoCertificado": "UAJMS-CERT-2026-B8E49A2D",
      "estado": "DESCARGADO"
    }
    ```

### Caso 3.2: Alerta por Certificado ANULADO
*   **URL:** `{{baseUrl}}/verificacion-certificados/UAJMS-CERT-ANULADO-999`
*   **Respuesta Esperada (200 OK):**
    ```json
    {
      "valido": false,
      "mensaje": "ALERTA: El certificado con este código ha sido ANULADO oficialmente por la institución.",
      "codigoCertificado": "UAJMS-CERT-ANULADO-999",
      "estado": "ANULADO"
    }
    ```
