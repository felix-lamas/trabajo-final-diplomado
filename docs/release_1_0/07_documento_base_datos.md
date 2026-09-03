# Documento de Base de Datos

## 1. Modelo entidad relacion
El modelo se organiza en torno a:
- Seguridad y usuarios.
- Catalogo academico.
- Eventos e inscripciones.
- Operacion financiera.
- Control de acceso.
- Certificacion.
- Encuestas.

## 2. Relaciones principales
- `usuario` se relaciona con `usuario_rol`, `inscripcion`, `evento`, `asistencia`, `credencial`, `certificado`, `encuesta`.
- `evento` se relaciona con `categoria_evento`, `usuario` como organizador, `inscripcion`, `encuesta`.
- `inscripcion` se relaciona con `usuario`, `evento`, `pago`, `asistencia`, `credencial`, `certificado`, `encuesta`.
- `pago` se relaciona con `inscripcion` y `comprobante_pago`.
- `encuesta` se relaciona con `evento`, `usuario`, `inscripcion`, `respuesta_encuesta`.

## 3. Indices
Indices recomendados:
- Correo de usuario.
- CI de usuario.
- Evento por estado.
- Evento por organizador.
- Inscripcion por usuario y evento.
- Pago por estado e inscripcion.
- Asistencia por inscripcion.
- Encuesta por evento y usuario.
- Token de recuperacion por token.

## 4. Optimizaciones
- Uso de UUID como PK.
- Soft delete para preservar trazabilidad.
- Indices compuestos para filtros recurrentes.
- Consulta agregada para dashboard y reportes.
- Restriccion de unicidad donde el dominio lo exige.

## 5. Observaciones operativas
El esquema esta orientado a consistencia y auditoria. Para produccion, se recomienda mantener:
- Estadisticas de consultas lentas.
- Revisiones periodicas de indices.
- Analisis de cardinalidad en tablas de alto crecimiento.
