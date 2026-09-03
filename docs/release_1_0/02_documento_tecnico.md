# Documento Tecnico

## 1. Estructura de modulos
### Backend
- `modulos/usuarios`
- `modulos/eventos`
- `modulos/categorias`
- `modulos/facultades`
- `modulos/carreras`
- `modulos/inscripciones`
- `modulos/pagos`
- `modulos/asistencias`
- `modulos/control_acceso`
- `modulos/codigo_qr`
- `modulos/credenciales`
- `modulos/certificados`
- `modulos/reportes`
- `modulos/encuestas`

### Frontend
- `features/auth`
- `features/publico`
- `features/admin`
- `features/privado`
- `features/pagos`
- `features/credenciales`
- `features/certificados`
- `features/asistencias`
- `features/reportes`
- `features/encuestas`

## 2. Entidades
Entidades principales del dominio:
- Usuario
- Rol
- Permiso
- UsuarioRol
- Facultad
- Carrera
- CategoriaEvento
- Evento
- Inscripcion
- Pago
- ComprobantePago
- Asistencia
- CodigoQr
- Credencial
- Certificado
- TokenRecuperacion
- Encuesta
- PreguntaEncuesta
- RespuestaEncuesta

## 3. Relaciones
- Un usuario puede pertenecer a una carrera.
- Una facultad tiene muchas carreras.
- Una categoria agrupa muchos eventos.
- Un evento tiene un organizador.
- Un usuario puede inscribirse en muchos eventos.
- Una inscripcion puede tener un pago.
- Un pago puede tener un comprobante.
- Una inscripcion puede generar asistencia, credencial y certificado.
- Un evento puede tener muchas encuestas.
- Una encuesta pertenece a un usuario, evento e inscripcion.
- Una encuesta tiene respuestas por pregunta.

## 4. API REST
La API se expone por recursos tematicos:
- Autenticacion y usuarios
- Catalogo academico
- Eventos
- Inscripciones
- Pagos
- Asistencia y control de acceso
- Credenciales y certificados
- Reportes y dashboard
- Encuestas

Principios:
- REST sobre HTTP.
- DTOs para entrada y salida.
- Codigos de respuesta coherentes.
- Documentacion OpenAPI/Swagger.

## 5. Seguridad
- JWT stateless.
- Roles y permisos.
- Validaciones por Bean Validation.
- Manejo global de excepciones.
- Recuperacion de contrasena con token temporal de un solo uso.
- Subida segura de archivos con control de tipo, extension y tamano.

## 6. Dashboard
El dashboard resume:
- Total de eventos.
- Usuarios y participantes.
- Inscripciones.
- Certificados.
- Ingresos.
- Indicadores operativos.
- Nivel de satisfaccion.
- Participacion de encuestas.
- Promedio de satisfaccion por evento.

## 7. Consideraciones tecnicas
- UUID como identificador principal.
- Soft delete para registros operativos.
- Angular con carga por modulo.
- PostgreSQL con indices compuestos en rutas criticas.
