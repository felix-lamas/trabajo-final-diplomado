# Pruebas Módulo: Gestión de Eventos

## 1. Escenarios de Prueba Backend (Postman)

### Crear Evento (Borrador)
- **POST** `/api/v1/eventos`
- **Body:**
```json
{
  "titulo": "Primer Congreso de Ingeniería Informática",
  "descripcion": "Evento principal de la facultad...",
  "categoriaId": "{{categoria_id}}",
  "modalidad": "PRESENCIAL",
  "tipoInscripcion": "PAGO",
  "costo": 50.00,
  "fechaInicio": "2026-08-15",
  "fechaFin": "2026-08-17",
  "cupoMaximo": 200
}
```

### Publicar Evento
- **PATCH** `/api/v1/eventos/{{evento_id}}/publicar`
- **Resultado:** Estado cambia de `BORRADOR` a `PUBLICADO`. Habilita visualización pública.

### Validaciones de Negocio
1. **Costo en Gratis:** Intentar crear evento `GRATUITO` con `costo: 10`. Esperado: `400 Bad Request`.
2. **Edición Restringida:** Intentar editar evento `PUBLICADO`. Esperado: `400 Bad Request`.
3. **Cupos:** Intentar reducir `cupoMaximo` por debajo de inscritos actuales. Esperado: `400 Bad Request`.

## 2. Escenarios de Prueba Frontend

### Flujo de Creación
1. Navegar a `/admin/eventos/nuevo`.
2. Seleccionar "Gratuito". Verificar que el campo "Costo" se deshabilita y marca 0 automatically.
3. Llenar campos obligatorios y guardar. Verificar `SnackBar` de éxito y redirección.

### Ciclo de Vida en Detalle
1. Ver detalle de un evento en `BORRADOR`.
2. Hacer clic en "Publicar". Confirmar que los botones de "Editar" y "Eliminar" desaparecen y aparecen "Cancelar" y "Finalizar".
3. Verificar barra de progreso de cupos.
