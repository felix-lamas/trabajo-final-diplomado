!INC Local Scripts.EAConstants-JavaScript

/*
 * FIGURA 3 — Arquitectura real de Vidia. JavaScript clásico para EA.
 * Basado en el script original: Component, Node, Boundary y Dependency.
 * Reejecución idempotente en un subpaquete propio; original intacto.
 * No requiere Node.js, SQL ni métodos privados de Enterprise Architect.
 *
 * Evidencia del repositorio:
 * - backend/pom.xml: Java 21 / Spring Boot 3.3.0.
 * - frontend/package.json: Angular 21; environment.ts: API HTTPS en Render.
 * - SDK local: Flutter 3.44.8. pubspec.yaml no fija la versión exacta del SDK.
 * - application-prod.yml: Storage supabase; implementación AWS SDK S3.
 * - SupabaseStorageConfiguration: endpoint HTTPS obligatorio.
 * - SupabaseS3StorageService: Put/Get/Delete/HeadObject, bytes y Content-Type.
 * - application.yml: MAIL_PROVIDER por defecto brevo-api, no smtp.
 * - BrevoApiCorreoProveedor: HTTPS POST /v3/smtp/email, JSON/textContent.
 *   La palabra smtp en esa URL NO convierte el transporte en SMTP.
 * - SmtpCorreoProveedor: alternativa condicional MAIL_PROVIDER=smtp;
 *   STARTTLS configurable. No prueba que Brevo SMTP esté activo en Render.
 * - PostgreSQL: driver JDBC; versión del servidor desplegado no comprobada.
 * - CertificadoDocumentoService: PDF generado al descargar, no objeto S3.
 *
 * Alojamiento Render según contexto del proyecto y configuración disponible.
 * El script no consulta credenciales, Render ni la base de datos.
 * No confirma SHA, perfil activo, disponibilidad ni ejecución en producción.
 *
 * API de automatización: Collections.AddNew/GetAt/Refresh, Update,
 * DiagramObjects, Element.ParentID, Connector.ClientID/SupplierID/Direction.
 * Referencia: https://sparxsystems.com/resources/user-guides/15.1/large-print/automation/automation.pdf
 * AddNew usa Y positivas; propiedades Top/Bottom persistidas usan Y negativas.
 * Para analizar sintaxis fuera de EA, omitir solo la primera directiva !INC.
 */

function main()
{
    var MARCA = "[VIDIA-ARQUITECTURA-ACTUALIZADA-v1]";
    var PAQUETE = "Vidia - Arquitectura actualizada (Figura 3)";
    var FIGURA = "Figura 3 - Arquitectura de Vidia - Actualizada";
    var seleccionado = Repository.GetTreeSelectedPackage();
    if (seleccionado == null)
    {
        Session.Prompt("Seleccione primero un paquete del modelo.", promptOK);
        return;
    }

    function guardar(objeto, mensaje)
    {
        if (!objeto.Update())
        {
            throw new Error(mensaje + ": " + objeto.GetLastError());
        }
    }

    function propio(objeto)
    {
        return String(objeto.Notes).indexOf(MARCA) == 0;
    }

    try
    {
        var destino = null;
        if (seleccionado.Name == PAQUETE && propio(seleccionado))
        {
            destino = seleccionado;
        }
        else
        {
            seleccionado.Packages.Refresh();
            for (var p = 0; p < seleccionado.Packages.Count; p++)
            {
                var hijo = seleccionado.Packages.GetAt(p);
                if (hijo.Name == PAQUETE)
                {
                    if (!propio(hijo))
                    {
                        throw new Error("El paquete homónimo no pertenece al script. No se modificó.");
                    }
                    destino = hijo;
                    break;
                }
            }
            if (destino == null)
            {
                destino = seleccionado.Packages.AddNew(PAQUETE, "");
                destino.Notes = MARCA + "\r\nModelo independiente de la Figura 3 original.";
                guardar(destino, "Crear paquete");
                seleccionado.Packages.Refresh();
            }
        }

        destino.Diagrams.Refresh();
        var diagrama = null;
        for (var d = 0; d < destino.Diagrams.Count; d++)
        {
            var candidato = destino.Diagrams.GetAt(d);
            if (candidato.Name == FIGURA)
            {
                if (!propio(candidato) || candidato.Type != "Component")
                {
                    throw new Error("Existe una figura homónima ajena o de otro tipo. No se modificó.");
                }
                diagrama = candidato;
                break;
            }
        }
        if (diagrama == null)
        {
            diagrama = destino.Diagrams.AddNew(FIGURA, "Component");
            diagrama.Notes = MARCA + "\r\nMonolito modular en capas. Dependencias orientadas del " +
                "consumidor al proveedor; no representan una red de microservicios. " +
                "Las respuestas regresan por las mismas comunicaciones, sin flechas duplicadas.";
            guardar(diagrama, "Crear diagrama");
            destino.Diagrams.Refresh();
        }

        // Busca también elementos anidados para que ParentID no cause duplicados.
        function buscarEn(coleccion, identificador)
        {
            coleccion.Refresh();
            for (var i = 0; i < coleccion.Count; i++)
            {
                var item = coleccion.GetAt(i);
                if (String(item.Notes).split("\r\n")[0].split("\n")[0] == identificador)
                {
                    return item;
                }
                var anidado = buscarEn(item.Elements, identificador);
                if (anidado != null) return anidado;
            }
            return null;
        }

        function elemento(clave, nombre, tipo, notas, padre)
        {
            var id = MARCA + " " + clave;
            var modelo = buscarEn(destino.Elements, id);
            if (modelo == null)
            {
                modelo = destino.Elements.AddNew(nombre, tipo);
            }
            else if (modelo.Type != tipo)
            {
                throw new Error("Tipo incompatible en elemento propio: " + clave);
            }
            modelo.Name = nombre;
            modelo.Notes = id + "\r\n" + notas;
            modelo.ParentID = padre == null ? 0 : padre.ElementID;
            guardar(modelo, "Guardar " + nombre);
            destino.Elements.Refresh();
            return modelo;
        }

        function colocar(modelo, l, r, t, b, orden)
        {
            diagrama.DiagramObjects.Refresh();
            var objeto = null;
            for (var i = 0; i < diagrama.DiagramObjects.Count; i++)
            {
                var actual = diagrama.DiagramObjects.GetAt(i);
                if (actual.ElementID == modelo.ElementID)
                {
                    objeto = actual;
                    break;
                }
            }
            if (objeto == null)
            {
                objeto = diagrama.DiagramObjects.AddNew(
                    "l=" + l + ";r=" + r + ";t=" + t + ";b=" + b + ";", "");
                objeto.ElementID = modelo.ElementID;
            }
            objeto.Left = l;
            objeto.Right = r;
            objeto.Top = -t;
            objeto.Bottom = -b;
            objeto.Sequence = orden;
            objeto.FontName = "Arial";
            objeto.FontSize = "12";
            objeto.BackgroundColor = 16777215;
            objeto.BorderColor = 0;
            objeto.FontColor = 0;
            guardar(objeto, "Posicionar " + modelo.Name);
            diagrama.DiagramObjects.Refresh();
        }

        function conectar(origen, receptor, etiqueta, notas)
        {
            origen.Connectors.Refresh();
            var conector = null;
            for (var i = 0; i < origen.Connectors.Count; i++)
            {
                var actual = origen.Connectors.GetAt(i);
                if (actual.Type == "Dependency" && actual.ClientID == origen.ElementID &&
                    actual.SupplierID == receptor.ElementID && propio(actual))
                {
                    conector = actual;
                    break;
                }
            }
            if (conector == null)
            {
                conector = origen.Connectors.AddNew(etiqueta, "Dependency");
            }
            // El nombre del conector es la etiqueta visible de la comunicación.
            conector.Name = etiqueta;
            conector.Notes = MARCA + "\r\n" + notas;
            conector.ClientID = origen.ElementID;
            conector.SupplierID = receptor.ElementID;
            conector.Direction = "Source -> Destination";
            guardar(conector, "Conectar " + origen.Name + " / " + receptor.Name);
            origen.Connectors.Refresh();
        }

        var sistema = elemento("sistema", "Vidia — Plataforma de Gestión de Eventos UAJMS",
            "Boundary", "Límite lógico: clientes, backend y persistencia. Proveedores externos " +
            "se dibujan fuera. Nodos muestran alojamiento, no procesos adicionales.", null);
        colocar(sistema, 30, 2170, 80, 1130, 1000);

        var renderWeb = elemento("render-web", "Render\nAlojamiento web (Static Site)", "Node",
            "render.yaml: rootDir frontend, build Angular, archivos estáticos y fallback SPA. " +
            "No se afirma que el servicio esté administrado por Blueprint.", null);
        colocar(renderWeb, 80, 490, 190, 500, 900);
        var web = elemento("web", "Aplicación web\nAngular 21\nParticipante / Organizador / Administrador",
            "Component", "Angular consume la API de producción mediante HTTPS. " +
            "La interfaz no accede directamente a BD, Storage o correo. " +
            "Versión mayor 21; package-lock del corte resuelve 21.2.17.", null);
        colocar(web, 110, 460, 290, 430, 1);

        var telefono = elemento("android", "Dispositivo Android\nCliente del participante", "Node",
            "Ejecución móvil en dispositivo; no alojada en Render. Sin administración ni " +
            "gestión móvil de organizadores.", null);
        colocar(telefono, 80, 490, 660, 950, 900);
        var movil = elemento("movil", "Aplicación móvil\nFlutter 3.44.8\nParticipante", "Component",
            "Versión observada del SDK local. API_BASE_URL HTTPS en release. " +
            "Cámara QR y GPS mediante plugins; llamadas protegidas a la API backend.", null);
        colocar(movil, 110, 460, 750, 890, 1);

        var renderApi = elemento("render-api", "Render\nAlojamiento backend (Web Service)", "Node",
            "Dockerfile: imagen Java 21, único JAR Spring Boot. Instancia y recursos " +
            "efectivos del servicio desplegado no se consultan desde el script.", null);
        colocar(renderApi, 820, 1460, 190, 1050, 900);
        var backend = elemento("backend", "Backend Vidia\nJava 21 / Spring Boot 3.3.0\nMonolito modular en capas",
            "Component", "Una aplicación/despliegue. Módulos de usuarios, categorías, eventos, " +
            "inscripciones, pagos, sesiones, asistencias, certificados y reportes. " +
            "Seguridad transversal: JWT, sesiones revocables, roles y ownership. " +
            "Los elementos anidados son capas internas, no microservicios.", null);
        colocar(backend, 850, 1430, 280, 1020, 700);
        var api = elemento("capa-api", "Capa de presentación\nControladores REST / DTO / validaciones",
            "Component", "Adaptadores HTTP bajo /api/v1 y manejo global de errores.", backend);
        colocar(api, 900, 1380, 420, 525, 1);
        var negocio = elemento("capa-negocio", "Capa de aplicación y dominio\nServicios y módulos de negocio",
            "Component", "Reglas/transacciones centralizadas: roles, eventos, inscripción, " +
            "pagos, sesiones, asistencia QR/GPS, certificados y reportes.", backend);
        colocar(negocio, 900, 1380, 645, 750, 1);
        var datos = elemento("capa-datos", "Capa de persistencia e integración\nRepositorios JPA / adaptadores externos",
            "Component", "Repositorios, PostgreSQL, Storage S3 y proveedor de correo. " +
            "Credenciales exclusivamente en backend/configuración segura.", backend);
        colocar(datos, 900, 1380, 870, 975, 1);

        var renderDb = elemento("render-db", "Render\nServicio de persistencia", "Node",
            "Alojamiento PostgreSQL indicado por el contexto de despliegue. No se verifican " +
            "instancia, versión, TLS ni valores DB_URL efectivos de producción.", null);
        colocar(renderDb, 1820, 2120, 225, 495, 900);
        var postgres = elemento("postgres", "Base de datos\nPostgreSQL", "Component",
            "Driver org.postgresql.Driver, URL jdbc:postgresql. SQL mediante JDBC/TCP; " +
            "JPA/Hibernate y migraciones Flyway V1–V5. Sin número de versión desplegada " +
            "ni puerto/TLS no comprobados.", null);
        colocar(postgres, 1840, 2100, 325, 430, 1);

        var storage = elemento("storage", "Servicio externo\nSupabase Storage privado\nAPI compatible con S3", "Component",
            "Perfil prod selecciona proveedor supabase. AWS SDK S3: objetos binarios, " +
            "Content-Type y metadatos; no API JSON de subida. Comprobantes PDF/JPEG/PNG " +
            "y QR de pago PNG/JPEG. Certificados PDF se generan en backend al descargar: " +
            "no se afirma persistencia de esos PDF en este Storage.", null);
        colocar(storage, 2310, 2720, 595, 745, 1);
        var correo = elemento("correo", "Servicio externo\nBrevo\nCorreo transaccional", "Component",
            "Proveedor predeterminado: brevo-api. HTTPS POST /v3/smtp/email, cuerpo JSON " +
            "con sender/to/subject/textContent. API key solo backend. " +
            "Alternativa smtp existe, pero no se confirma activa ni su servidor efectivo.", null);
        colocar(correo, 2310, 2720, 895, 1045, 1);

        conectar(web, backend, "HTTPS / JSON\nmultipart/form-data / archivos binarios",
            "REST JSON; cargas multipart; respuestas PDF, imágenes y exportaciones según endpoint. " +
            "Authorization cuando corresponde; acceso público también existe.");
        conectar(movil, backend, "HTTPS / JSON\nmultipart/form-data / PDF e imágenes",
            "REST compartida, comprobantes multipart y descargas binarias. API_BASE_URL " +
            "de producción debe configurarse; el script no asegura el valor del APK instalado.");
        conectar(api, negocio, "En proceso / llamadas Java y DTO",
            "Invocación dentro de la misma JVM; no HTTP ni intercambio entre servicios desplegados.");
        conectar(negocio, datos, "En proceso / llamadas Java y entidades",
            "Dependencia de servicios hacia repositorios y adaptadores en la misma aplicación.");
        conectar(backend, postgres, "JDBC sobre TCP / SQL",
            "JDBC PostgreSQL a través de repositorios JPA. No inferir puerto ni cifrado " +
            "del canal sin verificar configuración efectiva.");
        conectar(backend, storage, "HTTPS / API S3\nbytes PDF, JPEG y PNG",
            "Put/Get/Delete/HeadObject con SDK S3, Content-Type y transferencia binaria. " +
            "Endpoint HTTPS validado en SupabaseStorageConfiguration. Sin acceso directo cliente.");
        conectar(backend, correo, "HTTPS / API Brevo / JSON\ncontenido de correo en textContent",
            "Transporte confirmado por implementación predeterminada. /v3/smtp/email es " +
            "ruta HTTP, no una sesión SMTP. No se dibuja SMTP/TLS/MIME como canal activo.");

        var leyenda = elemento("leyenda", "Convenciones y alcance verificado", "Note",
            "Los nodos representan alojamiento; las flechas son dependencias consumidor/proveedor.\r\n" +
            "Las capas se ejecutan en el mismo monolito/JVM. No son microservicios.\r\n" +
            "Web y Flutter solo comunican con el backend para operaciones protegidas.\r\n" +
            "Brevo: configuración predeterminada brevo-api, HTTPS/JSON. SMTP es alternativa " +
            "condicional; STARTTLS configurable, no confirmado activo en producción.\r\n" +
            "Render/PostgreSQL: alojamiento según contexto; versión de BD y SHA desplegado no verificados.\r\n" +
            "Supabase guarda comprobantes y QR de pago; PDF de certificado generado en backend.\r\n" +
            "Revisar etiquetas y rutas en EA antes de exportar como figura vectorial.", null);
        colocar(leyenda, 820, 2170, 1200, 1450, 1);

        guardar(diagrama, "Guardar figura actualizada");
        Repository.ReloadDiagram(diagrama.DiagramID);
        Session.Output("Figura 3 actualizada preparada; original conservado. " +
            "Monolito modular, Storage S3 y Brevo HTTPS/JSON.");
        Session.Prompt("Figura 3 actualizada preparada.\nRevise etiquetas y rutas de " +
            "comunicación en Enterprise Architect antes de exportar.", promptOK);
    }
    catch (error)
    {
        var mensaje = String(error.message || error);
        Session.Output("Figura 3: ejecución detenida. " + mensaje);
        Session.Prompt("No se completó la figura actualizada:\n" + mensaje +
            "\nEl original permanece intacto. Puede reintentar: se reutilizan los objetos " +
            "propios que ya se guardaron.", promptOK);
    }
}

main();
