# E3.5.1 — Cierre visual y estructural de la monografía

Fecha: 2026-10-05. **Revisión realizada con reservas documentadas**, sin rehacer E3.5 ni cambiar requisitos, arquitectura, alcance o software.

## 1. Documento y páginas finales

Documento: `docs/monografia/Lamas-monografia-F.docx`.

Microsoft Word abrió y guardó la copia de revisión; actualizó campos, índices y paginación. La exportación PDF finalmente terminó correctamente: **102 páginas físicas**, con 13 preliminares y 89 páginas del cuerpo. No se conserva como supuesto el total anterior de 101 páginas.

Se revisaron visualmente las 102 páginas del PDF final, renderizadas a PNG con Poppler. La copia se trabajó fuera del repositorio en `C:\Trabajo_Final_Vidia\.e351-qa`; el original se sustituyó únicamente después de la revisión y comparación estructural. Se conserva allí el original de entrada y el PDF de revisión. La comprobación en Word fue mediante automatización COM, no una sesión manual de edición por interfaz gráfica.

## 2. Tablas revisadas

Se conservaron **24 tablas**; el texto completo de todas sus celdas es idéntico al DOCX E3.5 de entrada. Numeración secuencial mediante campos SEQ, sin duplicados de leyendas del cuerpo. Las páginas siguientes corresponden a la leyenda, que puede estar al final de una tabla multipágina.

| Tabla | Página impresa de leyenda | Tabla | Página impresa de leyenda |
|---|---:|---|---:|
| 1 | 6 | 13 | 50 |
| 2 | 17 | 14 | 51 |
| 3 | 20 | 15 | 51 |
| 4 | 26 | 16 | 52 |
| 5 | 28 | 17 | 52 |
| 6 | 39 | 18 | 53 |
| 7 | 42 | 19 | 53 |
| 8 | 45 | 20 | 54 |
| 9 | 46 | 21 | 55 |
| 10 | 46 | 22 | 63 |
| 11 | 48 | 23 | 67 |
| 12 | 49 | 24 | 81 |

Se ajustaron anchos al espacio útil, distribución de columnas, espaciado y repetición de encabezados. Se impidieron cortes internos de filas y se vinculó la última fila con su leyenda. Diccionario y API usan tamaño 11 para evitar desbordamiento; se conservó el formato académico general. No se suprimieron campos, criterios ni resultados pendientes.

## 3. Figuras revisadas

Figuras 1–6: páginas impresas 17, 29, 37, 40, 60 y 70. Se actualizaron campos SEQ e índice. Se separaron correctamente imagen y leyenda donde compartían párrafo y se compactó el fragmento textual de la figura 6. Se conservan cinco imágenes y cinco dibujos; la figura 6 es texto. Los bytes de las cinco imágenes coinciden con la entrada.

## 4. Índices y referencias

Índice general actualizado en Word; índices de tablas y figuras construidos con sus rótulos y actualizados; índice de anexos A–F con campos PAGEREF a sus encabezados. Anexos A–E: página 88; F: 89. Se verificaron las páginas visibles en el PDF, no solamente la presencia de campos. Se actualizaron campos de página y referencias existentes.

Comprobación XML: cero destinos inexistentes de hipervínculos internos. Búsqueda en texto PDF: sin mensajes de marcador/referencia no definidos. Esto no equivale a validar todas las citas bibliográficas ni referencias externas, que siguen pendientes donde así lo indica E3.5.

## 5. Comentarios

Diez comentarios inspeccionados y conservados: cinco resueltos documentalmente, dos pendientes del autor y tres pendientes técnicos. Se añadió clasificación dentro de los comentarios, sin inventar datos ni eliminarlos. Detalle: [E3_5_1_REVISION_COMENTARIOS.md](E3_5_1_REVISION_COMENTARIOS.md).

RF-09 permanece idéntico. **[REVISAR REGLA DE NEGOCIO]** Confirmar la condición de asistencia a al menos una sesión requerida para certificado no curricular. No se cambió por intuición.

## 6. Diagramas

Catorce fuentes WSD inspeccionadas: trece obsoletas y una arquitectura general parcialmente vigente pendiente de regeneración. No se demostraron fuentes originales exactas para las imágenes incrustadas de casos de uso, arquitectura, modelo y wireframes. No se generaron diagramas. Detalle: [E3_5_1_DIAGRAMAS.md](E3_5_1_DIAGRAMAS.md).

## 7. Problemas visuales corregidos

- Tablas que excedían el ancho disponible; columnas redistribuidas y encabezados repetidos.
- Filas partidas y separación innecesaria entre tabla y leyenda.
- Títulos cortos aislados: conservación con el párrafo siguiente.
- Salto manual redundante tras cambio de sección, que creaba una página vacía.
- Índices incompletos/no actualizados; sustitución de instrucciones provisionales por campos con resultados reales.
- Leyendas de figuras mezcladas con dibujos y fragmento de código excesivamente espaciado.

## 8. Problemas que permanecen

- Diagramas históricos pequeños, parcialmente obsoletos y con marcas de herramienta de evaluación. Se preservaron por instrucción; requieren fuentes válidas y regeneración posterior.
- Algunas palabras/identificadores se parten entre líneas en columnas estrechas (MoSCoW, diccionario, API, stack y estado de pruebas). No se observó texto fuera de margen; se registra como reserva de legibilidad, no como pérdida de contenido.
- Espacios amplios al mantener filas completas y separadores de capítulo. Páginas escasas de contenido en preliminares, conclusiones, bibliografía y anexos responden también a contenido del autor/evidencia pendiente; no se rellenaron ni eliminaron secciones.
- Datos académicos personales, bibliografía, metodología, conclusiones y anexos definitivos requieren al autor. No se certifica que el documento esté listo para defensa.

## 9. Limitaciones del render

La vía disponible fue Microsoft Word y exportación PDF, seguida de Poppler; no se utilizó LibreOffice. El PDF final sí se generó: no corresponde declarar pendiente todo el QA visual PDF. La revisión de páginas no valida técnicamente los diagramas históricos, resultados experimentales ni producción. Los comentarios se verificaron en OOXML, ya que el PDF de lectura no los muestra como globos.

## 10. Archivos modificados en esta fase

1. `docs/monografia/Lamas-monografia-F.docx` — formato, campos, índices y anotación de comentarios.
2. `docs/auditoria/E3_5_1_REVISION_COMENTARIOS.md` — nuevo.
3. `docs/auditoria/E3_5_1_DIAGRAMAS.md` — nuevo.
4. `docs/auditoria/E3_5_1_RESULTADO.md` — nuevo.

No se modificaron backend, frontend, Flutter, configuración, fuentes WSD ni informes E3.5. Los numerosos cambios previos del working tree, incluidas eliminaciones preexistentes, no pertenecen a esta fase y se conservaron.

## 11. Validaciones realizadas

| Comprobación | Resultado |
|---|---|
| Word abre, actualiza campos, guarda y exporta | Correcto; 102 páginas |
| XML de todas las partes del DOCX | Analizable sin errores |
| Tablas | 24; contenido textual idéntico en todas las celdas |
| Imágenes | 5; hashes idénticos a la entrada |
| Dibujos y secciones Word | 5 dibujos y 2 secciones, conservados |
| Comentarios | 10 conservados |
| RF | RF-01 a RF-13; sin RF-14; 7 MUST, 4 SHOULD, 2 COULD |
| RNF | RNF-01 a RNF-05; celdas y umbrales sin cambios |
| Roles y alcance | Texto de E3.5 conservado: ADMINISTRADOR, ORGANIZADOR, USUARIO; participante como descripción |
| Pérdida textual | Solo se retiraron cuatro instrucciones provisionales de índices, reemplazadas por índices operativos; resto de párrafos no vacíos conservado |
| Credenciales demo/JWT | No detectadas en búsqueda del texto del DOCX; revisión visual sin credenciales. No es un escaneo del historial Git |
| Resultados y producción | No se añadieron resultados; permanecen avisos de prueba/producción pendientes |
| Flutter | Continúan explícitos los flujos NO IMPLEMENTADOS y pendientes |
| git diff --check | Sin errores de whitespace; advertencias LF/CRLF sobre documentos preexistentes |

SHA-256 entrada E3.5: `C761947DE43E214536B940D726C0F53B4DE1AFADB7FA3D8E6D44BC4DE9A1F610`.

SHA-256 DOCX final: `58543E0DCDE970044ADF848C73B1DAF54BFD72A696328A644C280DBE402C9437`.

## 12. Herramientas y comandos

Se emplearon PowerShell/.NET ZIP/XML para inspección y ajustes localizados; Microsoft Word COM para `Fields.Update`, actualización de índices, repaginación, guardado y `ExportAsFixedFormat`; Poppler `pdfinfo`, `pdftoppm -scale-to 1200 -png` y `pdftotext -layout`; inspección de cada PNG; comparación de hashes, celdas, párrafos, campos y vínculos; `rg --files docs/design`; `git status --short`, `git diff --stat` y `git diff --check`.

Los auxiliares y renders permanecen fuera del repositorio en `.e351-qa`. El DOCX y los informes son archivos sin seguimiento previo o nuevos: `git diff --stat` no representa sus cambios binarios/textuales hasta que sean añadidos a Git. No se añadió nada al índice Git. No se ejecutaron pruebas de software porque esta fase no modifica software. No hubo commit ni push.

## 13. Cierre

E3.5.1 entrega el DOCX revisado y los tres informes. Se cierra la revisión disponible de formato/campos, **con reservas de legibilidad de diagramas y pendientes académicos/técnicos explícitos**. No se cambia la línea base funcional ni se declara aceptado el sistema en producción.
