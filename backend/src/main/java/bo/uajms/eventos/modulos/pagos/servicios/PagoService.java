package bo.uajms.eventos.modulos.pagos.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
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
import bo.uajms.eventos.modulos.pagos.repositorios.ComprobantePagoRepository;
import bo.uajms.eventos.modulos.pagos.repositorios.PagoRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final ComprobantePagoRepository comprobantePagoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final UsuarioRepository usuarioRepository;
    private final PagoMapper pagoMapper;
    private final ArchivoSeguroServicio archivoSeguroServicio;

    @Transactional
    public PagoResponse subirComprobante(UUID pagoId, MultipartFile archivo) {
        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", pagoId));

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
        Inscripcion inscripcion = inscripcionRepository.findById(request.getInscripcionId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción", request.getInscripcionId()));

        Usuario usuarioAutenticado = obtenerUsuarioAutenticado();
        if (!inscripcion.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new NegocioException("Solo puedes registrar pagos para tus propias inscripciones");
        }

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
        Usuario usuario = obtenerUsuarioAutenticado();
        return pagoRepository.findByUsuarioId(usuario.getId()).stream()
                .map(pagoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> listarPendientes() {
        return pagoRepository.findByEstado(EstadoPago.PENDIENTE).stream()
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
        return pagoRepository.findById(id)
                .map(pagoMapper::toResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", id));
    }

    @Transactional
    public PagoResponse validarPago(UUID id, ValidarPagoRequest request) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", id));

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
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", id));

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

    private Usuario obtenerUsuarioAutenticado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByCorreoElectronico(email)
                .orElseThrow(() -> new NegocioException("Usuario autenticado no encontrado"));
    }
}
