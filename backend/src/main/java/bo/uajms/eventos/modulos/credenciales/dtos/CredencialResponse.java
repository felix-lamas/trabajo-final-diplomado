package bo.uajms.eventos.modulos.credenciales.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CredencialResponse {
    private UUID id;
    private UUID usuarioId;
    private String usuarioNombre;
    private UUID eventoId;
    private String eventoTitulo;
    private UUID inscripcionId;
    private String codigoParticipante;
    private LocalDateTime fechaGeneracion;
    private String estado;
}
