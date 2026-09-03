package bo.uajms.eventos.modulos.encuestas.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.modulos.asistencias.repositorios.AsistenciaRepository;
import bo.uajms.eventos.modulos.encuestas.dtos.ComentarioEncuestaResponse;
import bo.uajms.eventos.modulos.encuestas.dtos.EncuestaResponse;
import bo.uajms.eventos.modulos.encuestas.dtos.EstadisticasEncuestaResponse;
import bo.uajms.eventos.modulos.encuestas.dtos.ResponderEncuestaRequest;
import bo.uajms.eventos.modulos.encuestas.entidades.Encuesta;
import bo.uajms.eventos.modulos.encuestas.entidades.PreguntaEncuesta;
import bo.uajms.eventos.modulos.encuestas.entidades.RespuestaEncuesta;
import bo.uajms.eventos.modulos.encuestas.entidades.TipoPreguntaEncuesta;
import bo.uajms.eventos.modulos.encuestas.repositorios.EncuestaRepository;
import bo.uajms.eventos.modulos.encuestas.repositorios.PreguntaEncuestaRepository;
import bo.uajms.eventos.modulos.encuestas.repositorios.RespuestaEncuestaRepository;
import bo.uajms.eventos.modulos.eventos.entidades.EstadoEvento;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EncuestaService {

    private final EncuestaRepository encuestaRepository;
    private final PreguntaEncuestaRepository preguntaRepository;
    private final RespuestaEncuestaRepository respuestaRepository;
    private final EventoRepository eventoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public EncuestaResponse responder(ResponderEncuestaRequest request) {
        Usuario usuario = obtenerUsuarioAutenticado();
        Evento evento = eventoRepository.findById(request.getEventoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento", request.getEventoId()));

        if (evento.getEstado() != EstadoEvento.FINALIZADO) {
            throw new NegocioException("La encuesta solo puede responderse despues de finalizar el evento");
        }

        Inscripcion inscripcion = inscripcionRepository.findByUsuarioIdAndEventoId(usuario.getId(), evento.getId())
                .orElseThrow(() -> new NegocioException("El usuario no esta inscrito en el evento"));

        if (!asistenciaRepository.existsByInscripcionIdAndFechaEliminacionIsNull(inscripcion.getId())) {
            throw new NegocioException("Solo participantes con asistencia registrada pueden responder la encuesta");
        }

        if (encuestaRepository.existsByEventoIdAndUsuarioId(evento.getId(), usuario.getId())) {
            throw new NegocioException("El participante ya respondio la encuesta de este evento");
        }

        PreguntaEncuesta preguntaCalificacion = obtenerOCrearPreguntaCalificacion();
        PreguntaEncuesta preguntaComentario = obtenerOCrearPreguntaComentario();

        Encuesta encuesta = Encuesta.builder()
                .evento(evento)
                .usuario(usuario)
                .inscripcion(inscripcion)
                .build();

        RespuestaEncuesta respuestaCalificacion = RespuestaEncuesta.builder()
                .encuesta(encuesta)
                .pregunta(preguntaCalificacion)
                .calificacion(request.getCalificacion())
                .build();

        RespuestaEncuesta respuestaComentario = RespuestaEncuesta.builder()
                .encuesta(encuesta)
                .pregunta(preguntaComentario)
                .comentario(normalizarComentario(request.getComentario()))
                .build();

        encuesta.getRespuestas().add(respuestaCalificacion);
        encuesta.getRespuestas().add(respuestaComentario);

        return aResponse(encuestaRepository.save(encuesta));
    }

    @Transactional(readOnly = true)
    public List<EncuestaResponse> listarPorEvento(UUID eventoId) {
        if (!eventoRepository.existsById(eventoId)) {
            throw new RecursoNoEncontradoException("Evento", eventoId);
        }
        return encuestaRepository.findByEventoId(eventoId).stream()
                .map(this::aResponse)
                .sorted(Comparator.comparing(EncuestaResponse::getFechaRespuesta).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public EstadisticasEncuestaResponse obtenerEstadisticas(UUID eventoId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento", eventoId));

        long totalAsistentes = asistenciaRepository.findByInscripcionEventoId(eventoId).stream()
                .map(a -> a.getInscripcion().getId())
                .distinct()
                .count();
        long totalRespuestas = encuestaRepository.countByEventoId(eventoId);
        Double promedio = respuestaRepository.promedioCalificacionPorEvento(eventoId, TipoPreguntaEncuesta.CALIFICACION);

        List<ComentarioEncuestaResponse> comentarios = respuestaRepository
                .comentariosPorEvento(eventoId, TipoPreguntaEncuesta.COMENTARIO)
                .stream()
                .map(r -> ComentarioEncuestaResponse.builder()
                        .participante(nombreParticipante(r.getEncuesta().getUsuario()))
                        .comentario(r.getComentario())
                        .fechaRespuesta(r.getEncuesta().getFechaCreacion())
                        .build())
                .toList();

        return EstadisticasEncuestaResponse.builder()
                .eventoId(evento.getId())
                .eventoTitulo(evento.getTitulo())
                .calificacionPromedio(redondear(promedio))
                .totalAsistentes(totalAsistentes)
                .totalRespuestas(totalRespuestas)
                .participacion(calcularParticipacion(totalRespuestas, totalAsistentes))
                .comentarios(comentarios)
                .build();
    }

    private Usuario obtenerUsuarioAutenticado() {
        String correo = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByCorreoElectronico(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    private PreguntaEncuesta obtenerOCrearPreguntaCalificacion() {
        return preguntaRepository.findFirstByTipoAndActivaTrueOrderByOrdenAsc(TipoPreguntaEncuesta.CALIFICACION)
                .orElseGet(() -> preguntaRepository.save(PreguntaEncuesta.builder()
                        .texto("Calificacion general del evento")
                        .tipo(TipoPreguntaEncuesta.CALIFICACION)
                        .obligatoria(true)
                        .orden(1)
                        .activa(true)
                        .build()));
    }

    private PreguntaEncuesta obtenerOCrearPreguntaComentario() {
        return preguntaRepository.findFirstByTipoAndActivaTrueOrderByOrdenAsc(TipoPreguntaEncuesta.COMENTARIO)
                .orElseGet(() -> preguntaRepository.save(PreguntaEncuesta.builder()
                        .texto("Comentario sobre la experiencia")
                        .tipo(TipoPreguntaEncuesta.COMENTARIO)
                        .obligatoria(false)
                        .orden(2)
                        .activa(true)
                        .build()));
    }

    private EncuestaResponse aResponse(Encuesta encuesta) {
        Integer calificacion = encuesta.getRespuestas().stream()
                .filter(r -> r.getPregunta().getTipo() == TipoPreguntaEncuesta.CALIFICACION)
                .map(RespuestaEncuesta::getCalificacion)
                .findFirst()
                .orElse(null);

        String comentario = encuesta.getRespuestas().stream()
                .filter(r -> r.getPregunta().getTipo() == TipoPreguntaEncuesta.COMENTARIO)
                .map(RespuestaEncuesta::getComentario)
                .findFirst()
                .orElse(null);

        return EncuestaResponse.builder()
                .id(encuesta.getId())
                .eventoId(encuesta.getEvento().getId())
                .eventoTitulo(encuesta.getEvento().getTitulo())
                .usuarioId(encuesta.getUsuario().getId())
                .participante(nombreParticipante(encuesta.getUsuario()))
                .calificacion(calificacion)
                .comentario(comentario)
                .fechaRespuesta(encuesta.getFechaCreacion())
                .build();
    }

    private String nombreParticipante(Usuario usuario) {
        return usuario.getNombres() + " " + usuario.getApellidos();
    }

    private String normalizarComentario(String comentario) {
        if (comentario == null || comentario.isBlank()) {
            return null;
        }
        return comentario.trim();
    }

    private Double calcularParticipacion(long respuestas, long asistentes) {
        if (asistentes == 0) {
            return 0.0;
        }
        return redondear((respuestas * 100.0) / asistentes);
    }

    private Double redondear(Double valor) {
        if (valor == null) {
            return 0.0;
        }
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
