package bo.uajms.eventos.modulos.categorias.controladores;

import bo.uajms.eventos.modulos.categorias.dtos.ActualizarCategoriaEventoRequest;
import bo.uajms.eventos.modulos.categorias.dtos.CategoriaEventoResponse;
import bo.uajms.eventos.modulos.categorias.dtos.CrearCategoriaEventoRequest;
import bo.uajms.eventos.modulos.categorias.servicios.CategoriaEventoService;
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
@RequestMapping("/api/v1/categorias-evento")
@RequiredArgsConstructor
@Tag(name = "Categorías de Evento", description = "Catálogo de clasificaciones para eventos")
@SecurityRequirement(name = "bearerAuth")
public class CategoriaEventoController {

    private final CategoriaEventoService categoriaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'ESTUDIANTE', 'PARTICIPANTE_EXTERNO')")
    @Operation(summary = "Listar todas las categorías")
    public ResponseEntity<List<CategoriaEventoResponse>> listar() {
        return ResponseEntity.ok(categoriaService.listarTodas());
    }

    @GetMapping("/activas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'ESTUDIANTE', 'PARTICIPANTE_EXTERNO')")
    @Operation(summary = "Listar solo categorías activas")
    public ResponseEntity<List<CategoriaEventoResponse>> listarActivas() {
        return ResponseEntity.ok(categoriaService.listarActivas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'ESTUDIANTE', 'PARTICIPANTE_EXTERNO')")
    @Operation(summary = "Obtener detalle de una categoría")
    public ResponseEntity<CategoriaEventoResponse> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear nueva categoría (Solo ADMIN)")
    public ResponseEntity<CategoriaEventoResponse> crear(@Valid @RequestBody CrearCategoriaEventoRequest request) {
        return new ResponseEntity<>(categoriaService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar categoría existente (Solo ADMIN)")
    public ResponseEntity<CategoriaEventoResponse> actualizar(@PathVariable UUID id, @Valid @RequestBody ActualizarCategoriaEventoRequest request) {
        return ResponseEntity.ok(categoriaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactivar/Eliminar categoría (Solo ADMIN)")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
