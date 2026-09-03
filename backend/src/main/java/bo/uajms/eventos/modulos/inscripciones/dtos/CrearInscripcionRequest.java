package bo.uajms.eventos.modulos.inscripciones.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearInscripcionRequest {
    @NotNull(message = "El ID del evento es obligatorio")
    private UUID eventoId;
}
