package bo.uajms.eventos.modulos.pagos.dtos;

import lombok.Data;
import jakarta.validation.constraints.Size;

@Data
public class ValidarPagoRequest {
    @Size(max = 1000)
    private String observacion;
}
