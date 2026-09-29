package bo.uajms.eventos.modulos.usuarios.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Resultado de registro pendiente de verificacion de correo")
public class RegistroResponse {
    @Schema(example = "usuario@demo.local")
    private String correoElectronico;
    @Schema(example = "false")
    private boolean correoVerificado;
    @Schema(example = "Registro recibido. Verifique su correo antes de iniciar sesion.")
    private String mensaje;
}
