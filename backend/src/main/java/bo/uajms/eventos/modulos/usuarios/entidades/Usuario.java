package bo.uajms.eventos.modulos.usuarios.entidades;

import bo.uajms.eventos.comun.EntidadBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.LinkedHashSet;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario extends EntidadBase {

    @Column(name = "correo_electronico", nullable = false, unique = true, length = 100)
    private String correoElectronico;

    @Column(name = "contrasena", nullable = false)
    private String contrasena;

    @Column(name = "nombres", nullable = false, length = 50)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 50)
    private String apellidos;

    @Column(name = "ci", nullable = false, unique = true, length = 20)
    private String ci;

    @Column(name = "ru", unique = true, length = 20)
    private String ru;

    @Column(name = "celular", length = 20)
    private String celular;

    @Column(name = "correo_verificado", nullable = false)
    @Builder.Default
    private boolean correoVerificado = false;

    @Column(name = "fotografia_url", length = 255)
    private String fotografiaUrl;

    @Column(name = "nombre_archivo_fotografia", length = 100)
    private String nombreArchivoFotografia;

    @Column(name = "fecha_carga_fotografia")
    private java.time.LocalDateTime fechaCargaFotografia;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_usuario", nullable = false, length = 20)
    private TipoUsuario tipoUsuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_solicitud_organizador", nullable = false, length = 20)
    @Builder.Default
    private EstadoSolicitudOrganizador estadoSolicitudOrganizador = EstadoSolicitudOrganizador.NINGUNA;

    @Column(name = "fecha_solicitud_organizador")
    private LocalDateTime fechaSolicitudOrganizador;

    @Column(name = "fecha_resolucion_organizador")
    private LocalDateTime fechaResolucionOrganizador;

    @Column(name = "motivo_rechazo_organizador", length = 500)
    private String motivoRechazoOrganizador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_resuelta_por_id")
    private Usuario solicitudResueltaPor;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    @Column(name = "motivo_solicitud_organizador", length = 1000)
    private String motivoSolicitudOrganizador;

    @Column(name = "informacion_adicional_organizador", length = 1000)
    private String informacionAdicionalOrganizador;

    @ElementCollection
    @CollectionTable(name = "usuario_solicitud_tipos_evento", joinColumns = @JoinColumn(name = "usuario_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 40)
    @Builder.Default
    private Set<TipoEventoSolicitud> tiposEventosSolicitud = new LinkedHashSet<>();

    public enum TipoUsuario {
        INTERNO, EXTERNO
    }

    public enum EstadoSolicitudOrganizador {
        NINGUNA, PENDIENTE, APROBADA, RECHAZADA
    }
}
