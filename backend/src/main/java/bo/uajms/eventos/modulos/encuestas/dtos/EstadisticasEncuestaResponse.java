package bo.uajms.eventos.modulos.encuestas.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class EstadisticasEncuestaResponse {
    private UUID eventoId;
    private String eventoTitulo;
    private Double calificacionPromedio;
    private long totalAsistentes;
    private long totalRespuestas;
    private Double participacion;
    private List<ComentarioEncuestaResponse> comentarios;
}
