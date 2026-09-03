# Encuestas de Satisfaccion por Evento

## Reglas implementadas

- Una encuesta por participante y por evento.
- Solo puede responderse cuando el evento ya está finalizado.
- Solo puede responder un participante con asistencia registrada.
- La calificación usa escala de 1 a 5.
- El comentario es opcional.

## API

- `POST /api/encuestas/responder`
- `GET /api/encuestas/evento/{id}`
- `GET /api/encuestas/estadisticas/{id}`

## Swagger

Los endpoints quedan documentados en `EncuestaController` con `@Operation`, `@ApiResponses` y `@SecurityRequirement`.

## Postman

Colección disponible en:

- `postman/encuestas_satisfaccion.postman_collection.json`
