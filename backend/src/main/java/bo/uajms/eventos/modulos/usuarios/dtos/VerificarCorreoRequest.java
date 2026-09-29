package bo.uajms.eventos.modulos.usuarios.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Token de un solo uso recibido por correo")
public class VerificarCorreoRequest {
    @NotBlank(message = "El token es obligatorio")
    @Schema(example = "token-de-verificacion-recibido-por-correo")
    private String token;
}
