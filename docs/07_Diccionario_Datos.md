# Diccionario de Datos Estandarizado (Español)

### Tabla: usuario (usuario)
| Columna | Tipo | Descripción |
| :--- | :--- | :--- |
| id | UUID | Clave Primaria. |
| correo_electronico | VARCHAR(100) | Email del usuario. |
| contrasena | VARCHAR(255) | Hash de seguridad. |
| nombres | VARCHAR(50) | Nombre(s). |
| apellidos | VARCHAR(50) | Apellido(s). |
| ci | VARCHAR(20) | Cédula de Identidad. |
| tipo_usuario | VARCHAR(20) | INTERNO o EXTERNO. |

### Tabla: evento (evento)
| Columna | Tipo | Descripción |
| :--- | :--- | :--- |
| id | UUID | Clave Primaria. |
| categoria_id | UUID | FK -> categoria_evento. |
| organizador_id | UUID | FK -> usuario. |
| titulo | VARCHAR(200) | Nombre del evento. |
| fecha_inicio | TIMESTAMPTZ | Inicio del evento. |
| cupo | INTEGER | Cantidad máxima de participantes. |
| estado | VARCHAR(20) | BORRADOR, PUBLICADO, FINALIZADO. |

### Tabla: inscripcion (inscripcion)
| Columna | Tipo | Descripción |
| :--- | :--- | :--- |
| id | UUID | Clave Primaria. |
| usuario_id | UUID | Participante. |
| evento_id | UUID | Evento vinculado. |
| estado | VARCHAR(20) | PENDIENTE, APROBADO, RECHAZADO. |
| datos_qr | TEXT | Token de acceso. |

### Tabla: pago (pago)
| Columna | Tipo | Descripción |
| :--- | :--- | :--- |
| id | UUID | Clave Primaria. |
| inscripcion_id | UUID | Vinculo a inscripción. |
| monto | NUMERIC(12,2) | Valor pagado. |
| url_comprobante | VARCHAR(255) | Link al archivo. |
| numero_referencia | VARCHAR(50) | Nro de transacción. |
| estado | VARCHAR(20) | PENDIENTE, VALIDADO, RECHAZADO. |
