package bo.uajms.eventos.modulos.encuestas.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class EncuestaResponse {
    private UUID id;
    private UUID eventoId;
    private String eventoTitulo;
    private UUID usuarioId;
    private String participante;
    private Integer calificacion;
    private String comentario;
    private LocalDateTime fechaRespuesta;
}
