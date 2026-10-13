!INC Local Scripts.EAConstants-JavaScript

/*
 * FIGURA 4 — Modelo versionado de Vidia, JPA y Flyway V1–V5.
 * Basado en el original: Class <<table>>, Attributes y Association.
 * Un único diagrama contiene las 19 tablas y 28 FK.
 * Diccionario completo en notas; atributos principales visibles.
 * Ajuste final de impresión y rutas en EA necesario.
 * No se ejecutó ni verificó visualmente en Enterprise Architect.
 *
 * Notas de tabla: diccionario completo, restricciones e índices declarados.
 * Atributos visibles: selección de claves y campos principales; no se eliminan
 * columnas del esquema. Notas aclaran auditoría y particularidades históricas.
 * Tipos PostgreSQL: varchar = character varying; timestamp(6) y time(6)
 * equivalen a WITHOUT TIME ZONE, NO a timestamptz. jsonb permanece jsonb.
 * V3 usa FK NOT VALID y reconciliación histórica: multiplicidades describen
 * modelo canónico limpio/JPA, no garantizan validación de toda fila histórica.
 * Sin acceso a DB/producción, SQL directo, borrados ni APIs de Node.js.
 *
 * EA: ConnectorEnd.Cardinality se persiste mediante Update del extremo.
 * Referencias de API: documentación Sparx ConnectorEnd, DiagramObject,
 * DiagramLink y Collection. Sequence alto coloca fondos detrás.
 * No se altera el paquete/diagrama original; se crea un subpaquete propio.
 */

function main()
{
    var MARCA = "[VIDIA-MODELO-DATOS-ACTUALIZADO-v1]";
    var NOMBRE = "Vidia - Modelo de datos actualizado";
    var seleccionado = Repository.GetTreeSelectedPackage();
    if (seleccionado == null)
    {
        Session.Prompt("Seleccione primero el paquete de destino.", promptOK);
        return;
    }
    function guardar(objeto, contexto)
    {
        if (!objeto.Update()) throw new Error(contexto + ": " + objeto.GetLastError());
    }
    function propio(objeto) { return String(objeto.Notes).indexOf(MARCA) == 0; }
    try
    {
        var destino = null;
        if (seleccionado.Name == NOMBRE && propio(seleccionado)) destino = seleccionado;
        else
        {
            seleccionado.Packages.Refresh();
            for (var i = 0; i < seleccionado.Packages.Count; i++)
            {
                var p = seleccionado.Packages.GetAt(i);
                if (p.Name == NOMBRE)
                {
                    if (!propio(p)) throw new Error("Paquete homónimo ajeno; no se modificó.");
                    destino = p; break;
                }
            }
            if (destino == null)
            {
                destino = seleccionado.Packages.AddNew(NOMBRE, "");
                destino.Notes = MARCA + "\r\nModelo propio, separado del original.";
                guardar(destino, "Crear paquete");
                seleccionado.Packages.Refresh();
            }
        }
        var TABLAS = [
    {
        "name": "asistencias",
        "columns": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "actualizado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "creado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_actualizacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_creacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_eliminacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "distancia_metros",
                "type": "numeric(10, 2)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_hora_registro",
                "type": "timestamp(6) without time zone",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "observacion",
                "type": "text",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "precision_gps_metros",
                "type": "numeric(10, 2)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "resultado_validacion",
                "type": "character varying(20)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "inscripcion_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "registrado_por_id",
                "type": "uuid",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "sesion_evento_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            }
        ],
        "constraints": [
            "CONSTRAINT asistencias_pkey PRIMARY KEY (id)",
            "CONSTRAINT uk_asistencia_inscripcion_sesion UNIQUE (inscripcion_id, sesion_evento_id)"
        ],
        "pk": [
            "id"
        ],
        "unique": [
            [
                "inscripcion_id",
                "sesion_evento_id"
            ]
        ],
        "source": "V3",
        "visible": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "inscripcion_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "sesion_evento_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "registrado_por_id",
                "type": "uuid",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "distancia_metros",
                "type": "numeric(10, 2)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "precision_gps_metros",
                "type": "numeric(10, 2)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            }
        ],
        "notes": "Fuente: V3 y entidad JPA correspondiente.\r\nUNIQUE(inscripcion_id, sesion_evento_id). registrado_por_id es nullable. Usuario participante se identifica también mediante inscripción. No FK directa a eventos ni qr_asistencia.\r\nBorrado lógico JPA con fecha_eliminacion y filtro IS NULL.\r\nÍndices JPA declarados (existencia desplegada no verificada): [('idx_asistencia_sesion', 'sesion_evento_id')]\r\nPK: id; UNIQUE: [['inscripcion_id', 'sesion_evento_id']]\r\nDiccionario físico completo (auditoría omitida visualmente, no del modelo):\r\nid uuid NOT NULL {PK}\r\nactualizado_por character varying(255) NULL\r\ncreado_por character varying(255) NULL\r\nfecha_actualizacion timestamp(6) without time zone NULL\r\nfecha_creacion timestamp(6) without time zone NULL\r\nfecha_eliminacion timestamp(6) without time zone NULL\r\ndistancia_metros numeric(10, 2) NULL\r\nfecha_hora_registro timestamp(6) without time zone NOT NULL\r\nobservacion text NULL\r\nprecision_gps_metros numeric(10, 2) NULL\r\nresultado_validacion character varying(20) NOT NULL\r\ninscripcion_id uuid NOT NULL {FK}\r\nregistrado_por_id uuid NULL {FK}\r\nsesion_evento_id uuid NOT NULL {FK}\r\nRestricciones: CONSTRAINT asistencias_pkey PRIMARY KEY (id); CONSTRAINT uk_asistencia_inscripcion_sesion UNIQUE (inscripcion_id, sesion_evento_id)"
    },
    {
        "name": "categorias_evento",
        "columns": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "actualizado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "creado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_actualizacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_creacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_eliminacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "descripcion",
                "type": "text",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "estado",
                "type": "character varying(255)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "nombre",
                "type": "character varying(100)",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": false
            },
            {
                "name": "nombre_normalizado",
                "type": "character varying(100)",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": false
            }
        ],
        "constraints": [
            "CONSTRAINT categorias_evento_pkey PRIMARY KEY (id)",
            "CONSTRAINT uk9dcouuesv8ant9k1jaiaihdu4 UNIQUE (nombre)",
            "CONSTRAINT ukobteg7chcc396i7hljq0xx152 UNIQUE (nombre_normalizado)"
        ],
        "pk": [
            "id"
        ],
        "unique": [
            [
                "nombre"
            ],
            [
                "nombre_normalizado"
            ]
        ],
        "source": "V3",
        "visible": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "nombre",
                "type": "character varying(100)",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": false
            },
            {
                "name": "nombre_normalizado",
                "type": "character varying(100)",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": false
            },
            {
                "name": "estado",
                "type": "character varying(255)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            }
        ],
        "notes": "Fuente: V3 y entidad JPA correspondiente.\r\nV2 valida nombre_normalizado=lower(trim(nombre)) y duplicados; UNIQUE físico para nombre y nombre_normalizado.\r\nBorrado lógico JPA con fecha_eliminacion y filtro IS NULL.\r\nÍndices JPA declarados (existencia desplegada no verificada): []\r\nPK: id; UNIQUE: [['nombre'], ['nombre_normalizado']]\r\nDiccionario físico completo (auditoría omitida visualmente, no del modelo):\r\nid uuid NOT NULL {PK}\r\nactualizado_por character varying(255) NULL\r\ncreado_por character varying(255) NULL\r\nfecha_actualizacion timestamp(6) without time zone NULL\r\nfecha_creacion timestamp(6) without time zone NULL\r\nfecha_eliminacion timestamp(6) without time zone NULL\r\ndescripcion text NULL\r\nestado character varying(255) NOT NULL\r\nnombre character varying(100) NOT NULL {UNIQUE}\r\nnombre_normalizado character varying(100) NOT NULL {UNIQUE}\r\nRestricciones: CONSTRAINT categorias_evento_pkey PRIMARY KEY (id); CONSTRAINT uk9dcouuesv8ant9k1jaiaihdu4 UNIQUE (nombre); CONSTRAINT ukobteg7chcc396i7hljq0xx152 UNIQUE (nombre_normalizado)"
    },
    {
        "name": "certificados",
        "columns": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "actualizado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "creado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_actualizacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_creacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_eliminacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "archivo_pdf_url",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "codigo_certificado",
                "type": "character varying(50)",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": false
            },
            {
                "name": "estado",
                "type": "character varying(20)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_emision",
                "type": "timestamp(6) without time zone",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "horas_academicas",
                "type": "integer",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "porcentaje_asistencia",
                "type": "numeric(5, 2)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "tipo_certificado",
                "type": "character varying(20)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "url_verificacion",
                "type": "character varying(255)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "evento_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "inscripcion_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": true
            },
            {
                "name": "usuario_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            }
        ],
        "constraints": [
            "CONSTRAINT certificados_pkey PRIMARY KEY (id)",
            "CONSTRAINT uk_certificado_codigo UNIQUE (codigo_certificado)",
            "CONSTRAINT uk_certificado_inscripcion UNIQUE (inscripcion_id)"
        ],
        "pk": [
            "id"
        ],
        "unique": [
            [
                "codigo_certificado"
            ],
            [
                "inscripcion_id"
            ]
        ],
        "source": "V3",
        "visible": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "inscripcion_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": true
            },
            {
                "name": "usuario_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "evento_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "codigo_certificado",
                "type": "character varying(50)",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": false
            },
            {
                "name": "porcentaje_asistencia",
                "type": "numeric(5, 2)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            }
        ],
        "notes": "Fuente: V3 y entidad JPA correspondiente.\r\nUNIQUE(inscripcion_id) y UNIQUE(codigo_certificado). usuario_id y evento_id son FK reales adicionales. archivo_pdf_url es referencia nullable; PDF generado al descargar, no BLOB.\r\nBorrado lógico JPA con fecha_eliminacion y filtro IS NULL.\r\nÍndices JPA declarados (existencia desplegada no verificada): [('idx_certificado_usuario', 'usuario_id'), ('idx_certificado_evento', 'evento_id')]\r\nPK: id; UNIQUE: [['codigo_certificado'], ['inscripcion_id']]\r\nDiccionario físico completo (auditoría omitida visualmente, no del modelo):\r\nid uuid NOT NULL {PK}\r\nactualizado_por character varying(255) NULL\r\ncreado_por character varying(255) NULL\r\nfecha_actualizacion timestamp(6) without time zone NULL\r\nfecha_creacion timestamp(6) without time zone NULL\r\nfecha_eliminacion timestamp(6) without time zone NULL\r\narchivo_pdf_url character varying(255) NULL\r\ncodigo_certificado character varying(50) NOT NULL {UNIQUE}\r\nestado character varying(20) NOT NULL\r\nfecha_emision timestamp(6) without time zone NOT NULL\r\nhoras_academicas integer NULL\r\nporcentaje_asistencia numeric(5, 2) NOT NULL\r\ntipo_certificado character varying(20) NOT NULL\r\nurl_verificacion character varying(255) NOT NULL\r\nevento_id uuid NOT NULL {FK}\r\ninscripcion_id uuid NOT NULL {FK} {UNIQUE}\r\nusuario_id uuid NOT NULL {FK}\r\nRestricciones: CONSTRAINT certificados_pkey PRIMARY KEY (id); CONSTRAINT uk_certificado_codigo UNIQUE (codigo_certificado); CONSTRAINT uk_certificado_inscripcion UNIQUE (inscripcion_id)"
    },
    {
        "name": "eventos",
        "columns": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "actualizado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "creado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_actualizacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_creacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_eliminacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "costo",
                "type": "numeric(12, 2)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "cupo_disponible",
                "type": "integer",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "cupo_limitado",
                "type": "boolean",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "cupo_maximo",
                "type": "integer",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "descripcion",
                "type": "text",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "direccion",
                "type": "character varying(500)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "email_contacto",
                "type": "character varying(100)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "emite_certificado",
                "type": "boolean",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "enlace_virtual",
                "type": "character varying(500)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "estado",
                "type": "character varying(30)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_envio_revision",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_fin",
                "type": "date",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_inicio",
                "type": "date",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_resolucion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "hora_fin",
                "type": "time(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "hora_inicio",
                "type": "time(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "horas_academicas",
                "type": "integer",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "imagen_portada",
                "type": "character varying(500)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "instrucciones_pago",
                "type": "text",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "latitud",
                "type": "numeric(10, 7)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "longitud",
                "type": "numeric(10, 7)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "modalidad",
                "type": "character varying(20)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "motivo_cancelacion",
                "type": "character varying(1000)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "motivo_rechazo",
                "type": "character varying(1000)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "objetivos",
                "type": "text",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "publico_objetivo",
                "type": "character varying(20)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "qr_pago_url",
                "type": "character varying(500)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "radio_metros",
                "type": "integer",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "requiere_inscripcion",
                "type": "boolean",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "telefono_contacto",
                "type": "character varying(20)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "tipo_certificado",
                "type": "character varying(20)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "tipo_inscripcion",
                "type": "character varying(20)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "titulo",
                "type": "character varying(200)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "ubicacion",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "whatsapp_contacto",
                "type": "character varying(20)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "categoria_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "organizador_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "resuelto_por_id",
                "type": "uuid",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "qr_pago_storage_key",
                "type": "character varying(500)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            }
        ],
        "constraints": [
            "CONSTRAINT eventos_pkey PRIMARY KEY (id)"
        ],
        "pk": [
            "id"
        ],
        "unique": [],
        "source": "V3",
        "visible": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "categoria_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "organizador_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "resuelto_por_id",
                "type": "uuid",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "titulo",
                "type": "character varying(200)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "estado",
                "type": "character varying(30)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "qr_pago_storage_key",
                "type": "character varying(500)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            }
        ],
        "notes": "Fuente: V3 y entidad JPA correspondiente.\r\nqr_pago_storage_key varchar(500): clave Supabase externa. qr_pago_url e imagen_portada son referencias, no bytes. Organizador obligatorio, resolutor opcional. JPA: sesiones cascade ALL/orphanRemoval; no equivale a ON DELETE CASCADE PostgreSQL.\r\nBorrado lógico JPA con fecha_eliminacion y filtro IS NULL.\r\nÍndices JPA declarados (existencia desplegada no verificada): []\r\nPK: id; UNIQUE: []\r\nDiccionario físico completo (auditoría omitida visualmente, no del modelo):\r\nid uuid NOT NULL {PK}\r\nactualizado_por character varying(255) NULL\r\ncreado_por character varying(255) NULL\r\nfecha_actualizacion timestamp(6) without time zone NULL\r\nfecha_creacion timestamp(6) without time zone NULL\r\nfecha_eliminacion timestamp(6) without time zone NULL\r\ncosto numeric(12, 2) NOT NULL\r\ncupo_disponible integer NULL\r\ncupo_limitado boolean NOT NULL\r\ncupo_maximo integer NULL\r\ndescripcion text NULL\r\ndireccion character varying(500) NULL\r\nemail_contacto character varying(100) NULL\r\nemite_certificado boolean NOT NULL\r\nenlace_virtual character varying(500) NULL\r\nestado character varying(30) NOT NULL\r\nfecha_envio_revision timestamp(6) without time zone NULL\r\nfecha_fin date NOT NULL\r\nfecha_inicio date NOT NULL\r\nfecha_resolucion timestamp(6) without time zone NULL\r\nhora_fin time(6) without time zone NULL\r\nhora_inicio time(6) without time zone NULL\r\nhoras_academicas integer NULL\r\nimagen_portada character varying(500) NULL\r\ninstrucciones_pago text NULL\r\nlatitud numeric(10, 7) NULL\r\nlongitud numeric(10, 7) NULL\r\nmodalidad character varying(20) NOT NULL\r\nmotivo_cancelacion character varying(1000) NULL\r\nmotivo_rechazo character varying(1000) NULL\r\nobjetivos text NULL\r\npublico_objetivo character varying(20) NOT NULL\r\nqr_pago_url character varying(500) NULL\r\nradio_metros integer NULL\r\nrequiere_inscripcion boolean NOT NULL\r\ntelefono_contacto character varying(20) NULL\r\ntipo_certificado character varying(20) NULL\r\ntipo_inscripcion character varying(20) NOT NULL\r\ntitulo character varying(200) NOT NULL\r\nubicacion character varying(255) NULL\r\nwhatsapp_contacto character varying(20) NULL\r\ncategoria_id uuid NOT NULL {FK}\r\norganizador_id uuid NOT NULL {FK}\r\nresuelto_por_id uuid NULL {FK}\r\nqr_pago_storage_key character varying(500) NULL\r\nRestricciones: CONSTRAINT eventos_pkey PRIMARY KEY (id)"
    },
    {
        "name": "inscripciones",
        "columns": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "actualizado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "creado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_actualizacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_creacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_eliminacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "codigo_participante",
                "type": "character varying(30)",
                "nn": false,
                "pk": false,
                "uq": true,
                "fk": false
            },
            {
                "name": "estado",
                "type": "character varying(30)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_inscripcion",
                "type": "timestamp(6) without time zone",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "observacion",
                "type": "text",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "evento_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "usuario_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            }
        ],
        "constraints": [
            "CONSTRAINT inscripciones_pkey PRIMARY KEY (id)",
            "CONSTRAINT uk_inscripcion_usuario_evento UNIQUE (usuario_id, evento_id)",
            "CONSTRAINT ukmey22k67agj6dplhkmixk9uf3 UNIQUE (codigo_participante)"
        ],
        "pk": [
            "id"
        ],
        "unique": [
            [
                "usuario_id",
                "evento_id"
            ],
            [
                "codigo_participante"
            ]
        ],
        "source": "V3",
        "visible": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "usuario_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "evento_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "estado",
                "type": "character varying(30)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "codigo_participante",
                "type": "character varying(30)",
                "nn": false,
                "pk": false,
                "uq": true,
                "fk": false
            }
        ],
        "notes": "Fuente: V3 y entidad JPA correspondiente.\r\n\r\nBorrado lógico JPA con fecha_eliminacion y filtro IS NULL.\r\nÍndices JPA declarados (existencia desplegada no verificada): [('idx_inscripcion_usuario', 'usuario_id'), ('idx_inscripcion_evento', 'evento_id'), ('idx_inscripcion_estado', 'estado')]\r\nPK: id; UNIQUE: [['usuario_id', 'evento_id'], ['codigo_participante']]\r\nDiccionario físico completo (auditoría omitida visualmente, no del modelo):\r\nid uuid NOT NULL {PK}\r\nactualizado_por character varying(255) NULL\r\ncreado_por character varying(255) NULL\r\nfecha_actualizacion timestamp(6) without time zone NULL\r\nfecha_creacion timestamp(6) without time zone NULL\r\nfecha_eliminacion timestamp(6) without time zone NULL\r\ncodigo_participante character varying(30) NULL {UNIQUE}\r\nestado character varying(30) NOT NULL\r\nfecha_inscripcion timestamp(6) without time zone NOT NULL\r\nobservacion text NULL\r\nevento_id uuid NOT NULL {FK}\r\nusuario_id uuid NOT NULL {FK}\r\nRestricciones: CONSTRAINT inscripciones_pkey PRIMARY KEY (id); CONSTRAINT uk_inscripcion_usuario_evento UNIQUE (usuario_id, evento_id); CONSTRAINT ukmey22k67agj6dplhkmixk9uf3 UNIQUE (codigo_participante)"
    },
    {
        "name": "pagos",
        "columns": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "actualizado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "creado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_actualizacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_creacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_eliminacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "comprobante_nombre_archivo",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "comprobante_tipo_contenido",
                "type": "character varying(100)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "comprobante_url",
                "type": "character varying(500)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "estado",
                "type": "character varying(20)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_carga_comprobante",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_pago",
                "type": "timestamp(6) without time zone",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_resolucion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "intentos_comprobante",
                "type": "integer",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "monto",
                "type": "numeric(12, 2)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "motivo_rechazo",
                "type": "character varying(1000)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "observacion",
                "type": "text",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "inscripcion_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": true
            },
            {
                "name": "resuelto_por_id",
                "type": "uuid",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": true
            }
        ],
        "constraints": [
            "CONSTRAINT pagos_pkey PRIMARY KEY (id)",
            "CONSTRAINT ukcmeveyyrpuai5j5frvbigmkd4 UNIQUE (inscripcion_id)"
        ],
        "pk": [
            "id"
        ],
        "unique": [
            [
                "inscripcion_id"
            ]
        ],
        "source": "V3",
        "visible": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "inscripcion_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": true
            },
            {
                "name": "resuelto_por_id",
                "type": "uuid",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "monto",
                "type": "numeric(12, 2)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "estado",
                "type": "character varying(20)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "comprobante_url",
                "type": "character varying(500)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            }
        ],
        "notes": "Fuente: V3 y entidad JPA correspondiente.\r\nUNIQUE(inscripcion_id): máximo un pago por inscripción. No existe usuario_id del pagador en esta tabla: se obtiene mediante inscripciones.usuario_id. comprobante_url se utiliza como clave de objeto externo, no contenido binario PostgreSQL.\r\nBorrado lógico JPA con fecha_eliminacion y filtro IS NULL.\r\nÍndices JPA declarados (existencia desplegada no verificada): []\r\nPK: id; UNIQUE: [['inscripcion_id']]\r\nDiccionario físico completo (auditoría omitida visualmente, no del modelo):\r\nid uuid NOT NULL {PK}\r\nactualizado_por character varying(255) NULL\r\ncreado_por character varying(255) NULL\r\nfecha_actualizacion timestamp(6) without time zone NULL\r\nfecha_creacion timestamp(6) without time zone NULL\r\nfecha_eliminacion timestamp(6) without time zone NULL\r\ncomprobante_nombre_archivo character varying(255) NULL\r\ncomprobante_tipo_contenido character varying(100) NULL\r\ncomprobante_url character varying(500) NULL\r\nestado character varying(20) NOT NULL\r\nfecha_carga_comprobante timestamp(6) without time zone NULL\r\nfecha_pago timestamp(6) without time zone NOT NULL\r\nfecha_resolucion timestamp(6) without time zone NULL\r\nintentos_comprobante integer NOT NULL\r\nmonto numeric(12, 2) NOT NULL\r\nmotivo_rechazo character varying(1000) NULL\r\nobservacion text NULL\r\ninscripcion_id uuid NOT NULL {FK} {UNIQUE}\r\nresuelto_por_id uuid NULL {FK}\r\nRestricciones: CONSTRAINT pagos_pkey PRIMARY KEY (id); CONSTRAINT ukcmeveyyrpuai5j5frvbigmkd4 UNIQUE (inscripcion_id)"
    },
    {
        "name": "permiso",
        "columns": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "actualizado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "creado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_actualizacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_creacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_eliminacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "descripcion",
                "type": "character varying(100)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "nombre",
                "type": "character varying(50)",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": false
            }
        ],
        "constraints": [
            "CONSTRAINT permiso_pkey PRIMARY KEY (id)",
            "CONSTRAINT uknwe6lkk7x7sbw94xcmbwgvycu UNIQUE (nombre)"
        ],
        "pk": [
            "id"
        ],
        "unique": [
            [
                "nombre"
            ]
        ],
        "source": "V3",
        "visible": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "nombre",
                "type": "character varying(50)",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": false
            },
            {
                "name": "descripcion",
                "type": "character varying(100)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            }
        ],
        "notes": "Fuente: V3 y entidad JPA correspondiente.\r\n\r\nNo se declara @SQLDelete/@SQLRestriction en esta entidad; fecha_eliminacion heredada no demuestra borrado lógico automático.\r\nÍndices JPA declarados (existencia desplegada no verificada): []\r\nPK: id; UNIQUE: [['nombre']]\r\nDiccionario físico completo (auditoría omitida visualmente, no del modelo):\r\nid uuid NOT NULL {PK}\r\nactualizado_por character varying(255) NULL\r\ncreado_por character varying(255) NULL\r\nfecha_actualizacion timestamp(6) without time zone NULL\r\nfecha_creacion timestamp(6) without time zone NULL\r\nfecha_eliminacion timestamp(6) without time zone NULL\r\ndescripcion character varying(100) NULL\r\nnombre character varying(50) NOT NULL {UNIQUE}\r\nRestricciones: CONSTRAINT permiso_pkey PRIMARY KEY (id); CONSTRAINT uknwe6lkk7x7sbw94xcmbwgvycu UNIQUE (nombre)"
    },
    {
        "name": "qr_asistencia",
        "columns": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "actualizado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "creado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_actualizacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_creacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_eliminacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "activo",
                "type": "boolean",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "emitido_en",
                "type": "timestamp(6) without time zone",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "expira_en",
                "type": "timestamp(6) without time zone",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "revocado_en",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "token_hash",
                "type": "character varying(64)",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": false
            },
            {
                "name": "version",
                "type": "bigint",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "generado_por_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "sesion_evento_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            }
        ],
        "constraints": [
            "CONSTRAINT qr_asistencia_pkey PRIMARY KEY (id)",
            "CONSTRAINT uk6s2onaib0e7w9xlsb1s9hdlhc UNIQUE (token_hash)"
        ],
        "pk": [
            "id"
        ],
        "unique": [
            [
                "token_hash"
            ]
        ],
        "source": "V3",
        "visible": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "sesion_evento_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "generado_por_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "token_hash",
                "type": "character varying(64)",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": false
            },
            {
                "name": "expira_en",
                "type": "timestamp(6) without time zone",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "activo",
                "type": "boolean",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "version",
                "type": "bigint",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            }
        ],
        "notes": "Fuente: V3 y entidad JPA correspondiente.\r\nFK hacia sesiones_evento y usuario generado_por_id, ambas NOT NULL. token_hash único; version bigint para bloqueo optimista. No FK desde asistencias al QR. QR temporal compartido por varios participantes elegibles.\r\nNo se declara @SQLDelete/@SQLRestriction en esta entidad; fecha_eliminacion heredada no demuestra borrado lógico automático.\r\nÍndices JPA declarados (existencia desplegada no verificada): [('idx_qr_asistencia_sesion_activo', 'sesion_evento_id,activo')]\r\nPK: id; UNIQUE: [['token_hash']]\r\nDiccionario físico completo (auditoría omitida visualmente, no del modelo):\r\nid uuid NOT NULL {PK}\r\nactualizado_por character varying(255) NULL\r\ncreado_por character varying(255) NULL\r\nfecha_actualizacion timestamp(6) without time zone NULL\r\nfecha_creacion timestamp(6) without time zone NULL\r\nfecha_eliminacion timestamp(6) without time zone NULL\r\nactivo boolean NOT NULL\r\nemitido_en timestamp(6) without time zone NOT NULL\r\nexpira_en timestamp(6) without time zone NOT NULL\r\nrevocado_en timestamp(6) without time zone NULL\r\ntoken_hash character varying(64) NOT NULL {UNIQUE}\r\nversion bigint NULL\r\ngenerado_por_id uuid NOT NULL {FK}\r\nsesion_evento_id uuid NOT NULL {FK}\r\nRestricciones: CONSTRAINT qr_asistencia_pkey PRIMARY KEY (id); CONSTRAINT uk6s2onaib0e7w9xlsb1s9hdlhc UNIQUE (token_hash)"
    },
    {
        "name": "rol",
        "columns": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "actualizado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "creado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_actualizacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_creacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_eliminacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "descripcion",
                "type": "character varying(100)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "nombre",
                "type": "character varying(50)",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": false
            }
        ],
        "constraints": [
            "CONSTRAINT rol_pkey PRIMARY KEY (id)",
            "CONSTRAINT uk43kr6s7bts1wqfv43f7jd87kp UNIQUE (nombre)"
        ],
        "pk": [
            "id"
        ],
        "unique": [
            [
                "nombre"
            ]
        ],
        "source": "V3",
        "visible": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "nombre",
                "type": "character varying(50)",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": false
            },
            {
                "name": "descripcion",
                "type": "character varying(100)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            }
        ],
        "notes": "Fuente: V3 y entidad JPA correspondiente.\r\n\r\nNo se declara @SQLDelete/@SQLRestriction en esta entidad; fecha_eliminacion heredada no demuestra borrado lógico automático.\r\nÍndices JPA declarados (existencia desplegada no verificada): []\r\nPK: id; UNIQUE: [['nombre']]\r\nDiccionario físico completo (auditoría omitida visualmente, no del modelo):\r\nid uuid NOT NULL {PK}\r\nactualizado_por character varying(255) NULL\r\ncreado_por character varying(255) NULL\r\nfecha_actualizacion timestamp(6) without time zone NULL\r\nfecha_creacion timestamp(6) without time zone NULL\r\nfecha_eliminacion timestamp(6) without time zone NULL\r\ndescripcion character varying(100) NULL\r\nnombre character varying(50) NOT NULL {UNIQUE}\r\nRestricciones: CONSTRAINT rol_pkey PRIMARY KEY (id); CONSTRAINT uk43kr6s7bts1wqfv43f7jd87kp UNIQUE (nombre)"
    },
    {
        "name": "rol_permiso",
        "columns": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "actualizado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "creado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_actualizacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_creacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_eliminacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "permiso_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "rol_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            }
        ],
        "constraints": [
            "CONSTRAINT rol_permiso_pkey PRIMARY KEY (id)"
        ],
        "pk": [
            "id"
        ],
        "unique": [],
        "source": "V3",
        "visible": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "rol_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "permiso_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            }
        ],
        "notes": "Fuente: V3 y entidad JPA correspondiente.\r\nNo se declaró UNIQUE(rol_id, permiso_id) en V3/JPA.\r\nNo se declara @SQLDelete/@SQLRestriction en esta entidad; fecha_eliminacion heredada no demuestra borrado lógico automático.\r\nÍndices JPA declarados (existencia desplegada no verificada): []\r\nPK: id; UNIQUE: []\r\nDiccionario físico completo (auditoría omitida visualmente, no del modelo):\r\nid uuid NOT NULL {PK}\r\nactualizado_por character varying(255) NULL\r\ncreado_por character varying(255) NULL\r\nfecha_actualizacion timestamp(6) without time zone NULL\r\nfecha_creacion timestamp(6) without time zone NULL\r\nfecha_eliminacion timestamp(6) without time zone NULL\r\npermiso_id uuid NOT NULL {FK}\r\nrol_id uuid NOT NULL {FK}\r\nRestricciones: CONSTRAINT rol_permiso_pkey PRIMARY KEY (id)"
    },
    {
        "name": "sesion_usuario",
        "columns": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "actualizado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "creado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_actualizacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_creacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_eliminacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_expiracion",
                "type": "timestamp(6) without time zone",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_revocacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "usuario_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            }
        ],
        "constraints": [
            "CONSTRAINT sesion_usuario_pkey PRIMARY KEY (id)"
        ],
        "pk": [
            "id"
        ],
        "unique": [],
        "source": "V3",
        "visible": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "usuario_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "fecha_expiracion",
                "type": "timestamp(6) without time zone",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_revocacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            }
        ],
        "notes": "Fuente: V3 y entidad JPA correspondiente.\r\nSesiones revocables vinculadas al usuario; fecha_revocacion nullable. No FK directa desde JWT.\r\nNo se declara @SQLDelete/@SQLRestriction en esta entidad; fecha_eliminacion heredada no demuestra borrado lógico automático.\r\nÍndices JPA declarados (existencia desplegada no verificada): []\r\nPK: id; UNIQUE: []\r\nDiccionario físico completo (auditoría omitida visualmente, no del modelo):\r\nid uuid NOT NULL {PK}\r\nactualizado_por character varying(255) NULL\r\ncreado_por character varying(255) NULL\r\nfecha_actualizacion timestamp(6) without time zone NULL\r\nfecha_creacion timestamp(6) without time zone NULL\r\nfecha_eliminacion timestamp(6) without time zone NULL\r\nfecha_expiracion timestamp(6) without time zone NOT NULL\r\nfecha_revocacion timestamp(6) without time zone NULL\r\nusuario_id uuid NOT NULL {FK}\r\nRestricciones: CONSTRAINT sesion_usuario_pkey PRIMARY KEY (id)"
    },
    {
        "name": "sesiones_evento",
        "columns": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "actualizado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "creado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_actualizacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_creacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_eliminacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "activa",
                "type": "boolean",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "descripcion",
                "type": "text",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha",
                "type": "date",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "historica",
                "type": "boolean",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "hora_fin",
                "type": "time(6) without time zone",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "hora_inicio",
                "type": "time(6) without time zone",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "latitud",
                "type": "numeric(10, 7)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "longitud",
                "type": "numeric(10, 7)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "nombre",
                "type": "character varying(150)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "radio_metros",
                "type": "integer",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "requiere_asistencia",
                "type": "boolean",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "evento_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            }
        ],
        "constraints": [
            "CONSTRAINT sesiones_evento_pkey PRIMARY KEY (id)"
        ],
        "pk": [
            "id"
        ],
        "unique": [],
        "source": "V3",
        "visible": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "evento_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "fecha",
                "type": "date",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "hora_inicio",
                "type": "time(6) without time zone",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "hora_fin",
                "type": "time(6) without time zone",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "activa",
                "type": "boolean",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "radio_metros",
                "type": "integer",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            }
        ],
        "notes": "Fuente: V3 y entidad JPA correspondiente.\r\n\r\nBorrado lógico JPA con fecha_eliminacion y filtro IS NULL.\r\nÍndices JPA declarados (existencia desplegada no verificada): [('idx_sesion_evento_evento_fecha', 'evento_id,fecha,hora_inicio')]\r\nPK: id; UNIQUE: []\r\nDiccionario físico completo (auditoría omitida visualmente, no del modelo):\r\nid uuid NOT NULL {PK}\r\nactualizado_por character varying(255) NULL\r\ncreado_por character varying(255) NULL\r\nfecha_actualizacion timestamp(6) without time zone NULL\r\nfecha_creacion timestamp(6) without time zone NULL\r\nfecha_eliminacion timestamp(6) without time zone NULL\r\nactiva boolean NOT NULL\r\ndescripcion text NULL\r\nfecha date NOT NULL\r\nhistorica boolean NOT NULL\r\nhora_fin time(6) without time zone NOT NULL\r\nhora_inicio time(6) without time zone NOT NULL\r\nlatitud numeric(10, 7) NULL\r\nlongitud numeric(10, 7) NULL\r\nnombre character varying(150) NOT NULL\r\nradio_metros integer NULL\r\nrequiere_asistencia boolean NOT NULL\r\nevento_id uuid NOT NULL {FK}\r\nRestricciones: CONSTRAINT sesiones_evento_pkey PRIMARY KEY (id)"
    },
    {
        "name": "token_recuperacion",
        "columns": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "actualizado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "creado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_actualizacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_creacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_eliminacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_expiracion",
                "type": "timestamp(6) without time zone",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "token",
                "type": "character varying(255)",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": false
            },
            {
                "name": "utilizado",
                "type": "boolean",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "usuario_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            }
        ],
        "constraints": [
            "CONSTRAINT token_recuperacion_pkey PRIMARY KEY (id)",
            "CONSTRAINT uk33lrkaqshli5r4svsvil7yfc0 UNIQUE (token)"
        ],
        "pk": [
            "id"
        ],
        "unique": [
            [
                "token"
            ]
        ],
        "source": "V3",
        "visible": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "usuario_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "token",
                "type": "character varying(255)",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": false
            },
            {
                "name": "fecha_expiracion",
                "type": "timestamp(6) without time zone",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "utilizado",
                "type": "boolean",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            }
        ],
        "notes": "Fuente: V3 y entidad JPA correspondiente.\r\nColumna física token varchar(255) UNIQUE; AutenticacionServicio almacena hash, no renombrar la columna a token_hash.\r\nNo se declara @SQLDelete/@SQLRestriction en esta entidad; fecha_eliminacion heredada no demuestra borrado lógico automático.\r\nÍndices JPA declarados (existencia desplegada no verificada): []\r\nPK: id; UNIQUE: [['token']]\r\nDiccionario físico completo (auditoría omitida visualmente, no del modelo):\r\nid uuid NOT NULL {PK}\r\nactualizado_por character varying(255) NULL\r\ncreado_por character varying(255) NULL\r\nfecha_actualizacion timestamp(6) without time zone NULL\r\nfecha_creacion timestamp(6) without time zone NULL\r\nfecha_eliminacion timestamp(6) without time zone NULL\r\nfecha_expiracion timestamp(6) without time zone NOT NULL\r\ntoken character varying(255) NOT NULL {UNIQUE}\r\nutilizado boolean NOT NULL\r\nusuario_id uuid NOT NULL {FK}\r\nRestricciones: CONSTRAINT token_recuperacion_pkey PRIMARY KEY (id); CONSTRAINT uk33lrkaqshli5r4svsvil7yfc0 UNIQUE (token)"
    },
    {
        "name": "token_verificacion_correo",
        "columns": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "actualizado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "creado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_actualizacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_creacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_eliminacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_expiracion",
                "type": "timestamp(6) without time zone",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "token_hash",
                "type": "character varying(64)",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": false
            },
            {
                "name": "utilizado",
                "type": "boolean",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "usuario_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            }
        ],
        "constraints": [
            "CONSTRAINT token_verificacion_correo_pkey PRIMARY KEY (id)",
            "CONSTRAINT uka184aisgc1eaapsoky0n6vdu3 UNIQUE (token_hash)"
        ],
        "pk": [
            "id"
        ],
        "unique": [
            [
                "token_hash"
            ]
        ],
        "source": "V3",
        "visible": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "usuario_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "token_hash",
                "type": "character varying(64)",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": false
            },
            {
                "name": "fecha_expiracion",
                "type": "timestamp(6) without time zone",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "utilizado",
                "type": "boolean",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            }
        ],
        "notes": "Fuente: V3 y entidad JPA correspondiente.\r\n\r\nNo se declara @SQLDelete/@SQLRestriction en esta entidad; fecha_eliminacion heredada no demuestra borrado lógico automático.\r\nÍndices JPA declarados (existencia desplegada no verificada): []\r\nPK: id; UNIQUE: [['token_hash']]\r\nDiccionario físico completo (auditoría omitida visualmente, no del modelo):\r\nid uuid NOT NULL {PK}\r\nactualizado_por character varying(255) NULL\r\ncreado_por character varying(255) NULL\r\nfecha_actualizacion timestamp(6) without time zone NULL\r\nfecha_creacion timestamp(6) without time zone NULL\r\nfecha_eliminacion timestamp(6) without time zone NULL\r\nfecha_expiracion timestamp(6) without time zone NOT NULL\r\ntoken_hash character varying(64) NOT NULL {UNIQUE}\r\nutilizado boolean NOT NULL\r\nusuario_id uuid NOT NULL {FK}\r\nRestricciones: CONSTRAINT token_verificacion_correo_pkey PRIMARY KEY (id); CONSTRAINT uka184aisgc1eaapsoky0n6vdu3 UNIQUE (token_hash)"
    },
    {
        "name": "usuario",
        "columns": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "actualizado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "creado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_actualizacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_creacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_eliminacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "apellidos",
                "type": "character varying(50)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "celular",
                "type": "character varying(20)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "ci",
                "type": "character varying(20)",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": false
            },
            {
                "name": "contrasena",
                "type": "character varying(255)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "correo_electronico",
                "type": "character varying(100)",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": false
            },
            {
                "name": "correo_verificado",
                "type": "boolean",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "estado_solicitud_organizador",
                "type": "character varying(20)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_carga_fotografia",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_resolucion_organizador",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_solicitud_organizador",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fotografia_url",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "motivo_rechazo_organizador",
                "type": "character varying(500)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "nombre_archivo_fotografia",
                "type": "character varying(100)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "nombres",
                "type": "character varying(50)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "ru",
                "type": "character varying(20)",
                "nn": false,
                "pk": false,
                "uq": true,
                "fk": false
            },
            {
                "name": "tipo_usuario",
                "type": "character varying(20)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "solicitud_resuelta_por_id",
                "type": "uuid",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "activo",
                "type": "boolean",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "motivo_solicitud_organizador",
                "type": "varchar(1000)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "informacion_adicional_organizador",
                "type": "varchar(1000)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            }
        ],
        "constraints": [
            "CONSTRAINT usuario_pkey PRIMARY KEY (id)",
            "CONSTRAINT ukdf45etfnvb4mcw8j04h8h93vd UNIQUE (ci)",
            "CONSTRAINT ukf7w2jekriedf7k6a4kaclt9t7 UNIQUE (correo_electronico)",
            "CONSTRAINT ukq8gbcdu6kstk7m1fs1cqxc0od UNIQUE (ru)"
        ],
        "pk": [
            "id"
        ],
        "unique": [
            [
                "ci"
            ],
            [
                "correo_electronico"
            ],
            [
                "ru"
            ]
        ],
        "source": "V3",
        "visible": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "correo_electronico",
                "type": "character varying(100)",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": false
            },
            {
                "name": "ci",
                "type": "character varying(20)",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": false
            },
            {
                "name": "ru",
                "type": "character varying(20)",
                "nn": false,
                "pk": false,
                "uq": true,
                "fk": false
            },
            {
                "name": "activo",
                "type": "boolean",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "correo_verificado",
                "type": "boolean",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "solicitud_resuelta_por_id",
                "type": "uuid",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "estado_solicitud_organizador",
                "type": "character varying(20)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            }
        ],
        "notes": "Fuente: V3 y entidad JPA correspondiente.\r\nV4: activo NOT NULL DEFAULT true. V5: motivo_solicitud_organizador e informacion_adicional_organizador varchar(1000), opcionales. tipo_usuario no sustituye roles. Autorreferencia solicitud_resuelta_por_id opcional. Nombres/apellidos y campos de fotografía constan en el diccionario completo.\r\nNo se declara @SQLDelete/@SQLRestriction en esta entidad; fecha_eliminacion heredada no demuestra borrado lógico automático.\r\nÍndices JPA declarados (existencia desplegada no verificada): []\r\nPK: id; UNIQUE: [['ci'], ['correo_electronico'], ['ru']]\r\nDiccionario físico completo (auditoría omitida visualmente, no del modelo):\r\nid uuid NOT NULL {PK}\r\nactualizado_por character varying(255) NULL\r\ncreado_por character varying(255) NULL\r\nfecha_actualizacion timestamp(6) without time zone NULL\r\nfecha_creacion timestamp(6) without time zone NULL\r\nfecha_eliminacion timestamp(6) without time zone NULL\r\napellidos character varying(50) NOT NULL\r\ncelular character varying(20) NULL\r\nci character varying(20) NOT NULL {UNIQUE}\r\ncontrasena character varying(255) NOT NULL\r\ncorreo_electronico character varying(100) NOT NULL {UNIQUE}\r\ncorreo_verificado boolean NOT NULL\r\nestado_solicitud_organizador character varying(20) NOT NULL\r\nfecha_carga_fotografia timestamp(6) without time zone NULL\r\nfecha_resolucion_organizador timestamp(6) without time zone NULL\r\nfecha_solicitud_organizador timestamp(6) without time zone NULL\r\nfotografia_url character varying(255) NULL\r\nmotivo_rechazo_organizador character varying(500) NULL\r\nnombre_archivo_fotografia character varying(100) NULL\r\nnombres character varying(50) NOT NULL\r\nru character varying(20) NULL {UNIQUE}\r\ntipo_usuario character varying(20) NOT NULL\r\nsolicitud_resuelta_por_id uuid NULL {FK}\r\nactivo boolean NOT NULL\r\nmotivo_solicitud_organizador varchar(1000) NULL\r\ninformacion_adicional_organizador varchar(1000) NULL\r\nRestricciones: CONSTRAINT usuario_pkey PRIMARY KEY (id); CONSTRAINT ukdf45etfnvb4mcw8j04h8h93vd UNIQUE (ci); CONSTRAINT ukf7w2jekriedf7k6a4kaclt9t7 UNIQUE (correo_electronico); CONSTRAINT ukq8gbcdu6kstk7m1fs1cqxc0od UNIQUE (ru)"
    },
    {
        "name": "usuario_rol",
        "columns": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "actualizado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "creado_por",
                "type": "character varying(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_actualizacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_creacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_eliminacion",
                "type": "timestamp(6) without time zone",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "rol_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "usuario_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            }
        ],
        "constraints": [
            "CONSTRAINT usuario_rol_pkey PRIMARY KEY (id)"
        ],
        "pk": [
            "id"
        ],
        "unique": [],
        "source": "V3",
        "visible": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "usuario_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "rol_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            }
        ],
        "notes": "Fuente: V3 y entidad JPA correspondiente.\r\nNo se declaró UNIQUE(usuario_id, rol_id) en V3/JPA. No inventar esa restricción ni una jerarquía de roles.\r\nNo se declara @SQLDelete/@SQLRestriction en esta entidad; fecha_eliminacion heredada no demuestra borrado lógico automático.\r\nÍndices JPA declarados (existencia desplegada no verificada): []\r\nPK: id; UNIQUE: []\r\nDiccionario físico completo (auditoría omitida visualmente, no del modelo):\r\nid uuid NOT NULL {PK}\r\nactualizado_por character varying(255) NULL\r\ncreado_por character varying(255) NULL\r\nfecha_actualizacion timestamp(6) without time zone NULL\r\nfecha_creacion timestamp(6) without time zone NULL\r\nfecha_eliminacion timestamp(6) without time zone NULL\r\nrol_id uuid NOT NULL {FK}\r\nusuario_id uuid NOT NULL {FK}\r\nRestricciones: CONSTRAINT usuario_rol_pkey PRIMARY KEY (id)"
    },
    {
        "name": "invitacion_administrador",
        "columns": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_creacion",
                "type": "timestamp(6)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_actualizacion",
                "type": "timestamp(6)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_eliminacion",
                "type": "timestamp(6)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "creado_por",
                "type": "varchar(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "actualizado_por",
                "type": "varchar(255)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "correo",
                "type": "varchar(100)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "token_hash",
                "type": "varchar(64)",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": false
            },
            {
                "name": "nombres",
                "type": "varchar(50)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "apellidos",
                "type": "varchar(50)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "ci",
                "type": "varchar(20)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "celular",
                "type": "varchar(20)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "estado",
                "type": "varchar(20)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_expiracion",
                "type": "timestamp(6)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_ultimo_envio",
                "type": "timestamp(6)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_aceptacion",
                "type": "timestamp(6)",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "invitante_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "usuario_id",
                "type": "uuid",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": true
            }
        ],
        "constraints": [],
        "pk": [
            "id"
        ],
        "unique": [
            [
                "token_hash"
            ]
        ],
        "source": "V4",
        "visible": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "invitante_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "usuario_id",
                "type": "uuid",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "correo",
                "type": "varchar(100)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "token_hash",
                "type": "varchar(64)",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": false
            },
            {
                "name": "estado",
                "type": "varchar(20)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "fecha_expiracion",
                "type": "timestamp(6)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            }
        ],
        "notes": "Fuente: V4 y entidad JPA correspondiente.\r\nV4: CHECK estado PENDIENTE/ACEPTADA/EXPIRADA/REVOCADA. Índices UNIQUE parciales: lower(correo) y ci WHERE estado=PENDIENTE; NO unicidad global de correo/ci. usuario_id opcional y no UNIQUE: físicamente un usuario puede asociarse a varias invitaciones. Credenciales nunca se representan como valores.\r\nNo se declara @SQLDelete/@SQLRestriction en esta entidad; fecha_eliminacion heredada no demuestra borrado lógico automático.\r\nÍndices JPA declarados (existencia desplegada no verificada): []\r\nPK: id; UNIQUE: [['token_hash']]\r\nDiccionario físico completo (auditoría omitida visualmente, no del modelo):\r\nid uuid NOT NULL {PK}\r\nfecha_creacion timestamp(6) NULL\r\nfecha_actualizacion timestamp(6) NULL\r\nfecha_eliminacion timestamp(6) NULL\r\ncreado_por varchar(255) NULL\r\nactualizado_por varchar(255) NULL\r\ncorreo varchar(100) NOT NULL\r\ntoken_hash varchar(64) NOT NULL {UNIQUE}\r\nnombres varchar(50) NOT NULL\r\napellidos varchar(50) NOT NULL\r\nci varchar(20) NOT NULL\r\ncelular varchar(20) NOT NULL\r\nestado varchar(20) NOT NULL\r\nfecha_expiracion timestamp(6) NOT NULL\r\nfecha_ultimo_envio timestamp(6) NOT NULL\r\nfecha_aceptacion timestamp(6) NULL\r\ninvitante_id uuid NOT NULL {FK}\r\nusuario_id uuid NULL {FK}\r\nRestricciones: "
    },
    {
        "name": "usuario_solicitud_tipos_evento",
        "columns": [
            {
                "name": "usuario_id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": true
            },
            {
                "name": "tipo_evento",
                "type": "varchar(40)",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            }
        ],
        "constraints": [
            "PRIMARY KEY (usuario_id, tipo_evento)"
        ],
        "pk": [
            "usuario_id",
            "tipo_evento"
        ],
        "unique": [],
        "source": "V5",
        "visible": [
            {
                "name": "usuario_id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": true
            },
            {
                "name": "tipo_evento",
                "type": "varchar(40)",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            }
        ],
        "notes": "Fuente: V5 y entidad JPA correspondiente.\r\nElementCollection Set en Usuario. PK COMPUESTA(usuario_id,tipo_evento), sin id UUID. CHECK seis códigos: CONFERENCIAS_CHARLAS, CURSOS_TALLERES, DIPLOMADOS_CAPACITACION, ACTIVIDADES_CULTURALES, ACTIVIDADES_DEPORTIVAS, OTROS_EVENTOS_UNIVERSITARIOS. No FK de tipo_evento a categorias_evento.\r\nNo se declara @SQLDelete/@SQLRestriction en esta entidad; fecha_eliminacion heredada no demuestra borrado lógico automático.\r\nÍndices JPA declarados (existencia desplegada no verificada): []\r\nPK: usuario_id,tipo_evento; UNIQUE: []\r\nDiccionario físico completo (auditoría omitida visualmente, no del modelo):\r\nusuario_id uuid NOT NULL {PK} {FK}\r\ntipo_evento varchar(40) NOT NULL {PK}\r\nRestricciones: PRIMARY KEY (usuario_id, tipo_evento)"
    },
    {
        "name": "solicitud_organizador_historial",
        "columns": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "usuario_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "estado",
                "type": "varchar(20)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "detalle",
                "type": "jsonb",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            }
        ],
        "constraints": [],
        "pk": [
            "id"
        ],
        "unique": [],
        "source": "V5",
        "visible": [
            {
                "name": "id",
                "type": "uuid",
                "nn": true,
                "pk": true,
                "uq": false,
                "fk": false
            },
            {
                "name": "usuario_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": true
            },
            {
                "name": "estado",
                "type": "varchar(20)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            },
            {
                "name": "detalle",
                "type": "jsonb",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            }
        ],
        "notes": "Fuente: V5 y entidad JPA correspondiente.\r\nV5: CHECK estado=RECHAZADA, detalle jsonb NOT NULL, índice idx_solicitud_organizador_historial_estado(estado). usuario_id es UUID escalar en JPA pero FK explícita en V5. No hereda EntidadBase.\r\nNo se declara @SQLDelete/@SQLRestriction en esta entidad; fecha_eliminacion heredada no demuestra borrado lógico automático.\r\nÍndices JPA declarados (existencia desplegada no verificada): []\r\nPK: id; UNIQUE: []\r\nDiccionario físico completo (auditoría omitida visualmente, no del modelo):\r\nid uuid NOT NULL {PK}\r\nusuario_id uuid NOT NULL {FK}\r\nestado varchar(20) NOT NULL\r\ndetalle jsonb NOT NULL\r\nRestricciones: "
    }
];
        var RELACIONES = [
    {
        "child": "invitacion_administrador",
        "column": "invitante_id",
        "parent": "usuario",
        "parentColumn": "id",
        "parentMultiplicity": "1",
        "childMultiplicity": "0..*"
    },
    {
        "child": "invitacion_administrador",
        "column": "usuario_id",
        "parent": "usuario",
        "parentColumn": "id",
        "parentMultiplicity": "0..1",
        "childMultiplicity": "0..*"
    },
    {
        "child": "usuario_solicitud_tipos_evento",
        "column": "usuario_id",
        "parent": "usuario",
        "parentColumn": "id",
        "parentMultiplicity": "1",
        "childMultiplicity": "0..*"
    },
    {
        "child": "solicitud_organizador_historial",
        "column": "usuario_id",
        "parent": "usuario",
        "parentColumn": "id",
        "parentMultiplicity": "1",
        "childMultiplicity": "0..*"
    },
    {
        "child": "asistencias",
        "column": "sesion_evento_id",
        "parent": "sesiones_evento",
        "parentColumn": "id",
        "parentMultiplicity": "1",
        "childMultiplicity": "0..*"
    },
    {
        "child": "asistencias",
        "column": "registrado_por_id",
        "parent": "usuario",
        "parentColumn": "id",
        "parentMultiplicity": "0..1",
        "childMultiplicity": "0..*"
    },
    {
        "child": "asistencias",
        "column": "inscripcion_id",
        "parent": "inscripciones",
        "parentColumn": "id",
        "parentMultiplicity": "1",
        "childMultiplicity": "0..*"
    },
    {
        "child": "certificados",
        "column": "evento_id",
        "parent": "eventos",
        "parentColumn": "id",
        "parentMultiplicity": "1",
        "childMultiplicity": "0..*"
    },
    {
        "child": "certificados",
        "column": "inscripcion_id",
        "parent": "inscripciones",
        "parentColumn": "id",
        "parentMultiplicity": "1",
        "childMultiplicity": "0..1"
    },
    {
        "child": "certificados",
        "column": "usuario_id",
        "parent": "usuario",
        "parentColumn": "id",
        "parentMultiplicity": "1",
        "childMultiplicity": "0..*"
    },
    {
        "child": "eventos",
        "column": "categoria_id",
        "parent": "categorias_evento",
        "parentColumn": "id",
        "parentMultiplicity": "1",
        "childMultiplicity": "0..*"
    },
    {
        "child": "eventos",
        "column": "organizador_id",
        "parent": "usuario",
        "parentColumn": "id",
        "parentMultiplicity": "1",
        "childMultiplicity": "0..*"
    },
    {
        "child": "eventos",
        "column": "resuelto_por_id",
        "parent": "usuario",
        "parentColumn": "id",
        "parentMultiplicity": "0..1",
        "childMultiplicity": "0..*"
    },
    {
        "child": "inscripciones",
        "column": "evento_id",
        "parent": "eventos",
        "parentColumn": "id",
        "parentMultiplicity": "1",
        "childMultiplicity": "0..*"
    },
    {
        "child": "inscripciones",
        "column": "usuario_id",
        "parent": "usuario",
        "parentColumn": "id",
        "parentMultiplicity": "1",
        "childMultiplicity": "0..*"
    },
    {
        "child": "pagos",
        "column": "resuelto_por_id",
        "parent": "usuario",
        "parentColumn": "id",
        "parentMultiplicity": "0..1",
        "childMultiplicity": "0..*"
    },
    {
        "child": "pagos",
        "column": "inscripcion_id",
        "parent": "inscripciones",
        "parentColumn": "id",
        "parentMultiplicity": "1",
        "childMultiplicity": "0..1"
    },
    {
        "child": "qr_asistencia",
        "column": "sesion_evento_id",
        "parent": "sesiones_evento",
        "parentColumn": "id",
        "parentMultiplicity": "1",
        "childMultiplicity": "0..*"
    },
    {
        "child": "qr_asistencia",
        "column": "generado_por_id",
        "parent": "usuario",
        "parentColumn": "id",
        "parentMultiplicity": "1",
        "childMultiplicity": "0..*"
    },
    {
        "child": "rol_permiso",
        "column": "rol_id",
        "parent": "rol",
        "parentColumn": "id",
        "parentMultiplicity": "1",
        "childMultiplicity": "0..*"
    },
    {
        "child": "rol_permiso",
        "column": "permiso_id",
        "parent": "permiso",
        "parentColumn": "id",
        "parentMultiplicity": "1",
        "childMultiplicity": "0..*"
    },
    {
        "child": "sesion_usuario",
        "column": "usuario_id",
        "parent": "usuario",
        "parentColumn": "id",
        "parentMultiplicity": "1",
        "childMultiplicity": "0..*"
    },
    {
        "child": "sesiones_evento",
        "column": "evento_id",
        "parent": "eventos",
        "parentColumn": "id",
        "parentMultiplicity": "1",
        "childMultiplicity": "0..*"
    },
    {
        "child": "token_recuperacion",
        "column": "usuario_id",
        "parent": "usuario",
        "parentColumn": "id",
        "parentMultiplicity": "1",
        "childMultiplicity": "0..*"
    },
    {
        "child": "token_verificacion_correo",
        "column": "usuario_id",
        "parent": "usuario",
        "parentColumn": "id",
        "parentMultiplicity": "1",
        "childMultiplicity": "0..*"
    },
    {
        "child": "usuario",
        "column": "solicitud_resuelta_por_id",
        "parent": "usuario",
        "parentColumn": "id",
        "parentMultiplicity": "0..1",
        "childMultiplicity": "0..*"
    },
    {
        "child": "usuario_rol",
        "column": "rol_id",
        "parent": "rol",
        "parentColumn": "id",
        "parentMultiplicity": "1",
        "childMultiplicity": "0..*"
    },
    {
        "child": "usuario_rol",
        "column": "usuario_id",
        "parent": "usuario",
        "parentColumn": "id",
        "parentMultiplicity": "1",
        "childMultiplicity": "0..*"
    }
];
        var elementos = {};
        var conectores = [];

        function crearElemento(clave, nombre, tipo, notas)
        {
            var id = MARCA + " " + clave;
            destino.Elements.Refresh();
            var e = null;
            for (var j = 0; j < destino.Elements.Count; j++)
            {
                var actual = destino.Elements.GetAt(j);
                if (String(actual.Notes).split("\r\n")[0].split("\n")[0] == id)
                {
                    if (actual.Type != tipo) throw new Error("Tipo incompatible: " + nombre);
                    e = actual; break;
                }
            }
            if (e == null) e = destino.Elements.AddNew(nombre, tipo);
            e.Name = nombre;
            e.Notes = id + "\r\n" + notas;
            if (tipo == "Class") e.Stereotype = "table";
            guardar(e, "Guardar " + nombre);
            destino.Elements.Refresh();
            return e;
        }
        function atributo(e, c)
        {
            e.Attributes.Refresh();
            var a = null;
            for (var j = 0; j < e.Attributes.Count; j++)
            {
                var actual = e.Attributes.GetAt(j);
                if (actual.Name == c.name) { a = actual; break; }
            }
            var tipo = c.type.replace("character varying", "varchar").replace(" without time zone", "");
            if (a == null) a = e.Attributes.AddNew(c.name, tipo);
            a.Type = tipo;
            a.Visibility = "Public";
            var marcas = [];
            if (c.pk) marcas.push("PK");
            if (c.fk) marcas.push("FK");
            if (c.uq) marcas.push("UQ");
            if (c.nn) marcas.push("NN");
            a.StereotypeEx = marcas.join(",");
            a.Notes = MARCA + "\r\nTipo físico: " + c.type + "; " +
                (c.nn ? "NOT NULL" : "NULL permitido") + ". " + marcas.join(", ") +
                ". Restricciones compuestas se especifican en notas de tabla.";
            guardar(a, "Guardar atributo " + e.Name + "." + c.name);
            e.Attributes.Refresh();
        }
        for (var t = 0; t < TABLAS.length; t++)
        {
            var definicion = TABLAS[t];
            var e = crearElemento("tabla-" + definicion.name, definicion.name, "Class", definicion.notes);
            elementos[definicion.name] = e;
            for (var a = 0; a < definicion.visible.length; a++) atributo(e, definicion.visible[a]);
        }
        for (var r = 0; r < RELACIONES.length; r++)
        {
            var rel = RELACIONES[r];
            var padre = elementos[rel.parent], hijo = elementos[rel.child];
            if (!padre || !hijo) throw new Error("Referencia no creada: " + rel.child + "." + rel.column);
            var clave = MARCA + " FK-" + rel.child + "-" + rel.column;
            padre.Connectors.Refresh();
            var c = null;
            for (var k = 0; k < padre.Connectors.Count; k++)
            {
                var existente = padre.Connectors.GetAt(k);
                if (String(existente.Notes).split("\r\n")[0] == clave)
                {
                    c = existente; break;
                }
            }
            if (c == null) c = padre.Connectors.AddNew(rel.column, "Association");
            c.Name = rel.column;
            c.ClientID = padre.ElementID;
            c.SupplierID = hijo.ElementID;
            c.Direction = "Unspecified";
            c.Notes = clave + "\r\n" + rel.child + "." + rel.column + " -> " +
                rel.parent + "." + rel.parentColumn + ". Multiplicidades según nulabilidad " +
                "y unicidad física declaradas. No implica cascada de borrado.";
            guardar(c, "Guardar FK " + rel.child + "." + rel.column);
            c.ClientEnd.Cardinality = rel.parentMultiplicity;
            guardar(c.ClientEnd, "Cardinalidad del padre " + rel.parent);
            c.SupplierEnd.Cardinality = rel.childMultiplicity;
            guardar(c.SupplierEnd, "Cardinalidad del hijo " + rel.child);
            padre.Connectors.Refresh();
            conectores.push({ modelo: c, parent: rel.parent, child: rel.child });
        }
        function diagrama(nombre)
        {
            destino.Diagrams.Refresh();
            var d = null;
            for (var j = 0; j < destino.Diagrams.Count; j++)
            {
                var actual = destino.Diagrams.GetAt(j);
                if (actual.Name == nombre)
                {
                    if (!propio(actual) || actual.Type != "Class") throw new Error("Diagrama homónimo ajeno.");
                    d = actual; break;
                }
            }
            if (d == null) d = destino.Diagrams.AddNew(nombre, "Class");
            d.Notes = MARCA + "\r\nVista del modelo físico versionado; auditoría y columnas " +
                "secundarias disponibles en notas de tabla. Un único diagrama completo.";
            guardar(d, "Guardar vista " + nombre);
            destino.Diagrams.Refresh();
            return d;
        }
        function colocar(d, e, l, t, w, h)
        {
            d.DiagramObjects.Refresh();
            var o = null;
            for (var j = 0; j < d.DiagramObjects.Count; j++)
            {
                var actual = d.DiagramObjects.GetAt(j);
                if (actual.ElementID == e.ElementID) { o = actual; break; }
            }
            if (o == null)
            {
                o = d.DiagramObjects.AddNew("l="+l+";r="+(l+w)+";t="+t+";b="+(t+h)+";", "");
                o.ElementID = e.ElementID;
            }
            o.Left=l; o.Right=l+w; o.Top=-t; o.Bottom=-(t+h);
            o.FontName="Arial"; o.FontSize="12";
            o.BackgroundColor=16777215; o.BorderColor=0; o.FontColor=0;
            o.ShowPublicAttributes=true; o.ShowPublicOperations=false;
            guardar(o, "Posicionar " + e.Name);
            d.DiagramObjects.Refresh();
        }
        function mostrarRelaciones(d, nombres)
        {
            d.DiagramLinks.Refresh();
            for (var j = 0; j < conectores.length; j++)
            {
                var ref=conectores[j];
                if (!nombres[ref.parent] || !nombres[ref.child]) continue;
                var l=null;
                for (var k=0; k<d.DiagramLinks.Count; k++)
                {
                    var actual=d.DiagramLinks.GetAt(k);
                    if (actual.ConnectorID==ref.modelo.ConnectorID) { l=actual; break; }
                }
                if (l==null)
                {
                    l=d.DiagramLinks.AddNew("", "");
                    l.ConnectorID=ref.modelo.ConnectorID;
                }
                l.IsHidden=false;
                l.HiddenLabels=false;
                l.LineStyle=2; // Auto Routing, documentado por EA.
                guardar(l, "Mostrar relación " + ref.modelo.Name);
                d.DiagramLinks.Refresh();
            }
        }
        // Seguridad a la izquierda; cuenta en el centro; dominio a la derecha.
        var vistas = [
            { nombre:"Figura 4 - Modelo de Datos de Vidia - Actualizado", filas:[
                ["permiso",20,30,330,135],
                ["rol_permiso",20,320,330,135],
                ["rol",430,320,330,135],
                ["usuario_rol",430,610,330,135],
                ["token_recuperacion",20,610,330,195],
                ["token_verificacion_correo",20,920,330,195],
                ["sesion_usuario",430,920,330,180],
                ["invitacion_administrador",430,30,330,235],
                ["usuario",840,320,350,240],
                ["usuario_solicitud_tipos_evento",840,30,350,145],
                ["solicitud_organizador_historial",840,920,350,170],
                ["categorias_evento",1250,30,330,155],
                ["eventos",1250,320,350,215],
                ["inscripciones",1660,320,350,185],
                ["pagos",1660,30,350,200],
                ["certificados",1660,920,350,205],
                ["sesiones_evento",1250,610,350,220],
                ["asistencias",1660,610,350,210],
                ["qr_asistencia",1250,920,350,240]
            ], texto:"MODELO COMPLETO: 19 tablas y 28 claves foráneas. " +
                "Pago y certificado: como máximo uno por inscripción. Asistencia: UNIQUE " +
                "(inscripcion_id,sesion_evento_id). Tipos de solicitud: PK compuesta " +
                "(usuario_id,tipo_evento), sin id y sin FK a categorias_evento. " +
                "Las relaciones con usuario distinguen propietario, resolutor y generador. " +
                "Archivos: referencias externas, no contenido binario en estas tablas." }
        ];
        for (var v=0; v<vistas.length; v++)
        {
            var vista=vistas[v], d=diagrama(vista.nombre), presentes={}, maxY=0;
            for (var f=0; f<vista.filas.length; f++)
            {
                var pos=vista.filas[f];
                presentes[pos[0]]=true;
                colocar(d,elementos[pos[0]],pos[1],pos[2],pos[3],pos[4]);
                maxY=Math.max(maxY,pos[2]+pos[4]);
            }
            var nota=crearElemento("nota-vista-"+v,"Claves, alcance y lectura","Note",
                vista.texto + "\r\nPK=clave primaria; FK=clave foránea; UQ=UNIQUE simple; NN=NOT NULL. " +
                "PK/UNIQUE compuestas e índices en notas. timestamp(6)/time(6) sin zona horaria. " +
                "No se afirma validación de FK históricas NOT VALID ni versión/esquema productivo. " +
                "Atributos de auditoría comunes disponibles en diccionario de cada tabla.");
            colocar(d,nota,20,maxY+55,1990,130);
            mostrarRelaciones(d,presentes);
            guardar(d,"Guardar vista final");
            Repository.ReloadDiagram(d.DiagramID);
        }
        Session.Output("Figura 4 actualizada: 19 tablas, " + RELACIONES.length +
            " FK en un único diagrama. Original conservado.");
        Session.Prompt("Modelo actualizado preparado.\nExporte el único diagrama " +
            "completo en orientación apaisada.\nRevise rutas, cardinalidades " +
            "y tamaño de texto en Enterprise Architect antes de incluirlos en la monografía.",promptOK);
    }
    catch(error)
    {
        var mensaje=String(error.message || error);
        Session.Output("Modelo detenido: "+mensaje);
        Session.Prompt("No se completó el modelo actualizado.\n"+mensaje+
            "\nOriginal intacto. Puede reintentar sin duplicar los objetos propios ya guardados.",promptOK);
    }
}
main();
