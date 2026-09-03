package bo.uajms.eventos.modulos.reportes.dtos;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteDataResponse {
    private String tipoReporte;
    private long totalRegistros;
    private List<Map<String, Object>> filas;
}
