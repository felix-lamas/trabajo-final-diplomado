# E3.5 — Plan de cambios controlados de la monografía

Fecha: 5 de octubre de 2026. Base técnica consultada: `fc9ce15`. Este plan se creó antes de modificar el DOCX.

## 1. Fuentes y decisión de identidad

Se leyó el contenido de `docs/monografia/Lamas-monografia-F.docx` (1949 párrafos XML, incluidos los de tablas; 24 tablas), el informe E3.4 y los auxiliares `docs/01` a `docs/10` indicados en el encargo, `docs/12_Matriz_de_Trazabilidad.md`, `docs/plan_maestro/01_estado_actual.md`, `06_arquitectura.md`, `07_modelo_datos.md`, `09_frontend_web.md`, `10_aplicacion_movil.md`, `11_seguridad.md`, `12_pruebas.md`, `13_despliegue.md` y `flutter/VidiaApp/README.md`.

El título actual ya expresa «Desarrollo de una plataforma web y móvil para la gestión de eventos universitarios en la Universidad Autónoma Juan Misael Saracho». Se conserva. Se incorpora Vidia como nombre del producto en el cuerpo académico; no se cambia autoría ni título. El objetivo general y los seis objetivos específicos se conservan literalmente.

## 2. Discrepancia de IDs en el encargo

La sección 7 del encargo menciona categorías para RF-02 e inscripción para RF-04. La monografía y la línea base E3.4 coinciden inequívocamente en otra asignación: RF-02 solicitud de organizador; RF-04 revisión administrativa de eventos; RF-05 catálogo/detalle; RF-06 inscripción; RF-11 categorías. Para cumplir la instrucción explícita de mantener la semántica/IDs y dar prioridad a E3.4, se conserva esta asignación y se deja constancia en el resultado. No se renumera ni se crea RF-14.

## 3. Cambios previstos por sección

| Sección | Contenido actual / problema | Cambio propuesto | Fuente | Impacto académico / evidencia futura |
|---|---|---|---|---|
| Portada y 1.5 | Título web+móvil y objetivos coherentes | Conservar literalmente; indicar evidencia pendiente del cumplimiento en el cuerpo | DOCX; objetivos E3.4 | Evita cambiar el problema para acomodarlo al estado parcial; falta concluir pruebas y despliegue |
| Resumen e introducción | Instrucciones de plantilla y resumen vacío | Mantener sección; redactar contexto sin resultados y marcar resumen final pendiente | DOCX; visión E3.4 | No inventar conclusión ni resultados |
| 1.2 Metodología exploratoria | Se afirma que una encuesta está en realización sin instrumentos/resultados adjuntos verificados | Marcar `[PENDIENTE DE EVIDENCIA METODOLÓGICA]`; enumerar instrumento, muestra, fechas y resultados faltantes | DOCX; E3.4 | Requiere evidencia del autor |
| 1.4 y alcance | Tecnologías y canales incompletos; límites dispersos | Precisar Web, Flutter Usuario, backend común, integraciones y exclusiones acordadas | Alcance/fuera de alcance E3.4; encargo | Mantener funciones pendientes en alcance |
| 2.1 | Fundamentos en plantilla | Mantener estructura y marcar desarrollo/citas pendientes | DOCX | No fabricar bibliografía ni revisión teórica |
| 2.2 Scrum | Cronograma calificado «efectivamente ejecutado», primera etapa de 10 días, RF-13 omitido; faltan capturas/enlace | Identificar como planificación histórica pendiente de evidencia; distinguir preparación de sprints semanales; incorporar retrospectiva y RF-13 | DOCX; E3.4 | No inventar fechas/ceremonias ni acreditar ejecución |
| 2.3.1 Actores | «Participante» presentado sin distinción técnica; gestión genérica de usuarios | Identificar ADMINISTRADOR/ORGANIZADOR/USUARIO; tercero público sin cuenta; permisos conforme al contrato | Actores E3.4; controladores | Solo tres roles; UI no es autorización |
| 2.3.2 RF | 13 IDs y prioridades correctos, descripciones/CA insuficientes | Ampliar RF-01,03,05,07; precisar RF-08,09,10,12; conservar semántica de RF-02,04,06,11,13 y prioridades | RF/reglas E3.4 | 7 MUST / 13 = 53,85 %; no implica cumplimiento |
| 2.3.3 RNF | Cinco métricas correctas | Conservar texto y umbrales; agregar advertencia de que resultados están pendientes | RNF E3.4; DOCX | No se acreditan 95 %, 100 % ni 80 % como resultados |
| 2.3.4 Casos de uso | Tres flujos detallados; faltan estados/pago externo y mapa de los demás | Mantener flujos, aclarar estados y añadir relación con los 13 RF; marcar diagrama pendiente | Casos E3.4; controladores | Detalle adicional y diagrama requieren revisión de legibilidad |
| 2.4.1 Arquitectura | Storage por definir; Angular descrito solo para gestión | Precisar monolito modular, tres canales/actores, SMTP Brevo y Supabase privado S3 desde backend; binarios/multipart; producción pendiente | Arquitectura E3.4; código actual | No afirmar microservicios ni Supabase Auth |
| 2.4.2 Modelo | Diccionario parcial; 14 tablas afirmadas en 2.6; faltan sesión/verificación/QR pago privado | Distinguir dominio, soporte y esquema físico; aclarar hashes y referencia privada; registrar estructuras técnicas faltantes; inventario físico pendiente | Entidades; modelo E3.4 | Preservar tablas; no deducir conteo físico por clases |
| 2.4.3 Interfaces | Listas previstas pueden leerse como terminadas | Describir Angular existente y Flutter parcial; etiquetar pagos/QR-GPS/certificados como NO IMPLEMENTADO en móvil | Estado E3.4; árbol Flutter | Wireframes pendientes no acreditan flujos |
| 2.4.4 API | Registro dice «200 + token»; inscripción incluye admin; faltan email/logout y QR pago | Corregir filas contra controladores y agregar operaciones relevantes sin inventar contratos | AutenticacionControlador, InscripcionController, EventoController y demás controladores | Tabla resumen; contrato completo OpenAPI; ejecución pendiente |
| 2.5 Stack | Almacenamiento indefinido; falta correo externo | Actualizar responsabilidades y añadir integraciones; versiones según proyecto | Arquitectura E3.4; dependencias/configuración | Proveedores configurados no significan producción verificada |
| 2.6 Implementación | Mezcla estado backend/móvil; conteo 14 tablas; ejemplo de código ficticio | Separar capacidades y evidencia; retirar afirmación física no probada; citar fragmento real de EventoController | Código; E3.4 | No afirmar Flutter completo |
| 2.7 Seguridad | Afirma ausencia de revocación en servidor | Corregir sesiones/JWT/logout; email/reset, ownership, 401/403, privacidad/validación de archivos y QR temporal | SesionUsuarioServicio, AutenticacionControlador; E3.4 | Concurrencia del token reset y cobertura exhaustiva pendientes |
| 2.8 Pruebas | Casos sin resultado; falta cobertura explícita de MUST negativos/RNF | Preservar casos, marcar ejecución pendiente, agregar escenarios faltantes y leyenda de estados | RF/RNF; E3.2 citado por E3.4 | Resultados E3.2 solo como antecedente local, no reejecución ni aceptación |
| 2.9 Despliegue | URL presentada sin estado de verificación y credenciales demo visibles | Eliminar credenciales; conservar URL solo como referencia no verificada; detallar evidencias pendientes de servicios y APK | E3.4; encargo | No probar ni desplegar en esta fase |
| Capítulo 3, bibliografía y anexos | Plantillas y conclusiones vacías | Mantener estructura; marcar evidencia/ejecución pendiente por objetivo/anexo | DOCX; encargo | Tutor, bibliografía completa, evidencia de campo y resultados siguen pendientes |
| Tablas/figuras/índices | Tablas saltan de 6 a 11; figuras duplican número 1 | Reconciliar rótulos secuenciales y referencias textuales; solicitar actualización de campos; preservar imágenes y estilos | Estructura original | La paginación final requiere render/Word |

## 4. Estrategia de edición y verificación

Se realizarán cambios locales de texto y filas necesarias sobre OOXML, conservando estilos, relaciones, imágenes, secciones y encabezados/pies del original. Se mantendrá una copia temporal recuperable fuera del repositorio. Las figuras cuyo contenido no pueda certificarse se conservarán acompañadas de `[PENDIENTE DE ACTUALIZACIÓN]`; no se inventarán diagramas.

Validaciones previstas: apertura ZIP/XML, conservación de partes y recursos gráficos, comparación de título/objetivos, integridad de tablas, RF/prioridades, RNF intactos, secuencia de rótulos, referencias internas, ausencia de credenciales en texto y revisión de imágenes. La skill de documentos exige renderizado visual; no se encontró LibreOffice en PATH ni rutas estándar. Se comprobará la alternativa disponible y se informará la limitación si no puede completarse el render, aplicando entonces la revisión estructural de la skill.

El alcance de escritura en el repositorio se limita al DOCX y a este plan y `E3_5_RESULTADO.md`. No se ejecutarán suites de software ni migraciones. El estado Git previo contiene cambios ajenos a E3.5, que se preservarán y se distinguirán del resultado.
