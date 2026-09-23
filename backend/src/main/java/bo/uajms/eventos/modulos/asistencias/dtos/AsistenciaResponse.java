package bo.uajms.eventos.modulos.asistencias.dtos;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsistenciaResponse {
    private UUID id;
    private String nombreParticipante;
    private String documentoIdentidad;
    private String codigoParticipante;
    private String evento;
    private UUID sesionEventoId;
    private String sesion;
    private LocalDateTime fechaHoraRegistro;
    private String registradoPor;
    private BigDecimal distanciaMetros;
    private BigDecimal precisionGpsMetros;
    private String resultadoValidacion;
    private String observacion;
}
