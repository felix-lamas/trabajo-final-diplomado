package bo.uajms.eventos.modulos.usuarios.dtos;
import jakarta.validation.constraints.*;
import lombok.Data;
import bo.uajms.eventos.modulos.usuarios.validadores.ContrasenaSegura;
@Data
public class AceptarInvitacionAdministradorRequest {
    @lombok.ToString.Exclude
    @com.fasterxml.jackson.annotation.JsonProperty(access=com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    @NotBlank @Size(max=200) private String token;
    @NotBlank @Size(max=50) private String nombres;
    @NotBlank @Size(max=50) private String apellidos;
    @NotBlank @Pattern(regexp="[A-Za-z0-9-]{4,20}") private String ci;
    @NotBlank @Pattern(regexp="[0-9+ -]{7,20}") private String celular;
    @lombok.ToString.Exclude
    @com.fasterxml.jackson.annotation.JsonProperty(access=com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    @NotBlank @Size(min=8, max=100) @ContrasenaSegura private String contrasena;
    @lombok.ToString.Exclude
    @com.fasterxml.jackson.annotation.JsonProperty(access=com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    @NotBlank @Size(max=100) private String confirmacionContrasena;
}
