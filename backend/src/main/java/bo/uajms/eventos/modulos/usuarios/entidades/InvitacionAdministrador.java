package bo.uajms.eventos.modulos.usuarios.entidades;

import bo.uajms.eventos.comun.EntidadBase;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "invitacion_administrador")
@Getter @Setter @NoArgsConstructor
public class InvitacionAdministrador extends EntidadBase {
    public enum Estado { PENDIENTE, ACEPTADA, EXPIRADA, REVOCADA }
    @Column(nullable=false, length=100) private String correo;
    @Column(name="token_hash", nullable=false, unique=true, length=64) private String tokenHash;
    @Column(nullable=false, length=50) private String nombres;
    @Column(nullable=false, length=50) private String apellidos;
    @Column(nullable=false, length=20) private String ci;
    @Column(nullable=false, length=20) private String celular;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private Estado estado = Estado.PENDIENTE;
    @Column(name="fecha_expiracion", nullable=false) private LocalDateTime fechaExpiracion;
    @Column(name="fecha_ultimo_envio", nullable=false) private LocalDateTime fechaUltimoEnvio;
    @Column(name="fecha_aceptacion") private LocalDateTime fechaAceptacion;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="invitante_id", nullable=false) private Usuario invitante;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="usuario_id") private Usuario usuario;
}
