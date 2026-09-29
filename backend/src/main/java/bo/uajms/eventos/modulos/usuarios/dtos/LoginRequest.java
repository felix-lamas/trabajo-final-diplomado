package bo.uajms.eventos.modulos.usuarios.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Credenciales de acceso")
public class LoginRequest {
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico es inválido")
    @Schema(example = "usuario@demo.local")
    private String correoElectronico;

    @NotBlank(message = "La contraseña es obligatoria")
    @Schema(example = "Demo2026!")
    private String contrasena;
}
