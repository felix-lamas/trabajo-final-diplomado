# Monografia Vidia: integracion final y comprobaciones

## Archivos entregados

- Monografia_Vidia_Final.docx: documento editable nuevo; originales conservados.
- Monografia_Vidia_Final.pdf: copia de revision exportada por Microsoft Word 16.0.
- fuentes_preservadas/: copias integras de los ocho documentos de entrada, comprobadas mediante SHA-256.
- material_evidencia/: copias de reportes y evidencias existentes; evidencias_finales/: cuatro capturas publicas de consulta.

## Fuentes y autoridad documental

1. Lineamientos-Tecnicos-Trabajo-Final-Diplomado-Desarrollo-Web-y-Apps-Moviles.pdf: requisitos complementarios de 2026, extension y anexos.
2. Formato-Trabajo-Final-Diplomado-Distintas-Areas.pdf: formato institucional, octubre de 2020.
3. Lamas-monografia-F.docx: contenido principal; seis objetivos especificos conservados literalmente.
4. Lamas_E3_2.8_revision_final_lineamientos.docx: casos y evidencias de pruebas.
5. P1 a P4: material orientador; no sustituye los documentos normativos.

## Paginacion y formato

- Total: 217 paginas. Preliminares: 14.
- Cuerpo principal: 40 paginas, de la pagina fisica 15 a 54.
- Anexos: desde la pagina fisica 55.
- Criterio: capitulo 1 hasta bibliografia inclusive, contando separadores; excluye preliminares y anexos conforme a la normativa. Cumple el maximo de 40 paginas en esta version.
- Carta; margenes izquierdo 4 cm y restantes 3 cm; Times New Roman 12, interlineado 1,5; portada con jerarquia normativa y codigo monoespaciado de 10 puntos.
- Espaciado posterior de 0,3 cm: la indicacion normativa consultada expresa 0,3 sin unidad; confirmar esta interpretacion en la revision institucional.
- Cuatro indices actualizados en Word; numeracion romana en preliminares y arabiga desde el capitulo 1; secciones y separadores de capitulo.
- Todas las tablas comprobadas contra el ancho disponible; sin paginas vacias detectadas; PDF abierto y texto de todas las paginas comprobado contra los limites.
- Revision visual por muestra representativa, no de todas las paginas. Revalidar paginacion despues de insertar figuras o cambiar texto.

## Integracion de 2.8

- Tabla principal de exactamente cinco columnas: ID, Escenario, Esperado, Obtenido y Estado.
- Veinte casos con IDs historicos conservados: 15 APROBADOS, 1 FALLIDO, 4 PENDIENTES, 0 OMITIDOS.
- Detalle Dado/cuando/entonces, solicitudes, resultados y figuras conservados en F.1 a F.4. Espacios complementarios en F.8.
- Pendientes: CP-01 (registro/verificacion completa), CP-04 (consulta posterior EN_REVISION), CP-11 (recorrido presencial QR/GPS), CP-24 (protocolo de rendimiento normativo). CP-29 conserva FALLIDO respecto a la expectativa de limitacion usada; verificar configuracion y despliegue efectivos antes de atribuir causa.
- Reportes existentes: backend 591 pruebas, 590 aprobadas y una omitida; Angular 316; Flutter 139. Se documenta su ejecucion de origen; no se reejecutaron suites durante esta integracion.
- No se confunden casos funcionales con pruebas unitarias ni mediciones locales con produccion. Rendimiento parcial no acredita RNF-01 completo.

## Coherencia corregida

- Arquitectura monolitica modular; nueve modulos backend; cuentas USUARIO + ORGANIZADOR y ADMINISTRADOR exclusivo.
- 18 entidades JPA y 19 tablas declaradas, no 14; modelo conceptual conserva ocho entidades principales. Diccionarios fisicos y correspondencia en F.7.
- Brevo API HTTP y almacenamiento privado mediante adaptador S3; PostgreSQL 16 acreditado para desarrollo, sin atribuirlo a la instancia productiva.
- QR temporal de sesion, no QR personal de inscripcion ni codigo consumido globalmente por el primer asistente.
- RNF-01 conserva cinco usuarios concurrentes y 95 % en tres segundos; las mediciones con otro protocolo quedan delimitadas.
- Conclusiones individuales para los seis objetivos; sin afirmar seguridad absoluta o validacion integral pendiente.
- Bibliografia ordenada y citas normalizadas; no se conserva una afirmacion de hash de APK sin comprobacion.

## Anexos y faltantes

A. Manual de usuario: integrado; completar capturas de registro, inscripcion, gestion organizadora y recorrido presencial.
B. Instalacion y despliegue: integrado; confirmar configuracion efectiva del alojamiento y ensayo de restauracion; secretos excluidos.
C. Repositorio: enlaces y captura publica.
D. Sistema publico e instalable Android: enlaces y capturas de web, Swagger y release; no se acredita identidad binaria de la APK instalada.
E. Diagramas: representaciones existentes conservadas; faltan exportaciones legibles y diagramas de secuencia indicados. Conservar marcas originales; no simular licencia.
F. Complementos: pruebas, reportes, diccionarios, requisitos y seguimiento; completar protocolo de encuesta y evidencias pendientes.

## Marcadores pendientes

Ademas de las figuras siguientes, faltan el escudo oficial en portada/contraportada y datos reales de tutor, tribunal y aprobacion. No se inventaron estos datos.

| Figura | Titulo | Ubicacion y contenido requerido |
|---|---|---|
| 1 | Casos de uso de Vidia | Apartado 2.3.5: exportar los tres actores y los casos reales; distinguir publicación administrativa y verificación pública. La representación existente se conserva en E.1. |
| 2 | Arquitectura general de Vidia | Apartado 2.4.1: exportar la arquitectura existente en E.2 con rótulos legibles, sin afirmar versiones productivas no verificadas. |
| 3 | Modelo conceptual de datos: ocho entidades | Apartado 2.4.2: exportar el modelo existente de E.3 con atributos principales y cardinalidades legibles en carta. |
| A.5 | Registro y activación de cuenta | Anexo A: registro, correo con token oculto y activación; corresponde a CP-01. No insertar credenciales. |
| A.6 | Inscripción mediante la interfaz | Anexo A: acción de inscripción y estado resultante con cuenta ficticia; el listado por sí solo no muestra el envío. |
| A.7 | Escaneo QR/GPS presencial | Anexo A: lector y resultado real del teléfono, con QR vigente oculto y sin coordenadas personales; CP-11. |
| A.8 | Gestión organizadora desde formulario | Anexo A: crear/editar y enviar a revisión un evento propio; consulta posterior EN_REVISION. |
| B.1 | Construcción y arranque del backend | Anexo B: runner y arranque local con fecha; ocultar conexión, claves y contraseñas. |
| B.2 | Compilación Android release | Anexo B: comando con API_BASE_URL y resultado, sin key.properties, contraseñas ni firma privada. |
| E.12 | Secuencia de inscripción | Anexo E: cliente, controlador, servicio y persistencia; validación de estado, cupo y duplicado. |
| E.13 | Secuencia QR/GPS | Anexo E: QR de sesión, escaneo Flutter, autenticación, ubicación y registro; no QR personal ni token global de un solo uso. |
| F.30 | E-PEND-01. Registro y verificación de correo | F.8: Qué debe mostrar: Formulario con datos ficticios; respuesta real de registro; recepción del correo con enlace/token ocultos; confirmación de activación y login posterior. La captura de Swagger del registro cuenta para API, pero no demuestra entrega del correo. Entorno y URL: Web de producción /auth/registro y rutas vigentes de verificación; Swagger /api/v1/auth/registro. Verificar la ruta exacta antes de ejecutar. Añadir fecha real, resultado y URL; ocultar credenciales y QR vigente. No cambiar estado sin comprobar criterio. |
| F.31 | E-PEND-02. Crear evento mediante formulario Web | F.8: Qué debe mostrar: Formulario del organizador completado y confirmación de guardado; título único [PRUEBA E3], resultado BORRADOR y propietario. Puede añadirse Swagger POST /api/v1/eventos 201 como evidencia de API. Entorno y URL: https://trabajo-final-diplomado-web.onrender.com/organizador/eventos; ruta real de creación desde ese listado. Añadir fecha real, resultado y URL; ocultar credenciales y QR vigente. No cambiar estado sin comprobar criterio. |
| F.32 | E-PEND-03. Confirmar evento EN_REVISION | F.8: Qué debe mostrar: Acción enviar a revisión y consulta posterior del mismo UUID con estado EN_REVISION. Swagger: PATCH /api/v1/eventos/{id}/enviar-revision seguido de GET /api/v1/eventos/{id}; mostrar respuesta real y ocultar JWT. Usar un evento nuevo elegible, no reabrir el ya finalizado. Entorno y URL: Swagger de producción y Web /organizador/eventos. Añadir fecha real, resultado y URL; ocultar credenciales y QR vigente. No cambiar estado sin comprobar criterio. |
| F.33 | E-PEND-04. Publicar desde el panel administrativo | F.8: Qué debe mostrar: Evento EN_REVISION antes de la acción, publicación autorizada y resultado PUBLICADO del mismo evento en catálogo. Capturar botón/confirmación y resultado; el listado genérico posterior no demuestra toda la interacción. Entorno y URL: https://trabajo-final-diplomado-web.onrender.com/admin/eventos. Añadir fecha real, resultado y URL; ocultar credenciales y QR vigente. No cambiar estado sin comprobar criterio. |
| F.34 | E-PEND-05. Inscribirse desde la interfaz | F.8: Qué debe mostrar: Evento publicado ajeno, acción de inscripción, confirmación y detalle propio; indicar si gratuito CONFIRMADA o pagado PENDIENTE_PAGO. Capturar la respuesta duplicada si se prueba esa variante. Entorno y URL: Web /eventos y /privado/inscripciones, o Flutter con API de producción. Añadir fecha real, resultado y URL; ocultar credenciales y QR vigente. No cambiar estado sin comprobar criterio. |
| F.35 | E-PEND-06. Enviar comprobante mediante formulario | F.8: Qué debe mostrar: Selección del PDF ficticio, envío y resultado PENDIENTE_VALIDACION del pago propio; título de evento y comprobante técnico sin datos bancarios reales. Entorno y URL: https://trabajo-final-diplomado-web.onrender.com/privado/pagos; pantalla de carga vigente. Añadir fecha real, resultado y URL; ocultar credenciales y QR vigente. No cambiar estado sin comprobar criterio. |
| F.36 | E-PEND-07. Validación administrativa del comprobante | F.8: Qué debe mostrar: Detalle del comprobante ficticio, acción de validar y resultado APROBADO; consultar también la inscripción y comprobar CONFIRMADA. Puede complementarse con Swagger PATCH pago y GET de inscripción, ocultando JWT. Entorno y URL: https://trabajo-final-diplomado-web.onrender.com/admin/pagos. Añadir fecha real, resultado y URL; ocultar credenciales y QR vigente. No cambiar estado sin comprobar criterio. |
| F.37 | E-PEND-08. Restricción de rol visible en Swagger | F.8: Qué debe mostrar: Solicitud con cuerpo válido ejecutada por USUARIO u ORGANIZADOR contra una operación exclusivamente administrativa; mostrar URL, HTTP 403 y ACCESS_DENIED. Ocultar JWT y el Authorization del curl. Los registros existentes ya acreditan 403; este espacio es un complemento visual. Entorno y URL: https://trabajo-final-diplomado.onrender.com/swagger-ui/index.html; publicación de eventos o validación de pagos según el rol. Añadir fecha real, resultado y URL; ocultar credenciales y QR vigente. No cambiar estado sin comprobar criterio. |
| F.38 | E-PEND-09. Escaneo físico y registro de asistencia con GPS | F.8: Qué debe mostrar: Lector QR en teléfono y resultado de registro real en sesión presencial; capturar también Mis asistencias. Identificar entorno, fecha, inscripción confirmada y sesión. No conservar QR vigente legible ni coordenadas personales. Si falla, conservar mensaje real y clasificar FALLIDO. Entorno y URL: Flutter instalado, paquete bo.edu.uajms.vidia; API productiva /api/v1/asistencias. Añadir fecha real, resultado y URL; ocultar credenciales y QR vigente. No cambiar estado sin comprobar criterio. |
| F.39 | E-PEND-10. Búsqueda y filtros desde el catálogo | F.8: Qué debe mostrar: Consulta o filtros usados y resultado correspondiente, con URL pública visible; incluir una variante sin resultados. No confundir el catálogo general con una búsqueda comprobada. Entorno y URL: https://trabajo-final-diplomado-web.onrender.com/eventos. Añadir fecha real, resultado y URL; ocultar credenciales y QR vigente. No cambiar estado sin comprobar criterio. |
| F.40 | E-PEND-11. Medición productiva con concurrencia | F.8: Qué debe mostrar: Reporte real de cinco usuarios concurrentes, número efectivo de registros, URL pública, fecha, herramienta, duración, porcentaje de respuestas dentro de tres segundos y errores. Adjuntar datos crudos y percentiles; no insertar un reporte simulado. Entorno y URL: GET https://trabajo-final-diplomado.onrender.com/api/v1/eventos/publicados; carga controlada. Añadir fecha real, resultado y URL; ocultar credenciales y QR vigente. No cambiar estado sin comprobar criterio. |
| F.41 | E-PEND-12. Recuperación y restablecimiento desde Web | F.8: Qué debe mostrar: Solicitud de recuperación con correo ficticio controlado, recepción del mensaje con token oculto, formulario de restablecimiento y login posterior. Nunca capturar contraseñas ni enlaces completos con token. Entorno y URL: Web /auth/login → enlace de recuperación; rutas reales de correo y restablecimiento. Añadir fecha real, resultado y URL; ocultar credenciales y QR vigente. No cambiar estado sin comprobar criterio. |
| F.43 | Soporte anonimizado de encuesta exploratoria | F.10: instrumento, 24 respuestas y conteos citados, sin datos personales. No inventar fechas ni participantes adicionales. |

## Comprobaciones pendientes y limites

- Confirmar edicion de Pressman originalmente consultada; se identifico la novena edicion inglesa de 2020.
- Confirmar recurso UAJMS originalmente empleado en la cita s. f.; la pagina pertinente no demuestra la consulta historica original.
- Distinguir elaboracion 2024 y publicacion 2025 del trabajo de Ean; no atribuir fechas de consulta inventadas.
- Validacion institucional y respaldo anonimizado de encuesta, matriz completa de autorizacion/IDOR de cuenta dual, usabilidad, navegadores y disponibilidad medida.
- Capturas publicas nuevas muestran accesibilidad puntual, no cobertura funcional completa. No se crearon datos de prueba en produccion en esta fase.
- Documento listo para revision humana con pendientes visibles; no se declara completamente terminado mientras falten esas evidencias y datos institucionales.
- No se modificaron codigo fuente, configuraciones ni migraciones. Sin commits ni pushes.

Detalle reproducible: integridad_fuentes.json, matriz_coherencia.json, paginacion_word.json, validacion_final.json y marcadores_pendientes.json.
