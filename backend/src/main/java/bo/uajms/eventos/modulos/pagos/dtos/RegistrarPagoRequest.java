package bo.uajms.eventos.modulos.pagos.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class RegistrarPagoRequest {
    @NotNull(message = "El ID de la inscripción es obligatorio")
    private UUID inscripcionId;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser positivo")
    private BigDecimal monto;

    private String observacion;
}
