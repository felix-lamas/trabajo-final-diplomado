package bo.uajms.eventos.modulos.sesiones.controladores;

import bo.uajms.eventos.modulos.sesiones.dtos.*;
import bo.uajms.eventos.modulos.sesiones.servicios.SesionEventoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Sesiones de evento", description = "Programacion y estado de sesiones de asistencia")
@SecurityRequirement(name = "bearerAuth")
public class SesionEventoController {
    private final SesionEventoService sesionService;

    @PostMapping("/api/v1/eventos/{eventoId}/sesiones")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','ORGANIZADOR')")
    @Operation(summary = "Crear sesion de evento",
            description = "El ORGANIZADOR solo puede crear sesiones en eventos propios y estados administrables.")
    @ApiResponse(responseCode = "201", description = "Sesion creada", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "404", description = "Evento inexistente o fuera del alcance")
    public ResponseEntity<SesionEventoResponse> crear(
                                                       @Parameter(description = "Identificador del evento") @PathVariable UUID eventoId,
                                                       @Valid @RequestBody SesionEventoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sesionService.crear(eventoId, request));
    }

    @GetMapping("/api/v1/eventos/{eventoId}/sesiones")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','ORGANIZADOR','USUARIO')")
    @Operation(summary = "Listar sesiones de un evento",
            description = "ADMINISTRADOR y ORGANIZADOR respetan el alcance del evento; USUARIO requiere acceso valido al evento.")
    @ApiResponse(responseCode = "404", description = "Evento inexistente o fuera del alcance")
    public ResponseEntity<List<SesionEventoResponse>> listar(
            @Parameter(description = "Identificador del evento") @PathVariable UUID eventoId) {
        return ResponseEntity.ok(sesionService.listarPorEvento(eventoId));
    }

    @GetMapping("/api/v1/sesiones/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','ORGANIZADOR','USUARIO')")
    @Operation(summary = "Obtener sesion",
            description = "Consulta una sesion aplicando el alcance del evento para el usuario autenticado.")
    @ApiResponse(responseCode = "404", description = "Sesion inexistente o fuera del alcance")
    public ResponseEntity<SesionEventoResponse> obtener(
            @Parameter(description = "Identificador de la sesion") @PathVariable UUID id) {
        return ResponseEntity.ok(sesionService.obtener(id));
    }

    @PutMapping("/api/v1/sesiones/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','ORGANIZADOR')")
    @Operation(summary = "Actualizar sesion",
            description = "El ORGANIZADOR solo puede modificar sesiones de eventos propios y no historicas.")
    @ApiResponse(responseCode = "404", description = "Sesion inexistente o fuera del alcance")
    public ResponseEntity<SesionEventoResponse> actualizar(
                                                            @Parameter(description = "Identificador de la sesion") @PathVariable UUID id,
                                                            @Valid @RequestBody SesionEventoRequest request) {
        return ResponseEntity.ok(sesionService.actualizar(id, request));
    }

    @PatchMapping("/api/v1/sesiones/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','ORGANIZADOR')")
    @Operation(summary = "Cambiar estado de sesion",
            description = "Activa o desactiva una sesion. El ORGANIZADOR solo puede operar sobre eventos propios.")
    @ApiResponse(responseCode = "404", description = "Sesion inexistente o fuera del alcance")
    public ResponseEntity<SesionEventoResponse> cambiarEstado(
                                                               @Parameter(description = "Identificador de la sesion") @PathVariable UUID id,
                                                               @Valid @RequestBody EstadoSesionRequest request) {
        return ResponseEntity.ok(sesionService.cambiarEstado(id, request.getActiva()));
    }
}
