package bo.uajms.eventos.modulos.categorias.dtos;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class CategoriaEventoResponse {
    private UUID id;
    private String nombre;
    private String descripcion;
    private String estado;
}
