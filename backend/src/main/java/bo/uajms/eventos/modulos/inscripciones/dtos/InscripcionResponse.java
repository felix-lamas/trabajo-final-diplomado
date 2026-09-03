package bo.uajms.eventos.modulos.inscripciones.dtos;

import bo.uajms.eventos.modulos.inscripciones.entidades.EstadoInscripcion;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class InscripcionResponse {
    private UUID id;
    private UUID eventoId;
    private String eventoTitulo;
    private LocalDateTime fechaInscripcion;
    private EstadoInscripcion estado;
}
