# Figuras E.12 y E.13 de Vidia

`crear_figuras_E12_E13.js` crea dos diagramas UML `Sequence` a partir del código local revisado el 12 de octubre de 2026. No requiere iniciar Vidia, acceder a PostgreSQL ni proporcionar credenciales.

## Ejecución en Enterprise Architect

1. Abra su modelo UML. Haga una copia de respaldo antes de incorporar los diagramas.
2. Seleccione en el Browser el paquete Anexo E o su paquete padre. La búsqueda se limita a la selección y sus descendientes; no recorre ni modifica otros sectores del modelo.
3. Abra la ventana **Scripting**, cree un script de tipo **JavaScript** en un grupo local y pegue el archivo completo. Conserve la directiva `!INC Local Scripts.EAConstants-JavaScript`.
4. Si no existe un paquete Anexo E bajo la selección, el script crea `Anexo E - Diagramas de Secuencia`. Si hay varios candidatos, se detiene para que seleccione directamente el apropiado.
5. Ejecute el script y revise los mensajes de diagnóstico. Los nombres son **Figura E.12. Secuencia de inscripción** y **Figura E.13. Secuencia QR/GPS**.
6. Si una figura ya existe, se omite íntegramente: no se reemplaza, elimina ni actualiza. Para generar una versión alternativa, seleccione otro paquete sin esas figuras. Una creación parcial también queda preservada; use otro destino después de corregir la causa indicada.

El archivo no está pensado para ejecutarse directamente con Node.js o desde un navegador. Sus objetos `Repository` y `Session` los suministra EA.

## Compatibilidad y comprobaciones

Instalación detectada: **Enterprise Architect Trial 17.2.1721.15**, `Repository.LibraryVersion = 1721`, Windows de 64 bits. Se comprobaron los tipos `Sequence`, `Object` e `InteractionFragment`, los operandos `Element.Partitions` y las propiedades especializadas `Connector.Properties.Item("IsReturn")` / `Item("IsAsync")`. No se utiliza un método `Partition.Update()` inexistente: los operandos se guardan mediante `Element.Update()`.

La comprobación automatizada usa el cuerpo JavaScript mediante Windows Script Host/JScript y la API COM real de EA en un modelo `.qea` temporal independiente. Se sustituye exclusivamente el alojamiento de `Repository` y `Session` y se llama a `ejecutarVidia`; no se simulan las operaciones del repositorio. Esta comprobación no equivale a haber ejecutado el botón Run del motor JavaScript integrado de EA. Consulte el informe para conocer el resultado final y su alcance.

## Revisión visual y exportación

- Compruebe la separación de mensajes en **Settings > Model > Options > Message Spacing**. El trazado de fragmentos está preparado para la separación de **35** observada en el modelo de prueba. `VIDIA_PASO` y `VIDIA_ORIGEN` están al inicio del script; otros ajustes del modelo, fuentes o márgenes pueden requerir adaptación. No se cambian preferencias globales desde el script.
- EA distribuye los mensajes según su orden, y no respeta siempre sus coordenadas explícitas. El script alinea los fragmentos con las filas calculadas para esa separación. Revise que cada respuesta esté dentro de su operando y que las guardas no tapen etiquetas.
- Las guardas largas se abrevian en la figura; el texto completo queda en las notas del operando. Las notas de mensajes explican las clases agrupadas, los códigos de error y las precondiciones.
- Los puntos y coma de las guardas se sustituyen por separadores `/` al guardar operandos, para no introducir delimitadores en el formato interno de Partitions. Esto no cambia la regla de negocio representada.
- E.12 tiene cinco participantes. E.13 agrupa Flutter, dispositivo, API/seguridad y repositorios para mantener siete participantes. Estas agrupaciones no son nuevas clases ni microservicios.
- Las devoluciones son mensajes de retorno; la lectura del escáner es un callback asíncrono. EA genera los focos de control. Revise su presentación en las ramas alternativas: no son una medición de tiempos de ejecución.
- Exporte desde EA con la opción de guardar la imagen del diagrama, preferentemente en formato vectorial compatible con su documento. Compruebe la legibilidad en el tamaño final, con orientación horizontal cuando convenga. No reduzca una imagen extensa hasta hacer ilegibles sus etiquetas.
- La instalación de prueba agrega una **marca de agua Trial**. Las exportaciones temporales no se entregan como figuras definitivas; no se ha quitado ni ocultado esa marca. La exportación académica final debe realizarse con una instalación autorizada que permita obtener el resultado requerido.

## Seguridad

No hay operaciones de borrado, SQL, llamadas HTTP, acceso a archivos, credenciales ni ejecución de código de Vidia en el script. Todos los participantes y mensajes nuevos son propios de cada figura creada. Los modelos anteriores y los archivos de la aplicación no se modifican.

## Referencias de la API

- [Connector: tipo Sequence, SequenceNo, coordenadas y Properties](https://sparxsystems.com/enterprise_architect_user_guide/17.2/add-ins___scripting/connector2_2.html).
- [Element: Object, InteractionFragment, Subtype y Partitions](https://sparxsystems.com/enterprise_architect_user_guide/17.2/add-ins___scripting/element2.html).
- [Partitions: Name, Note y Size; persistencia mediante elemento anfitrión](https://sparxsystems.com/enterprise_architect_user_guide/17.2/add-ins___scripting/partitions.html).
- [Opciones del modelo: separación de mensajes](https://sparxsystems.com/enterprise_architect_user_guide/17.2/modeling_fundamentals/model_specific_options.html).

La ayuda de Partitions menciona `Element.Save`; la clase Element expone `Update()`, cuya persistencia se comprobó directamente. El script utiliza esta operación disponible, no introduce un método Save inexistente.
