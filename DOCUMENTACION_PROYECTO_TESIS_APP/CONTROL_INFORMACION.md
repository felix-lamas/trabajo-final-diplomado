# Control de información

## Objetivo del documento
Controlar la fuente, confiabilidad y límite de cada conclusión.

## Información encontrada

| Información | Encontrada | Evidencia | Confiabilidad | Observación |
|---|---|---|---|---|
| Propósito de gestión de eventos | Sí | README, módulos | ALTA | consistente con código |
| Arquitectura monolito modular por capas | Sí | paquetes, Angular/Spring | ALTA | inferencia sustentada |
| Tecnologías/versiones declaradas | Sí | pom/package/compose | ALTA | declaradas, no ejecutadas |
| Modelo de datos JPA | Sí | entidades | ALTA | DDL SQL no localizado |
| Endpoints y roles | Sí | controladores | ALTA | rutas duplicadas en algunos módulos |
| Pantallas/rutas | Sí | Angular routes/modules | ALTA | visual no probado |
| Diagramas | Sí | 14 `.wsd` | MEDIA | no renderizados/comparados a detalle |
| Documentos de pruebas | Sí | docs/Postman | MEDIA | ejecución no comprobada |
| Tests automatizados | Parcial | un spec y dependencias | ALTA | no se hallaron Java Test fuente |
| Despliegue/uso productivo | No | ausencia de configuración | BAJA | NO VERIFICABLE |
| Aplicación móvil | No | ausencia de proyecto | ALTA | solamente propuesta |

## Evidencias
Las rutas de la tabla remiten a los artefactos inspeccionados.

## Estado
EXISTENTE.

## Observaciones
La confiabilidad expresa trazabilidad de la afirmación, no garantía de operación en producción.

## Inconsistencias
Las diferencias detectadas se desarrollan en los documentos 07, 09, 10, 15 y 16.

## Información faltante
Toda fila marcada como no verificada requiere una fuente externa o ejecución controlada.
