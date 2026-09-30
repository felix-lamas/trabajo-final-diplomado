package bo.uajms.eventos.modulos.asistencias.controladores;

import bo.uajms.eventos.modulos.asistencias.dtos.*;
import bo.uajms.eventos.modulos.asistencias.entidades.Asistencia;
import bo.uajms.eventos.modulos.asistencias.servicios.AsistenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/asistencias")
@RequiredArgsConstructor
@Tag(name = "Asistencias", description = "Registro con QR temporal y consulta de asistencia")
@SecurityRequirement(name = "bearerAuth")
public class AsistenciaController {
    private final AsistenciaService asistenciaService;

    @PostMapping
    @PreAuthorize("hasRole('USUARIO')")
    @Operation(summary = "Registrar asistencia",
            description = "Registra la asistencia del usuario autenticado validando inscripcion, sesion, QR temporal, GPS, precision, distancia y duplicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Asistencia registrada", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "QR, sesion, inscripcion o ubicacion no validos"),
            @ApiResponse(responseCode = "401", description = "Autenticacion requerida"),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado"),
            @ApiResponse(responseCode = "409", description = "La asistencia ya fue registrada")
    })
    public ResponseEntity<AsistenciaResponse> registrar(@Valid @RequestBody RegistrarAsistenciaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapear(asistenciaService.registrar(request)));
    }

    @GetMapping("/evento/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    @Operation(summary = "Listar asistencias de un evento",
            description = "El ADMINISTRADOR consulta cualquier evento; el ORGANIZADOR solo eventos propios.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asistencias del evento", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "Autenticacion requerida"),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado"),
            @ApiResponse(responseCode = "404", description = "Evento inexistente o fuera del alcance")
    })
    public ResponseEntity<List<AsistenciaResponse>> listarPorEvento(
            @Parameter(description = "Identificador del evento") @PathVariable UUID id) {
        return ResponseEntity.ok(asistenciaService.obtenerAsistenciasPorEvento(id).stream().map(this::mapear).toList());
    }

    @GetMapping("/mis-asistencias")
    @PreAuthorize("hasRole('USUARIO')")
    @Operation(summary = "Listar mis asistencias",
            description = "Devuelve exclusivamente las asistencias del usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Asistencias propias", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "401", description = "Autenticacion requerida")
    @ApiResponse(responseCode = "403", description = "Rol no autorizado")
    public ResponseEntity<List<AsistenciaResponse>> listarPropias() {
        return ResponseEntity.ok(asistenciaService.obtenerMisAsistencias().stream().map(this::mapear).toList());
    }

    private AsistenciaResponse mapear(Asistencia a) {
        return AsistenciaResponse.builder().id(a.getId())
                .nombreParticipante(a.getInscripcion().getUsuario().getNombres() + " " + a.getInscripcion().getUsuario().getApellidos())
                .documentoIdentidad(a.getInscripcion().getUsuario().getCi())
                .codigoParticipante(a.getInscripcion().getCodigoParticipante())
                .evento(a.getSesionEvento().getEvento().getTitulo()).sesionEventoId(a.getSesionEvento().getId())
                .sesion(a.getSesionEvento().getNombre()).fechaHoraRegistro(a.getFechaHoraRegistro())
                .registradoPor(a.getRegistradoPor() == null ? null
                        : a.getRegistradoPor().getNombres() + " " + a.getRegistradoPor().getApellidos())
                .distanciaMetros(a.getDistanciaMetros()).precisionGpsMetros(a.getPrecisionGpsMetros())
                .resultadoValidacion(a.getResultadoValidacion().name()).observacion(a.getObservacion()).build();
    }
}
