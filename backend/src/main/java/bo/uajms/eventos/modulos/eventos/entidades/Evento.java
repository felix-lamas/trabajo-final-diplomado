package bo.uajms.eventos.modulos.eventos.entidades;

import bo.uajms.eventos.comun.EntidadBase;
import bo.uajms.eventos.modulos.categorias.entidades.CategoriaEvento;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "eventos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE eventos SET fecha_eliminacion = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("fecha_eliminacion IS NULL")
public class Evento extends EntidadBase {

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(columnDefinition = "TEXT")
    private String objetivos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaEvento categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Modalidad modalidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_inscripcion", nullable = false, length = 20)
    private TipoInscripcion tipoInscripcion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costo;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    @Column(length = 255)
    private String ubicacion;

    @Column(length = 500)
    private String direccion;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitud;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitud;

    @Column(name = "radio_metros")
    private Integer radioMetros;

    @Column(name = "enlace_virtual", length = 500)
    private String enlaceVirtual;

    @Column(name = "requiere_inscripcion", nullable = false)
    @Builder.Default
    private Boolean requiereInscripcion = true;

    @Column(name = "cupo_limitado", nullable = false)
    @Builder.Default
    private Boolean cupoLimitado = true;

    @Column(name = "cupo_maximo")
    private Integer cupoMaximo;

    @Column(name = "cupo_disponible")
    private Integer cupoDisponible;

    @Column(name = "emite_certificado", nullable = false)
    @Builder.Default
    private Boolean emiteCertificado = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_certificado", length = 20)
    private TipoCertificadoEvento tipoCertificado;

    @Column(name = "horas_academicas")
    private Integer horasAcademicas;

    @Enumerated(EnumType.STRING)
    @Column(name = "publico_objetivo", nullable = false, length = 20)
    @Builder.Default
    private PublicoObjetivo publicoObjetivo = PublicoObjetivo.AMBOS;

    @Column(name = "telefono_contacto", length = 20)
    private String telefonoContacto;

    @Column(name = "email_contacto", length = 100)
    private String emailContacto;

    @Column(name = "whatsapp_contacto", length = 20)
    private String whatsappContacto;

    @Column(name = "qr_pago_url", length = 500)
    private String qrPagoUrl;

    @Column(name = "qr_pago_storage_key", length = 500)
    private String qrPagoStorageKey;

    @Column(name = "instrucciones_pago", columnDefinition = "TEXT")
    private String instruccionesPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoEvento estado;

    @Column(name = "imagen_portada", length = 500)
    private String imagenPortada;

    @Column(name = "motivo_rechazo", length = 1000)
    private String motivoRechazo;

    @Column(name = "motivo_cancelacion", length = 1000)
    private String motivoCancelacion;

    @Column(name = "fecha_envio_revision")
    private java.time.LocalDateTime fechaEnvioRevision;

    @Column(name = "fecha_resolucion")
    private java.time.LocalDateTime fechaResolucion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resuelto_por_id")
    private Usuario resueltoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizador_id", nullable = false)
    private Usuario organizador;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<bo.uajms.eventos.modulos.sesiones.entidades.SesionEvento> sesiones = new ArrayList<>();
}
