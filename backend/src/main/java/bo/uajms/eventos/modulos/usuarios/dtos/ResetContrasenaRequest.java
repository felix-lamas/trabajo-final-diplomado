package bo.uajms.eventos.modulos.usuarios.dtos;

import bo.uajms.eventos.modulos.usuarios.validadores.ContrasenaSegura;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Restablecimiento de contrasena mediante token de un solo uso")
public class ResetContrasenaRequest {
    @NotBlank(message = "El token es obligatorio")
    @Schema(example = "f12a4b6c8d...")
    private String token;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
    @ContrasenaSegura
    @Schema(example = "Nueva2026!")
    private String nuevaContrasena;

    @NotBlank(message = "La confirmación es obligatoria")
    @Schema(example = "Nueva2026!")
    private String confirmacion;
}
