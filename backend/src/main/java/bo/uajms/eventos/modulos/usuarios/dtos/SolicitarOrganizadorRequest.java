package bo.uajms.eventos.modulos.usuarios.dtos;

import bo.uajms.eventos.modulos.usuarios.entidades.TipoEventoSolicitud;
import jakarta.validation.constraints.*;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Data
public class SolicitarOrganizadorRequest {
    @Schema(description = "Motivo obligatorio; al menos 30 caracteres sin espacios exteriores", example = "Deseo organizar talleres educativos para la comunidad universitaria")
    @NotBlank(message = "El motivo es obligatorio")
    @Size(min = 30, max = 1000, message = "El motivo debe contener entre 30 y 1000 caracteres")
    private String motivoSolicitud;
    @Schema(description = "Seleccione uno o mas codigos del catalogo, sin duplicados; no se aceptan categorias arbitrarias")
    @NotEmpty(message = "Seleccione al menos un tipo de evento")
    @Size(max = 6, message = "Seleccione hasta seis tipos de eventos")
    private List<@NotNull TipoEventoSolicitud> tiposEventos;
    @Schema(description = "Informacion complementaria opcional, maximo 1000 caracteres")
    @Size(max = 1000, message = "La informacion adicional admite hasta 1000 caracteres")
    private String informacionAdicional;
}
