package bo.uajms.eventos.modulos.asistencias.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.CodigosError;
import bo.uajms.eventos.core.excepciones.ConflictoException;
import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.asistencias.dtos.RegistrarAsistenciaRequest;
import bo.uajms.eventos.modulos.asistencias.entidades.Asistencia;
import bo.uajms.eventos.modulos.asistencias.infraestructura.QrAsistenciaTemporal;
import bo.uajms.eventos.modulos.asistencias.repositorios.AsistenciaRepository;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.eventos.entidades.Modalidad;
import bo.uajms.eventos.modulos.eventos.entidades.EstadoEvento;
import bo.uajms.eventos.modulos.inscripciones.entidades.*;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.sesiones.entidades.SesionEvento;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.*;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AsistenciaService {
    private static final BigDecimal PRECISION_MAXIMA = new BigDecimal("30.00");
    private static final double RADIO_TIERRA_METROS = 6_371_000d;

    private final AsistenciaRepository asistenciaRepository;
    private final EventoRepository eventoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final QrAsistenciaService qrService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final Clock clock;

    @Transactional
    public Asistencia registrar(RegistrarAsistenciaRequest request) {
        if (!usuarioAutenticadoService.tieneRol("USUARIO"))
            throw new AccessDeniedException("Solo USUARIO puede registrar su asistencia");
        Usuario usuario = usuarioAutenticadoService.obtenerUsuario();
        QrAsistenciaTemporal qr = qrService.resolverToken(request.getToken());
        SesionEvento sesion = qr.getSesionEvento();
        Inscripcion inscripcion = inscripcionRepository.findByUsuarioIdAndEventoId(usuario.getId(), sesion.getEvento().getId())
                .orElseThrow(() -> new NegocioException(CodigosError.INSCRIPTION_REQUIRED,
                        "Inscripcion no valida para la sesion"));
        if (inscripcion.getEstado() != EstadoInscripcion.CONFIRMADA)
            throw new NegocioException(CodigosError.INSCRIPTION_NOT_CONFIRMED,
                    "Inscripcion no valida para la sesion");
        if (!inscripcion.getUsuario().getId().equals(usuario.getId())
                || !inscripcion.getEvento().getId().equals(sesion.getEvento().getId()))
            throw new NegocioException(CodigosError.INSCRIPTION_REQUIRED,
                    "Inscripcion no valida para la sesion");

        LocalDateTime ahora = LocalDateTime.now(clock);
        validarSesion(sesion, ahora);
        qrService.validarVigencia(qr, ahora);
        BigDecimal distancia = validarGps(sesion, request);
        BigDecimal precision = request.getPrecision() == null ? null
                : request.getPrecision().setScale(2, RoundingMode.HALF_UP);
        if (asistenciaRepository.existsByInscripcionIdAndSesionEventoId(inscripcion.getId(), sesion.getId()))
            throw new ConflictoException(CodigosError.ATTENDANCE_DUPLICATED,
                    "La asistencia ya fue registrada para esta sesion");

        Asistencia asistencia = Asistencia.builder().inscripcion(inscripcion).sesionEvento(sesion)
                .registradoPor(usuario).fechaHoraRegistro(ahora).distanciaMetros(distancia)
                .precisionGpsMetros(precision)
                .resultadoValidacion(Asistencia.ResultadoValidacion.VALIDADA).build();
        try {
            return asistenciaRepository.saveAndFlush(asistencia);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictoException(CodigosError.ATTENDANCE_DUPLICATED,
                    "La asistencia ya fue registrada para esta sesion");
        }
    }

    @Transactional(readOnly = true)
    public List<Asistencia> obtenerAsistenciasPorEvento(UUID eventoId) {
        validarEventoVisible(eventoId);
        return asistenciaRepository.findBySesionEventoEventoId(eventoId);
    }

    @Transactional(readOnly = true)
    public List<Asistencia> obtenerMisAsistencias() {
        Usuario usuario = usuarioAutenticadoService.obtenerUsuario();
        return asistenciaRepository.findByInscripcionUsuarioId(usuario.getId());
    }

    @Transactional(readOnly = true)
    public List<Asistencia> obtenerAsistenciasPorInscripcion(UUID inscripcionId) {
        Usuario usuario = usuarioAutenticadoService.obtenerUsuario();
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR"))
            inscripcionRepository.findById(inscripcionId).orElseThrow(() -> new RecursoNoEncontradoException("Inscripcion", inscripcionId));
        else if (usuarioAutenticadoService.tieneRol("ORGANIZADOR"))
            inscripcionRepository.findByIdAndEventoOrganizadorId(inscripcionId, usuario.getId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Inscripcion", inscripcionId));
        else inscripcionRepository.findByIdAndUsuarioId(inscripcionId, usuario.getId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Inscripcion", inscripcionId));
        return asistenciaRepository.findByInscripcionId(inscripcionId);
    }

    private void validarSesion(SesionEvento sesion, LocalDateTime ahora) {
        if (sesion.getEvento().getEstado() != EstadoEvento.PUBLICADO)
            throw new NegocioException(CodigosError.EVENT_NOT_PUBLISHED, "El evento no esta publicado");
        if (!Boolean.TRUE.equals(sesion.getActiva()))
            throw new NegocioException(CodigosError.ATTENDANCE_SESSION_INACTIVE,
                    "La sesion no admite asistencia");
        if (!Boolean.TRUE.equals(sesion.getRequiereAsistencia()))
            throw new NegocioException(CodigosError.ATTENDANCE_SESSION_NOT_REQUIRED,
                    "La sesion no admite asistencia");
        LocalDateTime inicio = LocalDateTime.of(sesion.getFecha(), sesion.getHoraInicio());
        LocalDateTime fin = LocalDateTime.of(sesion.getFecha(), sesion.getHoraFin());
        if (ahora.isBefore(inicio) || ahora.isAfter(fin))
            throw new NegocioException(CodigosError.ATTENDANCE_OUTSIDE_WINDOW,
                    "La sesion esta fuera de la ventana de asistencia");
    }

    private BigDecimal validarGps(SesionEvento sesion, RegistrarAsistenciaRequest request) {
        if (sesion.getEvento().getModalidad() == Modalidad.VIRTUAL
                && sesion.getLatitud() == null && sesion.getLongitud() == null) return null;
        validarRangos(request);
        if (request.getPrecision().compareTo(PRECISION_MAXIMA) > 0)
            throw new NegocioException(CodigosError.ATTENDANCE_GPS_ACCURACY_INVALID,
                    "La precision GPS debe ser de 30 metros o mejor");
        if (sesion.getLatitud() == null || sesion.getLongitud() == null || sesion.getRadioMetros() == null)
            throw new NegocioException("La sesion no tiene configuracion GPS valida");
        BigDecimal distancia = BigDecimal.valueOf(haversine(
                sesion.getLatitud().doubleValue(), sesion.getLongitud().doubleValue(),
                request.getLatitud().doubleValue(), request.getLongitud().doubleValue()))
                .setScale(2, RoundingMode.HALF_UP);
        if (distancia.add(request.getPrecision()).compareTo(BigDecimal.valueOf(sesion.getRadioMetros())) > 0)
            throw new NegocioException(CodigosError.ATTENDANCE_OUTSIDE_RADIUS,
                    "La ubicacion esta fuera del radio permitido");
        return distancia;
    }

    private void validarRangos(RegistrarAsistenciaRequest request) {
        if (request.getLatitud() == null || request.getLongitud() == null || request.getPrecision() == null
                || request.getLatitud().compareTo(BigDecimal.valueOf(-90)) < 0
                || request.getLatitud().compareTo(BigDecimal.valueOf(90)) > 0
                || request.getLongitud().compareTo(BigDecimal.valueOf(-180)) < 0
                || request.getLongitud().compareTo(BigDecimal.valueOf(180)) > 0
                || request.getPrecision().signum() < 0)
            throw new NegocioException(CodigosError.ATTENDANCE_GPS_INVALID, "Datos GPS invalidos");
    }

    static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1), dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return RADIO_TIERRA_METROS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private void validarEventoVisible(UUID eventoId) {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) {
            eventoRepository.findById(eventoId).orElseThrow(() -> new RecursoNoEncontradoException("Evento", eventoId));
            return;
        }
        if (!usuarioAutenticadoService.tieneRol("ORGANIZADOR")) throw new AccessDeniedException("Acceso no permitido");
        UUID organizadorId = usuarioAutenticadoService.obtenerUsuario().getId();
        eventoRepository.findByIdAndOrganizadorId(eventoId, organizadorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento", eventoId));
    }
}
