package bo.uajms.eventos.modulos.asistencias.controladores;

import bo.uajms.eventos.modulos.asistencias.dtos.AsistenciaResponse;
import bo.uajms.eventos.modulos.asistencias.servicios.AsistenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/asistencias", "/api/v1/asistencias"})
@RequiredArgsConstructor
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    @GetMapping("/evento/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR', 'PERSONAL_CONTROL')")
    public ResponseEntity<List<AsistenciaResponse>> listarPorEvento(@PathVariable UUID id) {
        List<AsistenciaResponse> response = asistenciaService.obtenerAsistenciasPorEvento(id)
                .stream()
                .map(a -> AsistenciaResponse.builder()
                        .id(a.getId())
                        .nombreParticipante(a.getInscripcion().getUsuario().getNombres() + " " + a.getInscripcion().getUsuario().getApellidos())
                        .documentoIdentidad(a.getInscripcion().getUsuario().getCi())
                        .codigoParticipante(a.getInscripcion().getCodigoParticipante())
                        .evento(a.getInscripcion().getEvento().getTitulo())
                        .fechaHoraRegistro(a.getFechaHoraRegistro())
                        .usuarioControl(a.getUsuarioControl().getNombres() + " " + a.getUsuarioControl().getApellidos())
                        .observacion(a.getObservacion())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
