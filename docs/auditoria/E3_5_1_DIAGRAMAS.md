# E3.5.1 — Inventario de diagramas

Fecha: 2026-10-05. Revisión documental; no se generaron ni sustituyeron diagramas.

## Fuentes editables encontradas

Se inspeccionaron `docs/design/README.md` y los 14 archivos `.wsd` siguientes. El README los identifica como material histórico. Tener una fuente editable no acredita que corresponda exactamente a una imagen incrustada en Word.

| Archivo en docs/design | Estado | Motivo / trabajo posterior |
|---|---|---|
| 01_Casos_de_Uso_General.wsd | OBSOLETO | Roles de control y validador financiero; reconciliar con tres roles y verificador externo |
| 02_Casos_de_Uso_Modulos.wsd | OBSOLETO | Control de acceso/credenciales y reportes antiguos; revisar RF actuales |
| 03_Secuencia_Auth.wsd | OBSOLETO | Rutas sin /api/v1; no representa verificación de correo ni sesiones/revocación actuales |
| 03_Secuencia_Certificado.wsd | OBSOLETO | Ruta y generación durante descarga antiguas; reglas de elegibilidad no reconciliadas |
| 03_Secuencia_Inscripcion_Pago.wsd | OBSOLETO | BillingController, rutas antiguas y actor financiero; inscripción/pago no canónicos |
| 04_Actividades_Certificacion.wsd | OBSOLETO | Coordinador y emisión masiva automática; no refleja generación autorizada |
| 04_Actividades_Control_Acceso.wsd | OBSOLETO | Personal de control y escaneo de credencial; no representa asistencia por sesión/QR temporal/GPS |
| 04_Actividades_Inscripcion_Pago.wsd | OBSOLETO | Estados y credencial de acceso antiguos; actor financiero |
| 05_Modelo_Conceptual.wsd | OBSOLETO | Facultad, entrada/salida y ausencia de sesión; reconciliar relaciones |
| 06_Clases_Analisis.wsd | OBSOLETO | qrData y check-in/check-out; faltan sesión y estructuras técnicas actuales |
| 07_ER_Definitivo.wsd | OBSOLETO | Facultad/carrera/escaneador y modelo incompleto respecto de sesiones y tokens |
| 07_MER.wsd | OBSOLETO | Modelo legacy con QR por inscripción y entrada/salida |
| 08_Arquitectura_Capas.wsd | OBSOLETO | Paquetes/capas antiguos; reutilizable conceptualmente, no representación exacta del monolito modular |
| 08_Arquitectura_General.wsd | PENDIENTE DE REGENERACIÓN | Base Java/Angular/PostgreSQL parcialmente vigente; faltan Flutter, Brevo, Supabase y API canónica; aclarar API Gateway |

Inventario: 13 fuentes obsoletas y una parcialmente vigente pendiente de regeneración. No se encontraron otras fuentes equivalentes vinculadas de forma verificable a las imágenes del DOCX. Regenerar exige primero reconciliar las fuentes; no basta con ejecutar PlantUML.

## Figuras de la monografía

Página = numeración impresa del cuerpo, no ordinal físico del PDF. La numeración 1–6 es secuencial, sin duplicación de leyendas en el cuerpo; su aparición en el índice no es un duplicado.

| Figura | Página | Contenido | Estado | Fuente y limitación |
|---|---:|---|---|---|
| 1 | 17 | Captura del tablero | SIN FUENTE EDITABLE | Captura histórica conservada; fecha/cierre de iteración pendientes de evidencia |
| 2 | 29 | Casos de uso | OBSOLETO / PENDIENTE DE REGENERACIÓN | Existen WSD relacionados, sin correspondencia exacta demostrada con la imagen. Revisar actores, permisos, verificador y RF; texto interno pequeño |
| 3 | 37 | Arquitectura | OBSOLETO / PENDIENTE DE REGENERACIÓN | WSD relacionados no equivalen a la fuente original incrustada. Faltan integraciones/canales; anotación «por definir» y marca de herramienta de evaluación |
| 4 | 40 | Modelo de datos | OBSOLETO / PENDIENTE DE REGENERACIÓN | Fuentes conceptuales/ER históricas; falta fuente original vinculada y contraste con esquema objetivo. Imagen pequeña y marca de evaluación |
| 5 | 60 | Wireframes | PENDIENTE DE REGENERACIÓN / SIN FUENTE EDITABLE VINCULADA | Contrastar pantallas y estados reales; datos de diseño no son evidencia de ejecución |
| 6 | 70 | Fragmento de creación de evento | ACTUAL, DOCUMENTAL | Texto editable del DOCX con referencia a EventoController.java; se compactó formato, no código ni contrato. No acredita prueba funcional |

El paquete conserva **cinco archivos de imagen y cinco objetos de dibujo**, idénticos a la entrada; la sexta figura es un fragmento textual, no una sexta imagen. No se borraron imágenes ni marcas de procedencia. No se inventaron fuentes, diagramas ni despliegues.

## Trabajo pendiente

Solicitar fuentes originales de las figuras 2–5, o autorizar posteriormente su reconstrucción con fuentes reconciliadas y trazabilidad al sistema. Exportar a resolución legible y repetir QA/índices. Las imágenes históricas siguen explícitamente marcadas como pendientes en E3.5; E3.5.1 no certifica su vigencia técnica.
