package bo.uajms.eventos.modulos.eventos.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EventoCancelacionRequest {
    @NotBlank @Size(max = 1000) private String motivo;
}
