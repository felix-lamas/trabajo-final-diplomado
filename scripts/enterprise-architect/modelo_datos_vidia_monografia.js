!INC Local Scripts.EAConstants-JavaScript

/*
 * Figura 4: modelo conceptual de Vidia para monografía, ocho entidades.
 * Contrastado con JPA y Flyway V1–V5. No es el esquema físico completo.
 * PK/FK/UQ/NN identifican claves, unicidad simple y nulabilidad.
 * Se omiten columnas secundarias, seguridad y relaciones de resolutores;
 * las omisiones son explícitas en la leyenda y no cambian el sistema.
 * varchar equivale a character varying; timestamp(6)/time(6) sin zona.
 * V3 conserva datos históricos y puede omitir UNIQUE si hay duplicados;
 * las multiplicidades describen el modelo canónico, no una DB inspeccionada.
 * Paquete propio e idempotente: no elimina ni sobrescribe objetos originales.
 * API clásica de EA: Collections.AddNew, Update, ConnectorEnd.Cardinality.
 * No ejecutado ni revisado visualmente en Enterprise Architect.
 */

function main()
{
    var MARCA = "[VIDIA-MODELO-CONCEPTUAL-MONOGRAFIA-v1]";
    var NOMBRE = "Vidia - Modelo conceptual para monografía";
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
                "name": "fecha_hora_registro",
                "type": "timestamp(6) without time zone",
                "nn": true,
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
            }
        ],
        "notes": "Vista conceptual de asistencias. Fuente: entidad JPA y Flyway V1–V5. Solo se muestran identificadores y campos principales. UNIQUE(inscripcion_id,sesion_evento_id).",
        "label": "ASISTENCIA"
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
                "name": "descripcion",
                "type": "text",
                "nn": false,
                "pk": false,
                "uq": false,
                "fk": false
            }
        ],
        "notes": "Vista conceptual de categorias_evento. Fuente: entidad JPA y Flyway V1–V5. Solo se muestran identificadores y campos principales. ",
        "label": "CATEGORIA_EVENTO"
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
                "name": "inscripcion_id",
                "type": "uuid",
                "nn": true,
                "pk": false,
                "uq": true,
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
                "name": "fecha_emision",
                "type": "timestamp(6) without time zone",
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
            }
        ],
        "notes": "Vista conceptual de certificados. Fuente: entidad JPA y Flyway V1–V5. Solo se muestran identificadores y campos principales. ",
        "label": "CERTIFICADO"
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
                "name": "titulo",
                "type": "character varying(200)",
                "nn": true,
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
                "name": "fecha_inicio",
                "type": "date",
                "nn": true,
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
                "name": "estado",
                "type": "character varying(30)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            }
        ],
        "notes": "Vista conceptual de eventos. Fuente: entidad JPA y Flyway V1–V5. Solo se muestran identificadores y campos principales. ",
        "label": "EVENTO"
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
                "name": "codigo_participante",
                "type": "character varying(30)",
                "nn": false,
                "pk": false,
                "uq": true,
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
                "name": "estado",
                "type": "character varying(30)",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            }
        ],
        "notes": "Vista conceptual de inscripciones. Fuente: entidad JPA y Flyway V1–V5. Solo se muestran identificadores y campos principales. UNIQUE(usuario_id,evento_id).",
        "label": "INSCRIPCION"
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
        "notes": "Vista conceptual de pagos. Fuente: entidad JPA y Flyway V1–V5. Solo se muestran identificadores y campos principales. ",
        "label": "PAGO"
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
                "name": "nombre",
                "type": "character varying(150)",
                "nn": true,
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
                "name": "requiere_asistencia",
                "type": "boolean",
                "nn": true,
                "pk": false,
                "uq": false,
                "fk": false
            }
        ],
        "notes": "Vista conceptual de sesiones_evento. Fuente: entidad JPA y Flyway V1–V5. Solo se muestran identificadores y campos principales. ",
        "label": "SESION_EVENTO"
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
                "name": "nombres",
                "type": "character varying(50)",
                "nn": true,
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
                "name": "correo_electronico",
                "type": "character varying(100)",
                "nn": true,
                "pk": false,
                "uq": true,
                "fk": false
            }
        ],
        "notes": "Vista conceptual de usuario. Fuente: entidad JPA y Flyway V1–V5. Solo se muestran identificadores y campos principales. ",
        "label": "USUARIO"
    }
];
    var RELACIONES = [
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
        "column": "inscripcion_id",
        "parent": "inscripciones",
        "parentColumn": "id",
        "parentMultiplicity": "1",
        "childMultiplicity": "0..1"
    },
    {
        "child": "sesiones_evento",
        "column": "evento_id",
        "parent": "eventos",
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
            var e = crearElemento("tabla-" + definicion.name, definicion.label, "Class", definicion.notes);
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
        var vistas = [
            { nombre:"Figura 4 - Modelo de Datos de Vidia", filas:[
                ["usuario",20,310,300,170],
                ["categorias_evento",430,20,300,140],
                ["eventos",430,310,330,245],
                ["inscripciones",880,310,330,205],
                ["pagos",880,20,330,185],
                ["sesiones_evento",430,680,330,225],
                ["asistencias",880,680,330,185],
                ["certificados",20,680,330,225]
            ], texto:"Vista conceptual: ocho entidades del dominio. Nombres conceptuales en " +
                "singular; nombres físicos en notas. Se omiten seguridad, tokens, auditoría " +
                "y relaciones técnicas de resolución/registro, incluida la autorreferencia " +
                "de usuario; permanecen en la implementación. Pago y certificado: máximo uno " +
                "por inscripción. Asistencia: única por inscripción y sesión. Comprobante: " +
                "referencia a archivo externo, no archivo binario en PostgreSQL." }
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
                "Cardinalidades del modelo JPA y esquema canónico; datos históricos o producción no inspeccionados. " +
                "Atributos de auditoría comunes disponibles en diccionario de cada tabla.");
            colocar(d,nota,20,maxY+55,1190,130);
            mostrarRelaciones(d,presentes);
            guardar(d,"Guardar vista final");
            Repository.ReloadDiagram(d.DiagramID);
        }
        Session.Output("Figura 4 actualizada: 8 entidades, " + RELACIONES.length +
            " relaciones de dominio en un único diagrama. Original conservado.");
        Session.Prompt("Modelo conceptual preparado.\nExporte el único diagrama " +
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
