package bo.uajms.eventos.modulos.inscripciones.dtos;

import bo.uajms.eventos.modulos.inscripciones.entidades.EstadoInscripcion;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ComprobanteInscripcionResponse {
    private UUID inscripcionId;
    private String codigoInscripcion;
    private UUID eventoId;
    private String eventoTitulo;
    private String participante;
    private String ci;
    private String ru;
    private BigDecimal monto;
    private LocalDateTime fechaInscripcion;
    private EstadoInscripcion estadoInscripcion;
    private String estadoPago;
    private String codigoVerificacion;
}
