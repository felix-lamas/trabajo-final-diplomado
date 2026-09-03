package bo.uajms.eventos.modulos.inscripciones.controladores;

import bo.uajms.eventos.modulos.inscripciones.dtos.CrearInscripcionRequest;
import bo.uajms.eventos.modulos.inscripciones.dtos.DetalleInscripcionResponse;
import bo.uajms.eventos.modulos.inscripciones.dtos.InscripcionResponse;
import bo.uajms.eventos.modulos.inscripciones.servicios.InscripcionService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Inscripciones", description = "Gestión de registro de participantes en eventos")
@SecurityRequirement(name = "bearerAuth")
public class InscripcionController {

    private final InscripcionService inscripcionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ESTUDIANTE', 'PARTICIPANTE_EXTERNO')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Inscribirse a un evento")
    public ResponseEntity<DetalleInscripcionResponse> inscribir(@Valid @RequestBody CrearInscripcionRequest request) {
        return new ResponseEntity<>(inscripcionService.inscribir(request), HttpStatus.CREATED);
    }

    @GetMapping("/mis-inscripciones")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ESTUDIANTE', 'PARTICIPANTE_EXTERNO')")
    @Operation(summary = "Listar inscripciones del usuario autenticado")
    public ResponseEntity<List<InscripcionResponse>> listarMisInscripciones() {
        return ResponseEntity.ok(inscripcionService.listarMisInscripciones());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'ESTUDIANTE', 'PARTICIPANTE_EXTERNO')")
    @Operation(summary = "Obtener detalle de una inscripción")
    public ResponseEntity<DetalleInscripcionResponse> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(inscripcionService.obtenerPorId(id));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ESTUDIANTE', 'PARTICIPANTE_EXTERNO')")
    @Operation(summary = "Cancelar inscripción propia")
    public ResponseEntity<Void> cancelar(@PathVariable UUID id) {
        inscripcionService.cancelar(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/evento/{eventoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Listar todos los inscritos de un evento (Admin/Organizador)")
    public ResponseEntity<List<InscripcionResponse>> listarInscritosEvento(@PathVariable UUID eventoId) {
        return ResponseEntity.ok(inscripcionService.listarInscritosEvento(eventoId));
    }
}
