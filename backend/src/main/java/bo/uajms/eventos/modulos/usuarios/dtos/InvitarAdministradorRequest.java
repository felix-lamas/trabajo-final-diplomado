package bo.uajms.eventos.modulos.usuarios.dtos;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class InvitarAdministradorRequest {
    @NotBlank @Size(max=50) private String nombres;
    @NotBlank @Size(max=50) private String apellidos;
    @NotBlank @Email @Size(max=100) private String correo;
    @NotBlank @Pattern(regexp="[A-Za-z0-9-]{4,20}") private String ci;
    @NotBlank @Pattern(regexp="[0-9+ -]{7,20}") private String celular;
}
