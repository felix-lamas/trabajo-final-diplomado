package bo.uajms.eventos.modulos.inscripciones.controladores;

import bo.uajms.eventos.modulos.inscripciones.dtos.CrearInscripcionRequest;
import bo.uajms.eventos.modulos.inscripciones.dtos.DetalleInscripcionResponse;
import bo.uajms.eventos.modulos.inscripciones.dtos.InscripcionResponse;
import bo.uajms.eventos.modulos.inscripciones.servicios.InscripcionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inscripciones")
@RequiredArgsConstructor
@Tag(name = "Inscripciones", description = "Registro de participantes en eventos")
@SecurityRequirement(name = "bearerAuth")
public class InscripcionController {

    private final InscripcionService inscripcionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USUARIO')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Inscribirse a un evento",
            description = "Crea una inscripcion para el usuario autenticado; valida publicacion, cupo y duplicados.")
    @ApiResponse(responseCode = "201", description = "Inscripcion creada", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "404", description = "Evento inexistente")
    @ApiResponse(responseCode = "409", description = "El usuario ya esta inscrito")
    public ResponseEntity<DetalleInscripcionResponse> inscribir(@Valid @RequestBody CrearInscripcionRequest request) {
        return new ResponseEntity<>(inscripcionService.inscribir(request), HttpStatus.CREATED);
    }

    @GetMapping("/mis-inscripciones")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USUARIO')")
    @Operation(summary = "Listar mis inscripciones",
            description = "Devuelve exclusivamente las inscripciones del usuario autenticado.")
    public ResponseEntity<List<InscripcionResponse>> listarMisInscripciones() {
        return ResponseEntity.ok(inscripcionService.listarMisInscripciones());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'USUARIO')")
    @Operation(summary = "Obtener inscripcion",
            description = "USUARIO consulta inscripciones propias; ORGANIZADOR, inscripciones de eventos propios; ADMINISTRADOR, cualquier inscripcion.")
    @ApiResponse(responseCode = "404", description = "Inscripcion inexistente o fuera del alcance")
    public ResponseEntity<DetalleInscripcionResponse> obtenerPorId(
            @Parameter(description = "Identificador de la inscripcion") @PathVariable UUID id) {
        return ResponseEntity.ok(inscripcionService.obtenerPorId(id));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USUARIO')")
    @Operation(summary = "Cancelar inscripcion",
            description = "USUARIO solo cancela una inscripcion propia; ADMINISTRADOR conserva alcance global.")
    @ApiResponse(responseCode = "404", description = "Inscripcion inexistente o fuera del alcance")
    public ResponseEntity<Void> cancelar(
            @Parameter(description = "Identificador de la inscripcion") @PathVariable UUID id) {
        inscripcionService.cancelar(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/evento/{eventoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Listar inscritos de un evento",
            description = "ORGANIZADOR solo consulta inscritos de eventos propios; ADMINISTRADOR conserva alcance global.")
    @ApiResponse(responseCode = "404", description = "Evento inexistente o fuera del alcance")
    public ResponseEntity<List<InscripcionResponse>> listarInscritosEvento(
            @Parameter(description = "Identificador del evento") @PathVariable UUID eventoId) {
        return ResponseEntity.ok(inscripcionService.listarInscritosEvento(eventoId));
    }
}
