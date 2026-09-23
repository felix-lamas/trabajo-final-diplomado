package bo.uajms.eventos.modulos.sesiones.controladores;

import bo.uajms.eventos.modulos.sesiones.dtos.*;
import bo.uajms.eventos.modulos.sesiones.servicios.SesionEventoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SesionEventoController {
    private final SesionEventoService sesionService;

    @PostMapping("/api/v1/eventos/{eventoId}/sesiones")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','ORGANIZADOR')")
    public ResponseEntity<SesionEventoResponse> crear(@PathVariable UUID eventoId,
                                                       @Valid @RequestBody SesionEventoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sesionService.crear(eventoId, request));
    }

    @GetMapping("/api/v1/eventos/{eventoId}/sesiones")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','ORGANIZADOR','USUARIO')")
    public ResponseEntity<List<SesionEventoResponse>> listar(@PathVariable UUID eventoId) {
        return ResponseEntity.ok(sesionService.listarPorEvento(eventoId));
    }

    @GetMapping("/api/v1/sesiones/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','ORGANIZADOR','USUARIO')")
    public ResponseEntity<SesionEventoResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(sesionService.obtener(id));
    }

    @PutMapping("/api/v1/sesiones/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','ORGANIZADOR')")
    public ResponseEntity<SesionEventoResponse> actualizar(@PathVariable UUID id,
                                                            @Valid @RequestBody SesionEventoRequest request) {
        return ResponseEntity.ok(sesionService.actualizar(id, request));
    }

    @PatchMapping("/api/v1/sesiones/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','ORGANIZADOR')")
    public ResponseEntity<SesionEventoResponse> cambiarEstado(@PathVariable UUID id,
                                                               @Valid @RequestBody EstadoSesionRequest request) {
        return ResponseEntity.ok(sesionService.cambiarEstado(id, request.getActiva()));
    }
}
