package bo.uajms.eventos.modulos.encuestas.controladores;

import bo.uajms.eventos.modulos.encuestas.dtos.EncuestaResponse;
import bo.uajms.eventos.modulos.encuestas.dtos.EstadisticasEncuestaResponse;
import bo.uajms.eventos.modulos.encuestas.dtos.ResponderEncuestaRequest;
import bo.uajms.eventos.modulos.encuestas.servicios.EncuestaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/encuestas", "/api/v1/encuestas"})
@RequiredArgsConstructor
@Tag(name = "Encuestas", description = "Encuestas de satisfaccion por evento")
@SecurityRequirement(name = "bearerAuth")
public class EncuestaController {

    private final EncuestaService encuestaService;

    @PostMapping("/responder")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Responder encuesta de satisfaccion")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encuesta registrada"),
            @ApiResponse(responseCode = "400", description = "Evento no finalizado, sin asistencia o encuesta duplicada"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    public ResponseEntity<EncuestaResponse> responder(@Valid @RequestBody ResponderEncuestaRequest request) {
        return ResponseEntity.ok(encuestaService.responder(request));
    }

    @GetMapping("/evento/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Listar respuestas de encuesta por evento")
    public ResponseEntity<List<EncuestaResponse>> listarPorEvento(@PathVariable UUID id) {
        return ResponseEntity.ok(encuestaService.listarPorEvento(id));
    }

    @GetMapping("/estadisticas/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Obtener estadisticas de satisfaccion por evento")
    public ResponseEntity<EstadisticasEncuestaResponse> estadisticas(@PathVariable UUID id) {
        return ResponseEntity.ok(encuestaService.obtenerEstadisticas(id));
    }
}
