package bo.uajms.eventos.modulos.eventos.controladores;

import bo.uajms.eventos.modulos.eventos.dtos.*;
import bo.uajms.eventos.modulos.eventos.servicios.EventoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import bo.uajms.eventos.modulos.eventos.entidades.Modalidad;
import bo.uajms.eventos.modulos.eventos.entidades.TipoInscripcion;

@RestController
@RequestMapping("/api/v1/eventos")
@RequiredArgsConstructor
@Tag(name = "Eventos", description = "Gestión central de eventos universitarios")
@SecurityRequirement(name = "bearerAuth")
public class EventoController {

    private final EventoService eventoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'USUARIO')")
    @Operation(summary = "Listar eventos segÃºn el alcance del usuario autenticado")
    public ResponseEntity<List<EventoResponse>> listar() {
        return ResponseEntity.ok(eventoService.listarTodos());
    }

    @GetMapping("/publicados")
    @SecurityRequirements
    @Operation(summary = "Listar solo eventos publicados (Acceso Público)")
    public ResponseEntity<List<EventoResponse>> listarPublicados() {
        return ResponseEntity.ok(eventoService.listarPublicados());
    }

    @GetMapping("/publicados/buscar")
    @SecurityRequirements
    @Operation(summary = "Buscar eventos publicados")
    public ResponseEntity<List<EventoResponse>> buscarPublicados(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) UUID categoriaId,
            @RequestParam(required = false) TipoInscripcion tipo,
            @RequestParam(required = false) Modalidad modalidad) {
        return ResponseEntity.ok(eventoService.buscarPublicados(texto, categoriaId, tipo, modalidad));
    }

    @GetMapping("/revision")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<EventoResponse>> listarEnRevision() {
        return ResponseEntity.ok(eventoService.listarEnRevision());
    }

    @GetMapping("/{id}")
    @SecurityRequirements
    @Operation(summary = "Obtener detalle completo de un evento")
    public ResponseEntity<EventoDetalleResponse> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(eventoService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZADOR')")
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
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Publicar evento como parte de la revisiÃ³n administrativa")
    public ResponseEntity<Void> publicar(@PathVariable UUID id) {
        eventoService.publicar(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/enviar-revision")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    public ResponseEntity<Void> enviarARevision(@PathVariable UUID id) {
        eventoService.enviarARevision(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> rechazar(@PathVariable UUID id,
                                          @Valid @RequestBody EventoRechazoRequest request) {
        eventoService.rechazar(id, request.getMotivo());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/volver-borrador")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    public ResponseEntity<Void> volverABorrador(@PathVariable UUID id) {
        eventoService.volverABorrador(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Cambiar estado a CANCELADO")
    public ResponseEntity<Void> cancelar(@PathVariable UUID id,
                                          @Valid @RequestBody EventoCancelacionRequest request) {
        eventoService.cancelar(id, request.getMotivo());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/finalizar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Cambiar estado a FINALIZADO")
    public ResponseEntity<Void> finalizar(@PathVariable UUID id) {
        eventoService.finalizar(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/categoria/{id}")
    @SecurityRequirements
    @Operation(summary = "Filtrar eventos por categoría")
    public ResponseEntity<List<EventoResponse>> listarPorCategoria(@PathVariable UUID id) {
        return ResponseEntity.ok(eventoService.listarPorCategoria(id));
    }
}
