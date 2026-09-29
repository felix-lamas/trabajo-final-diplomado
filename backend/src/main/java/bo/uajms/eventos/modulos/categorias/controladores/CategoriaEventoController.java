package bo.uajms.eventos.modulos.categorias.controladores;

import bo.uajms.eventos.modulos.categorias.dtos.ActualizarCategoriaEventoRequest;
import bo.uajms.eventos.modulos.categorias.dtos.CategoriaEventoResponse;
import bo.uajms.eventos.modulos.categorias.dtos.CrearCategoriaEventoRequest;
import bo.uajms.eventos.modulos.categorias.servicios.CategoriaEventoService;
import bo.uajms.eventos.core.excepciones.ErrorRespuesta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
            description = "Devuelve categorias activas e inactivas. Requiere autenticacion con rol ADMINISTRADOR, ORGANIZADOR o USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categorias obtenidas", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "Autenticacion requerida", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ResponseEntity<List<CategoriaEventoResponse>> listar() {
        return ResponseEntity.ok(categoriaService.listarTodas());
    }

    @GetMapping("/activas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'USUARIO')")
    @Operation(summary = "Listar categorias activas",
            description = "Devuelve las categorias disponibles para clasificar o filtrar eventos. Requiere cualquiera de los tres roles del sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categorias activas obtenidas", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "Autenticacion requerida", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ResponseEntity<List<CategoriaEventoResponse>> listarActivas() {
        return ResponseEntity.ok(categoriaService.listarActivas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'USUARIO')")
    @Operation(summary = "Obtener categoria",
            description = "Devuelve el DTO de una categoria por UUID. Requiere rol ADMINISTRADOR, ORGANIZADOR o USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria obtenida", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "UUID invalido", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "401", description = "Autenticacion requerida", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "404", description = "Categoria inexistente", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ResponseEntity<CategoriaEventoResponse> obtenerPorId(
            @Parameter(description = "Identificador de la categoria") @PathVariable UUID id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear categoria",
            description = "Crea una categoria activa, recorta su nombre y exige unicidad sin distinguir mayusculas. Exclusivo de ADMINISTRADOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoria creada", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "401", description = "Autenticacion requerida", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "403", description = "Solo ADMINISTRADOR", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "409", description = "Nombre normalizado duplicado", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ResponseEntity<CategoriaEventoResponse> crear(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nombre obligatorio y descripcion opcional de la categoria",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CrearCategoriaEventoRequest.class)))
            @Valid @RequestBody CrearCategoriaEventoRequest request) {
        return new ResponseEntity<>(categoriaService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar categoria",
            description = "Actualiza nombre, descripcion y estado respetando la unicidad normalizada. Exclusivo de ADMINISTRADOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria actualizada", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Solicitud o UUID invalido", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "401", description = "Autenticacion requerida", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "403", description = "Solo ADMINISTRADOR", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "404", description = "Categoria inexistente", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "409", description = "Nombre normalizado duplicado", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ResponseEntity<CategoriaEventoResponse> actualizar(
            @Parameter(description = "Identificador de la categoria") @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nombre, descripcion y estado ACTIVO o INACTIVO",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ActualizarCategoriaEventoRequest.class)))
            @Valid @RequestBody ActualizarCategoriaEventoRequest request) {
        return ResponseEntity.ok(categoriaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactivar categoria",
            description = "Realiza eliminacion logica solo si ningun evento la referencia. Exclusivo de ADMINISTRADOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria eliminada logicamente"),
            @ApiResponse(responseCode = "400", description = "UUID invalido", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "401", description = "Autenticacion requerida", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "403", description = "Solo ADMINISTRADOR", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "404", description = "Categoria inexistente", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "409", description = "Categoria referenciada por eventos", content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "Identificador de la categoria") @PathVariable UUID id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
