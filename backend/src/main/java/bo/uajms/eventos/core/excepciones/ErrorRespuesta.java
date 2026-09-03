package bo.uajms.eventos.core.excepciones;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ErrorRespuesta {
    private String codigo;
    private String mensaje;
    private List<String> detalles;
    private LocalDateTime timestamp;
    private String ruta;
}
