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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

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
    private final AlmacenamientoArchivos almacenamientoArchivos;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final Clock clock;

    @Transactional
    public PagoResponse subirComprobante(UUID pagoId, MultipartFile archivo) {
        exigirUsuario();
        Pago pago = obtenerPagoPropioParaActualizar(pagoId);
        validarPagoCargable(pago);

        ArchivoSeguroServicio.ArchivoProcesado procesado = archivoSeguroServicio.procesarComprobante(archivo);
        String storageKey = "comprobantes/" + pago.getId() + "/" + UUID.randomUUID() + "." + procesado.extension();
        String referenciaAnterior = pago.getComprobanteUrl();
        try {
            almacenamientoArchivos.guardar(storageKey, procesado.contenido(), procesado.tipoContenido());
            pago.setComprobanteUrl(storageKey);
            pago.setComprobanteNombreArchivo("comprobante-" + UUID.randomUUID() + "." + procesado.extension());
            pago.setComprobanteTipoContenido(procesado.tipoContenido());
            pago.setFechaCargaComprobante(LocalDateTime.now(clock));
            pago.setIntentosComprobante(Optional.ofNullable(pago.getIntentosComprobante()).orElse(0) + 1);
            pago.setEstado(EstadoPago.PENDIENTE_VALIDACION);

            Inscripcion inscripcion = pago.getInscripcion();
            inscripcion.setEstado(EstadoInscripcion.PENDIENTE_VALIDACION);
            inscripcionRepository.save(inscripcion);
            PagoResponse response = pagoMapper.toResponse(pagoRepository.save(pago));
            registrarLimpiezaTransaccional(storageKey, referenciaAnterior, pagoId);
            return response;
        } catch (RuntimeException exception) {
            eliminarObjetoNuevo(storageKey, pagoId);
            throw exception;
        }
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
            throw new ConflictoException(CodigosError.CONFLICT, "Ya existe un pago para esta inscripcion");

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

    @Transactional(readOnly = true)
    public ComprobanteDescarga descargarComprobante(UUID id) {
        Pago pago = obtenerPagoVisible(id);
        if (pago.getComprobanteUrl() == null || pago.getComprobanteTipoContenido() == null) {
            throw new RecursoNoEncontradoException(CodigosError.PAYMENT_RECEIPT_NOT_FOUND,
                    "Comprobante no encontrado");
        }
        Resource recurso = ArchivoSeguroServicio.esClaveStorage(pago.getComprobanteUrl())
                ? almacenamientoArchivos.descargar(pago.getComprobanteUrl()).orElseThrow(() ->
                    new RecursoNoEncontradoException(CodigosError.PAYMENT_RECEIPT_NOT_FOUND, "Comprobante no encontrado"))
                : cargarArchivoLegacy(pago.getComprobanteUrl());
        return new ComprobanteDescarga(recurso, nombreLogico(pago), pago.getComprobanteTipoContenido());
    }

    private Resource cargarArchivoLegacy(String referencia) {
        try {
            return archivoSeguroServicio.cargarArchivo(referencia);
        } catch (NegocioException exception) {
            throw new RecursoNoEncontradoException(CodigosError.PAYMENT_RECEIPT_NOT_FOUND,
                    "Comprobante no encontrado");
        }
    }

    private void registrarLimpiezaTransaccional(String nuevo, String anterior, UUID pagoId) {
        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    eliminarReferenciaAnterior(anterior, pagoId);
                }

                @Override
                public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED) eliminarObjetoNuevo(nuevo, pagoId);
                }
            });
        } else {
            eliminarReferenciaAnterior(anterior, pagoId);
        }
    }

    private void eliminarObjetoNuevo(String storageKey, UUID pagoId) {
        try {
            almacenamientoArchivos.eliminar(storageKey);
        } catch (RuntimeException exception) {
            // Solo se registra el identificador técnico del pago; nunca credenciales ni contenido.
            org.slf4j.LoggerFactory.getLogger(PagoService.class)
                    .warn("No se pudo compensar objeto de comprobante para pago {}", pagoId);
        }
    }

    private void eliminarReferenciaAnterior(String anterior, UUID pagoId) {
        if (anterior == null || anterior.isBlank()) return;
        try {
            if (ArchivoSeguroServicio.esClaveStorage(anterior)) {
                almacenamientoArchivos.eliminar(anterior);
            } else {
                archivoSeguroServicio.eliminarArchivoLegacy(anterior);
            }
        } catch (RuntimeException | java.io.IOException exception) {
            org.slf4j.LoggerFactory.getLogger(PagoService.class)
                    .warn("Quedo pendiente la limpieza del comprobante anterior del pago {}", pagoId);
        }
    }

    @Transactional
    public PagoResponse validarPago(UUID id, ValidarPagoRequest request) {
        Pago pago = obtenerPagoGestionableParaActualizar(id);
        validarPendienteConComprobante(pago);
        Usuario resolutor = usuarioAutenticadoService.obtenerUsuario();
        pago.setEstado(EstadoPago.APROBADO);
        pago.setObservacion(request == null ? null : request.getObservacion());
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
            throw new ConflictoException(CodigosError.PAYMENT_INVALID_STATE,
                    "El pago no admite carga de comprobante en su estado actual");
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
            throw new ConflictoException(CodigosError.PAYMENT_INVALID_STATE,
                    "El pago no esta pendiente de validacion");
        if (pago.getComprobanteUrl() == null || pago.getFechaCargaComprobante() == null)
            throw new NegocioException(CodigosError.PAYMENT_RECEIPT_REQUIRED,
                    "El pago no tiene comprobante");
    }

    private Pago obtenerPagoVisible(UUID id) {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) return pagoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", id));
        UUID usuarioId = usuarioAutenticadoService.obtenerUsuario().getId();
        Optional<Pago> visible = Optional.empty();
        if (usuarioAutenticadoService.tieneRol("ORGANIZADOR")) {
            visible = pagoRepository.findByIdAndInscripcionEventoOrganizadorId(id, usuarioId);
        }
        if (visible.isEmpty() && usuarioAutenticadoService.tieneRol("USUARIO")) {
            visible = pagoRepository.findByIdAndInscripcionUsuarioId(id, usuarioId);
        }
        if (!usuarioAutenticadoService.tieneRol("ORGANIZADOR")
                && !usuarioAutenticadoService.tieneRol("USUARIO")) {
            throw new AccessDeniedException("El rol no permite consultar pagos");
        }
        return visible.orElseThrow(() -> new RecursoNoEncontradoException("Pago", id));
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
    private String nombreLogico(Pago pago) {
        String extension = switch (pago.getComprobanteTipoContenido()) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "application/pdf" -> ".pdf";
            default -> "";
        };
        return "comprobante-pago-" + pago.getId() + extension;
    }

    public record ComprobanteDescarga(Resource recurso, String nombreArchivo, String tipoContenido) {}
    private void exigirUsuario() { if (!usuarioAutenticadoService.tieneRol("USUARIO")) throw new AccessDeniedException("Se requiere rol USUARIO"); }
    private void exigirAdministrador() { if (!usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) throw new AccessDeniedException("Se requiere rol ADMINISTRADOR"); }
}
