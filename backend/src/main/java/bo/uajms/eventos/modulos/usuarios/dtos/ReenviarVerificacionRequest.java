package bo.uajms.eventos.modulos.usuarios.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Solicitud no enumerativa de reenvio de verificacion")
public class ReenviarVerificacionRequest {
    @NotBlank(message = "El correo electronico es obligatorio")
    @Email(message = "El formato del correo electronico es invalido")
    @Schema(example = "usuario@demo.local")
    private String correoElectronico;
}
