# Auditoría y Soporte Estandarizado

### Tabla: auditoria (auditoria)
| Columna | Tipo | Descripción |
| :--- | :--- | :--- |
| id | UUID | PK. |
| nombre_tabla | VARCHAR(50) | Tabla afectada. |
| id_registro | UUID | Registro afectado. |
| accion | VARCHAR(10) | INSERTAR, ACTUALIZAR, ELIMINAR. |
| datos_anteriores | JSONB | Estado previo. |
| datos_nuevos | JSONB | Estado posterior. |
| usuario_id | UUID | Responsable del cambio. |

### Tabla: notificacion (notificacion)
| Columna | Tipo | Descripción |
| :--- | :--- | :--- |
| id | UUID | PK. |
| usuario_id | UUID | Destinatario. |
| titulo | VARCHAR(100) | Cabecera. |
| mensaje | TEXT | Cuerpo del aviso. |
| fue_leido | BOOLEAN | Estado de lectura. |
