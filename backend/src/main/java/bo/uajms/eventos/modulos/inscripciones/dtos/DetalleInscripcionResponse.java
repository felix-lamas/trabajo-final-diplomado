package bo.uajms.eventos.modulos.inscripciones.dtos;

import bo.uajms.eventos.modulos.inscripciones.entidades.EstadoInscripcion;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class DetalleInscripcionResponse {
    private UUID id;
    private UUID usuarioId;
    private String usuarioNombre;
    private UUID eventoId;
    private String eventoTitulo;
    private LocalDateTime fechaInscripcion;
    private EstadoInscripcion estado;
    private String observacion;
    private String modalidalEvento;
    private String ubicacionEvento;
}
