package bo.uajms.eventos.modulos.pagos.dtos;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class ComprobantePagoResponse {
    private UUID id;
    private String urlArchivo;
    private String nombreArchivo;
    private String tipoContenido;
}
