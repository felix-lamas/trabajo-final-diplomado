package bo.uajms.eventos.modulos.inscripciones.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.CodigosError;
import bo.uajms.eventos.core.excepciones.ConflictoException;
import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.eventos.entidades.EstadoEvento;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.eventos.entidades.TipoInscripcion;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.inscripciones.dtos.CrearInscripcionRequest;
import bo.uajms.eventos.modulos.inscripciones.dtos.ComprobanteInscripcionResponse;
import bo.uajms.eventos.modulos.inscripciones.dtos.DetalleInscripcionResponse;
import bo.uajms.eventos.modulos.inscripciones.dtos.InscripcionResponse;
import bo.uajms.eventos.modulos.inscripciones.entidades.EstadoInscripcion;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.inscripciones.mappers.InscripcionMapper;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.pagos.entidades.Pago;
import bo.uajms.eventos.modulos.pagos.entidades.EstadoPago;
import bo.uajms.eventos.modulos.pagos.repositorios.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Clock;
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
    private final PagoRepository pagoRepository;
    private final Clock clock;

    @Transactional
    public DetalleInscripcionResponse inscribir(CrearInscripcionRequest request) {
        exigirUsuario();
        Usuario usuario = usuarioAutenticadoService.obtenerUsuario();
        Evento evento = eventoRepository.findByIdForUpdate(request.getEventoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento", request.getEventoId()));

        // Regla: Unicidad
        if (inscripcionRepository.existsByUsuarioIdAndEventoId(usuario.getId(), evento.getId())) {
            throw new ConflictoException(CodigosError.INSCRIPTION_DUPLICATED,
                    "Ya te encuentras inscrito en este evento");
        }

        // Regla: Estado del evento
        if (evento.getEstado() != EstadoEvento.PUBLICADO) {
            throw new NegocioException(CodigosError.EVENT_NOT_PUBLISHED,
                    "No se permiten inscripciones para este evento en su estado actual: " + evento.getEstado());
        }

        if (!Boolean.TRUE.equals(evento.getRequiereInscripcion())) {
            throw new NegocioException("Este evento no requiere inscripcion");
        }

        // Regla: Cupos
        if (Boolean.TRUE.equals(evento.getCupoLimitado())
                && (evento.getCupoDisponible() == null || evento.getCupoDisponible() <= 0)) {
            throw new NegocioException(CodigosError.INSCRIPTION_CAPACITY_FULL,
                    "Ya no quedan cupos disponibles para este evento");
        }

        if (evento.getTipoInscripcion() == TipoInscripcion.PAGO
                && (evento.getCosto() == null || evento.getCosto().signum() <= 0)) {
            throw new NegocioException("El evento pagado no tiene monto valido");
        }

        // Regla: Estado inicial basado en costo
        EstadoInscripcion estadoInicial = (evento.getTipoInscripcion() == TipoInscripcion.GRATUITO)
                ? EstadoInscripcion.CONFIRMADA
                : EstadoInscripcion.PENDIENTE_PAGO;

        Inscripcion inscripcion = Inscripcion.builder()
                .usuario(usuario)
                .evento(evento)
                .codigoParticipante(generarCodigoInscripcion())
                .fechaInscripcion(LocalDateTime.now(clock))
                .estado(estadoInicial)
                .build();

        // Actualizar cupo
        if (Boolean.TRUE.equals(evento.getCupoLimitado())) {
            evento.setCupoDisponible(evento.getCupoDisponible() - 1);
            eventoRepository.save(evento);
        }

        Inscripcion guardada = inscripcionRepository.save(inscripcion);
        if (evento.getTipoInscripcion() == TipoInscripcion.PAGO) {
            Pago pago = Pago.builder().inscripcion(guardada).monto(evento.getCosto())
                    .fechaPago(LocalDateTime.now(clock)).estado(EstadoPago.PENDIENTE_PAGO)
                    .intentosComprobante(0).build();
            pagoRepository.save(pago);
        }
        return inscripcionMapper.toDetalleResponse(guardada);
    }

    @Transactional(readOnly = true)
    public List<InscripcionResponse> listarMisInscripciones() {
        exigirUsuario();
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
        exigirUsuario();
        Usuario usuario = usuarioAutenticadoService.obtenerUsuario();
        // Un pago asociado se bloquea primero para que una carga/validación concurrente
        // no pueda sobrescribir el estado CANCELADA de la inscripción.
        pagoRepository.findByInscripcionIdAndUsuarioIdForUpdate(id, usuario.getId());
        UUID eventoId = inscripcionRepository.findEventoIdByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción", id));
        Evento evento = eventoRepository.findByIdForUpdate(eventoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento", eventoId));
        Inscripcion inscripcion = inscripcionRepository.findByIdAndUsuarioForUpdate(id, usuario.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción", id));

        if (!evento.getId().equals(inscripcion.getEvento().getId())) {
            throw new ConflictoException(CodigosError.CONFLICT,
                    "La inscripción cambió durante la cancelación");
        }
        if (evento.getEstado() != EstadoEvento.PUBLICADO) {
            throw new NegocioException("Solo se pueden cancelar inscripciones de eventos publicados");
        }
        if (inscripcion.getEstado() == EstadoInscripcion.CANCELADA) {
            throw new ConflictoException(CodigosError.CONFLICT,
                    "La inscripción ya se encuentra cancelada");
        }

        inscripcion.setEstado(EstadoInscripcion.CANCELADA);
        if (Boolean.TRUE.equals(evento.getCupoLimitado())) {
            if (evento.getCupoDisponible() == null || evento.getCupoMaximo() == null
                    || evento.getCupoDisponible() >= evento.getCupoMaximo()) {
                throw new ConflictoException(CodigosError.CONFLICT,
                        "El cupo del evento no admite una devolución adicional");
            }
            evento.setCupoDisponible(evento.getCupoDisponible() + 1);
            eventoRepository.save(evento);
        }

        inscripcionRepository.save(inscripcion);
    }

    @Transactional(readOnly = true)
    public ComprobanteInscripcionResponse obtenerComprobantePropio(UUID id) {
        exigirUsuario();
        Usuario usuario = usuarioAutenticadoService.obtenerUsuario();
        Inscripcion inscripcion = inscripcionRepository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción", id));
        Evento evento = inscripcion.getEvento();
        Optional<Pago> pago = pagoRepository.findByInscripcionId(id);
        BigDecimal monto = evento.getTipoInscripcion() == TipoInscripcion.GRATUITO
                ? BigDecimal.ZERO
                : pago.map(Pago::getMonto).orElse(evento.getCosto());
        String estadoPago = evento.getTipoInscripcion() == TipoInscripcion.GRATUITO
                ? "NO_APLICA"
                : pago.map(p -> p.getEstado().name()).orElse(EstadoPago.PENDIENTE_PAGO.name());

        return ComprobanteInscripcionResponse.builder()
                .inscripcionId(inscripcion.getId())
                .codigoInscripcion(inscripcion.getCodigoParticipante())
                .eventoId(evento.getId())
                .eventoTitulo(evento.getTitulo())
                .participante(usuario.getNombres() + " " + usuario.getApellidos())
                .ci(usuario.getCi())
                .ru(usuario.getRu())
                .monto(monto)
                .fechaInscripcion(inscripcion.getFechaInscripcion())
                .estadoInscripcion(inscripcion.getEstado())
                .estadoPago(estadoPago)
                .codigoVerificacion(inscripcion.getCodigoParticipante())
                .build();
    }

    private Optional<Inscripcion> obtenerInscripcionVisible(UUID id) {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) {
            return inscripcionRepository.findById(id);
        }

        UUID usuarioId = usuarioAutenticadoService.obtenerUsuario().getId();
        Optional<Inscripcion> visible = Optional.empty();
        if (usuarioAutenticadoService.tieneRol("ORGANIZADOR")) {
            visible = inscripcionRepository.findByIdAndEventoOrganizadorId(id, usuarioId);
        }
        if (visible.isEmpty() && usuarioAutenticadoService.tieneRol("USUARIO")) {
            visible = inscripcionRepository.findByIdAndUsuarioId(id, usuarioId);
        }
        return visible;
    }

    private void exigirUsuario() {
        if (!usuarioAutenticadoService.tieneRol("USUARIO")) {
            throw new AccessDeniedException("La operación corresponde exclusivamente al participante");
        }
    }

    private String generarCodigoInscripcion() {
        return "INS-" + UUID.randomUUID().toString().replace("-", "").substring(0, 24).toUpperCase();
    }
}
