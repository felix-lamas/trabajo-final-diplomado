package bo.uajms.eventos.modulos.sesiones.controladores;

import bo.uajms.eventos.modulos.sesiones.dtos.GoogleMapsCoordinatesResponse;
import bo.uajms.eventos.modulos.sesiones.dtos.GoogleMapsLinkRequest;
import bo.uajms.eventos.modulos.sesiones.servicios.GoogleMapsLinkResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/maps")
@RequiredArgsConstructor
@Tag(name = "Ubicación Google Maps", description = "Resolución controlada de enlaces compartidos de Google Maps")
@SecurityRequirement(name = "bearerAuth")
public class GoogleMapsController {
    private final GoogleMapsLinkResolver resolver;

    @PostMapping("/coordenadas-compartidas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','ORGANIZADOR')")
    @Operation(summary = "Obtener coordenadas desde un enlace compartido de Google Maps")
    public GoogleMapsCoordinatesResponse obtenerCoordenadas(@Valid @RequestBody GoogleMapsLinkRequest request) {
        return resolver.resolve(request.url());
    }
}
