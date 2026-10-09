package bo.uajms.eventos.modulos.usuarios.dtos;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class ConsultarInvitacionAdministradorRequest {
    @lombok.ToString.Exclude
    @com.fasterxml.jackson.annotation.JsonProperty(access=com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    @NotBlank @Size(max=200) private String token;
}
