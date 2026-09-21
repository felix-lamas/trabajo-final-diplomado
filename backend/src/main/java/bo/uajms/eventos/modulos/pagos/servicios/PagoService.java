package bo.uajms.eventos.modulos.pagos.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.eventos.entidades.TipoInscripcion;
import bo.uajms.eventos.modulos.inscripciones.entidades.EstadoInscripcion;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.pagos.dtos.PagoResponse;
import bo.uajms.eventos.modulos.pagos.dtos.RegistrarPagoRequest;
import bo.uajms.eventos.modulos.pagos.dtos.ValidarPagoRequest;
import bo.uajms.eventos.modulos.pagos.entidades.ComprobantePago;
import bo.uajms.eventos.modulos.pagos.entidades.EstadoPago;
import bo.uajms.eventos.modulos.pagos.entidades.Pago;
import bo.uajms.eventos.modulos.pagos.mappers.PagoMapper;
import bo.uajms.eventos.modulos.pagos.repositorios.PagoRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final PagoMapper pagoMapper;
    private final ArchivoSeguroServicio archivoSeguroServicio;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    @Transactional
    public PagoResponse subirComprobante(UUID pagoId, MultipartFile archivo) {
        Pago pago = obtenerPagoParaCargaComprobante(pagoId);

        if (pago.getEstado() == EstadoPago.VALIDADO) {
            throw new NegocioException("No se puede reemplazar el comprobante de un pago VALIDADO");
        }

        ArchivoSeguroServicio.ArchivoGuardado guardado =
                archivoSeguroServicio.guardarComprobante(archivo, "comprobantes");

        ComprobantePago comprobante = ComprobantePago.builder()
                .pago(pago)
                .urlArchivo(guardado.urlArchivo())
                .nombreArchivo(guardado.nombreArchivo())
                .tipoContenido(guardado.tipoContenido())
                .build();

        pago.setComprobante(comprobante);
        return pagoMapper.toResponse(pagoRepository.save(pago));
    }

    @Transactional
    public PagoResponse registrarPago(RegistrarPagoRequest request) {
        Usuario usuarioAutenticado = usuarioAutenticadoService.obtenerUsuario();
        Inscripcion inscripcion = inscripcionRepository
                .findByIdAndUsuarioId(request.getInscripcionId(), usuarioAutenticado.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción", request.getInscripcionId()));

        if (inscripcion.getEvento().getTipoInscripcion() == TipoInscripcion.GRATUITO) {
            throw new NegocioException("No se requiere pago para eventos gratuitos");
        }

        if (pagoRepository.findByInscripcionId(inscripcion.getId()).isPresent()) {
            throw new NegocioException("Ya existe un pago registrado para esta inscripción");
        }

        Pago pago = Pago.builder()
                .inscripcion(inscripcion)
                .monto(request.getMonto())
                .fechaPago(LocalDateTime.now())
                .estado(EstadoPago.PENDIENTE)
                .observacion(request.getObservacion())
                .build();

        inscripcion.setEstado(EstadoInscripcion.PENDIENTE_VALIDACION);
        inscripcionRepository.save(inscripcion);

        return pagoMapper.toResponse(pagoRepository.save(pago));
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> listarMisPagos() {
        Usuario usuario = usuarioAutenticadoService.obtenerUsuario();
        return pagoRepository.findByUsuarioId(usuario.getId()).stream()
                .map(pagoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> listarPendientes() {
        List<Pago> pagos;
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) {
            pagos = pagoRepository.findByEstado(EstadoPago.PENDIENTE);
        } else {
            UUID organizadorId = usuarioAutenticadoService.obtenerUsuario().getId();
            pagos = pagoRepository.findByEstadoAndInscripcionEventoOrganizadorId(
                    EstadoPago.PENDIENTE,
                    organizadorId
            );
        }

        return pagos.stream()
                .map(pagoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> listarTodos() {
        return pagoRepository.findAll().stream()
                .map(pagoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PagoResponse obtenerPorId(UUID id) {
        return pagoMapper.toResponse(obtenerPagoVisible(id));
    }

    @Transactional
    public PagoResponse validarPago(UUID id, ValidarPagoRequest request) {
        Pago pago = obtenerPagoGestionable(id);

        if (pago.getEstado() != EstadoPago.PENDIENTE) {
            throw new NegocioException("Solo se pueden validar pagos en estado PENDIENTE");
        }

        pago.setEstado(EstadoPago.VALIDADO);
        pago.setObservacion(request.getObservacion());
        
        Inscripcion inscripcion = pago.getInscripcion();
        inscripcion.setEstado(EstadoInscripcion.CONFIRMADA);
        inscripcionRepository.save(inscripcion);

        return pagoMapper.toResponse(pagoRepository.save(pago));
    }

    @Transactional
    public PagoResponse rechazarPago(UUID id, ValidarPagoRequest request) {
        Pago pago = obtenerPagoGestionable(id);

        if (pago.getEstado() != EstadoPago.PENDIENTE) {
            throw new NegocioException("Solo se pueden rechazar pagos en estado PENDIENTE");
        }

        pago.setEstado(EstadoPago.RECHAZADO);
        pago.setObservacion(request.getObservacion());

        Inscripcion inscripcion = pago.getInscripcion();
        inscripcion.setEstado(EstadoInscripcion.RECHAZADA);
        inscripcionRepository.save(inscripcion);

        return pagoMapper.toResponse(pagoRepository.save(pago));
    }

    private Pago obtenerPagoVisible(UUID id) {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) {
            return obtenerPagoGlobal(id);
        }

        UUID usuarioId = usuarioAutenticadoService.obtenerUsuario().getId();
        if (usuarioAutenticadoService.tieneRol("ORGANIZADOR")) {
            return pagoRepository.findByIdAndInscripcionEventoOrganizadorId(id, usuarioId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Pago", id));
        }

        return pagoRepository.findByIdAndInscripcionUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", id));
    }

    private Pago obtenerPagoParaCargaComprobante(UUID id) {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) {
            return obtenerPagoGlobal(id);
        }

        UUID usuarioId = usuarioAutenticadoService.obtenerUsuario().getId();
        return pagoRepository.findByIdAndInscripcionUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", id));
    }

    private Pago obtenerPagoGestionable(UUID id) {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) {
            return obtenerPagoGlobal(id);
        }

        UUID organizadorId = usuarioAutenticadoService.obtenerUsuario().getId();
        return pagoRepository.findByIdAndInscripcionEventoOrganizadorId(id, organizadorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", id));
    }

    private Pago obtenerPagoGlobal(UUID id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", id));
    }
}
