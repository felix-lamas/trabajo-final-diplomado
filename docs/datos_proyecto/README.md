# Índice de documentación Vidia

## Línea base auxiliar reconciliada (E3.4)

- [Visión](01_Vision_del_Proyecto.md)
- [Objetivo general](02_Objetivos_Generales.md)
- [Objetivos específicos](03_Objetivos_Especificos.md)
- [Alcance](04_Alcance.md) y [fuera de alcance](05_Fuera_de_Alcance.md)
- [Actores](06_Actores_del_Sistema.md)
- [Requisitos funcionales](07_Requerimientos_Funcionales.md)
- [Requisitos no funcionales](08_Requerimientos_No_Funcionales.md)
- [Reglas de negocio](09_Reglas_De_Negocio.md)
- [Casos de uso](10_Casos_de_Uso.md)
- [Matriz de trazabilidad](12_Matriz_de_Trazabilidad.md)
- [Estado del plan maestro](plan_maestro/01_estado_actual.md)
- [Informe de alineación E3.4](auditoria/E3_4_ALINEACION_DOCUMENTAL.md)

Estos archivos son documentación técnica/académica auxiliar reconciliada. La monografía `monografia/Lamas-monografia-F.docx` no se modificó en E3.4 y será revisada en la fase controlada E3.5. La fuente de contrato de API es la especificación OpenAPI generada por el backend; ninguna colección Postman antigua la sustituye.

## Material histórico que requiere revisión

- `release_1_0/`: paquete anterior, marcado por archivo como histórico; no acredita producción vigente.
- `plan_maestro/PLAN_MAESTRO_IMPLEMENTACION.md`, `14_sprints.md`, `15_decisiones.md` y `16_bitacora_cambios.md`: conservar por trazabilidad, con notas de vigencia/supersesión.
- `design/`: diagramas anteriores; verificar actores, estados, entidades y endpoints antes de reutilizar.
- `../postman/encuestas_satisfaccion.postman_collection.json`: colección legacy de encuestas; no representa una capacidad actual.
- `auditoria/DOCUMENTACION_MAESTRA_PROYECTO.md` y `ANALISIS_PROYECTO_PERFIL.md`: snapshots de auditoría previos, no fuente normativa ni estado actual sin revalidar.

Los resultados de pruebas, disponibilidad, compatibilidad y despliegue deben citar una ejecución fechada y reproducible. La existencia de configuración o de un documento de release no equivale a verificación operacional.
