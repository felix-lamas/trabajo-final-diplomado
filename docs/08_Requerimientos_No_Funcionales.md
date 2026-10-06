# Requisitos no funcionales de Vidia

Se conservan las métricas de la monografía vigente. No se consideran alcanzadas hasta que exista protocolo y resultado reproducible. La disponibilidad de 99,5 % que aparece en un documento auxiliar no reemplaza el RNF-05 normativo actual.

| ID | Tipo | Requisito y métrica normativa | Evidencia requerida |
|---|---|---|---|
| RNF-01 | Rendimiento | Al menos 95 % de solicitudes principales de consulta/autenticación/inscripción/consulta de información responden en ≤3 s con 20 usuarios concurrentes. | Herramienta/versiones, endpoints, dataset, entorno, duración, percentiles y reporte fechado. |
| RNF-02 | Seguridad | 100 % de endpoints protegidos rechazan autenticación ausente o inválida y las operaciones restringidas rechazan rol insuficiente. | Inventario endpoint×rol, ejecuciones 401/403 y suite automatizada, distinguiendo test local de producción. |
| RNF-03 | Usabilidad | Al menos 80 % de usuarios de prueba completan las tareas principales sin asistencia. | Protocolo, tareas, participantes, denominador, resultados y evidencia anonimizada. |
| RNF-04 | Compatibilidad | Navegadores Chrome, Edge y Firefox en versiones estables probadas; Android 12 o superior para aplicación móvil. | Matriz de versiones/dispositivos y ejecución de flujos por plataforma. |
| RNF-05 | Disponibilidad | Disponibilidad mínima 95 % durante el período de prueba, excluyendo mantenimiento programado. | Ventana definida, sonda/monitoreo, incidencias, cálculo y URL/versión observada. |

Los resultados de rendimiento, usabilidad, compatibilidad y disponibilidad están pendientes de evidencia formal. El presupuesto de bundle no sustituye la medición del umbral RNF-01.
