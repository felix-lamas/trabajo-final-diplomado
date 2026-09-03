package bo.uajms.eventos.modulos.control_acceso.dtos;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ControlAccesoResponse {
    private UUID id;
    private String nombreParticipante;
    private String evento;
    private LocalDateTime fechaHoraIngreso;
    private String estadoIngreso;
    private String usuarioControl;
    private String observacion;
}
