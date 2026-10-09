package bo.uajms.eventos.modulos.usuarios.entidades;

import bo.uajms.eventos.modulos.usuarios.dtos.SolicitudOrganizadorResponse;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;

@Entity @Table(name = "solicitud_organizador_historial")
@Getter @Setter @NoArgsConstructor
public class SolicitudOrganizadorHistorial {
    @Id private UUID id;
    @Column(name = "usuario_id", nullable = false) private UUID usuarioId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Usuario.EstadoSolicitudOrganizador estado;
    @JdbcTypeCode(SqlTypes.JSON) @Column(nullable = false, columnDefinition = "jsonb")
    private SolicitudOrganizadorResponse detalle;
}
