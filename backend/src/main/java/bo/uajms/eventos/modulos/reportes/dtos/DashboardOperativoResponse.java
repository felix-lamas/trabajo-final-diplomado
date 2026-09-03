package bo.uajms.eventos.modulos.reportes.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardOperativoResponse {
    private long eventosActivos;
    private long eventosFinalizados;
    private long pagosPendientes;
    private long pagosValidados;
    private long qrUtilizados;
    private long asistenciasRegistradas;
}
