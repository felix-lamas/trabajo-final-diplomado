package bo.uajms.eventos.modulos.usuarios.dtos;

import lombok.Builder;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Estado de una solicitud para convertirse en ORGANIZADOR")
public class SolicitudOrganizadorResponse {
    @Schema(description = "Usuario propietario de la solicitud")
    private UUID usuarioId;
    private String nombres;
    private String apellidos;
    private String correoElectronico;
    @Schema(description = "NINGUNA, PENDIENTE, APROBADA o RECHAZADA", example = "PENDIENTE")
    private String estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaResolucion;
    @Schema(description = "Motivo obligatorio cuando la solicitud fue rechazada")
    private String motivoRechazo;
    private UUID resueltaPorId;
}
