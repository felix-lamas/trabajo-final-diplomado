# Documento de Arquitectura

## 1. Vision general
La Plataforma Web para la Gestion Integral de Eventos Universitarios - UAJMS es un sistema empresarial para centralizar la administracion de eventos academicos, cientificos y culturales. El sistema cubre desde la publicacion del evento hasta la inscripcion, validacion de pagos, control de acceso, emision de credenciales y certificados, asi como encuestas de satisfaccion y reportes ejecutivos.

La arquitectura fue definida para priorizar mantenibilidad, seguridad, trazabilidad y evolucion incremental sin romper el dominio funcional.

## 2. Arquitectura monolitica modular
El sistema adopta un monolito modular. Esto significa que el despliegue es unico, pero el codigo se organiza por contextos funcionales claramente delimitados.

Ventajas:
- Menor complejidad operativa que una arquitectura distribuida.
- Coherencia transaccional en procesos que cruzan modulos.
- Evolucion por dominios sin acoplamiento excesivo.
- Facil auditoria y despliegue en entornos academicos e institucionales.

Modulos principales:
- Seguridad y autenticacion
- Usuarios
- Facultades y carreras
- Categorias y eventos
- Inscripciones
- Pagos
- Codigo QR y control de acceso
- Asistencia
- Credenciales
- Certificados
- Reportes y dashboard
- Encuestas de satisfaccion

## 3. Justificacion tecnologica
### Backend
Se utiliza Java 21 con Spring Boot 3.3 por madurez, soporte enterprise, fuerte tipado, ecosistema de seguridad y persistencia, y capacidad de organizar el dominio mediante capas.

### Frontend
Se utiliza Angular 21 con Angular Material y Tailwind CSS por su capacidad de construir interfaces reactivas, escalables y adecuadas para workflows administrativos.

### Base de datos
PostgreSQL 16 fue seleccionado por robustez ACID, soporte nativo a UUID, JSONB, indices compuestos y capacidades analiticas suficientes para la capa operacional del sistema.

### Seguridad
Spring Security con JWT permite un modelo stateless, adecuado para API REST y para la integracion de frontend web.

## 4. Backend
El backend esta estructurado en:
- `core`: configuracion transversal, seguridad, excepciones y Swagger.
- `comun`: clases base compartidas.
- `modulos`: contextos de negocio independientes.

Patrones observados:
- Controladores REST delgados.
- Servicios con reglas de negocio.
- Repositorios con Spring Data JPA.
- DTOs para entrada y salida.
- Entidades con auditoria y soft delete.

## 5. Frontend
El frontend Angular se organiza por:
- `core`: servicios globales, modelos y acceso a API.
- `features`: modulos funcionales con carga diferida.
- `shared`: componentes reutilizables.

Se prioriza:
- Lazy loading por modulo funcional.
- Separacion entre pantallas publicas, privadas y administrativas.
- Uso de componentes Material para formularios, tablas, alertas y paneles.

## 6. PostgreSQL
PostgreSQL soporta:
- Claves primarias UUID.
- Restricciones de unicidad.
- Relaciones uno a uno, uno a muchos y muchos a muchos.
- Borrado logico con `fecha_eliminacion`.
- Indices compuestos para filtros criticos.

El modelo permanece orientado a normalizacion y consistencia operativa.

## 7. Seguridad JWT
La autenticacion se basa en JWT sin estado.

Flujo:
1. Usuario autentica con correo y contrasena.
2. El backend valida credenciales y genera JWT.
3. El frontend almacena el token y lo envia en `Authorization: Bearer`.
4. Los filtros de seguridad validan token y contexto de usuario.

El JWT permite control de acceso por rol y reduce dependencia de sesiones de servidor.

## 8. Control de acceso
El control de acceso combina:
- Roles funcionales.
- Permisos de operacion.
- Validacion de estado del participante.
- Codigo QR unico para validacion presencial.
- Reglas de asistencia y pago previas.

Este esquema evita accesos no autorizados a eventos, certificados y recursos privados.
