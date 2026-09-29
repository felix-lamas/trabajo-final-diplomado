package bo.uajms.eventos.modulos.usuarios.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Datos editables del perfil propio")
public class ActualizarPerfilRequest {
    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 50, message = "Los nombres no pueden superar 50 caracteres")
    @Schema(example = "Maria Elena")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 50, message = "Los apellidos no pueden superar 50 caracteres")
    @Schema(example = "Flores Rojas")
    private String apellidos;

    @NotBlank(message = "El telefono es obligatorio")
    @Pattern(regexp = "[0-9+ -]{7,20}", message = "El telefono tiene un formato invalido")
    @Schema(example = "+591 72900000")
    private String celular;
}
