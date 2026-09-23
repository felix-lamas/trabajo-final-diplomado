package bo.uajms.eventos.modulos.sesiones.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.eventos.entidades.*;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.inscripciones.entidades.EstadoInscripcion;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.sesiones.dtos.*;
import bo.uajms.eventos.modulos.sesiones.entidades.SesionEvento;
import bo.uajms.eventos.modulos.sesiones.repositorios.SesionEventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SesionEventoService {
    private final SesionEventoRepository sesionRepository;
    private final EventoRepository eventoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final Clock clock;

    @Transactional
    public SesionEventoResponse crear(UUID eventoId, SesionEventoRequest request) {
        Evento evento = obtenerEventoGestionable(eventoId);
        validarEventoModificable(evento);
        validarDatos(request, evento);
        SesionEvento sesion = SesionEvento.builder().evento(evento).historica(false).build();
        aplicar(sesion, request);
        return mapear(sesionRepository.save(sesion));
    }

    @Transactional(readOnly = true)
    public List<SesionEventoResponse> listarPorEvento(UUID eventoId) {
        validarEventoVisible(eventoId);
        return sesionRepository.findByEventoIdOrderByFechaAscHoraInicioAsc(eventoId).stream().map(this::mapear).toList();
    }

    @Transactional(readOnly = true)
    public SesionEventoResponse obtener(UUID id) {
        return mapear(obtenerSesionVisible(id));
    }

    @Transactional
    public SesionEventoResponse actualizar(UUID id, SesionEventoRequest request) {
        SesionEvento sesion = obtenerSesionGestionable(id);
        validarNoIniciada(sesion);
        validarEventoModificable(sesion.getEvento());
        validarDatos(request, sesion.getEvento());
        aplicar(sesion, request);
        return mapear(sesionRepository.save(sesion));
    }

    @Transactional
    public SesionEventoResponse cambiarEstado(UUID id, boolean activa) {
        SesionEvento sesion = obtenerSesionGestionable(id);
        validarNoIniciada(sesion);
        sesion.setActiva(activa);
        return mapear(sesionRepository.save(sesion));
    }

    public LocalDateTime inicio(SesionEvento sesion) {
        return LocalDateTime.of(sesion.getFecha(), sesion.getHoraInicio());
    }

    public LocalDateTime fin(SesionEvento sesion) {
        return LocalDateTime.of(sesion.getFecha(), sesion.getHoraFin());
    }

    private void validarDatos(SesionEventoRequest request, Evento evento) {
        if (request.getFecha() == null || request.getHoraInicio() == null || request.getHoraFin() == null
                || request.getRequiereAsistencia() == null || request.getActiva() == null)
            throw new NegocioException("Faltan datos obligatorios de la sesion");
        if (!request.getHoraInicio().isBefore(request.getHoraFin()))
            throw new NegocioException("La hora de inicio debe ser anterior a la hora de fin");
        if (request.getRadioMetros() == null || request.getRadioMetros() <= 0 || request.getRadioMetros() > 500)
            throw new NegocioException("El radio debe estar entre 1 y 500 metros");
        boolean algunaCoordenada = request.getLatitud() != null || request.getLongitud() != null;
        if ((request.getLatitud() == null) != (request.getLongitud() == null))
            throw new NegocioException("Latitud y longitud deben informarse juntas");
        if (Boolean.TRUE.equals(request.getRequiereAsistencia()) && evento.getModalidad() == Modalidad.PRESENCIAL
                && (!algunaCoordenada || request.getRadioMetros() == null))
            throw new NegocioException("La sesion presencial requerida necesita punto GPS y radio");
        if (evento.getFechaInicio() != null && evento.getFechaFin() != null
                && (request.getFecha().isBefore(evento.getFechaInicio()) || request.getFecha().isAfter(evento.getFechaFin())))
            throw new NegocioException("La fecha de la sesion debe estar dentro del evento");
    }

    private void validarEventoModificable(Evento evento) {
        if (evento.getEstado() == EstadoEvento.CANCELADO || evento.getEstado() == EstadoEvento.FINALIZADO
                || evento.getEstado() == EstadoEvento.EN_REVISION)
            throw new NegocioException("El estado del evento no permite gestionar sesiones");
    }

    private void validarNoIniciada(SesionEvento sesion) {
        if (Boolean.TRUE.equals(sesion.getHistorica()) || !LocalDateTime.now(clock).isBefore(inicio(sesion)))
            throw new NegocioException("Una sesion iniciada o historica no admite modificaciones");
    }

    private Evento obtenerEventoGestionable(UUID eventoId) {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) return eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento", eventoId));
        if (usuarioAutenticadoService.tieneRol("ORGANIZADOR")) return eventoRepository
                .findByIdAndOrganizadorId(eventoId, usuarioAutenticadoService.obtenerUsuario().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento", eventoId));
        throw new AccessDeniedException("El rol no permite gestionar sesiones");
    }

    private SesionEvento obtenerSesionGestionable(UUID id) {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) return sesionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesion", id));
        if (usuarioAutenticadoService.tieneRol("ORGANIZADOR")) return sesionRepository
                .findByIdAndEventoOrganizadorId(id, usuarioAutenticadoService.obtenerUsuario().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesion", id));
        throw new AccessDeniedException("El rol no permite gestionar sesiones");
    }

    private void validarEventoVisible(UUID eventoId) {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) {
            eventoRepository.findById(eventoId).orElseThrow(() -> new RecursoNoEncontradoException("Evento", eventoId));
        } else if (usuarioAutenticadoService.tieneRol("ORGANIZADOR")) {
            eventoRepository.findByIdAndOrganizadorId(eventoId, usuarioAutenticadoService.obtenerUsuario().getId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Evento", eventoId));
        } else if (usuarioAutenticadoService.tieneRol("USUARIO")) {
            UUID usuarioId = usuarioAutenticadoService.obtenerUsuario().getId();
            if (inscripcionRepository.findByUsuarioIdAndEventoIdAndEstado(usuarioId, eventoId, EstadoInscripcion.CONFIRMADA).isEmpty())
                throw new RecursoNoEncontradoException("Evento", eventoId);
        } else throw new AccessDeniedException("El rol no permite consultar sesiones");
    }

    private SesionEvento obtenerSesionVisible(UUID id) {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) return sesionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesion", id));
        if (usuarioAutenticadoService.tieneRol("ORGANIZADOR")) return sesionRepository
                .findByIdAndEventoOrganizadorId(id, usuarioAutenticadoService.obtenerUsuario().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesion", id));
        if (!usuarioAutenticadoService.tieneRol("USUARIO"))
            throw new AccessDeniedException("El rol no permite consultar sesiones");
        SesionEvento sesion = sesionRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Sesion", id));
        validarEventoVisible(sesion.getEvento().getId());
        return sesion;
    }

    private void aplicar(SesionEvento sesion, SesionEventoRequest request) {
        sesion.setNombre(request.getNombre().trim()); sesion.setDescripcion(request.getDescripcion());
        sesion.setFecha(request.getFecha()); sesion.setHoraInicio(request.getHoraInicio()); sesion.setHoraFin(request.getHoraFin());
        sesion.setRequiereAsistencia(request.getRequiereAsistencia()); sesion.setLatitud(request.getLatitud());
        sesion.setLongitud(request.getLongitud()); sesion.setRadioMetros(request.getRadioMetros()); sesion.setActiva(request.getActiva());
    }

    private SesionEventoResponse mapear(SesionEvento s) {
        return SesionEventoResponse.builder().id(s.getId()).eventoId(s.getEvento().getId()).nombre(s.getNombre())
                .descripcion(s.getDescripcion()).fecha(s.getFecha()).horaInicio(s.getHoraInicio()).horaFin(s.getHoraFin())
                .requiereAsistencia(s.getRequiereAsistencia()).latitud(s.getLatitud()).longitud(s.getLongitud())
                .radioMetros(s.getRadioMetros()).activa(s.getActiva()).historica(s.getHistorica()).build();
    }
}
