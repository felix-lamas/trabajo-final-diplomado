package bo.uajms.eventos.modulos.usuarios.dtos;

import bo.uajms.eventos.modulos.usuarios.entidades.Usuario.TipoUsuario;
import bo.uajms.eventos.modulos.usuarios.validadores.ContrasenaSegura;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistroUsuarioRequest {

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 50, message = "Los nombres no pueden superar 50 caracteres")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 50, message = "Los apellidos no pueden superar 50 caracteres")
    private String apellidos;

    @NotBlank(message = "El correo electronico es obligatorio")
    @Email(message = "El formato del correo electronico es invalido")
    @Size(max = 100, message = "El correo no puede superar 100 caracteres")
    private String correoElectronico;

    @NotBlank(message = "El CI es obligatorio")
    @Pattern(regexp = "[A-Za-z0-9-]{4,20}", message = "El CI tiene un formato invalido")
    private String ci;

    @Pattern(regexp = "(?:|[A-Za-z0-9-]{4,20})", message = "El RU tiene un formato invalido")
    private String ru;

    @NotBlank(message = "El telefono es obligatorio")
    @Pattern(regexp = "[0-9+ -]{7,20}", message = "El telefono tiene un formato invalido")
    private String celular;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
    @ContrasenaSegura
    private String contrasena;

    @NotBlank(message = "La confirmacion de contrasena es obligatoria")
    private String confirmacionContrasena;

    @NotNull(message = "El tipo de usuario es obligatorio")
    private TipoUsuario tipoUsuario;
}
