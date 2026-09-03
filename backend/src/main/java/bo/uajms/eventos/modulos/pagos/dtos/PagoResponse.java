package bo.uajms.eventos.modulos.pagos.dtos;

import bo.uajms.eventos.modulos.pagos.entidades.EstadoPago;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PagoResponse {
    private UUID id;
    private UUID inscripcionId;
    private String eventoTitulo;
    private String usuarioNombre;
    private BigDecimal monto;
    private LocalDateTime fechaPago;
    private EstadoPago estado;
    private String observacion;
    private ComprobantePagoResponse comprobante;
}
