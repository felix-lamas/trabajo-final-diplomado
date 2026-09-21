package bo.uajms.eventos.modulos.inscripciones.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.eventos.entidades.EstadoEvento;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.eventos.entidades.TipoInscripcion;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.inscripciones.dtos.CrearInscripcionRequest;
import bo.uajms.eventos.modulos.inscripciones.dtos.DetalleInscripcionResponse;
import bo.uajms.eventos.modulos.inscripciones.dtos.InscripcionResponse;
import bo.uajms.eventos.modulos.inscripciones.entidades.EstadoInscripcion;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.inscripciones.mappers.InscripcionMapper;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final EventoRepository eventoRepository;
    private final InscripcionMapper inscripcionMapper;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    @Transactional
    public DetalleInscripcionResponse inscribir(CrearInscripcionRequest request) {
        Usuario usuario = usuarioAutenticadoService.obtenerUsuario();
        Evento evento = eventoRepository.findById(request.getEventoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento", request.getEventoId()));

        // Regla: Unicidad
        if (inscripcionRepository.existsByUsuarioIdAndEventoId(usuario.getId(), evento.getId())) {
            throw new NegocioException("Ya te encuentras inscrito en este evento");
        }

        // Regla: Estado del evento
        if (evento.getEstado() != EstadoEvento.PUBLICADO) {
            throw new NegocioException("No se permiten inscripciones para este evento en su estado actual: " + evento.getEstado());
        }

        // Regla: Cupos
        if (evento.getCupoDisponible() <= 0) {
            throw new NegocioException("Ya no quedan cupos disponibles para este evento");
        }

        // Regla: Estado inicial basado en costo
        EstadoInscripcion estadoInicial = (evento.getTipoInscripcion() == TipoInscripcion.GRATUITO)
                ? EstadoInscripcion.CONFIRMADA
                : EstadoInscripcion.PENDIENTE_PAGO;

        Inscripcion inscripcion = Inscripcion.builder()
                .usuario(usuario)
                .evento(evento)
                .fechaInscripcion(LocalDateTime.now())
                .estado(estadoInicial)
                .build();

        // Actualizar cupo
        evento.setCupoDisponible(evento.getCupoDisponible() - 1);
        eventoRepository.save(evento);

        return inscripcionMapper.toDetalleResponse(inscripcionRepository.save(inscripcion));
    }

    @Transactional(readOnly = true)
    public List<InscripcionResponse> listarMisInscripciones() {
        Usuario usuario = usuarioAutenticadoService.obtenerUsuario();
        return inscripcionRepository.findByUsuarioId(usuario.getId()).stream()
                .map(inscripcionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InscripcionResponse> listarInscritosEvento(UUID eventoId) {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) {
            return mapearLista(inscripcionRepository.findByEventoId(eventoId));
        }

        if (!usuarioAutenticadoService.tieneRol("ORGANIZADOR")) {
            throw new RecursoNoEncontradoException("Evento", eventoId);
        }

        UUID organizadorId = usuarioAutenticadoService.obtenerUsuario().getId();
        eventoRepository.findByIdAndOrganizadorId(eventoId, organizadorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento", eventoId));

        return mapearLista(inscripcionRepository.findByEventoId(eventoId));
    }

    private List<InscripcionResponse> mapearLista(List<Inscripcion> inscripciones) {
        return inscripciones.stream()
                .map(inscripcionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DetalleInscripcionResponse obtenerPorId(UUID id) {
        return obtenerInscripcionVisible(id)
                .map(inscripcionMapper::toDetalleResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción", id));
    }

    @Transactional
    public void cancelar(UUID id) {
        Usuario usuario = usuarioAutenticadoService.obtenerUsuario();
        Inscripcion inscripcion = inscripcionRepository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción", id));

        if (inscripcion.getEstado() == EstadoInscripcion.CANCELADA || inscripcion.getEstado() == EstadoInscripcion.RECHAZADA) {
            throw new NegocioException("La inscripción ya se encuentra en estado: " + inscripcion.getEstado());
        }

        inscripcion.setEstado(EstadoInscripcion.CANCELADA);
        
        // Devolver cupo
        Evento evento = inscripcion.getEvento();
        evento.setCupoDisponible(evento.getCupoDisponible() + 1);
        eventoRepository.save(evento);

        inscripcionRepository.save(inscripcion);
    }

    private Optional<Inscripcion> obtenerInscripcionVisible(UUID id) {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) {
            return inscripcionRepository.findById(id);
        }

        UUID usuarioId = usuarioAutenticadoService.obtenerUsuario().getId();
        if (usuarioAutenticadoService.tieneRol("ORGANIZADOR")) {
            return inscripcionRepository.findByIdAndEventoOrganizadorId(id, usuarioId);
        }

        return inscripcionRepository.findByIdAndUsuarioId(id, usuarioId);
    }
}
