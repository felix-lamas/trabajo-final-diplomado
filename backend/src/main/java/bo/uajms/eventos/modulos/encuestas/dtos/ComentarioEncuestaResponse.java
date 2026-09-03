package bo.uajms.eventos.modulos.encuestas.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ComentarioEncuestaResponse {
    private String participante;
    private String comentario;
    private LocalDateTime fechaRespuesta;
}
