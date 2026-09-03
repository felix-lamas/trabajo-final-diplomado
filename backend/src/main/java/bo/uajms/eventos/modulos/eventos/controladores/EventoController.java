package bo.uajms.eventos.modulos.eventos.controladores;

import bo.uajms.eventos.modulos.eventos.dtos.*;
import bo.uajms.eventos.modulos.eventos.servicios.EventoService;
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
@RequestMapping("/api/v1/eventos")
@RequiredArgsConstructor
@Tag(name = "Eventos", description = "Gestión central de eventos universitarios")
@SecurityRequirement(name = "bearerAuth")
public class EventoController {

    private final EventoService eventoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'ESTUDIANTE', 'PARTICIPANTE_EXTERNO')")
    @Operation(summary = "Listar todos los eventos")
    public ResponseEntity<List<EventoResponse>> listar() {
        return ResponseEntity.ok(eventoService.listarTodos());
    }

    @GetMapping("/publicados")
    @Operation(summary = "Listar solo eventos publicados (Acceso Público)")
    public ResponseEntity<List<EventoResponse>> listarPublicados() {
        return ResponseEntity.ok(eventoService.listarPublicados());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle completo de un evento")
    public ResponseEntity<EventoDetalleResponse> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(eventoService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear nuevo evento")
    public ResponseEntity<EventoDetalleResponse> crear(@Valid @RequestBody CrearEventoRequest request) {
        return new ResponseEntity<>(eventoService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Actualizar evento en borrador")
    public ResponseEntity<EventoDetalleResponse> actualizar(@PathVariable UUID id, @Valid @RequestBody ActualizarEventoRequest request) {
        return ResponseEntity.ok(eventoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar evento (Solo si está en BORRADOR)")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        eventoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/publicar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Cambiar estado a PUBLICADO")
    public ResponseEntity<Void> publicar(@PathVariable UUID id) {
        eventoService.publicar(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Cambiar estado a CANCELADO")
    public ResponseEntity<Void> cancelar(@PathVariable UUID id) {
        eventoService.cancelar(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Cambiar estado a FINALIZADO")
    public ResponseEntity<Void> finalizar(@PathVariable UUID id) {
        eventoService.finalizar(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/categoria/{id}")
    @Operation(summary = "Filtrar eventos por categoría")
    public ResponseEntity<List<EventoResponse>> listarPorCategoria(@PathVariable UUID id) {
        return ResponseEntity.ok(eventoService.listarPorCategoria(id));
    }
}
