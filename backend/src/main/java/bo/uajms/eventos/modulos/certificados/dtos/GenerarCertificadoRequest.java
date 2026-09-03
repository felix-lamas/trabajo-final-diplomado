package bo.uajms.eventos.modulos.certificados.dtos;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerarCertificadoRequest {
    private UUID inscripcionId;
}
