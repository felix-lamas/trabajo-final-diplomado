package bo.uajms.eventos.modulos.asistencias.servicios;

import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.asistencias.entidades.Asistencia;
import bo.uajms.eventos.modulos.asistencias.repositorios.AsistenciaRepository;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    @Transactional
    public Asistencia registrarAsistencia(Inscripcion inscripcion, Usuario usuarioControl, String observacion) {
        Asistencia asistencia = Asistencia.builder()
                .inscripcion(inscripcion)
                .usuarioControl(usuarioControl)
                .fechaHoraRegistro(LocalDateTime.now())
                .observacion(observacion)
                .build();
        return asistenciaRepository.save(asistencia);
    }

    public List<Asistencia> obtenerAsistenciasPorEvento(UUID eventoId) {
        validarEventoVisible(eventoId);
        return asistenciaRepository.findByInscripcionEventoId(eventoId);
    }

    public List<Asistencia> obtenerAsistenciasPorInscripcion(UUID inscripcionId) {
        return asistenciaRepository.findByInscripcionId(inscripcionId);
    }

    private void validarEventoVisible(UUID eventoId) {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")
                || usuarioAutenticadoService.tieneRol("PERSONAL_CONTROL")) {
            eventoRepository.findById(eventoId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Evento", eventoId));
            return;
        }

        UUID organizadorId = usuarioAutenticadoService.obtenerUsuario().getId();
        eventoRepository.findByIdAndOrganizadorId(eventoId, organizadorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento", eventoId));
    }
}
