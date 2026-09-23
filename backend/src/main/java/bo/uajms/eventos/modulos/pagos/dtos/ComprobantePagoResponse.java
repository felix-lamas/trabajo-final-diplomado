package bo.uajms.eventos.modulos.pagos.dtos;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;
import java.time.LocalDateTime;

@Data
@Builder
public class ComprobantePagoResponse {
    private UUID id;
    private String nombreArchivo;
    private String tipoContenido;
    private LocalDateTime fechaCarga;
    private Boolean disponible;
}
