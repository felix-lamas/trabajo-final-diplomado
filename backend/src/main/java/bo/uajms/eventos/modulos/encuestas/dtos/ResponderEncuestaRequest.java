package bo.uajms.eventos.modulos.encuestas.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class ResponderEncuestaRequest {

    @NotNull(message = "El evento es obligatorio")
    private UUID eventoId;

    @NotNull(message = "La calificacion es obligatoria")
    @Min(value = 1, message = "La calificacion minima es 1")
    @Max(value = 5, message = "La calificacion maxima es 5")
    private Integer calificacion;

    @Size(max = 1000, message = "El comentario no debe superar 1000 caracteres")
    private String comentario;
}
