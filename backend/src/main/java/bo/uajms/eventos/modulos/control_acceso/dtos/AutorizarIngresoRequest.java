package bo.uajms.eventos.modulos.control_acceso.dtos;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutorizarIngresoRequest {
    private UUID credencialId;
    private String observacion;
}
