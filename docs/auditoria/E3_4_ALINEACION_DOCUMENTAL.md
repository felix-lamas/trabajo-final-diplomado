# E3.4 — Alineación documental del proyecto Vidia

**Alcance:** reconciliación de documentación auxiliar. No se modificaron backend, Angular, Flutter (código), configuración ni la monografía DOCX. No se ejecutaron pruebas de software ni se creó commit/push.

**Criterio:** esta línea base documenta el sistema acordado y diferencia lo presente en código de lo pendiente, probado localmente y verificado en producción. No convierte presencia de código en evidencia de aceptación o despliegue. La monografía actual se conserva sin editar; su actualización controlada corresponde a E3.5.

## 1. Línea base funcional alineada

- Se conservan exactamente **13 RF**, sin renumerar ni crear RF-14.
- Prioridades: MUST RF-01/03/04/05/06/07/08; SHOULD RF-02/09/10/11; COULD RF-12/13.
- Roles funcionales: `ADMINISTRADOR`, `ORGANIZADOR`, `USUARIO`. “Participante” describe al usuario en el dominio, no un rol adicional. El verificador público es actor externo sin cuenta.
- Se conservan los cinco RNF y sus métricas acordadas. En particular, RNF-05 es ≥95 % durante el periodo de prueba; no se reduce por la cifra auxiliar 99,5 %.
- Web Angular cubre los flujos web descritos en los auxiliares. Flutter es canal previsto para Usuario, pero pagos/comprobantes, captura de asistencia QR/GPS y certificados/historial integral se describen como pendientes, no como capacidades completadas.
- El backend conserva la autoridad sobre estados, permisos, ownership, elegibilidad y validaciones.

## 2. Contratos y conceptos aclarados

- **Pagos:** no existe gateway ni checkout. El participante paga externamente; un evento pagado puede mostrar instrucciones y una imagen QR opcional del organizador. Después presenta un comprobante privado, que revisa un organizador autorizado o administrador conforme al backend. Se conservan los estados reales; no se inventan estados de pago.
- **QR de pago:** imagen del medio de pago que configura el organizador, almacenada en Supabase Storage privado y servida por la API Vidia. No es un QR generado por Vidia ni un comprobante.
- **QR de asistencia:** token temporal asociado a una sesión; puede ser utilizado por participantes distintos. La unicidad del registro es por inscripción y sesión; no se describe como de un solo uso global.
- **Verificación de certificado:** consulta pública por código, separada de ambos QR anteriores.
- **Certificados:** elegibilidad y emisión dependen del backend; curricular exige 80 % de asistencia según la regla actual. No se afirma generación automática al finalizar un evento.
- **Integraciones:** Brevo se utiliza mediante SMTP; Supabase Storage es almacenamiento de objetos privado accedido solo por Spring Boot. Supabase no proporciona autenticación ni reemplaza PostgreSQL. `Evento.imagenPortada` sigue siendo URL HTTP(S).
- **Modelo:** se distinguen ocho entidades conceptuales del dominio y estructuras técnicas de soporte. No se declara un número de tablas físicas sin contrastarlo con el esquema PostgreSQL desplegado.
- **Dashboards:** se excluyen encuestas, satisfacción y contadores placeholder (incluido `qrUtilizados=0`) como métricas reales.

## 3. Archivos auxiliares alineados o señalados

### Línea base de producto y requisitos

- `README.md`
- `docs/README.md`
- `docs/01_Vision_del_Proyecto.md`
- `docs/02_Objetivos_Generales.md`
- `docs/03_Objetivos_Especificos.md`
- `docs/04_Alcance.md`
- `docs/05_Fuera_de_Alcance.md`
- `docs/06_Actores_del_Sistema.md`
- `docs/07_Requerimientos_Funcionales.md`
- `docs/08_Requerimientos_No_Funcionales.md`
- `docs/09_Reglas_De_Negocio.md`
- `docs/10_Casos_de_Uso.md`
- `docs/12_Matriz_de_Trazabilidad.md`
- `flutter/VidiaApp/README.md` (documentación de estado; no código Flutter).

### Plan maestro y estado

- `docs/plan_maestro/01_estado_actual.md` a `10_aplicacion_movil.md`, `11_seguridad.md`, `12_pruebas.md` y `13_despliegue.md` presentan la línea base reconciliada y separan estado estático, ejecución local y producción.
- `docs/plan_maestro/14_sprints.md`, `15_decisiones.md`, `16_bitacora_cambios.md` y `PLAN_MAESTRO_IMPLEMENTACION.md` mantienen su valor histórico con advertencias de vigencia/supersesión. DEC-009 queda aclarada para no implicar uso global de un solo uso; DEC-012 ya no presenta el flujo EN_REVISION como pendiente.
- `PLAN_MAESTRO_FINALIZACION_UAJMS_VIDIA.md` se señala como plan histórico, no evidencia de entrega o producción.

### Paquetes legacy identificados

- Todos los Markdown de `docs/release_1_0/` se rotularon como paquete histórico que requiere actualización y verificación antes de reutilizarse. La narrativa de defensa no acredita funcionalidades, pruebas ni producción.
- `docs/design/README.md` declara los diagramas `.wsd` como históricos hasta validar actores, estados, modelo y API.
- `postman/README.md` señala `encuestas_satisfaccion.postman_collection.json` como legacy; `recuperacion_contrasena.postman_collection.json` es solo referencia pendiente de contrastar con OpenAPI.
- `docs/README.md` señala `docs/auditoria/DOCUMENTACION_MAESTRA_PROYECTO.md` y `docs/ANALISIS_PROYECTO_PERFIL.md` como snapshots previos que no son fuente normativa ni estado actual sin revalidación.

## 4. Contradicciones corregidas en auxiliares

1. “No se desarrollará aplicación móvil” se reemplaza por el alcance web + Flutter para Usuario, señalando explícitamente flujos Flutter pendientes.
2. Se eliminan roles funcionales antiguos y se normalizan actor público/participante sin inventar roles.
3. Se conservan las 13 RF, sus IDs y prioridades; el QR de pago se integra dentro de RF-03/RF-05, no como RF-14.
4. Las reglas de pago dejan claro que es externo/manual, sin checkout; QR de pago, comprobante y QR de asistencia quedan separados.
5. Se aclaran estados, ownership, reenvío de comprobante, asistencia por sesión y elegibilidad de certificados para no afirmar estados/reglas antiguos.
6. La documentación de seguridad reconoce sesiones y revocación de servidor, además de BCrypt, JWT, roles, ownership y almacenamiento privado, en lugar de afirmar ausencia de revocación.
7. Se corrige la presentación de métricas placeholder como si fueran estadísticas calculadas.
8. Se sustituyen afirmaciones de disponibilidad 99,5 % por el RNF normativo acordado de 95 % en la línea auxiliar, sin reportarlo como resultado medido.
9. Se deja de inferir cantidad de tablas físicas contando clases JPA.
10. Se distingue “configurado” de “verificado”: los documentos de plan/release, URLs o variables por sí solos no acreditan producción.

## 5. Contradicciones pendientes y elementos que requieren decisión

- **Monografía DOCX:** permanece intacta por instrucción. Todavía contiene texto que requiere reconciliación con esta línea base: actores/roles, fuera de alcance móvil, QR de asistencia de un solo uso, seguridad/revocación, descripción de pagos, generación de certificados, arquitectura/integraciones, RNF y tabla/cantidad del modelo. Resolver y aprobar en E3.5; no asumir aquí que el texto ya cambió.
- **Esquema real PostgreSQL:** verificar columnas/tablas y migraciones aplicadas en la base objetivo antes de fijar el diagrama físico. La documentación evita declarar número de tablas.
- **Flutter:** implementar y probar antes de describir RF-07 y RF-08 como completos en el canal móvil; completar RF-09/RF-10 según alcance aprobado.
- **Dashboard API:** los campos placeholder identificados requieren una decisión técnica posterior: retirar del contrato si no se usan o implementar cálculo real. Esta fase solo impide documentarlos como métricas.
- **Producción:** desplegar/verificar frontend, backend, PostgreSQL/migración, CORS/TLS, health, Brevo y Supabase; producir evidencia fechada de RNF. Nada de esto se verificó en esta alineación.
- **Pruebas de aceptación:** faltan resultados reproducibles por RF/CA, especialmente MUST móvil, 401/403, rendimiento, usabilidad, compatibilidad y disponibilidad.
- **Fuentes auxiliares antiguas:** `docs/auditoria/DOCUMENTACION_MAESTRA_PROYECTO.md`, `docs/ANALISIS_PROYECTO_PERFIL.md`, diagramas `.wsd`, colecciones Postman y release 1.0 quedan identificados como snapshots/históricos; no se reescribieron todos sus detalles.

## 6. Cambios que deberán entrar en E3.5 (monografía)

1. Ajustar el alcance para que incluya Web Angular y Flutter de Usuario; precisar que la app Flutter no incluye operación móvil de Administrador/Organizador.
2. Normalizar actores a los tres roles funcionales y separar al tercero verificador público como actor sin cuenta.
3. Incorporar la lista acordada de 13 RF y prioridades, criterios aclarados y los cinco RNF sin alterar umbrales.
4. Actualizar objetivos, casos de uso, reglas y diagramas para estados reales, ownership y flujos backend.
5. Describir pago externo sin gateway; distinguir QR de pago, comprobante y QR temporal de asistencia.
6. Describir Flutter según el estado real, manteniendo como pendiente cualquier flujo todavía no implementado o no probado.
7. Actualizar arquitectura: monolito modular Spring Boot/Java 21, Angular 21, Flutter 3.44.8, PostgreSQL, REST `/api/v1`, SMTP Brevo y Supabase Storage privado desde backend.
8. Corregir seguridad para BCrypt/JWT, roles, ownership, 401/403, sesiones/revocación, verificación y recuperación; aclarar la privacidad de archivos.
9. Describir certificado curricular/no curricular, regla 80 % y verificación pública sin afirmar automatización no existente.
10. Reemplazar diagramas/modelo/API obsoletos y resolver la descripción de tablas solo con esquema verificado.
11. Separar resultados implementados, pruebas ejecutadas/aprobadas, pruebas de integración y verificación en producción; no completar conclusiones sin evidencia.
12. Completar la evidencia metodológica/experimental pendiente (incluidas encuestas si la monografía afirma que se realizaron) sin inventar muestra ni resultados.

## 7. Validación y alcance técnico de E3.4

- No se ejecutaron suites backend, Angular o Flutter: no hubo cambios de código y los resultados anteriores son referencias históricas E3.2, no resultados de esta fase.
- Se verificó manualmente la presencia de los documentos indicados y se revisaron enlaces relativos principales de los índices. No hay verificador formal de Markdown documentado en el repositorio; no se instalaron herramientas.
- `git diff --check` y revisión final de `git status` quedan registrados en el cierre de la fase.
- Impacto técnico esperado: ninguno en runtime, contratos, datos ni permisos; los cambios son documentales.
- Se preservan cambios preexistentes del working tree; no se restauraron ni borraron archivos ajenos a esta tarea.
