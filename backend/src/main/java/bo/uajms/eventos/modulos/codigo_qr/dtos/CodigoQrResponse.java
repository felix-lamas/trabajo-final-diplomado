package bo.uajms.eventos.modulos.codigo_qr.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CodigoQrResponse {
    private UUID id;
    private UUID credencialId;
    private String contenido;
    private LocalDateTime fechaGeneracion;
    private Boolean activo;
    private String qrBase64; // Useful for frontend to display
}
