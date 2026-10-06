package bo.uajms.eventos.core.controladores;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Salud", description = "Disponibilidad de la API")
public class SaludController {

    @GetMapping(value = "/salud", produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirements
    @Operation(summary = "Verificar disponibilidad de la API",
            description = "Verifica que la API esté disponible.")
    @ApiResponse(responseCode = "200", description = "API disponible")
    public ResponseEntity<Map<String, String>> salud() {
        return ResponseEntity.ok(Map.of("estado", "UP"));
    }
}
