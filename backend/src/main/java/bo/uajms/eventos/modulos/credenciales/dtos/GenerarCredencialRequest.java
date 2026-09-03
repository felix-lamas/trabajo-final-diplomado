package bo.uajms.eventos.modulos.credenciales.dtos;

import lombok.Data;
import java.util.UUID;

@Data
public class GenerarCredencialRequest {
    private UUID inscripcionId;
}
