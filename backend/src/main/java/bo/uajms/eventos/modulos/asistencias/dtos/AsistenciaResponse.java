package bo.uajms.eventos.modulos.asistencias.dtos;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsistenciaResponse {
    private UUID id;
    private String nombreParticipante;
    private String documentoIdentidad;
    private String codigoParticipante;
    private String evento;
    private LocalDateTime fechaHoraRegistro;
    private String usuarioControl;
    private String observacion;
}
