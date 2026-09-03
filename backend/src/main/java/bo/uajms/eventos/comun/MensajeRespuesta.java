package bo.uajms.eventos.comun;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MensajeRespuesta {
    private String mensaje;
    private String detalle;
}
