!INC Local Scripts.EAConstants-JavaScript

/* Vidia: E.12 / E.13. JavaScript clásico para el motor de EA.
 * No abre archivos, no ejecuta SQL ni llama a Vidia.
 * Las agrupaciones de clases y los escenarios omitidos se explican en el informe.
 * EA 17.2: Sequence, Object, InteractionFragment, Partitions y Connector.Properties.
 * Las propiedades IsReturn/IsAsync se comprobaron en la instalación detectada.
 * Las coordenadas verticales de mensajes son negativas en el modelo de EA.
 */
var VIDIA_PAQUETE = "Anexo E - Diagramas de Secuencia";
var VIDIA_PASO = 35;
var VIDIA_ORIGEN = 100; // Separación y origen comprobados en EA 17.2 / modelo temporal.

function describirError(error) {
    return error.message || error.description || String(error);
}

function comprobar(objeto, contexto) {
    if (!objeto.Update()) {
        var detalle = "";
        try { detalle = objeto.GetLastError(); } catch (ignorado) {}
        throw new Error(contexto + ": " + detalle);
    }
}

function mensaje(desde, hasta, texto, retorno, nota) {
    return { clase: "mensaje", desde: desde, hasta: hasta, texto: texto,
        retorno: retorno === true, nota: nota || "" };
}
function fragmento(operador, ramas) {
    return { clase: "fragmento", operador: operador, ramas: ramas };
}
function rama(guarda, pasos) { return { guarda: guarda, pasos: pasos }; }
function senal(desde, hasta, texto, nota) {
    var p = mensaje(desde, hasta, texto, false, nota);
    p.asincrono = true;
    return p;
}

function especificacionesVidia() {
    var m = mensaje, f = fragmento, r = rama;
    return [
        {
            nombre: "Figura E.12. Secuencia de inscripción",
            nota: "InscripcionService.inscribir; transacción con bloqueo pesimista del evento. " +
                "Precondición: JWT/sesión válidos, rol USUARIO y eventoId válido. " +
                "La consulta de duplicado precede al estado y al cupo. " +
                "Persistencia agrupa InscripcionRepository y PagoRepository con PostgreSQL.",
            actores: [
                ["cliente", "Participante / cliente", "Object", "boundary"],
                ["api", "InscripcionController", "Object", "control"],
                ["servicio", "InscripcionService", "Object", "control"],
                ["evento", "EventoRepository", "Object", "entity"],
                ["datos", "Repositorios / PostgreSQL", "Object", "entity"]
            ],
            pasos: [
                m("cliente", "api", "POST /api/v1/inscripciones\n{eventoId}; JWT", false),
                m("api", "servicio", "inscribir(request)\n@Valid; rol USUARIO", false),
                m("servicio", "servicio", "exigirUsuario(); obtenerUsuario()", false,
                    "UsuarioAutenticadoService; no se acepta un usuarioId arbitrario del cliente."),
                m("servicio", "evento", "findByIdForUpdate(eventoId)", false,
                    "EventoRepository: @Lock(PESSIMISTIC_WRITE); @Transactional en inscribir."),
                m("evento", "servicio", "Evento o vacío", true),
                f("opt", [r("evento encontrado", [
                    m("servicio", "datos", "existsByUsuarioIdAndEventoId", false,
                        "InscripcionRepository: consulta visible con borrado lógico."),
                    m("datos", "servicio", "duplicado: sí / no", true)
                ])]),
                f("alt", [
                    r("evento inexistente", [
                        m("servicio", "api", "RecursoNoEncontradoException", true),
                        m("api", "cliente", "404 / RESOURCE_NOT_FOUND", true)
                    ]),
                    r("evento existe y duplicado", [
                        m("servicio", "api", "ConflictoException", true),
                        m("api", "cliente", "409 / INSCRIPTION_DUPLICATED", true)
                    ]),
                    r("sin duplicado; evento no disponible", [
                        m("servicio", "api", "NegocioException", true,
                            "Estado distinto de PUBLICADO: EVENT_NOT_PUBLISHED. " +
                            "Si no requiere inscripción: BUSINESS_RULE_VIOLATION."),
                        m("api", "cliente", "400 / regla de negocio", true)
                    ]),
                    r("reglas previas válidas; sin cupos", [
                        m("servicio", "api", "NegocioException", true),
                        m("api", "cliente", "400 / INSCRIPTION_CAPACITY_FULL", true)
                    ]),
                    r("cupo válido; evento de pago sin monto válido", [
                        m("servicio", "api", "NegocioException", true),
                        m("api", "cliente", "400 / BUSINESS_RULE_VIOLATION", true)
                    ]),
                    r("todas las validaciones satisfactorias", [
                        m("servicio", "servicio", "Crear inscripción y código", false,
                            "GRATUITO: CONFIRMADA; PAGO: PENDIENTE_PAGO. Fecha con Clock."),
                        f("opt", [r("cupo limitado", [
                            m("servicio", "evento", "save(evento): cupo - 1", false)
                        ])]),
                        m("servicio", "datos", "InscripcionRepository.save", false),
                        m("datos", "servicio", "Inscripción guardada", true),
                        f("opt", [r("tipoInscripcion = PAGO", [
                            m("servicio", "datos", "PagoRepository.save\nPENDIENTE_PAGO", false)
                        ])]),
                        m("servicio", "api", "DetalleInscripcionResponse", true,
                            "Mapeo y commit al salir del servicio transaccional; no se representa SQL literal."),
                        m("api", "cliente", "201 / detalle de inscripción", true)
                    ])
                ])
            ]
        },
        {
            nombre: "Figura E.13. Secuencia QR/GPS",
            nota: "QR compartido por sesión, distinto del JWT. No se consume al registrar asistencia. " +
                "API / seguridad agrupa filtros, autorización, controladores y manejo de errores. " +
                "Dispositivo agrupa MobileScanner y Geolocator; persistencia agrupa repositorios JPA/PostgreSQL. " +
                "Se representa emisión autorizada; rechazos de emisión se detallan en el informe.",
            actores: [
                ["gestor", "Gestor autorizado", "Object", "boundary"],
                ["movil", "Participante / Flutter", "Object", "boundary"],
                ["dispositivo", "Escáner / ubicación", "Object", "boundary"],
                ["api", "API / seguridad", "Object", "control"],
                ["qr", "QrAsistenciaService", "Object", "control"],
                ["servicio", "AsistenciaService", "Object", "control"],
                ["datos", "Repositorios / PostgreSQL", "Object", "entity"]
            ],
            pasos: [
                m("gestor", "api", "POST sesiones/{sesionId}/qr/generar\nJWT de gestor", false,
                    "Ruta completa /api/v1/sesiones/{sesionId}/qr/generar. " +
                    "ADMINISTRADOR u ORGANIZADOR; organizador opera eventos propios."),
                m("api", "qr", "generar(sesionId)", false),
                m("qr", "datos", "Sesión bloqueada; revocar QR activos", false,
                    "SesionEventoRepository.findByIdForUpdate, validarOwnership, validar emisión, " +
                    "QrAsistenciaTemporalRepository.findActivosBySesionIdForUpdate y saveAll si hay activos."),
                m("qr", "datos", "save(QR): hash SHA-256; vigencia", false,
                    "32 bytes aleatorios / Base64URL. expiraEn = min(ahora + 2 min, fin de sesión)."),
                m("qr", "api", "QrAsistenciaResponse con token", true),
                m("api", "gestor", "201 / mostrar QR de sesión", true,
                    "La UI Angular genera la imagen con el token recibido. GET QR devuelve metadatos sin token."),
                m("movil", "dispositivo", "Abrir MobileScanner", false),
                f("opt", [r("QR leído", [
                    senal("dispositivo", "movil", "BarcodeCapture: rawValue",
                        "Callback asíncrono _onCapture; no es una devolución síncrona. " +
                        "Si falla la cámara, este callback no entrega un token.")
                ])]),
                f("opt", [r("QR leído; GPS requerido y consentido", [
                    m("movil", "dispositivo", "currentPosition(): permiso y GPS", false,
                        "AsistenciaFlow y GeolocatorAttendanceLocationSource; consentimiento, " +
                        "LocationAccuracy.best, timeLimit 20 s. Si no hay consentimiento no se solicita posición."),
                    m("dispositivo", "movil", "Posición o error de ubicación", true,
                        "Latitude/longitude/accuracy o AttendanceLocationException. " +
                        "La observación de error se agrupa con su tratamiento local.")
                ])]),
                f("alt", [
                    r("cámara/ubicación no disponibles o permiso rechazado", [
                        m("movil", "movil", "Mostrar error; no enviar asistencia", false,
                            "Fallo local: cámara, confirmación de ubicación, servicios GPS, permisos o timeout. " +
                            "No existe respuesta HTTP de registro porque no se envía la solicitud.")
                    ]),
                    r("lectura disponible; preparación satisfactoria", [
                        m("movil", "api", "POST /api/v1/asistencias\ntoken QR; GPS si aplica; JWT", false,
                            "BackendAsistenciaRepository.register / ApiService: JSON; Authorization Bearer JWT. " +
                            "JWT de sesión independiente del token QR enviado en el cuerpo."),
                        f("alt", [
                            r("JWT ausente/inválido o sesión revocada/expirada", [
                                m("api", "movil", "401 / error de autenticación", true,
                                    "AUTH_REQUIRED, AUTH_INVALID_TOKEN o AUTH_INVALID_SESSION según la causa.")
                            ]),
                            r("autenticado; DTO válido; sin rol USUARIO", [
                                m("api", "movil", "403 / ACCESS_DENIED", true)
                            ]),
                            r("autenticado; DTO inválido", [
                                m("api", "movil", "400 / VALIDATION_ERROR", true)
                            ]),
                            r("autenticado USUARIO; entrada válida", [
                                m("api", "servicio", "registrar(request); @Valid", false),
                                m("servicio", "qr", "resolverToken(token)", false),
                                m("qr", "datos", "findByTokenHashForUpdate", false),
                                m("datos", "qr", "QR temporal o vacío", true),
                                m("qr", "servicio", "QR / QR_INVALID", true),
                                f("alt", [
                                    r("QR no encontrado", [
                                        m("servicio", "api", "NegocioException(QR_INVALID)", true),
                                        m("api", "movil", "400 / QR_INVALID", true)
                                    ]),
                                    r("QR encontrado", [
                                        m("servicio", "datos", "Buscar inscripción usuario/evento", false,
                                            "InscripcionRepository.findByUsuarioIdAndEventoId."),
                                        m("datos", "servicio", "Inscripción o vacío", true),
                                        m("servicio", "servicio", "Validar inscripción y sesión", false,
                                            "CONFIRMADA, usuario/evento coincidentes; PUBLICADO; sesión activa, " +
                                            "requiereAsistencia y ahora dentro de inicio/fin."),
                                        f("opt", [r("inscripción y sesión válidas", [
                                            m("servicio", "qr", "validarVigencia(qr, ahora)", false),
                                            f("opt", [r("QR vigente", [
                                                m("servicio", "servicio", "validarGps: distancia + precisión", false,
                                            "Retornos tempranos por excepciones: no se ejecutan validaciones posteriores al primer rechazo. " +
                                            "VIRTUAL sin coordenadas de sesión: GPS omitido. En otros casos precision <= 30 m " +
                                            "y Haversine + precision <= radioMetros; no es prueba antifraude absoluta.")
                                            ])])
                                        ])]),
                                        f("alt", [
                                            r("primera regla de inscripción/sesión/QR/GPS falla", [
                                                m("servicio", "api", "NegocioException / código de causa", true),
                                                m("api", "movil", "400 / rechazo de asistencia", true,
                                                    "INSCRIPTION_REQUIRED/NOT_CONFIRMED; EVENT_NOT_PUBLISHED; " +
                                                    "ATTENDANCE_SESSION_INACTIVE/NOT_REQUIRED/OUTSIDE_WINDOW; " +
                                                    "QR_REVOKED/EXPIRED/INVALID; ATTENDANCE_GPS_INVALID/ACCURACY_INVALID/OUTSIDE_RADIUS.")
                                            ]),
                                            r("reglas previas válidas", [
                                                m("servicio", "datos", "existsByInscripcionIdAndSesionEventoId", false),
                                                m("datos", "servicio", "duplicado: sí / no", true),
                                                f("alt", [
                                                    r("asistencia duplicada", [
                                                        m("servicio", "api", "ConflictoException", true),
                                                        m("api", "movil", "409 / ATTENDANCE_DUPLICATED", true)
                                                    ]),
                                                    r("sin duplicado", [
                                                        m("servicio", "datos", "saveAndFlush(asistencia)\nVALIDADA", false),
                                                        f("alt", [
                                                            r("integridad rechazada al guardar", [
                                                                m("datos", "servicio", "DataIntegrityViolationException", true),
                                                                m("servicio", "api", "ConflictoException", true),
                                                                m("api", "movil", "409 / ATTENDANCE_DUPLICATED", true,
                                                                    "El servicio traduce cualquier DataIntegrityViolationException " +
                                                                    "del saveAndFlush como duplicado; véase limitación en el informe.")
                                                            ]),
                                                            r("escritura confirmada", [
                                                                m("datos", "servicio", "Asistencia guardada", true),
                                                                m("servicio", "api", "Asistencia", true),
                                                                m("api", "movil", "201 / AsistenciaResponse", true),
                                                                m("movil", "movil", "Mostrar resultado; detener escáner", false)
                                                            ])
                                                        ])
                                                    ])
                                                ])
                                            ])
                                        ])
                                    ])
                                ])
                            ])
                        ])
                    ])
                ])
            ]
        }
    ];
}

function altoPasos(pasos) {
    var alto = 0, i, j;
    for (i = 0; i < pasos.length; i++) {
        if (pasos[i].clase === "mensaje") alto += VIDIA_PASO;
        else for (j = 0; j < pasos[i].ramas.length; j++)
            alto += altoPasos(pasos[i].ramas[j].pasos);
    }
    return alto;
}

function guardaBreve(texto) {
    var equivalencias = {
        "evento existe y duplicado": "inscripción duplicada",
        "sin duplicado; evento no disponible": "evento no disponible",
        "reglas previas válidas; sin cupos": "sin cupos",
        "cupo válido; evento de pago sin monto válido": "pago mal configurado",
        "todas las validaciones satisfactorias": "inscripción válida",
        "cámara/ubicación no disponibles o permiso rechazado": "rechazo local",
        "lectura disponible; preparación satisfactoria": "preparación correcta",
        "JWT ausente/inválido o sesión revocada/expirada": "autenticación rechazada",
        "autenticado; DTO válido; sin rol USUARIO": "sin rol USUARIO",
        "autenticado; DTO inválido": "DTO inválido",
        "autenticado USUARIO; entrada válida": "entrada autorizada",
        "primera regla de inscripción/sesión/QR/GPS falla": "regla incumplida",
        "reglas previas válidas": "reglas válidas",
        "integridad rechazada al guardar": "integridad rechazada"
    };
    return equivalencias[texto] || texto;
}

function crearDiagramaVidia(repo, destino, spec) {
    destino.Diagrams.Refresh();
    var i, existente;
    for (i = 0; i < destino.Diagrams.Count; i++) {
        existente = destino.Diagrams.GetAt(i);
        if (existente.Name === spec.nombre) {
            Session.Output("OMITIDO: ya existe '" + spec.nombre + "'. No se modifica. " +
                "Para otra versión seleccione otro paquete de destino.");
            return null;
        }
    }
    var d = destino.Diagrams.AddNew(spec.nombre, "Sequence");
    d.Notes = spec.nota + " Generación en curso; revise el diagnóstico antes de exportar.";
    comprobar(d, "Crear " + spec.nombre);
    destino.Diagrams.Refresh();
    var participantes = {}, xs = {}, secuencia = 0;
    var ancho = (spec.actores.length - 1) * 250 + 220;
    var fondo = 120 + altoPasos(spec.pasos) + 30;

    function posicionar(e, l, t, rr, b) {
        var o = d.DiagramObjects.AddNew("l=" + l + ";r=" + rr + ";t=" + t + ";b=" + b + ";", "");
        o.ElementID = e.ElementID;
        comprobar(o, "Posicionar " + e.Name);
        return o;
    }
    for (i = 0; i < spec.actores.length; i++) {
        var a = spec.actores[i];
        var e = destino.Elements.AddNew(a[1], a[2]);
        e.Notes = spec.nombre + ". Rol conceptual: " + a[3] + ". " + spec.nota;
        comprobar(e, "Crear participante " + a[1]);
        if (e.ElementID <= 0) throw new Error("EA no asignó identificador a " + a[1]);
        participantes[a[0]] = Number(e.ElementID); xs[a[0]] = 100 + i * 250;
        posicionar(e, xs[a[0]] - 95, 25, xs[a[0]] + 95, fondo);
    }

    function agregarMensaje(paso, y) {
        var idDe = participantes[paso.desde], idA = participantes[paso.hasta];
        if (!idDe || !idA) throw new Error("Participante inexistente: " + paso.texto);
        // Conservar identificadores estables, no referencias COM de una colección
        // que puede ser refrescada por EA al insertar fragmentos anidados.
        var de = repo.GetElementByID(idDe);
        if (!de) throw new Error("EA no localiza el emisor de " + paso.texto);
        var c = de.Connectors.AddNew(paso.texto, "Sequence");
        c.ClientID = idDe; c.SupplierID = idA;
        c.DiagramID = d.DiagramID; c.Direction = "Source -> Destination";
        // EA usa SequenceNo y una separación predeterminada de 28 unidades.
        // Los huecos reservan filas para cabeceras/guardas de fragmentos.
        ++secuencia;
        c.SequenceNo = Math.round((y - 100) / 28) + 1;
        c.StartPointX = xs[paso.desde]; c.EndPointX = xs[paso.hasta];
        c.StartPointY = -y; c.EndPointY = -(y + (paso.desde === paso.hasta ? 10 : 0));
        c.Notes = paso.nota;
        comprobar(c, "Crear mensaje " + paso.texto);
        // Estas propiedades especializadas existen en EA 17.2; si no están,
        // se detiene con diagnóstico, en vez de dibujar retornos incorrectos.
        c.Properties.Item("IsReturn").Value = paso.retorno;
        c.Properties.Item("IsAsync").Value = paso.asincrono === true;
        comprobar(c, "Configurar retorno/sincronía " + paso.texto);
    }

    function dibujar(pasos, profundidad) {
        var k, j;
        for (k = 0; k < pasos.length; k++) {
            var paso = pasos[k];
            var y = VIDIA_ORIGEN + VIDIA_PASO * (secuencia + 1);
            if (paso.clase === "mensaje") agregarMensaje(paso, y);
            else {
                var margenFinal = secuencia + altoPasos([paso]) / VIDIA_PASO ===
                    altoPasos(spec.pasos) / VIDIA_PASO ? 15 : 0;
                var h = altoPasos([paso]) - 2 + margenFinal;
                var frag = destino.Elements.AddNew("", "InteractionFragment");
                frag.Subtype = paso.operador === "opt" ? 1 : 0;
                frag.Notes = "Operador UML " + paso.operador + ". " + spec.nombre;
                comprobar(frag, "Crear fragmento " + paso.operador);
                for (j = 0; j < paso.ramas.length; j++) {
                    // Evitar delimitadores del formato interno de Partitions.
                    var pr = frag.Partitions.AddNew(guardaBreve(paso.ramas[j].guarda).replace(/;/g, " / "), "Partition");
                    pr.Note = paso.ramas[j].guarda.replace(/;/g, " / ");
                    var ajuste = paso.ramas.length === 1 ? -2 :
                        (j === 0 ? 5 : (j === paso.ramas.length - 1 ? -7 : 0));
                    if (j === paso.ramas.length - 1) ajuste += margenFinal;
                    pr.Size = String(altoPasos(paso.ramas[j].pasos) + ajuste);
                }
                // Partition no tiene Update(): persiste mediante Element.Update().
                comprobar(frag, "Guardar operandos " + paso.operador);
                posicionar(frag, 2 + profundidad * 18, y - 20, ancho - profundidad * 3, y - 20 + h);
                for (j = 0; j < paso.ramas.length; j++) dibujar(paso.ramas[j].pasos, profundidad + 1);
            }
        }
    }
    // EA reordena mensajes con su separación del modelo. Los fragmentos se
    // ajustan a esa separación, en lugar de confiar en coordenadas de mensajes.
    dibujar(spec.pasos, 0);
    destino.Elements.Refresh();
    d.DiagramObjects.Refresh(); d.DiagramLinks.Refresh();
    d.Notes = spec.nota + " Generación terminada. Fuente: código local de Vidia; consulte informe_validacion.md.";
    comprobar(d, "Guardar " + spec.nombre);
    repo.ReloadDiagram(d.DiagramID);
    Session.Output("CREADO: " + spec.nombre + "; participantes=" + spec.actores.length +
        "; mensajes=" + secuencia + ". Revisar activaciones y exportación en EA.");
    return d;
}

function buscarDestinoVidia(paquete, encontrados) {
    if (paquete.Name === VIDIA_PAQUETE || /^Anexo E(?:\b|\s|-)/i.test(paquete.Name))
        encontrados.push(paquete);
    for (var i = 0; i < paquete.Packages.Count; i++)
        buscarDestinoVidia(paquete.Packages.GetAt(i), encontrados);
}

function ejecutarVidia(repo, seleccionado) {
    if (!seleccionado) throw new Error("Seleccione el paquete de destino en el Browser de EA.");
    var candidatos = [];
    buscarDestinoVidia(seleccionado, candidatos);
    if (candidatos.length > 1)
        throw new Error("Hay varios paquetes Anexo E bajo la selección. Seleccione directamente uno de ellos.");
    var destino;
    if (candidatos.length === 1) destino = candidatos[0];
    else {
        destino = seleccionado.Packages.AddNew(VIDIA_PAQUETE, "Package");
        comprobar(destino, "Crear paquete de secuencias");
        seleccionado.Packages.Refresh();
    }
    var specs = especificacionesVidia();
    for (var i = 0; i < specs.length; i++) crearDiagramaVidia(repo, destino, specs[i]);
    repo.RefreshModelView(destino.PackageID);
    return destino;
}

function main() {
    try {
        if (typeof Repository === "undefined" || Repository.Models.Count === 0)
            throw new Error("Abra un modelo UML en Enterprise Architect antes de ejecutar el script.");
        Session.Output("Vidia E.12/E.13. API EA build: " + Repository.LibraryVersion);
        ejecutarVidia(Repository, Repository.GetTreeSelectedPackage());
    } catch (error) {
        Session.Output("ERROR: " + describirError(error));
        Session.Prompt("No se completó la generación:\n" + describirError(error) +
            "\nNo se borró ningún elemento. Si hay una figura parcial, conserve su diagnóstico y use otro paquete.", promptOK);
    }
}

main();
