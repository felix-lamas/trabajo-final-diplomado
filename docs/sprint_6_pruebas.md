# Casos de Prueba Postman - Sprint 6: Control de Acceso y Asistencia

Esta es la especificación técnica para la colección de pruebas en Postman para validar las reglas de negocio de control de acceso.

## Variables de Colección
*   `{{baseUrl}}`: `http://localhost:8080/api`
*   `{{token_control}}`: JWT token del usuario con rol `PERSONAL_CONTROL` o `ADMIN`.

---

## 1. Validar Código QR (`POST /api/control-acceso/validar-qr`)

### Caso 1.1: QR en Estado GENERADO (Válido para ingreso)
*   **Body (JSON):**
    ```json
    {
      "tokenQr": "TOKEN_VALIDO_QR_123"
    }
    ```
*   **Respuesta Esperada (200 OK):**
    ```json
    {
      "credencialId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
      "inscripcionId": "f47ac10b-58cc-4372-a567-0e02b2c3d4e5",
      "fotografiaUrl": "https://storage.uajms.edu.bo/fotos/participante1.jpg",
      "nombreCompleto": "Juan Pérez Miranda",
      "documentoIdentidad": "7432189",
      "carrera": "Ingeniería de Sistemas",
      "facultad": "Facultad de Ciencias y Tecnología",
      "evento": "Congreso Internacional de IA 2026",
      "estadoPago": "VALIDADO",
      "estadoInscripcion": "APROBADO",
      "estadoQr": "GENERADO",
      "codigoParticipante": "UAJMS-EVT-2026-000123",
      "puedeIngresar": true,
      "mensajeValidacion": "Listo para validación visual"
    }
    ```

### Caso 1.2: QR en Estado UTILIZADO (Intento de fraude o reingreso ilegal)
*   **Body (JSON):**
    ```json
    {
      "tokenQr": "TOKEN_YA_USADO_456"
    }
    ```
*   **Respuesta Esperada (200 OK):**
    ```json
    {
      "credencialId": "b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e",
      "puedeIngresar": false,
      "estadoQr": "UTILIZADO",
      "mensajeValidacion": "ERROR: El código QR ya ha sido utilizado."
    }
    ```

---

## 2. Autorizar Ingreso (`POST /api/control-acceso/autorizar`)

### Caso 2.1: Autorización Exitosa
*   **Body (JSON):**
    ```json
    {
      "credencialId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
      "observacion": "Identidad confirmada visualmente. Ingreso OK."
    }
    ```
*   **Respuesta Esperada (200 OK):**
    ```json
    {
      "id": "c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f",
      "nombreParticipante": "Juan Pérez Miranda",
      "evento": "Congreso Internacional de IA 2026",
      "fechaHoraIngreso": "2026-06-18T09:30:15",
      "estadoIngreso": "AUTORIZADO",
      "usuarioControl": "Carlos Operador",
      "observacion": "Identidad confirmada visualmente. Ingreso OK."
    }
    ```

### Caso 2.2: Intento de Autorizar QR ya UTILIZADO (Regla de bloqueo)
*   **Body (JSON):**
    ```json
    {
      "credencialId": "b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e",
      "observacion": "Intento de forzar reingreso"
    }
    ```
*   **Respuesta Esperada (400 Bad Request):**
    ```json
    {
      "mensaje": "No se puede autorizar un QR que no esté en estado GENERADO",
      "codigo": "NEGOCIO_ERROR"
    }
    ```
    *Nota: El sistema registrará de forma automática un evento `REINTENTO` en la tabla `control_acceso` como auditoría.*

---

## 3. Denegar Ingreso Manualmente (`POST /api/control-acceso/denegar`)

*   **Body (JSON):**
    ```json
    {
      "credencialId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
      "observacion": "Fotografía de perfil no coincide con la persona física."
    }
    ```
*   **Respuesta Esperada (200 OK):**
    ```json
    {
      "id": "e5f6a7b8-c9d0-1e2f-3a4b-5c6d7e8f9a0b",
      "nombreParticipante": "Juan Pérez Miranda",
      "estadoIngreso": "DENEGADO",
      "observacion": "Fotografía de perfil no coincide con la persona física."
    }
    ```

---

## 4. Métodos Alternativos de Identificación (Contingencia)

### Caso 4.1: Búsqueda por Código Único de Participante
*   **GET** `/api/control-acceso/codigo/UAJMS-EVT-2026-000123`
*   **Respuesta Esperada (200 OK):** Misma estructura que `ValidarQrResponse` para permitir la posterior confirmación visual por pantalla.

### Caso 4.2: Búsqueda por Documento de Identidad (CI)
*   **GET** `/api/control-acceso/documento/7432189?eventoId=e47ac10b-58cc-4372-a567-0e02b2c3d4e5`
*   **Respuesta Esperada (200 OK):** Ficha del participante para validación visual.
