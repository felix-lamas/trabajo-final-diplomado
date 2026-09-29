package bo.uajms.eventos.modulos.usuarios.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Motivo administrativo para rechazar una solicitud pendiente")
public class RechazarSolicitudOrganizadorRequest {
    @NotBlank(message = "El motivo de rechazo es obligatorio")
    @Size(max = 500, message = "El motivo no puede superar 500 caracteres")
    @Schema(example = "Debe completar la informacion institucional requerida")
    private String motivo;
}
