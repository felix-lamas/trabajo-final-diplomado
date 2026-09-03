package bo.uajms.eventos.modulos.carreras.controladores;

import bo.uajms.eventos.modulos.carreras.dtos.ActualizarCarreraRequest;
import bo.uajms.eventos.modulos.carreras.dtos.CarreraResponse;
import bo.uajms.eventos.modulos.carreras.dtos.CrearCarreraRequest;
import bo.uajms.eventos.modulos.carreras.servicios.CarreraService;
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
@RequestMapping("/api/v1/carreras")
@RequiredArgsConstructor
@Tag(name = "Carreras", description = "Gestión de divisiones académicas")
@SecurityRequirement(name = "bearerAuth")
public class CarreraController {

    private final CarreraService carreraService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'ESTUDIANTE')")
    @Operation(summary = "Listar todas las carreras")
    public ResponseEntity<List<CarreraResponse>> listar() {
        return ResponseEntity.ok(carreraService.listarTodas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'ESTUDIANTE')")
    @Operation(summary = "Obtener una carrera por su ID")
    public ResponseEntity<CarreraResponse> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(carreraService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear una nueva carrera (Solo ADMIN)")
    public ResponseEntity<CarreraResponse> crear(@Valid @RequestBody CrearCarreraRequest request) {
        return new ResponseEntity<>(carreraService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar una carrera existente (Solo ADMIN)")
    public ResponseEntity<CarreraResponse> actualizar(@PathVariable UUID id, @Valid @RequestBody ActualizarCarreraRequest request) {
        return ResponseEntity.ok(carreraService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactivar/Eliminar una carrera (Solo ADMIN)")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        carreraService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
