package bo.uajms.eventos.modulos.asistencias.servicios;

import bo.uajms.eventos.modulos.asistencias.entidades.Asistencia;
import bo.uajms.eventos.modulos.asistencias.repositorios.AsistenciaRepository;
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
        return asistenciaRepository.findByInscripcionEventoId(eventoId);
    }

    public List<Asistencia> obtenerAsistenciasPorInscripcion(UUID inscripcionId) {
        return asistenciaRepository.findByInscripcionId(inscripcionId);
    }
}
