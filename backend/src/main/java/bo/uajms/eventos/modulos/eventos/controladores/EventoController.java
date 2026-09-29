package bo.uajms.eventos.modulos.eventos.controladores;

import bo.uajms.eventos.modulos.eventos.dtos.*;
import bo.uajms.eventos.modulos.eventos.entidades.Modalidad;
import bo.uajms.eventos.modulos.eventos.entidades.TipoInscripcion;
import bo.uajms.eventos.modulos.eventos.servicios.EventoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

@RestController
@RequestMapping("/api/v1/eventos")
@RequiredArgsConstructor
@Tag(name = "Eventos", description = "Creacion, revision, publicacion y consulta de eventos universitarios")
@SecurityRequirement(name = "bearerAuth")
public class EventoController {

    private final EventoService eventoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'USUARIO')")
    @Operation(summary = "Listar eventos segun alcance",
            description = "ADMINISTRADOR recibe todos; ORGANIZADOR solo eventos propios; USUARIO solo eventos PUBLICADO.")
    public ResponseEntity<List<EventoResponse>> listar() {
        return ResponseEntity.ok(eventoService.listarTodos());
    }

    @GetMapping("/publicados")
    @SecurityRequirements
    @Operation(summary = "Listar eventos publicados",
            description = "Catalogo publico de eventos en estado PUBLICADO.")
    public ResponseEntity<List<EventoResponse>> listarPublicados() {
        return ResponseEntity.ok(eventoService.listarPublicados());
    }

    @GetMapping("/publicados/buscar")
    @SecurityRequirements
    @Operation(summary = "Buscar eventos publicados",
            description = "Filtra el catalogo publico mediante criterios opcionales combinables.")
    public ResponseEntity<List<EventoResponse>> buscarPublicados(
            @Parameter(description = "Texto contenido en titulo o descripcion", example = "innovacion")
            @RequestParam(required = false) String texto,
            @Parameter(description = "Identificador de categoria")
            @RequestParam(required = false) UUID categoriaId,
            @Parameter(description = "Tipo de inscripcion")
            @RequestParam(required = false) TipoInscripcion tipo,
            @Parameter(description = "Modalidad del evento")
            @RequestParam(required = false) Modalidad modalidad) {
        return ResponseEntity.ok(eventoService.buscarPublicados(texto, categoriaId, tipo, modalidad));
    }

    @GetMapping("/revision")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar eventos en revision",
            description = "Devuelve eventos EN_REVISION para resolucion administrativa.")
    public ResponseEntity<List<EventoResponse>> listarEnRevision() {
        return ResponseEntity.ok(eventoService.listarEnRevision());
    }

    @GetMapping("/{id}")
    @SecurityRequirements
    @Operation(summary = "Obtener detalle de evento",
            description = "Sin JWT solo expone eventos PUBLICADO. ADMINISTRADOR ve cualquier evento y ORGANIZADOR puede ver eventos propios no publicados.")
    @ApiResponse(responseCode = "404", description = "Evento inexistente o no visible para el solicitante")
    public ResponseEntity<EventoDetalleResponse> obtenerPorId(
            @Parameter(description = "Identificador del evento") @PathVariable UUID id) {
        return ResponseEntity.ok(eventoService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZADOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear evento",
            description = "Crea un evento BORRADOR propiedad del ORGANIZADOR autenticado; no publica directamente.")
    @ApiResponse(responseCode = "201", description = "Evento creado en BORRADOR", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "404", description = "Categoria activa inexistente")
    public ResponseEntity<EventoDetalleResponse> crear(@Valid @RequestBody CrearEventoRequest request) {
        return new ResponseEntity<>(eventoService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Actualizar evento",
            description = "Solo admite BORRADOR o RECHAZADO. ORGANIZADOR requiere ownership del evento.")
    @ApiResponse(responseCode = "404", description = "Evento inexistente o fuera del alcance")
    public ResponseEntity<EventoDetalleResponse> actualizar(
            @Parameter(description = "Identificador del evento") @PathVariable UUID id,
            @Valid @RequestBody ActualizarEventoRequest request) {
        return ResponseEntity.ok(eventoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar evento",
            description = "Elimina un evento solo en BORRADOR. ORGANIZADOR requiere ownership.")
    @ApiResponse(responseCode = "204", description = "Evento eliminado")
    @ApiResponse(responseCode = "404", description = "Evento inexistente o fuera del alcance")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "Identificador del evento") @PathVariable UUID id) {
        eventoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/publicar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Publicar evento",
            description = "Transicion administrativa EN_REVISION a PUBLICADO.")
    @ApiResponse(responseCode = "404", description = "Evento inexistente")
    public ResponseEntity<Void> publicar(
            @Parameter(description = "Identificador del evento") @PathVariable UUID id) {
        eventoService.publicar(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/enviar-revision")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    @Operation(summary = "Enviar evento a revision",
            description = "Transicion BORRADOR a EN_REVISION para un evento propio y completo.")
    @ApiResponse(responseCode = "404", description = "Evento inexistente o ajeno")
    public ResponseEntity<Void> enviarARevision(
            @Parameter(description = "Identificador del evento") @PathVariable UUID id) {
        eventoService.enviarARevision(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Rechazar evento",
            description = "Transicion administrativa EN_REVISION a RECHAZADO con motivo obligatorio.")
    @ApiResponse(responseCode = "404", description = "Evento inexistente")
    public ResponseEntity<Void> rechazar(
            @Parameter(description = "Identificador del evento") @PathVariable UUID id,
            @Valid @RequestBody EventoRechazoRequest request) {
        eventoService.rechazar(id, request.getMotivo());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/volver-borrador")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    @Operation(summary = "Volver evento a borrador",
            description = "Transicion RECHAZADO a BORRADOR para el ORGANIZADOR propietario.")
    @ApiResponse(responseCode = "404", description = "Evento inexistente o ajeno")
    public ResponseEntity<Void> volverABorrador(
            @Parameter(description = "Identificador del evento") @PathVariable UUID id) {
        eventoService.volverABorrador(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Cancelar evento",
            description = "Transicion PUBLICADO a CANCELADO con motivo. ORGANIZADOR requiere ownership.")
    @ApiResponse(responseCode = "404", description = "Evento inexistente o fuera del alcance")
    public ResponseEntity<Void> cancelar(
            @Parameter(description = "Identificador del evento") @PathVariable UUID id,
            @Valid @RequestBody EventoCancelacionRequest request) {
        eventoService.cancelar(id, request.getMotivo());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/finalizar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Finalizar evento",
            description = "Transicion administrativa PUBLICADO a FINALIZADO cuando ya concluyo su fecha y hora final.")
    @ApiResponse(responseCode = "404", description = "Evento inexistente")
    public ResponseEntity<Void> finalizar(
            @Parameter(description = "Identificador del evento") @PathVariable UUID id) {
        eventoService.finalizar(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/categoria/{id}")
    @SecurityRequirements
    @Operation(summary = "Listar eventos publicados por categoria",
            description = "Filtro publico de eventos PUBLICADO por categoria.")
    public ResponseEntity<List<EventoResponse>> listarPorCategoria(
            @Parameter(description = "Identificador de la categoria") @PathVariable UUID id) {
        return ResponseEntity.ok(eventoService.listarPorCategoria(id));
    }
}
