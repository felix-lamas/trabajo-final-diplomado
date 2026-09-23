package bo.uajms.eventos.modulos.pagos.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class RegistrarPagoRequest {
    @NotNull(message = "El ID de la inscripción es obligatorio")
    private UUID inscripcionId;

    private String observacion;
}
