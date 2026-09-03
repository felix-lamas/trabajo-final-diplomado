package bo.uajms.eventos.modulos.reportes.dtos;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardAcademicoResponse {
    private List<Map<String, Object>> participacionPorFacultad;
    private List<Map<String, Object>> participacionPorCarrera;
    private List<Map<String, Object>> participacionPorCategoria;
    private List<Map<String, Object>> participacionPorPeriodo;
}
