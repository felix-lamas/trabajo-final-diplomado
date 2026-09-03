package bo.uajms.eventos.modulos.reportes.dtos;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardEjecutivoResponse {
    private long totalEventos;
    private long totalUsuarios;
    private long totalParticipantes;
    private long totalInscripciones;
    private long totalCertificados;
    private BigDecimal ingresosGenerados;
    private Double nivelSatisfaccion;
    private Double participacionEncuestas;
    private List<Map<String, Object>> promedioSatisfaccionPorEvento;
}
