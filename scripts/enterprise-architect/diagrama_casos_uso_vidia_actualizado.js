!INC Local Scripts.EAConstants-JavaScript

/*
 * Figura 2 actualizada — Vidia / UAJMS — RF-01 a RF-13.
 * Basado en el script suministrado: JavaScript clásico, Repository y Session.
 * Ejecutar en Enterprise Architect con un paquete seleccionado.
 * Crea un subpaquete independiente; nunca modifica el diagrama original.
 * Reutiliza paquete, diagrama, elementos, posiciones y asociaciones propios.
 * No utiliza SQL, DeleteAt, generación automática de IDs ni APIs de Node.js.
 *
 * Semántica UML:
 * - Asociaciones actor/caso de uso sin dirección, no flujos de trabajo.
 * - Autenticación es precondición; no un <<include>> repetido en cada caso.
 * - Enviar a revisión y aprobar/publicar son objetivos de actores diferentes,
 *   no un <<include>> que otorgue al organizador autoridad de publicación.
 * - Sin generalización Organizador -> Participante: la cuenta dual conserva
 *   explícitamente USUARIO + ORGANIZADOR, no una jerarquía de roles.
 * - Verificación pública sin autenticación; no se añade un cuarto actor.
 *
 * API contrastada con documentación de Sparx Systems:
 * https://sparxsystems.com/enterprise_architect_user_guide/17.2/add-ins___scripting/addandmanagediagrams.html
 * https://sparxsystems.com/enterprise_architect_user_guide/17.1/add-ins___scripting/diagramobjects.html
 * https://sparxsystems.com/resources/user-guides/15.1/large-print/automation/automation.pdf
 * AddNew recibe coordenadas Y positivas; Top/Bottom persistidos son negativos.
 * Validación externa: omitir únicamente la directiva !INC al analizar sintaxis.
 * La comprobación con dobles de API no equivale a ejecución real en EA.
 */

function main()
{
    var MARCA = "[VIDIA-CASOS-USO-ACTUALIZADO-v1]";
    var NOMBRE_PAQUETE = "Vidia - Casos de uso actualizados (RF-01 a RF-13)";
    var NOMBRE_DIAGRAMA = "Figura 2 - Casos de uso de Vidia - Actualizado";
    var seleccionado = Repository.GetTreeSelectedPackage();

    if (seleccionado == null)
    {
        Session.Prompt("Seleccione el paquete de destino en Enterprise Architect.", promptOK);
        return;
    }

    function guardar(objeto, contexto)
    {
        if (!objeto.Update())
        {
            throw new Error(contexto + ": " + objeto.GetLastError());
        }
    }

    function pertenece(objeto)
    {
        return String(objeto.Notes).indexOf(MARCA) == 0;
    }

    function obtenerPaquete()
    {
        // También permite volver a ejecutar seleccionando el subpaquete creado.
        if (seleccionado.Name == NOMBRE_PAQUETE && pertenece(seleccionado))
        {
            return seleccionado;
        }
        seleccionado.Packages.Refresh();
        for (var i = 0; i < seleccionado.Packages.Count; i++)
        {
            var existente = seleccionado.Packages.GetAt(i);
            if (existente.Name == NOMBRE_PAQUETE)
            {
                if (!pertenece(existente))
                {
                    throw new Error("Existe un paquete homónimo ajeno al script. No se modificó.");
                }
                return existente;
            }
        }
        var nuevo = seleccionado.Packages.AddNew(NOMBRE_PAQUETE, "");
        nuevo.Notes = MARCA + "\r\nModelo independiente del diagrama original.";
        guardar(nuevo, "Crear paquete actualizado");
        seleccionado.Packages.Refresh();
        return nuevo;
    }

    try
    {
        var destino = obtenerPaquete();
        destino.Diagrams.Refresh();
        var diagrama = null;
        for (var d = 0; d < destino.Diagrams.Count; d++)
        {
            var candidato = destino.Diagrams.GetAt(d);
            if (candidato.Name == NOMBRE_DIAGRAMA)
            {
                if (!pertenece(candidato) || candidato.Type != "Use Case")
                {
                    throw new Error("Existe un diagrama homónimo ajeno o de otro tipo. No se modificó.");
                }
                diagrama = candidato;
                break;
            }
        }
        if (diagrama == null)
        {
            diagrama = destino.Diagrams.AddNew(NOMBRE_DIAGRAMA, "Use Case");
            diagrama.Notes = MARCA + "\r\nRF-01 a RF-13. Web: gestión y administración. " +
                "Flutter: participación. Modelo de objetivos, no secuencia temporal.";
            guardar(diagrama, "Crear diagrama actualizado");
            destino.Diagrams.Refresh();
        }

        function elemento(clave, nombre, tipo, notas)
        {
            var identificador = MARCA + " " + clave;
            destino.Elements.Refresh();
            for (var i = 0; i < destino.Elements.Count; i++)
            {
                var encontrado = destino.Elements.GetAt(i);
                var primeraLinea = String(encontrado.Notes).split("\r\n")[0].split("\n")[0];
                if (primeraLinea == identificador)
                {
                    if (encontrado.Type != tipo)
                    {
                        throw new Error("Tipo incompatible en elemento propio: " + clave);
                    }
                    encontrado.Name = nombre;
                    encontrado.Notes = identificador + "\r\n" + notas;
                    guardar(encontrado, "Actualizar " + nombre);
                    return encontrado;
                }
            }
            var nuevo = destino.Elements.AddNew(nombre, tipo);
            nuevo.Notes = identificador + "\r\n" + notas;
            guardar(nuevo, "Crear " + nombre);
            destino.Elements.Refresh();
            return nuevo;
        }

        function colocar(modelo, izquierda, derecha, arriba, abajo, fondo)
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
                    "l=" + izquierda + ";r=" + derecha + ";t=" + arriba + ";b=" + abajo + ";", "");
                objeto.ElementID = modelo.ElementID;
            }
            // Propiedades directas: convención cartesiana de EA, Y negativa.
            objeto.Left = izquierda;
            objeto.Right = derecha;
            objeto.Top = -arriba;
            objeto.Bottom = -abajo;
            objeto.Sequence = fondo ? 1000 : 1;
            objeto.FontName = "Arial";
            objeto.FontSize = "11";
            objeto.BackgroundColor = 16777215;
            objeto.BorderColor = 0;
            objeto.FontColor = 0;
            guardar(objeto, "Posicionar " + modelo.Name);
            diagrama.DiagramObjects.Refresh();
        }

        function asociar(actor, casoUso)
        {
            actor.Connectors.Refresh();
            for (var i = 0; i < actor.Connectors.Count; i++)
            {
                var existente = actor.Connectors.GetAt(i);
                if (existente.Type == "Association" &&
                    ((existente.ClientID == actor.ElementID && existente.SupplierID == casoUso.ElementID) ||
                     (existente.SupplierID == actor.ElementID && existente.ClientID == casoUso.ElementID)))
                {
                    return; // La misma asociación no se crea por segunda vez.
                }
            }
            var relacion = actor.Connectors.AddNew("", "Association");
            relacion.ClientID = actor.ElementID;
            relacion.SupplierID = casoUso.ElementID;
            relacion.Direction = "Unspecified";
            guardar(relacion, "Asociar " + actor.Name + " / " + casoUso.Name);
            actor.Connectors.Refresh();
        }

        var limite = elemento("sistema", "Vidia - Plataforma de eventos universitarios UAJMS", "Boundary",
            "Backend: fuente de verdad. Web para participantes, organizadores y administradores. " +
            "Flutter para participantes. Sin pasarela integrada ni funciones fuera del alcance.");
        colocar(limite, 230, 1570, 170, 1730, true);

        var administrador = elemento("actor-admin", "Administrador", "Actor",
            "ADMINISTRADOR. Cuenta administrativa independiente. No se asocia a inscripción, " +
            "pago propio ni registro de asistencia. Administración exclusivamente Web.");
        var organizador = elemento("actor-organizador", "Organizador", "Actor",
            "ORGANIZADOR. Gestión Web limitada a eventos propios. Una cuenta dual conserva " +
            "USUARIO y actúa como Participante al inscribirse, pagar o registrar asistencia.");
        var participante = elemento("actor-participante", "Participante", "Actor",
            "USUARIO. Funciones de participación Web/Flutter. Representa también a quien " +
            "inicia registro y consultas públicas; esas operaciones no exigen sesión.");
        colocar(administrador, 35, 135, 400, 520, false);
        colocar(organizador, 850, 950, 15, 130, false);
        colocar(participante, 1660, 1760, 760, 880, false);

        var actores = { A: administrador, O: organizador, P: participante };
        var casos = [
            // Columna administrativa: no atribuir publicación al organizador.
            ["resolver-organizador", "Autorizar o rechazar solicitudes\nde organizador [RF-02]", "A", 280, 230,
                "Resolver solicitudes pendientes. Aprobación agrega ORGANIZADOR y conserva USUARIO. " +
                "Rechazo conserva roles y motivo; reintento según backend."],
            ["revisar-eventos", "Revisar y aprobar/publicar\neventos [RF-04]", "A", 280, 410,
                "Solo ADMINISTRADOR publica eventos EN_REVISION; puede rechazarlos con motivo. " +
                "No publica el organizador ni se omite la revisión administrativa."],
            ["cerrar-eventos", "Cancelar o finalizar\neventos [RF-04]", "A", 280, 590,
                "Transiciones administrativas según estado, motivo y fecha admitidos por backend."],
            ["categorias", "Gestionar categorías\nde eventos [RF-11]", "A", 280, 770,
                "Crear, editar y eliminar cuando no existan referencias; validar unicidad normalizada."],
            ["validar-pagos", "Revisar y validar\ncomprobantes de pago [RF-07]", "AO", 280, 950,
                "ADMINISTRADOR o ORGANIZADOR del evento aprueba/rechaza comprobantes. " +
                "Aprobación confirma inscripción. No es procesamiento bancario."],
            ["certificar", "Generar certificados\nelegibles [RF-09]", "AO", 280, 1130,
                "ADMINISTRADOR u ORGANIZADOR propietario. Evento FINALIZADO, inscripción CONFIRMADA, " +
                "pago APROBADO si corresponde y asistencia exigida. Curricular: al menos 80 %."],
            ["reportes", "Consultar estadísticas y\nexportar reportes [RF-12]", "AO", 280, 1310,
                "Datos reales y exportación PDF/XLSX según alcance. Organizador: eventos propios. " +
                "No presentar satisfacción, encuestas o QR utilizados placeholder como mediciones."],

            // Columna de organización y operaciones compartidas por ownership.
            ["gestionar-eventos", "Crear y editar\neventos propios [RF-03]", "O", 750, 230,
                "Crear BORRADOR y editar en estados permitidos. Incluye información de pago externo " +
                "y QR de pago opcional. No permite editar eventos ajenos."],
            ["enviar-revision", "Enviar eventos propios\na revisión [RF-03]", "O", 750, 410,
                "BORRADOR válido pasa a EN_REVISION; corregir/reenviar rechazados según transiciones. " +
                "No concede autoridad para publicar."],
            ["participantes-evento", "Consultar participantes\ndel evento [RF-06]", "AO", 750, 590,
                "Organizador consulta inscripciones de eventos propios; administrador según alcance global."],
            ["sesiones", "Gestionar sesiones\ndel evento [RF-08]", "AO", 750, 770,
                "Configurar fecha/horario, estado, modalidad, coordenadas y radio según validaciones. " +
                "Organizador limitado al evento propio."],
            ["emitir-qr", "Generar o renovar QR\nde sesión [RF-08]", "AO", 750, 950,
                "QR temporal vinculado a sesión, dentro del alcance autorizado. Se puede utilizar " +
                "por múltiples participantes elegibles; no es un token global de un solo uso."],
            ["consultar-asistencias", "Consultar asistencias\ndel evento [RF-08]", "AO", 750, 1130,
                "Consulta de asistencias registradas por evento. Organizador: solo eventos propios. " +
                "No equivale a registrar asistencia como participante."],
            ["invitar-admin", "Invitar y gestionar\nadministradores", "A", 750, 1310,
                "Capacidad adicional implementada, sin inventar un nuevo RF. Invitación/activación " +
                "segura para cuenta solo ADMINISTRADOR; impedir desactivar al último activo. " +
                "La activación mediante token es pública controlada, no registro libre de administradores."],

            // Columna participante: también cubre consultas públicas.
            ["registro", "Registrarse y verificar\ncorreo [RF-01]", "P", 1220, 230,
                "Registro público crea USUARIO. Verificación por enlace de correo, token temporal. " +
                "Recibir enlace y verificar son parte del flujo; no exigir login previo al registro."],
            ["solicitar-organizador", "Solicitar condición\nde organizador [RF-02]", "P", 1220, 410,
                "USUARIO elegible: motivo de 30 a 1000 caracteres, categorías controladas e información " +
                "opcional. Consultar estado/rechazo y volver a solicitar cuando backend lo permita. " +
                "ADMINISTRADOR no solicita; no duplicar solicitud pendiente."],
            ["catalogo", "Buscar y consultar\neventos publicados [RF-05]", "P", 1220, 590,
                "Consulta pública sin autenticación, disponible a cualquier persona. " +
                "Filtros reales de texto, categoría, modalidad y tipo de inscripción."],
            ["inscribirse", "Inscribirse y consultar\ninscripciones propias [RF-06]", "P", 1220, 770,
                "Usuario autenticado, evento PUBLICADO, inscripción habilitada/cupo. Gratis: CONFIRMADA; " +
                "pagado: PENDIENTE_PAGO. Controlar duplicados y cancelación según reglas."],
            ["comprobante", "Presentar y consultar\ncomprobantes propios [RF-07]", "P", 1220, 950,
                "Pagar externamente según instrucciones del evento; el sistema recibe comprobante, " +
                "no realiza pago bancario. Subir archivo privado, consultar estado y reenviar tras " +
                "rechazo cuando esté permitido. La carga no ocurre en toda inscripción gratuita."],
            ["asistencia", "Registrar asistencia con\nQR y ubicación [RF-08]", "P", 1220, 1130,
                "Flutter: consultar sesión, escanear QR y obtener GPS cuando corresponda. Backend exige " +
                "USUARIO, inscripción propia CONFIRMADA, QR vigente, sesión activa, evento PUBLICADO, " +
                "ventana temporal y ausencia de duplicado. GPS: precisión y distancia/radio válidos. " +
                "Cuenta dual actúa bajo USUARIO; ADMINISTRADOR no registra asistencia."],
            ["certificados-propios", "Consultar y descargar\ncertificados propios [RF-09]", "P", 1220, 1310,
                "Consultar certificados emitidos para el usuario autenticado y descargar PDF autorizado."],
            ["historial", "Consultar historial\nde participación [RF-10]", "P", 1220, 1490,
                "Consultar inscripciones, pagos, asistencias y certificados propios por recursos existentes. " +
                "Flutter agrega los cuatro dominios; no presupone endpoint agregado único."],

            // Franja inferior común. Sin <<include>> de login ni jerarquía de actores.
            ["login", "Iniciar y cerrar\nsesión [RF-01]", "AOP", 280, 1490,
                "Mismo mecanismo de login para todos los roles. JWT/sesión revocable; logout revoca. " +
                "Los casos protegidos requieren sesión válida como precondición."],
            ["cuenta", "Gestionar perfil y\nrecuperar acceso [RF-01]", "AOP", 750, 1490,
                "Editar perfil propio/cambiar contraseña con autenticación. Solicitar recuperación " +
                "y restablecer por enlace sin login, con token válido. No solicitar token manual como flujo principal."]
        ];

        for (var c = 0; c < casos.length; c++)
        {
            var datos = casos[c];
            var caso = elemento(datos[0], datos[1], "UseCase", datos[5]);
            colocar(caso, datos[3], datos[3] + 300, datos[4], datos[4] + 100, false);
            for (var a = 0; a < datos[2].length; a++)
            {
                asociar(actores[datos[2].charAt(a)], caso);
            }
        }

        // Banda pública separada: no conectarla a login mediante include/extend.
        var publico = elemento("verificacion-publica",
            "Verificar públicamente un certificado [RF-13]", "UseCase",
            "Cualquier persona, con o sin cuenta/sesión, consulta por código. " +
            "No exige JWT ni acceso al PDF privado. Código inexistente: valido=false, NO_REGISTRADO. " +
            "Las asociaciones con los tres actores no son restricciones de acceso por rol.");
        colocar(publico, 650, 1150, 1630, 1710, false);
        asociar(administrador, publico);
        asociar(organizador, publico);
        asociar(participante, publico);

        var nota = elemento("leyenda", "Alcance y permisos", "Note",
            "Lectura del diagrama:\r\n" +
            "1. RF-05 y RF-13 son públicos: no requieren autenticación.\r\n" +
            "2. USUARIO + ORGANIZADOR conserva ambos roles; participa como Participante.\r\n" +
            "3. Organizador gestiona sus eventos y envía a revisión; Administrador aprueba/publica.\r\n" +
            "4. Pagos externos con comprobante; sin pasarela integrada.\r\n" +
            "5. Flutter captura QR/GPS; gestión organizador/administrador exclusivamente Web.\r\n" +
            "6. Cada caso conserva precondiciones y trazabilidad en sus notas.\r\n" +
            "Sin chat, streaming propio, reconocimiento facial, multiuniversidad, " +
            "facturación electrónica ni blockchain.");
        colocar(nota, 280, 1520, 1800, 2040, false);

        guardar(diagrama, "Guardar diagrama actualizado");
        Repository.ReloadDiagram(diagrama.DiagramID);
        Session.Output("Vidia: diagrama actualizado independiente, " +
            (casos.length + 1) + " casos y tres actores. Original conservado.");
        Session.Prompt("Diagrama actualizado preparado.\n\n" +
            "Revise las rutas de asociaciones en Enterprise Architect antes de exportar.\n" +
            "Las notas de cada caso documentan permisos y precondiciones.", promptOK);
    }
    catch (error)
    {
        Session.Output("Vidia: ejecución detenida. " + String(error.message || error));
        Session.Prompt("No se completó el diagrama actualizado.\n" +
            String(error.message || error) + "\n\nEl diagrama original no se modifica. " +
            "Puede reintentar: se reutilizarán los elementos propios ya guardados.", promptOK);
    }
}

main();
