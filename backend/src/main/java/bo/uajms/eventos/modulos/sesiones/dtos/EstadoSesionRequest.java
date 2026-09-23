package bo.uajms.eventos.modulos.sesiones.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EstadoSesionRequest {
    @NotNull private Boolean activa;
}
