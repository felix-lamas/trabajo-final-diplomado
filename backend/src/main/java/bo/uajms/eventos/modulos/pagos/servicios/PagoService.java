package bo.uajms.eventos.modulos.pagos.servicios;

import bo.uajms.eventos.core.excepciones.*;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.eventos.entidades.TipoInscripcion;
import bo.uajms.eventos.modulos.inscripciones.entidades.*;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.pagos.dtos.*;
import bo.uajms.eventos.modulos.pagos.entidades.*;
import bo.uajms.eventos.modulos.pagos.mappers.PagoMapper;
import bo.uajms.eventos.modulos.pagos.repositorios.PagoRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PagoService {
    private final PagoRepository pagoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final PagoMapper pagoMapper;
    private final ArchivoSeguroServicio archivoSeguroServicio;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final Clock clock;

    @Transactional
    public PagoResponse subirComprobante(UUID pagoId, MultipartFile archivo) {
        exigirUsuario();
        Pago pago = obtenerPagoPropioParaActualizar(pagoId);
        validarPagoCargable(pago);

        ArchivoSeguroServicio.ArchivoGuardado guardado = archivoSeguroServicio.guardarComprobante(archivo, "comprobantes");
        LocalDateTime ahora = LocalDateTime.now(clock);
        pago.setComprobanteUrl(guardado.rutaInterna());
        pago.setComprobanteNombreArchivo(guardado.nombreArchivo());
        pago.setComprobanteTipoContenido(guardado.tipoContenido());
        pago.setFechaCargaComprobante(ahora);
        pago.setIntentosComprobante(Optional.ofNullable(pago.getIntentosComprobante()).orElse(0) + 1);
        pago.setEstado(EstadoPago.PENDIENTE_VALIDACION);

        Inscripcion inscripcion = pago.getInscripcion();
        inscripcion.setEstado(EstadoInscripcion.PENDIENTE_VALIDACION);
        inscripcionRepository.save(inscripcion);
        return pagoMapper.toResponse(pagoRepository.save(pago));
    }

    /** Compatibilidad para inscripciones pagadas históricas sin Pago asociado. */
    @Transactional
    public PagoResponse registrarPago(RegistrarPagoRequest request) {
        exigirUsuario();
        Usuario usuario = usuarioAutenticadoService.obtenerUsuario();
        Inscripcion inscripcion = inscripcionRepository.findByIdAndUsuarioForUpdate(request.getInscripcionId(), usuario.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Inscripcion", request.getInscripcionId()));
        validarInscripcionPagadaPendiente(inscripcion);
        if (pagoRepository.findByInscripcionId(inscripcion.getId()).isPresent())
            throw new NegocioException("Ya existe un pago para esta inscripcion");

        Pago pago = crearPagoPendiente(inscripcion, request.getObservacion(), LocalDateTime.now(clock));
        return pagoMapper.toResponse(pagoRepository.save(pago));
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> listarMisPagos() {
        exigirUsuario();
        return mapear(pagoRepository.findByUsuarioId(usuarioAutenticadoService.obtenerUsuario().getId()));
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> listarPendientes() {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR"))
            return mapear(pagoRepository.findByEstado(EstadoPago.PENDIENTE_VALIDACION));
        if (usuarioAutenticadoService.tieneRol("ORGANIZADOR"))
            return mapear(pagoRepository.findByEstadoAndInscripcionEventoOrganizadorId(
                    EstadoPago.PENDIENTE_VALIDACION, usuarioAutenticadoService.obtenerUsuario().getId()));
        throw new AccessDeniedException("El rol no permite revisar pagos");
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> listarTodos() {
        exigirAdministrador();
        return mapear(pagoRepository.findAll());
    }

    @Transactional(readOnly = true)
    public PagoResponse obtenerPorId(UUID id) {
        return pagoMapper.toResponse(obtenerPagoVisible(id));
    }

    @Transactional
    public PagoResponse validarPago(UUID id, ValidarPagoRequest request) {
        Pago pago = obtenerPagoGestionableParaActualizar(id);
        validarPendienteConComprobante(pago);
        Usuario resolutor = usuarioAutenticadoService.obtenerUsuario();
        pago.setEstado(EstadoPago.APROBADO);
        pago.setObservacion(request == null ? null : request.getObservacion());
        pago.setMotivoRechazo(null);
        pago.setFechaResolucion(LocalDateTime.now(clock));
        pago.setResueltoPor(resolutor);
        pago.getInscripcion().setEstado(EstadoInscripcion.CONFIRMADA);
        inscripcionRepository.save(pago.getInscripcion());
        return pagoMapper.toResponse(pagoRepository.save(pago));
    }

    @Transactional
    public PagoResponse rechazarPago(UUID id, ValidarPagoRequest request) {
        String motivo = request == null ? null : request.getObservacion();
        if (motivo == null || motivo.isBlank()) throw new NegocioException("El motivo de rechazo es obligatorio");
        Pago pago = obtenerPagoGestionableParaActualizar(id);
        validarPendienteConComprobante(pago);
        pago.setEstado(EstadoPago.RECHAZADO);
        pago.setMotivoRechazo(motivo.trim());
        pago.setObservacion(motivo.trim());
        pago.setFechaResolucion(LocalDateTime.now(clock));
        pago.setResueltoPor(usuarioAutenticadoService.obtenerUsuario());
        pago.getInscripcion().setEstado(EstadoInscripcion.PENDIENTE_PAGO);
        inscripcionRepository.save(pago.getInscripcion());
        return pagoMapper.toResponse(pagoRepository.save(pago));
    }

    private Pago crearPagoPendiente(Inscripcion inscripcion, String observacion, LocalDateTime fecha) {
        BigDecimal monto = inscripcion.getEvento().getCosto();
        if (monto == null || monto.signum() <= 0) throw new NegocioException("El evento pagado no tiene monto valido");
        return Pago.builder().inscripcion(inscripcion).monto(monto).fechaPago(fecha)
                .estado(EstadoPago.PENDIENTE_PAGO).observacion(observacion).intentosComprobante(0).build();
    }

    private void validarPagoCargable(Pago pago) {
        Inscripcion inscripcion = pago.getInscripcion();
        if (inscripcion.getEvento().getTipoInscripcion() != TipoInscripcion.PAGO
                || inscripcion.getEstado() != EstadoInscripcion.PENDIENTE_PAGO
                || (pago.getEstado() != EstadoPago.PENDIENTE_PAGO && pago.getEstado() != EstadoPago.RECHAZADO))
            throw new NegocioException("El pago no admite carga de comprobante en su estado actual");
    }

    private void validarInscripcionPagadaPendiente(Inscripcion inscripcion) {
        if (inscripcion.getEvento().getTipoInscripcion() != TipoInscripcion.PAGO)
            throw new NegocioException("No se requiere pago para eventos gratuitos");
        if (inscripcion.getEstado() != EstadoInscripcion.PENDIENTE_PAGO)
            throw new NegocioException("La inscripcion no admite iniciar un pago");
    }

    private void validarPendienteConComprobante(Pago pago) {
        if (pago.getEstado() != EstadoPago.PENDIENTE_VALIDACION
                || pago.getInscripcion().getEstado() != EstadoInscripcion.PENDIENTE_VALIDACION)
            throw new NegocioException("El pago no esta pendiente de validacion");
        if (pago.getComprobanteUrl() == null || pago.getFechaCargaComprobante() == null)
            throw new NegocioException("El pago no tiene comprobante");
    }

    private Pago obtenerPagoVisible(UUID id) {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) return pagoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", id));
        UUID usuarioId = usuarioAutenticadoService.obtenerUsuario().getId();
        if (usuarioAutenticadoService.tieneRol("ORGANIZADOR")) return pagoRepository
                .findByIdAndInscripcionEventoOrganizadorId(id, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", id));
        if (usuarioAutenticadoService.tieneRol("USUARIO")) return pagoRepository
                .findByIdAndInscripcionUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", id));
        throw new AccessDeniedException("El rol no permite consultar pagos");
    }

    private Pago obtenerPagoPropioParaActualizar(UUID id) {
        UUID usuarioId = usuarioAutenticadoService.obtenerUsuario().getId();
        return pagoRepository.findByIdAndUsuarioForUpdate(id, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", id));
    }

    private Pago obtenerPagoGestionableParaActualizar(UUID id) {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) return pagoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", id));
        if (usuarioAutenticadoService.tieneRol("ORGANIZADOR")) return pagoRepository
                .findByIdAndOrganizadorForUpdate(id, usuarioAutenticadoService.obtenerUsuario().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", id));
        throw new AccessDeniedException("El rol no permite resolver pagos");
    }

    private List<PagoResponse> mapear(List<Pago> pagos) { return pagos.stream().map(pagoMapper::toResponse).toList(); }
    private void exigirUsuario() { if (!usuarioAutenticadoService.tieneRol("USUARIO")) throw new AccessDeniedException("Se requiere rol USUARIO"); }
    private void exigirAdministrador() { if (!usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) throw new AccessDeniedException("Se requiere rol ADMINISTRADOR"); }
}
