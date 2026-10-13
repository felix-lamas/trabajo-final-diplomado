# Anexo B: entrega y comprobaciones

- Word: Anexo_B_Manual_de_Instalacion_Despliegue_Mantenimiento_Vidia.docx
- PDF: Anexo_B_Manual_de_Instalacion_Despliegue_Mantenimiento_Vidia.pdf
- 35 p?ginas; nueve capturas reales; 24 tablas; doce marcadores editables.
- Word abri?, actualiz? campos y export? PDF; sin im?genes/tablas fuera del ancho disponible, p?ginas vac?as ni texto fuera de p?gina.
- Fuentes originales intactas: 613 archivos comprobados por SHA-256.

## Ejecuciones nuevas

| Comando | Resultado | Registro |
|---|---|---|
| mvn -B clean verify | C?digo 0 | backend_verify.txt |
| npm ci --no-audit --fund=false | C?digo 0 | angular_npm_ci.txt |
| npx --no-install ng version | C?digo 0 | angular_version.txt |
| npm test -- --watch=false | C?digo 0 | angular_test.txt |
| npm run build | C?digo 0 | angular_build.txt |
| flutter doctor | C?digo 0 | flutter_doctor.txt |
| flutter pub get | C?digo 0 | flutter_pub_get.txt |
| flutter test --reporter expanded | C?digo 0 | flutter_test.txt |
| flutter analyze | C?digo 1 | flutter_analyze.txt |
| flutter build apk --debug --dart-define=API_BASE_URL=http://10.0.2.2:18080/api/v1 | C?digo 0 | flutter_debug_apk.txt |

Backend: 591 pruebas, 590 aprobadas, una omitida; cero fallos/errores. Angular: 316 pruebas/57 archivos y build correcto con avisos de tama?o. Flutter: 139 aprobadas; analyze c?digo 1 con 17 infos; doctor advierte licencias; debug generado y firma verificada, sin instalaci?n ni publicaci?n.

El APK debug se construy? para el puerto 18080; no se prob? comunicaci?n. Para el arranque final en 8080 se debe recompilar con la URL correcta.

Tambi?n se ejecutaron: initdb y pg_ctl para cl?ster temporal, psql, migraciones al arranque, mvn spring-boot:run, npm start, java -jar con dev,demo, acceso/perfil/logout exclusivamente locales, pg_dump y pg_restore aislados, docker compose config --services, apksigner verify y aapt2 dump badging.

Primer java -jar: fallo por 8080 ocupado por nuestro Maven; log conservado, proceso propio identificado/detenido y reejecuci?n correcta.

## Producci?n de solo lectura

Web, Swagger, OpenAPI, release y salud GET/HEAD: 200. HEAD salud sin cuerpo. Perfil an?nimo y ra?z backend: 401. Fecha y URL exactas: consultas_publicas.json. No prueba disponibilidad continua ni conexi?n SQL del servicio publicado.

## Pendientes

- B.3: versiones instaladas de las herramientas. Capturar la consola real con java -version, mvn -version, node --version, npm --version, flutter --version y dart --version. Los resultados ya están en versiones_instaladas.json; no recrear una terminal.
- B.5: contenedores PostgreSQL y pgAdmin en ejecución. Mostrar docker compose ps y disponibilidad de la base aislada, sin imprimir variables ni credenciales. La validación estática no acredita contenedores iniciados.
- B.5: historial de migraciones locales. Capturar la consulta real de flyway_schema_history. El resultado está conservado en migraciones_y_restauracion.json; no mostrar una conexión productiva.
- B.6: compilación y pruebas del backend. Capturar el BUILD SUCCESS y resumen del runner real. Reportes conservados: backend_verify.txt, backend_junit_resumen.json y junit_backend/. No se obtuvo una captura de terminal gráfica.
- B.6: arranque efectivo del backend. Mostrar Tomcat started on port 8080 y la URL local de salud. Logs reales: backend_arranque.txt, backend_jar_seed.txt y el primer fallo backend_jar_puerto_ocupado.txt.
- B.7: instalación, pruebas y build de Angular. Capturar la consola real de npm ci, el resumen 316/316 y el resultado del build. Los registros están en angular_npm_ci.txt, angular_test.txt y angular_build.txt.
- B.8: Flutter doctor, pruebas y análisis. Capturar el resultado real de doctor, las 139 pruebas y los 17 avisos de análisis. Los archivos flutter_doctor.txt, flutter_test.txt y flutter_analyze.txt conservan la ejecución.
- B.8: compilación Android release y verificación de firma. Debe mostrar una compilación real y apksigner verify de app-release.apk, sin contenidos de key.properties, claves, contraseñas ni sustitución de la firma actual.
- B.9: configuración real de Render. Mostrar raíz, buildCommand, directorio publicado, Dockerfile, perfil y nombres de variables desde el panel autorizado, ocultando todos los valores sensibles. No se obtuvo acceso al panel.
- B.11: respaldo y restauración locales. Capturar pg_dump, pg_restore y consultas de comparación del clúster aislado. La ejecución y su alcance están en migraciones_y_restauracion.json; no insertar datos o credenciales de producción.
- B.14: construcción y arranque del contenedor backend. Debe mostrar docker build y ejecución real con una base aislada. Hoy el daemon no estaba disponible; el arranque del JAR no sustituye esa prueba.
- B.14: correo y almacenamiento en un entorno de prueba. Mostrar resultados reales con cuentas y bucket aislados autorizados, sin secretos ni envíos a usuarios reales. La prueba Supabase opt-in fue omitida deliberadamente.

## L?mites

- Docker instalado solo como cliente: daemon no disponible. Descriptor v?lido; no imagen ni contenedores probados.
- Firma release: key.properties y keystore presentes, credenciales efectivas incompletas. No se gener? ni reemplaz? firma.
- Render privado, credenciales efectivas, versi?n PostgreSQL productiva y SHA desplegado no verificados.
- Brevo/Supabase externos no ejercitados; smoke Supabase omitida.
- El cl?ster temporal us? trust en loopback; no demuestra autenticaci?n PostgreSQL por contrase?a. Rol de aplicaci?n sin privilegios globales, due?o de su base temporal.
- Respaldo/restauraci?n: esquema y una categor?a ficticia antes del seed. No demuestra recuperaci?n institucional, archivos externos ni respaldo autom?tico.
- Rotaci?n de secretos, actualizaciones, alertas/retenci?n y liberaciones futuras son procedimientos propuestos, no acciones ejecutadas.
- Ninguna escritura productiva, despliegue, push ni commit. Copias temporales utilizadas; monograf?a, Anexo A y otros documentos originales sin cambios.

- Precisi?n del proveedor: prod declara Supabase, pero hay que comprobar la selecci?n efectiva; no se afirma un guard adicional por perfil.
