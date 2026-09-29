package bo.uajms.eventos.modulos.usuarios.dtos;

import bo.uajms.eventos.modulos.usuarios.entidades.Usuario.TipoUsuario;
import bo.uajms.eventos.modulos.usuarios.validadores.ContrasenaSegura;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Datos para registrar una cuenta con rol inicial USUARIO")
public class RegistroUsuarioRequest {

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 50, message = "Los nombres no pueden superar 50 caracteres")
    @Schema(example = "Maria Elena")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 50, message = "Los apellidos no pueden superar 50 caracteres")
    @Schema(example = "Flores Rojas")
    private String apellidos;

    @NotBlank(message = "El correo electronico es obligatorio")
    @Email(message = "El formato del correo electronico es invalido")
    @Size(max = 100, message = "El correo no puede superar 100 caracteres")
    @Schema(example = "maria.flores@uajms.edu.bo")
    private String correoElectronico;

    @NotBlank(message = "El CI es obligatorio")
    @Pattern(regexp = "[A-Za-z0-9-]{4,20}", message = "El CI tiene un formato invalido")
    @Schema(example = "1234567")
    private String ci;

    @Pattern(regexp = "(?:|[A-Za-z0-9-]{4,20})", message = "El RU tiene un formato invalido")
    @Schema(description = "Obligatorio para tipo UAJMS", example = "RU-20260001")
    private String ru;

    @NotBlank(message = "El telefono es obligatorio")
    @Pattern(regexp = "[0-9+ -]{7,20}", message = "El telefono tiene un formato invalido")
    @Schema(example = "+591 72900000")
    private String celular;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
    @ContrasenaSegura
    @Schema(example = "Segura2026!")
    private String contrasena;

    @NotBlank(message = "La confirmacion de contrasena es obligatoria")
    @Schema(example = "Segura2026!")
    private String confirmacionContrasena;

    @NotNull(message = "El tipo de usuario es obligatorio")
    @Schema(example = "UAJMS")
    private TipoUsuario tipoUsuario;
}
