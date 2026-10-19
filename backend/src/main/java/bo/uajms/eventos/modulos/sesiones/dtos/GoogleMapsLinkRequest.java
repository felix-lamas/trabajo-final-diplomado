package bo.uajms.eventos.modulos.sesiones.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GoogleMapsLinkRequest(
        @NotBlank @Size(max = 2048) String url
) {}
