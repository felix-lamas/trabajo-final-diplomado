package bo.uajms.eventos.modulos.carreras.dtos;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class CarreraResponse {
    private UUID id;
    private String nombre;
    private String descripcion;
    private String estado;
    private UUID facultadId;
    private String facultadNombre;
}
