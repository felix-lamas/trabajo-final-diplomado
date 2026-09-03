package bo.uajms.eventos.modulos.control_acceso.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidarQrRequest {
    private String tokenQr;
}
