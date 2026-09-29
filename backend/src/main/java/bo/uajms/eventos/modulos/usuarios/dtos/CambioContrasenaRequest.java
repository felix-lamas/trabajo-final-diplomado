package bo.uajms.eventos.modulos.usuarios.dtos;

import bo.uajms.eventos.modulos.usuarios.validadores.ContrasenaSegura;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Cambio autenticado de contrasena del usuario actual")
public class CambioContrasenaRequest {
    @NotBlank(message = "La contraseña actual es obligatoria")
    @Schema(example = "Actual2026!")
    private String contrasenaActual;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
    @ContrasenaSegura
    @Schema(example = "NuevaSegura2026!")
    private String nuevaContrasena;

    @NotBlank(message = "La confirmación es obligatoria")
    @Schema(example = "NuevaSegura2026!")
    private String confirmacion;
}
