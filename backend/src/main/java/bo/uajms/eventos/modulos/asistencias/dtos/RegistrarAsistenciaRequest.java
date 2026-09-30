package bo.uajms.eventos.modulos.asistencias.dtos;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RegistrarAsistenciaRequest {
    @NotBlank @Size(max = 200)
    @Schema(description = "Token temporal obtenido del QR de la sesion", requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;
    @DecimalMin("-90.0") @DecimalMax("90.0")
    @Schema(description = "Latitud del dispositivo; obligatoria cuando la sesion exige GPS")
    private BigDecimal latitud;
    @DecimalMin("-180.0") @DecimalMax("180.0")
    @Schema(description = "Longitud del dispositivo; obligatoria cuando la sesion exige GPS")
    private BigDecimal longitud;
    @DecimalMin("0.0")
    @Schema(description = "Precision reportada por el dispositivo en metros; maxima 30 cuando se exige GPS")
    private BigDecimal precision;
}
