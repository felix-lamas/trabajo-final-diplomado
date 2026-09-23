package bo.uajms.eventos.modulos.asistencias.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RegistrarAsistenciaRequest {
    @NotBlank @Size(max = 200) private String token;
    @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") private BigDecimal latitud;
    @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") private BigDecimal longitud;
    @NotNull @DecimalMin("0.0") private BigDecimal precision;
}
