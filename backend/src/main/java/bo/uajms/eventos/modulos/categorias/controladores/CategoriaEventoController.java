package bo.uajms.eventos.modulos.categorias.controladores;

import bo.uajms.eventos.modulos.categorias.dtos.ActualizarCategoriaEventoRequest;
import bo.uajms.eventos.modulos.categorias.dtos.CategoriaEventoResponse;
import bo.uajms.eventos.modulos.categorias.dtos.CrearCategoriaEventoRequest;
import bo.uajms.eventos.modulos.categorias.servicios.CategoriaEventoService;
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
@RequestMapping("/api/v1/categorias-evento")
@RequiredArgsConstructor
@Tag(name = "Categorias de evento", description = "Catalogo de clasificaciones para eventos")
@SecurityRequirement(name = "bearerAuth")
public class CategoriaEventoController {

    private final CategoriaEventoService categoriaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'USUARIO')")
    @Operation(summary = "Listar categorias",
            description = "Devuelve categorias activas e inactivas para usuarios autenticados.")
    public ResponseEntity<List<CategoriaEventoResponse>> listar() {
        return ResponseEntity.ok(categoriaService.listarTodas());
    }

    @GetMapping("/activas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'USUARIO')")
    @Operation(summary = "Listar categorias activas",
            description = "Devuelve las categorias disponibles para clasificar o filtrar eventos.")
    public ResponseEntity<List<CategoriaEventoResponse>> listarActivas() {
        return ResponseEntity.ok(categoriaService.listarActivas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'USUARIO')")
    @Operation(summary = "Obtener categoria",
            description = "Devuelve el detalle de una categoria por su identificador.")
    @ApiResponse(responseCode = "404", description = "Categoria inexistente")
    public ResponseEntity<CategoriaEventoResponse> obtenerPorId(
            @Parameter(description = "Identificador de la categoria") @PathVariable UUID id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear categoria",
            description = "Crea una categoria unica. Operacion exclusiva de ADMINISTRADOR.")
    @ApiResponse(responseCode = "201", description = "Categoria creada", useReturnTypeSchema = true)
    public ResponseEntity<CategoriaEventoResponse> crear(@Valid @RequestBody CrearCategoriaEventoRequest request) {
        return new ResponseEntity<>(categoriaService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar categoria",
            description = "Actualiza nombre, descripcion y estado. Operacion exclusiva de ADMINISTRADOR.")
    @ApiResponse(responseCode = "404", description = "Categoria inexistente")
    public ResponseEntity<CategoriaEventoResponse> actualizar(
            @Parameter(description = "Identificador de la categoria") @PathVariable UUID id,
            @Valid @RequestBody ActualizarCategoriaEventoRequest request) {
        return ResponseEntity.ok(categoriaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactivar categoria",
            description = "Realiza la eliminacion logica de una categoria. Operacion exclusiva de ADMINISTRADOR.")
    @ApiResponse(responseCode = "204", description = "Categoria desactivada")
    @ApiResponse(responseCode = "404", description = "Categoria inexistente")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "Identificador de la categoria") @PathVariable UUID id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
