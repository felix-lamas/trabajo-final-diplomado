package bo.uajms.eventos.modulos.usuarios.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Opcion del catalogo controlado para solicitudes de organizador")
public record TipoEventoSolicitudResponse(
        @Schema(example = "CURSOS_TALLERES") String codigo,
        @Schema(example = "Cursos y talleres") String nombre) {
}
