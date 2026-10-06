# Colecciones Postman

Las colecciones aquí existentes no son una especificación normativa; el contrato vigente es OpenAPI del backend.

- `encuestas_satisfaccion.postman_collection.json`: **legacy/obsoleta**. El módulo de encuestas no forma parte de las capacidades actuales confirmadas; no ejecutar como flujo vigente.
- `recuperacion_contrasena.postman_collection.json`: material de referencia que debe verificarse contra `/api/v1` y respuestas actuales antes de reutilizarse. No contiene evidencia de una ejecución reciente por su sola presencia.

La colección reproducible de aceptación E3 (login, 401, 403, caminos feliz/error y salud) queda pendiente de una fase de pruebas. Mantener tokens/contraseñas únicamente en variables locales no versionadas; nunca guardar credenciales reales en una colección compartida.
