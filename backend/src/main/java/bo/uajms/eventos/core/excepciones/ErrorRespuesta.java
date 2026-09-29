package bo.uajms.eventos.core.excepciones;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@Schema(description = "Formato comun de errores HTTP de Vidia")
public class ErrorRespuesta {
    @Schema(description = "Codigo estable del error", example = "VALIDATION_ERROR")
    private String codigo;
    @Schema(description = "Mensaje seguro para el consumidor", example = "Datos de entrada invalidos")
    private String mensaje;
    @Schema(description = "Detalles de validacion cuando corresponda")
    private List<String> detalles;
    @Schema(description = "Fecha y hora local del error", example = "2026-09-28T14:30:00")
    private LocalDateTime timestamp;
    @Schema(description = "Ruta que produjo el error", example = "uri=/api/v1/eventos")
    private String ruta;
}
