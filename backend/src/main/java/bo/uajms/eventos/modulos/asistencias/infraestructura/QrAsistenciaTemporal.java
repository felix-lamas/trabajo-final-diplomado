package bo.uajms.eventos.modulos.asistencias.infraestructura;

import bo.uajms.eventos.comun.EntidadBase;
import bo.uajms.eventos.modulos.sesiones.entidades.SesionEvento;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Infraestructura técnica: no forma parte del modelo conceptual académico. */
@Entity
@Table(name = "qr_asistencia", uniqueConstraints = @UniqueConstraint(columnNames = "token_hash"),
        indexes = @Index(name = "idx_qr_asistencia_sesion_activo", columnList = "sesion_evento_id,activo"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QrAsistenciaTemporal extends EntidadBase {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sesion_evento_id", nullable = false)
    private SesionEvento sesionEvento;

    @Column(name = "token_hash", nullable = false, length = 64, updatable = false)
    private String tokenHash;

    @Column(name = "emitido_en", nullable = false, updatable = false)
    private LocalDateTime emitidoEn;

    @Column(name = "expira_en", nullable = false, updatable = false)
    private LocalDateTime expiraEn;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "generado_por_id", nullable = false, updatable = false)
    private Usuario generadoPor;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "revocado_en")
    private LocalDateTime revocadoEn;

    @Version
    private Long version;
}
