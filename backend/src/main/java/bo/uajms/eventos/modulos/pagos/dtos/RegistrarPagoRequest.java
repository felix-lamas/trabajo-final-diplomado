package bo.uajms.eventos.modulos.pagos.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class RegistrarPagoRequest {
    @NotNull(message = "El ID de la inscripción es obligatorio")
    private UUID inscripcionId;

    @Size(max = 1000)
    private String observacion;
}
