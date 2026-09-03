package bo.uajms.eventos.modulos.control_acceso.entidades;

import bo.uajms.eventos.comun.EntidadBase;
import bo.uajms.eventos.modulos.credenciales.entidades.Credencial;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "control_acceso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE control_acceso SET fecha_eliminacion = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("fecha_eliminacion IS NULL")
public class ControlAcceso extends EntidadBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credencial_id", nullable = false)
    private Credencial credencial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_control_id", nullable = false)
    private Usuario usuarioControl;

    @Column(name = "fecha_hora_ingreso", nullable = false)
    private LocalDateTime fechaHoraIngreso;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_ingreso", nullable = false, length = 20)
    private EstadoIngreso estadoIngreso;

    @Column(columnDefinition = "TEXT")
    private String observacion;

    public enum EstadoIngreso {
        AUTORIZADO, DENEGADO, REINTENTO
    }
}
