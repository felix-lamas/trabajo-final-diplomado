package bo.uajms.eventos.modulos.facultades.controladores;

import bo.uajms.eventos.modulos.carreras.dtos.CarreraResponse;
import bo.uajms.eventos.modulos.carreras.servicios.CarreraService;
import bo.uajms.eventos.modulos.facultades.dtos.ActualizarFacultadRequest;
import bo.uajms.eventos.modulos.facultades.dtos.CrearFacultadRequest;
import bo.uajms.eventos.modulos.facultades.dtos.FacultadResponse;
import bo.uajms.eventos.modulos.facultades.servicios.FacultadService;
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
@RequestMapping("/api/v1/facultades")
@RequiredArgsConstructor
@Tag(name = "Facultades", description = "Gestión de unidades académicas mayores")
@SecurityRequirement(name = "bearerAuth")
public class FacultadController {

    private final FacultadService facultadService;
    private final CarreraService carreraService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'ESTUDIANTE')")
    @Operation(summary = "Listar todas las facultades")
    public ResponseEntity<List<FacultadResponse>> listar() {
        return ResponseEntity.ok(facultadService.listarTodas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'ESTUDIANTE')")
    @Operation(summary = "Obtener una facultad por su ID")
    public ResponseEntity<FacultadResponse> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(facultadService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear una nueva facultad (Solo ADMIN)")
    public ResponseEntity<FacultadResponse> crear(@Valid @RequestBody CrearFacultadRequest request) {
        return new ResponseEntity<>(facultadService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar una facultad existente (Solo ADMIN)")
    public ResponseEntity<FacultadResponse> actualizar(@PathVariable UUID id, @Valid @RequestBody ActualizarFacultadRequest request) {
        return ResponseEntity.ok(facultadService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactivar/Eliminar una facultad (Solo ADMIN)")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        facultadService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/carreras")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'ESTUDIANTE')")
    @Operation(summary = "Listar carreras de una facultad específica")
    public ResponseEntity<List<CarreraResponse>> listarCarrerasPorFacultad(@PathVariable UUID id) {
        return ResponseEntity.ok(carreraService.listarPorFacultad(id));
    }
}
