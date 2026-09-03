package bo.uajms.eventos.modulos.facultades.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActualizarFacultadRequest {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
    private String nombre;
    
    private String descripcion;
    
    @NotBlank(message = "El estado es obligatorio")
    private String estado;
}
