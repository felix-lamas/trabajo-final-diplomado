package bo.uajms.eventos.modulos.facultades.dtos;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class FacultadResponse {
    private UUID id;
    private String nombre;
    private String descripcion;
    private String estado;
    private Long cantidadCarreras;
}
