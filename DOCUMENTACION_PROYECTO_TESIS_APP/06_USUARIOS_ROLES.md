# Usuarios y roles

## Objetivo del documento
Registrar roles observables en autorizaciones y rutas.

## Información encontrada
| Rol | Permisos verificables | Evidencia |
|---|---|---|
| ADMINISTRADOR | catálogos, usuarios, eventos, pagos, reportes y acceso a operaciones protegidas | `@PreAuthorize` en controladores; rutas `/admin` |
| ORGANIZADOR | eventos, inscritos, pagos pendientes, certificados, reportes y control | controladores de eventos/pagos/certificados/reportes |
| ESTUDIANTE | inscripción, pagos propios, credenciales y perfil | `InscripcionController`, `PagoController`, `CredencialController` |
| PARTICIPANTE_EXTERNO | registro, inscripción, pagos y credenciales | autenticación/inscripción/pago |
| PARTICIPANTE | certificados | `CertificadoController` |
| PERSONAL_CONTROL | QR, acceso, asistencias, dashboard operativo | `ControlAccesoController`, `AsistenciaController` |

## Evidencias
`backend/.../controladores/*.java`; `frontend/src/app/app.routes.ts`.

## Estado
EXISTENTE.

## Observaciones
Los roles aparecen como cadenas; las entidades `Rol`, `Permiso`, `UsuarioRol` y `RolPermiso` indican modelo RBAC.

## Inconsistencias
`PARTICIPANTE` y `PARTICIPANTE_EXTERNO` no son equivalentes en las anotaciones; no se puede inferir la asignación de roles sin datos.

## Información faltante
NO ENCONTRADO EN EL PROYECTO: matriz completa rol-permiso poblada y flujo de administración de roles.
