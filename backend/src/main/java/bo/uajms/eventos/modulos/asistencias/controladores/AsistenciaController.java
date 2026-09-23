package bo.uajms.eventos.modulos.asistencias.controladores;

import bo.uajms.eventos.modulos.asistencias.dtos.*;
import bo.uajms.eventos.modulos.asistencias.entidades.Asistencia;
import bo.uajms.eventos.modulos.asistencias.servicios.AsistenciaService;
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
public class AsistenciaController {
    private final AsistenciaService asistenciaService;

    @PostMapping
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<AsistenciaResponse> registrar(@Valid @RequestBody RegistrarAsistenciaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapear(asistenciaService.registrar(request)));
    }

    @GetMapping("/evento/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')")
    public ResponseEntity<List<AsistenciaResponse>> listarPorEvento(@PathVariable UUID id) {
        return ResponseEntity.ok(asistenciaService.obtenerAsistenciasPorEvento(id).stream().map(this::mapear).toList());
    }

    @GetMapping("/mis-asistencias")
    @PreAuthorize("hasRole('USUARIO')")
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
