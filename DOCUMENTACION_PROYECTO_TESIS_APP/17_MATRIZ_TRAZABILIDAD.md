# Matriz de trazabilidad

## Objetivo del documento
Relacionar funcionalidades con artefactos verificables.

## Información encontrada
| Función | Código | Endpoint | Datos | Interfaz | Prueba/evidencia |
|---|---|---|---|---|---|
| Autenticación | `AutenticacionServicio` | `/api/v1/auth/*` | usuario, token_recuperacion | `features/auth` | docs sprint/recuperación |
| Eventos | `EventoService` | `/api/v1/eventos` | eventos, categorias_evento | público/admin | sprint 2 |
| Inscripción | `InscripcionService` | `/api/v1/inscripciones` | inscripciones | público/privado | sprint 3 |
| Pagos | `PagoService` | `/api/v1/pagos` | pagos, comprobantes | pagos/admin | sprint 6/7 |
| Credencial/acceso | servicios de dominio | credenciales/control-acceso | credenciales, QR, control | asistencias | sprint 6/7 |
| Certificados | `CertificadoService` | certificados/verificación | certificados | certificados | sprint 8 |
| Encuestas | `EncuestaService` | encuestas | encuesta/preguntas/respuestas | encuestas | Postman |

## Evidencias
`docs/07_Requerimientos_Funcionales.md`; controladores, servicios, entidades y módulos Angular citados.

## Estado
PARCIAL: implementación trazable; ejecución y resultados no verificados.

## Observaciones
Para una matriz académica final deben asignarse IDs de requisito estables.

## Inconsistencias
No todos los requisitos tienen prueba fuente automatizada localizada.

## Información faltante
NO VERIFICABLE: cobertura requisito-prueba y resultados de aceptación.
