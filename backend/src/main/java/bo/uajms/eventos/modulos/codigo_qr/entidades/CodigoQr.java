package bo.uajms.eventos.modulos.codigo_qr.entidades;

import bo.uajms.eventos.comun.EntidadBase;
import bo.uajms.eventos.modulos.credenciales.entidades.Credencial;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "codigos_qr")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE codigos_qr SET fecha_eliminacion = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("fecha_eliminacion IS NULL")
public class CodigoQr extends EntidadBase {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credencial_id", nullable = false, unique = true)
    private Credencial credencial;

    @Column(nullable = false, length = 500)
    private String contenido;

    @Column(name = "fecha_generacion", nullable = false)
    @Builder.Default
    private LocalDateTime fechaGeneracion = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_qr", nullable = false, length = 20)
    @Builder.Default
    private EstadoQr estadoQr = EstadoQr.GENERADO;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    public enum EstadoQr {
        GENERADO, UTILIZADO, ANULADO
    }
}
