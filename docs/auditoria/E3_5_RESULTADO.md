# E3.5 — Resultado de la actualización controlada de la monografía

Fecha: 5 de octubre de 2026. Repositorio: `plataforma-eventos-uajms`. Rama: `main`. HEAD consultado y conservado: `fc9ce15`.

**Resultado:** reconciliación textual y estructural aplicada. El DOCX actualizado abre en Microsoft Word. La revisión visual integral de las páginas **NO quedó completada**: no se dispone de LibreOffice y la exportación PDF alternativa desde Word quedó bloqueada. Por tanto, este informe no acredita una versión tipográfica final lista para impresión ni cumplimiento de los RF/RNF en producción.

Se creó primero `E3_5_PLAN_CAMBIOS_MONOGRAFIA.md`, después se trabajó sobre una copia temporal y finalmente se reemplazó el DOCX solicitado, comprobando previamente que el original no había cambiado desde el inicio. No se modificó código, configuración de aplicaciones ni documentación auxiliar de E3.4. No se ejecutaron migraciones, despliegues, commit ni push.

## 1. Archivos modificados

Únicamente estos tres archivos del repositorio pertenecen a E3.5:

| Archivo | Acción |
|---|---|
| `docs/monografia/Lamas-monografia-F.docx` | Actualización controlada de contenido y comentarios de revisión |
| `docs/auditoria/E3_5_PLAN_CAMBIOS_MONOGRAFIA.md` | Creado antes de modificar la monografía |
| `docs/auditoria/E3_5_RESULTADO.md` | Creado para registrar resultado, evidencias y limitaciones |

La carpeta `docs/monografia/` ya figuraba sin seguimiento de Git al comenzar. No se debe interpretar su aparición en `git status` como creación de todos sus documentos durante E3.5.

Una copia recuperable del original y los instrumentos locales de comprobación se conservan fuera del repositorio, en `C:\Trabajo_Final_Vidia\.e35-qa-20261005`. Esa copia contiene el documento anterior, incluidas sus referencias demo retiradas de la versión final: no debe publicarse ni adjuntarse a la entrega.

Identidad de los archivos:

- Original: 2.138.081 bytes; SHA-256 `89331A247A4926DA8050A6D24EEE7193FB1DADCB8D2C2C8F4E194669FA814EEA`.
- Actualizado: 2.084.925 bytes; SHA-256 `C761947DE43E214536B940D726C0F53B4DE1AFADB7FA3D8E6D44BC4DE9A1F610`.
- La reducción de tamaño del contenedor ZIP no implica eliminación de imágenes: todas las partes originales permanecen, y los recursos gráficos se conservaron byte por byte.

## 2. Secciones modificadas

| Sección | Reconciliación aplicada |
|---|---|
| Resumen e introducción | Contexto del producto sin resultados inventados; resumen definitivo pendiente |
| 1.1 Antecedentes | Se mantienen autores y antecedentes; se exige verificar/completar sus referencias |
| 1.2 Problema / evidencia exploratoria | Se conserva el problema; encuesta e instrumentos marcados como evidencia metodológica pendiente |
| 1.4 Justificación, alcance y exclusiones | Web y Flutter Usuario, integraciones reales, alcance pendiente y exclusiones acordadas |
| 1.5 Objetivos | Objetivo general y seis específicos intactos; se añade advertencia sobre evidencia futura de cumplimiento |
| 2.1 Fundamentos | Se mantiene la sección pendiente, sin inventar bibliografía |
| 2.2 Metodología | Scrum reducido semanal; cronograma conservado como planificación histórica, no ejecución acreditada; preparación de diez días diferenciada de sprint semanal |
| 2.3.1 Actores | Tres roles autenticados y tercero público sin cuenta |
| 2.3.2 RF | Tabla reconciliada de trece requisitos, criterios, prioridades, estados y separación de QR |
| 2.3.3 RNF | Tabla original intacta; umbrales expresamente separados de resultados |
| 2.3.4 Casos de uso | Acceso con correo verificado/sesión, gestión por estados e inscripción; relación de los flujos restantes y diagramas pendientes |
| 2.4 Diseño | Arquitectura, modelo, interfaces y tabla API corregidos sin convertir la monografía en un manual de endpoints |
| 2.5 Stack | Responsabilidades de clientes/backend y Brevo/Supabase; despliegue no verificado |
| 2.6 Implementación | Backend/web diferenciados de Flutter parcial; fragmento real de `EventoController`; sin conteo no demostrado de tablas físicas |
| 2.7 Seguridad | BCrypt, JWT y sesión revocable, correo/reset, ownership, 401/403, archivos privados y asistencia temporal |
| 2.8 Pruebas | Casos conservados y ampliados a CP-01–CP-28; resultados pendientes, negativos MUST y protocolos RNF |
| 2.9 Despliegue | URL heredada identificada como no verificada; retiradas credenciales demo; servicios y APK pendientes |
| Capítulo 3 | Estructura de conclusiones/recomendaciones conservada; conclusiones por objetivo pendientes de evidencia |
| Bibliografía y anexos | Estructura conservada; referencias, manuales, diagramas y evidencias faltantes explícitos |

## 3. RF modificados

Se conservan los trece IDs, su orden y prioridades. Se actualizaron títulos/criterios conforme a E3.4 y se precisaron flujos; **no se creó RF-14**.

| RF | Prioridad | Contenido final / cambio |
|---|---|---|
| RF-01 | MUST | Registro, autenticación y recuperación segura; verificación, perfil/cambio de contraseña y sesión/revocación. Se incorpora CA-01.6 para perfil y cambio de contraseña solicitado en E3.5 |
| RF-02 | SHOULD | Solicitar condición de organizador; resolución administrativa. Se conserva su semántica |
| RF-03 | MUST | Crear/gestionar/enviar eventos a revisión; ownership y QR de pago opcional con carga, reemplazo y eliminación |
| RF-04 | MUST | Revisión administrativa y transiciones de eventos; se conserva su semántica |
| RF-05 | MUST | Catálogo/detalle público, filtros existentes e información/QR de pago cuando corresponde |
| RF-06 | MUST | Inscripción gratuita y pagada; unicidad, capacidad y estados autorizados |
| RF-07 | MUST | Pago externo, comprobante privado, revisión, aprobación/rechazo y reenvío; sin gateway ni checkout |
| RF-08 | MUST | Sesiones, QR temporal, inscripción, GPS, radio/precisión y duplicidad; integración Flutter pendiente |
| RF-09 | SHOULD | Emisión autorizada, elegibilidad y descarga PDF. Curricular: 80 % de sesiones requeridas y horas positivas. No curricular: asistencia a al menos una sesión requerida, sin porcentaje adicional. No hay emisión automática por finalizar |
| RF-10 | SHOULD | Historial propio por dominios; no se inventa endpoint agregado |
| RF-11 | SHOULD | Gestión de categorías; se conserva su semántica |
| RF-12 | COULD | Dashboards/reportes calculados y disponibles; se excluyen indicadores ficticios o placeholder como resultados |
| RF-13 | COULD | Verificación pública de certificado por código, sin cuenta |

MUST: 7; SHOULD: 4; COULD: 2. Cálculo: `7 / 13 × 100 = 53,85 %`. La prioridad no acredita implementación completa ni aceptación.

**Discrepancia resuelta con fuente:** la sección 7 del encargo llama categorías a RF-02 e inscripción a RF-04. La monografía original y E3.4 coinciden en RF-02 solicitud de organizador, RF-04 revisión, RF-05 catálogo, RF-06 inscripción y RF-11 categorías. Se preservó esa asignación conforme a las reglas de no renumerar/no alterar semántica y prioridad de E3.4. La discrepancia queda registrada aquí y en un comentario del DOCX; no se corrigió silenciosamente.

## 4. RNF modificados

**Ninguna métrica ni ID fue modificado.** Los veinte párrafos de la tabla original de RNF se compararon literalmente con la versión final y permanecen intactos.

| RNF | Umbral conservado | Evidencia |
|---|---|---|
| RNF-01 | 95 % de operaciones principales ≤3 s con 20 usuarios concurrentes | Pendiente de ejecución |
| RNF-02 | 100 % de endpoints protegidos rechaza autenticación inválida/ausente; operaciones restringidas rechazan rol insuficiente | Pendiente de matriz exhaustiva y ejecución |
| RNF-03 | 80 % de usuarios completa tareas sin asistencia | Pendiente de protocolo, muestra y ejecución |
| RNF-04 | Chrome, Edge y Firefox estables; Android 12+ | Pendiente de evidencias por versión/dispositivo |
| RNF-05 | Disponibilidad ≥95 % en periodo de prueba, excluido mantenimiento programado | Pendiente de periodo y monitoreo |

Se añadió una advertencia junto a la tabla: estos valores son requisitos, no porcentajes medidos. Los casos CP-24 a CP-28 proponen su comprobación sin atribuir resultados.

## 5. Actores

Roles autenticados: **ADMINISTRADOR, ORGANIZADOR, USUARIO**. Participante describe a USUARIO; tipo UAJMS/externo no crea roles adicionales. El tercero verificador es público, sin cuenta.

Se corrigieron descripciones que presumían un CRUD administrativo de usuarios no constatado. Se documentan las consultas y operaciones realmente expuestas. La presencia de “estudiantes”, “carreras” o “facultades” en el contexto universitario no se interpreta como rol ni como módulo implementado.

## 6. Arquitectura

- Monolito modular: Java 21, Spring Boot, Spring Security, JWT, JPA/Hibernate y API común `/api/v1`.
- Angular 21: canal web público, Usuario, Organizador y Administrador.
- Flutter 3.44.8: canal móvil de Usuario; no se declaran aplicaciones móviles administrativas/de organizador.
- PostgreSQL: persistencia relacional; Docker local usa PostgreSQL 16. Versión/esquema productivo pendientes de verificación.
- Brevo: correo transaccional SMTP; no autenticación externa.
- Supabase Storage: objetos privados vía S3-compatible desde backend; no reemplaza PostgreSQL ni Spring Security.
- Portada del evento: URL HTTP(S). Referencia privada del QR de pago separada; sin exposición de storage key en DTO.
- JSON, multipart y respuestas binarias se distinguen. QR de pago y comprobante conservan sus contratos independientes.
- Render/TLS/configuración no se presentan como producción comprobada.

## 7. Diagramas

Se conservaron las cinco imágenes originales y sus seis inserciones. No se inventaron nuevos diagramas ni se eliminaron imágenes para ocultar discrepancias.

| Recurso | Tratamiento |
|---|---|
| Tablero GitLab | Conservado; fecha, ceremonias y cierre requieren evidencia |
| Casos de uso | Marcado `[PENDIENTE DE ACTUALIZACIÓN]`: permisos, RF completos y actor público |
| Arquitectura (imagen repetida en el original) | Conservada; requiere corregir Storage “por definir”, acceso web Usuario, `/api/v1`, Brevo y Supabase |
| Modelo | Conservado como modelo anterior pendiente de reconciliar, no prueba del esquema físico |
| Wireframes | Identificados como ilustrativos, con datos ficticios de diseño, no registros ni resultados reales |
| Fragmento de código | Reemplazado por operación real de creación de `EventoController` |

Los rótulos de figuras quedaron secuenciales del 1 al 6. Permanecen pendientes los diagramas detallados de inscripción, pago, asistencia, certificados y despliegue que el documento aún no desarrolla. Las marcas de herramienta presentes en imágenes originales no fueron alteradas.

## 8. Tablas

- Se conservan las **24 tablas** originales; no se rehízo el documento desde una plantilla nueva.
- Rótulos manuales reconciliados: Tabla 1 a Tabla 24, eliminando los saltos anteriores.
- Tabla RF: trece filas de requisitos más cabecera.
- Tabla RNF: cinco requisitos sin cambio de texto/umbrales.
- Diccionario: se añaden `correo_verificado` y `qr_pago_storage_key`, se precisan hashes/referencias y se mencionan `SesionUsuario` y `TokenVerificacionCorreo` como estructuras técnicas cuyo diccionario completo sigue pendiente.
- API: registro sin JWT, permisos/entradas corregidos y ocho filas adicionales para correo/recuperación/logout y QR de pago.
- Arquitectura/stack: responsabilidades de correo y Storage explícitas.
- Pruebas: se conservan quince casos y se agregan trece escenarios negativos/funcionales/RNF. Los 28 permanecen pendientes de ejecución vinculada a aceptación; no se colocó PASS sin evidencia.

Se preservaron estilos, numeración automática, geometría original de tablas, secciones, encabezados y pies. Se marcaron campos para actualización en Word. La integridad estructural no sustituye la revisión final de saltos de página y altura/legibilidad de las filas ampliadas.

## 9. Contenido marcado como pendiente

- `[PENDIENTE DE EVIDENCIA METODOLÓGICA]`: instrumento, selección/muestra, fechas, respuestas anonimizadas y análisis de encuesta.
- `[PENDIENTE DE EVIDENCIA]`: bibliografía, trazabilidad de pruebas, tablero/ceremonias, seguridad exhaustiva, esquema objetivo, manuales y resultados.
- `[PENDIENTE DE EJECUCIÓN]`: RF/RNF, flujos móviles, instalable Android y conclusiones dependientes de esas ejecuciones.
- `[PENDIENTE DE PRODUCCIÓN]`: servicios, TLS/CORS, versión publicada, PostgreSQL/migraciones, Brevo, Supabase y disponibilidad.
- `[PENDIENTE DE ACTUALIZACIÓN]`: diagramas, anexos e índices/paginación finales.
- `NO IMPLEMENTADO` en Flutter: pagos/comprobantes, captura QR/GPS, certificados e historial integral; se conserva su pertenencia al alcance final.

La leyenda separa IMPLEMENTADO, PROBADO LOCALMENTE, VERIFICADO MEDIANTE PRUEBAS, PENDIENTE DE PRUEBA, PENDIENTE DE PRODUCCIÓN y NO IMPLEMENTADO.

## 10. Contenido deliberadamente no modificado

- Título académico, autoría, objetivo general y seis objetivos específicos: conservados literalmente.
- Problema y formulación: no se sustituyeron por una descripción acomodada a las funciones actuales.
- Cinco RNF, umbrales, trece IDs RF y prioridades.
- Estructura académica, capítulos y secciones, incluso cuando faltan resultados.
- Citas existentes: conservadas con exigencia de completar/verificar referencias; no se fabricaron fuentes.
- Imágenes originales: preservadas, con advertencias localizadas cuando están desactualizadas.
- Backend, Angular, Flutter, configuración, migraciones y documentación auxiliar de E3.4.
- Deuda técnica de concurrencia del token de reset: mencionada, no presentada como corregida.
- Datos reales y servicios externos: no se consultaron ni modificaron cuentas, pagos, Storage ni PostgreSQL.

## 11. Contradicciones restantes y decisiones requeridas

| Asunto | Estado / información faltante |
|---|---|
| Etiquetas RF-02/04/05 del encargo | Resuelto conservando semántica coincidente de monografía y E3.4; no renumeración |
| Diagramas antiguos frente al texto actualizado | Pendiente explícito; faltan diagramas finales editables y revisión de legibilidad |
| Diccionario frente al esquema físico | Pendiente; falta contraste del esquema PostgreSQL objetivo y migraciones aplicadas |
| Fecha/calendario de Scrum | **DECISIÓN REQUERIDA:** autor debe aportar calendario ejecutado, año, registros y distinguir planificación histórica de ejecución. No se inventan fechas |
| Encuesta/exploración | **DECISIÓN REQUERIDA:** aportar instrumento y evidencia de ejecución, o acordar con tutor la metodología válida. No se elimina la sección ni se inventa una muestra |
| Fundamentos y bibliografía | **DECISIÓN REQUERIDA:** completar fuentes verificadas y confirmar formato institucional; los antecedentes citados no se revalidaron externamente en E3.5 |
| Cobertura móvil adicional de perfil/alta/recuperación | **DECISIÓN REQUERIDA:** confirmar recorrido móvil final y evidencia esperada. Pagos, asistencia y certificados NO se eliminan del alcance |
| Producción y monitoreo | **DECISIÓN REQUERIDA:** identificar versión/entorno final, periodo RNF-05 y accesos de evaluación seguros. URL escrita no equivale a disponibilidad comprobada |
| Índices y presentación final | Pendiente actualización visual en Word: la revisión PNG/PDF integral no se completó |

No se resuelve ninguno de estos puntos inventando resultados. No se requiere cambiar título u objetivos para ocultar brechas.

## 12. Evidencias todavía necesarias

1. Instrumentos y evidencia metodológica del problema, sin atribuir aceptación institucional no documentada.
2. Implementación e integración Flutter de RF-07, RF-08 y RF-09 e historial relacionado; pruebas en Android 12+.
3. Matriz por RF/CA con camino feliz y error, fecha, entorno, datos de prueba, obtenido y artefacto.
4. Matriz exhaustiva de 401/403/ownership, token vencido/usado/revocado y controles de archivos.
5. Protocolos y resultados RNF sin reducir sus umbrales.
6. Pruebas de integración reales de Brevo y Supabase, separadas de mocks y vinculadas a la versión evaluada.
7. Esquema PostgreSQL objetivo, restricciones y migraciones efectivamente aplicadas.
8. Producción, health, TLS/CORS, persistencia, APK y monitoreo de disponibilidad.
9. Diagramas finales, bibliografía completa, manuales y evidencia por objetivo para conclusiones.
10. Revisión visual completa del DOCX, actualización de índices y verificación de paginación/tablas antes de impresión.

Los resultados del informe E3.2 (460 pruebas backend, 0 fallos, 0 errores, 1 omitida; 271 Angular aprobadas; Flutter inconcluso) se citan únicamente como **antecedente local** sobre `fc9ce15`. No se reejecutaron en esta fase ni se utilizaron como evidencia de producción o aceptación integral.

## 13. Riesgos académicos

- Presentar umbrales RNF como resultados o antecedentes locales como producción sería incorrecto; el documento lo distingue.
- La monografía sigue siendo un documento de trabajo: contiene secciones de plantilla pendientes, diagramas antiguos señalados y conclusiones no definitivas.
- Los RF móviles permanecen en alcance: su ausencia de implementación es una brecha técnica, no una razón para retirar requisitos.
- Una tabla estructuralmente válida puede presentar saltos/espaciado poco adecuados al aumentar su texto. No se certifica maquetación final sin revisión de todas las páginas.
- Los diez comentarios de revisión facilitan control documental; deben revisarse y resolverse antes de emitir la versión académica definitiva.
- Los auxiliares E3.4 conservan referencias históricas a que el DOCX aún no se había editado. Son reportes de esa fase; no fueron reescritos ni deben confundirse con el estado posterior E3.5.

## 14. Validaciones realmente ejecutadas

| Comprobación | Resultado |
|---|---|
| ZIP y todos los XML/relaciones | Apertura y parseo correctos |
| Partes originales | Ninguna eliminada |
| Partes alteradas | `document.xml`, `settings.xml`, relación/tipo de contenido de comentarios; nueva `comments.xml` |
| Recursos no intervenidos | Estilos, numbering, imágenes, encabezados/pies y otras partes conservados byte por byte |
| Tablas/dibujos/secciones | 24 tablas y 6 dibujos conservados; mismo número de secciones |
| Párrafos XML | 1949 → 2121, incluidos párrafos de tablas; no es un conteo de párrafos narrativos ni de páginas |
| Título/objetivos | Comparación literal aprobada |
| RF | RF-01 a RF-13, 13 filas, 7/4/2 prioridades, criterios presentes, sin RF-14 |
| RNF | Cinco IDs; veinte párrafos originales de tabla conservados literalmente |
| Rótulos | 24 tablas y 6 figuras secuenciales |
| Referencias internas | Marcadores originales conservados; anclas de hipervínculos apuntan a marcadores existentes. Campos pendientes de actualización visual |
| Comentarios | 10 comentarios; inicio/fin/referencia y parte de comentarios coherentes |
| Codificación | Tildes conservadas, sin mojibake detectado |
| Credenciales | Valores demo originales ausentes del texto final; sin patrones de JWT/clave real detectados en la revisión. No es una auditoría de secretos de todo el historial Git |
| Microsoft Word | La copia final abrió y calculó 101 páginas, 24 tablas y 10 comentarios. El archivo se cerró sin guardar desde Word |
| Renderizado | No completado: LibreOffice ausente; exportación PDF de una copia de revisión detenida. No hay aprobación de QA visual integral |
| Cierre de herramientas | El primer helper de Word se detuvo por bloqueo de exportación; el último abrió correctamente pero su cierre COM devolvió error de argumento. Se cerraron exclusivamente las instancias temporales identificadas; no se guardó desde Word ni se alteraron documentos del usuario |
| `git diff --check` | Sin errores de whitespace; avisos LF/CRLF preexistentes no son fallos de la validación |
| Tests de software / producción | No ejecutados en E3.5; no eran necesarios para esta edición documental |

La skill de documentos orientó la edición localizada y la conservación de OOXML; ante la ausencia de LibreOffice se aplicó su alternativa de revisión estructural, complementada con apertura real en Word. La skill PDF se consultó para evaluar la alternativa visual, pero no se obtuvo PDF final de revisión.

## 15. Fuentes consultadas

### Fuentes documentales leídas

- `docs/monografia/Lamas-monografia-F.docx`: texto completo de párrafos/tablas y revisión de las cinco imágenes originales.
- `docs/auditoria/E3_4_ALINEACION_DOCUMENTAL.md`.
- `docs/01_Vision_del_Proyecto.md`.
- `docs/02_Objetivos_Generales.md`.
- `docs/03_Objetivos_Especificos.md`.
- `docs/04_Alcance.md`.
- `docs/05_Fuera_de_Alcance.md`.
- `docs/06_Actores_del_Sistema.md`.
- `docs/07_Requerimientos_Funcionales.md`.
- `docs/08_Requerimientos_No_Funcionales.md`.
- `docs/09_Reglas_De_Negocio.md`.
- `docs/10_Casos_de_Uso.md`.
- `docs/12_Matriz_de_Trazabilidad.md`.
- `docs/plan_maestro/01_estado_actual.md`.
- `docs/plan_maestro/06_arquitectura.md`.
- `docs/plan_maestro/07_modelo_datos.md`.
- `docs/plan_maestro/09_frontend_web.md`.
- `docs/plan_maestro/10_aplicacion_movil.md`.
- `docs/plan_maestro/11_seguridad.md`.
- `docs/plan_maestro/12_pruebas.md`.
- `docs/plan_maestro/13_despliegue.md`.
- `flutter/VidiaApp/README.md`.
- `docs/auditoria/E3_2_MATRIZ_NORMATIVA.md`: resultados históricos y commit inspeccionado, no fuente normativa sustitutiva de E3.4.

### Código contrastado mediante lectura o búsquedas puntuales

Prefijo backend: `backend/src/main/java/bo/uajms/eventos/modulos/`.

- `usuarios/controladores/AutenticacionControlador.java` y `UsuarioControlador.java`.
- `usuarios/servicios/AutenticacionServicio.java` y `SesionUsuarioServicio.java`.
- `usuarios/entidades/Usuario.java`, `SesionUsuario.java`, `TokenRecuperacion.java`, `TokenVerificacionCorreo.java`.
- `eventos/controladores/EventoController.java` y `eventos/entidades/Evento.java`.
- `inscripciones/controladores/InscripcionController.java`.
- `pagos/controladores/PagoController.java`.
- `asistencias/controladores/AsistenciaController.java` y `QrAsistenciaController.java`.
- `asistencias/servicios/AsistenciaService.java` y `QrAsistenciaService.java`.
- `sesiones/controladores/SesionEventoController.java`.
- `certificados/controladores/CertificadoController.java` y `certificados/servicios/CertificadoService.java`.
- `reportes/controladores/DashboardController.java`.
- `docker/docker-compose.yml`: versión local PostgreSQL.

El estado Angular/Flutter se contrastó con documentación E3.4, sus documentos de estado y el inventario de `frontend/src/app` y `flutter/VidiaApp/lib`; esta fase no representa una nueva ejecución E2E de esos clientes. No se utilizó un snapshot histórico para sustituir la línea base normativa.

## 16. Comandos, Git y alcance de la entrega

Comandos/operaciones utilizados:

- `git status --short`, `git branch --show-current`, `git rev-parse --short HEAD`, `git diff --stat`, `git diff --check`.
- `rg --files`, `rg -n` y `Get-Content -Encoding UTF8` para localizar/leer fuentes; búsquedas sobre rutas inexistentes se corrigieron buscando en el árbol real, sin crear esas rutas.
- Lectura ZIP/XML con `System.IO.Compression` y `XmlDocument`; copia temporal y `Get-FileHash -Algorithm SHA256`.
- `apply_patch` para los informes y scripts temporales de edición/validación, situados estos últimos fuera del repositorio.
- Ejecución de `edit-monograph.ps1` y `validate-monograph.ps1` desde la carpeta temporal, cargados explícitamente como UTF-8. Una primera ejecución con codificación implícita produjo texto mal codificado solo en una copia temporal; se regeneró desde el original y se comprobó antes de publicar. Ese borrador no se entregó.
- Automatización de Word con copia temporal de solo lectura: apertura/repaginación; intento PDF bloqueado; nueva apertura final sin PDF. Cierre de las instancias temporales identificadas.
- Publicación con `Copy-Item` solo después de comparar el hash del original del repositorio con la copia inicial. El hash final coincide con la copia validada.

Estado Git: se mantiene `main` / `fc9ce15`, sin staging ni commit. El working tree ya contenía modificaciones de E3.4, eliminaciones y archivos sin seguimiento. Entre los cambios previos están `.gitignore`, README, auxiliares, release, Flutter README y las eliminaciones de `backend/package-lock.json`, `backend/src/package-lock.json`, `.hintrc`, gitignores antiguos y `documentacion/PORTADA.docx`. **No fueron realizadas ni restauradas por E3.5.**

El `git diff --stat` de archivos seguidos conserva el resumen previo de 36 archivos, 439 inserciones y 643 eliminaciones; no describe por sí solo esta fase porque el DOCX y los nuevos informes están sin seguimiento. Para E3.5, el resumen real es: un DOCX existente actualizado y dos informes nuevos. El estado detallado se mostró mediante `git status`; no se atribuye todo el working tree a esta tarea.

## 17. Cierre y siguiente acción

La reconciliación documental solicitada está aplicada y es trazable. Se conservaron alcance web+móvil, roles, RF, RNF, objetivos y pendientes, sin inventar resultados ni alterar implementación.

**Pendiente antes de considerar cumplido el criterio de presentación académica:** abrir el DOCX en Word, actualizar índices/campos y revisar visualmente las 101 páginas calculadas —el total puede cambiar al actualizar índices—, especialmente filas RF/API/pruebas ampliadas y diagramas. La exportación visual bloqueada impide declarar todos los criterios de formato como aprobados en esta fase.

No se inicia Flutter, no se implementan funciones, no se cambia arquitectura, no se despliega y no se crea commit ni push.
