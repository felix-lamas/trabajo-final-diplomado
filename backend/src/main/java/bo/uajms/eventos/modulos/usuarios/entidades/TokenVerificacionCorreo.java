package bo.uajms.eventos.modulos.usuarios.entidades;

import bo.uajms.eventos.comun.EntidadBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_verificacion_correo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenVerificacionCorreo extends EntidadBase {

    /** Hash SHA-256 del token. El valor plano solo se entrega al servicio de correo. */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "utilizado", nullable = false)
    private boolean utilizado;
}
