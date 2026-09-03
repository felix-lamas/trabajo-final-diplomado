package bo.uajms.eventos.modulos.carreras.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.UUID;

@Data
public class CrearCarreraRequest {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
    private String nombre;
    
    private String descripcion;
    
    @NotNull(message = "El ID de facultad es obligatorio")
    private UUID facultadId;
}
