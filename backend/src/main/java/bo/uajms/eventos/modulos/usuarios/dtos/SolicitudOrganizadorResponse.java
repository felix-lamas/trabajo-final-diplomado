package bo.uajms.eventos.modulos.usuarios.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class SolicitudOrganizadorResponse {
    private UUID usuarioId;
    private String nombres;
    private String apellidos;
    private String correoElectronico;
    private String estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaResolucion;
    private String motivoRechazo;
    private UUID resueltaPorId;
}
