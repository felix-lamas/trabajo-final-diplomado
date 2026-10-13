# Pruebas Módulo: Inscripciones

## 1. Escenarios de Prueba Backend (Postman)

### Inscribirse a Evento Gratuito
- **POST** `/api/v1/inscripciones`
- **Body:** `{ "eventoId": "{{evento_gratuito_id}}" }`
- **Resultado:** `201 Created`, `estado: "CONFIRMADA"`. El cupo del evento disminuye en 1.

### Inscribirse a Evento de Pago
- **POST** `/api/v1/inscripciones`
- **Body:** `{ "eventoId": "{{evento_pago_id}}" }`
- **Resultado:** `201 Created`, `estado: "PENDIENTE_PAGO"`.

### Intentar Doble Inscripción
- **POST** `/api/v1/inscripciones`
- **Body:** `{ "eventoId": "{{evento_ya_inscrito_id}}" }`
- **Resultado:** `400 Bad Request` ("Ya te encuentras inscrito...").

### Cancelar Inscripción
- **PATCH** `/api/v1/inscripciones/{{inscripcion_id}}/cancelar`
- **Resultado:** `200 OK`. El estado cambia a `CANCELADA` y el cupo del evento se libera (+1).

## 2. Escenarios de Prueba Frontend

### Flujo de Usuario (Estudiante)
1. Navegar al detalle de un evento publicado.
2. Hacer clic en "Inscribirme Ahora".
3. Verificar redirección a "Mis Inscripciones" y ver el nuevo registro en la tabla.
4. Probar el botón de cancelar y confirmar el diálogo. Verificar que el estado cambia visualmente.

## 3. Seguridad
- Intentar inscribirse sin token: `401 Unauthorized`.
- Intentar cancelar inscripción de otro usuario: `400 Bad Request` ("Solo puedes cancelar tus propias inscripciones").
