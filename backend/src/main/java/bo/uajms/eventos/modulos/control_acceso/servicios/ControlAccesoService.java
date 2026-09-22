package bo.uajms.eventos.modulos.control_acceso.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.asistencias.servicios.AsistenciaService;
import bo.uajms.eventos.modulos.codigo_qr.entidades.CodigoQr;
import bo.uajms.eventos.modulos.codigo_qr.repositorios.CodigoQrRepository;
import bo.uajms.eventos.modulos.control_acceso.dtos.*;
import bo.uajms.eventos.modulos.control_acceso.entidades.ControlAcceso;
import bo.uajms.eventos.modulos.control_acceso.repositorios.ControlAccesoRepository;
import bo.uajms.eventos.modulos.credenciales.entidades.Credencial;
import bo.uajms.eventos.modulos.credenciales.repositorios.CredencialRepository;
import bo.uajms.eventos.modulos.eventos.entidades.EstadoEvento;
import bo.uajms.eventos.modulos.eventos.entidades.TipoInscripcion;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.inscripciones.entidades.EstadoInscripcion;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.pagos.entidades.EstadoPago;
import bo.uajms.eventos.modulos.pagos.repositorios.PagoRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ControlAccesoService {

    private final ControlAccesoRepository controlAccesoRepository;
    private final CredencialRepository credencialRepository;
    private final CodigoQrRepository codigoQrRepository;
    private final InscripcionRepository inscripcionRepository;
    private final EventoRepository eventoRepository;
    private final AsistenciaService asistenciaService;
    private final PagoRepository pagoRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public ValidarQrResponse validarQr(String tokenQr) {
        CodigoQr codigoQr;
        if (tieneAlcanceGlobal()) {
            codigoQr = codigoQrRepository.findByContenido(tokenQr)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Código QR no encontrado"));
        } else {
            UUID organizadorId = usuarioAutenticadoService.obtenerUsuario().getId();
            codigoQr = codigoQrRepository
                    .findByContenidoAndCredencialInscripcionEventoOrganizadorId(tokenQr, organizadorId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Código QR no encontrado"));
        }

        return construirValidarQrResponse(codigoQr.getCredencial(), codigoQr);
    }

    public ValidarQrResponse buscarPorCodigoParticipante(String codigo) {
        Inscripcion inscripcion;
        if (tieneAlcanceGlobal()) {
            inscripcion = inscripcionRepository.findByCodigoParticipante(codigo)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción no encontrada"));
        } else {
            UUID organizadorId = usuarioAutenticadoService.obtenerUsuario().getId();
            inscripcion = inscripcionRepository
                    .findByCodigoParticipanteAndEventoOrganizadorId(codigo, organizadorId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción no encontrada"));
        }
        
        Credencial credencial = credencialRepository.findByInscripcionId(inscripcion.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Credencial no encontrada"));

        CodigoQr qr = codigoQrRepository.findByCredencialId(credencial.getId()).orElse(null);
        return construirValidarQrResponse(credencial, qr);
    }

    public ValidarQrResponse buscarPorDocumentoIdentidad(String ci, UUID eventoId) {
        validarEventoVisible(eventoId);
        Inscripcion inscripcion = inscripcionRepository.findByUsuarioCiAndEventoId(ci, eventoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción no encontrada para el evento especificado"));

        Credencial credencial = credencialRepository.findByInscripcionId(inscripcion.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Credencial no encontrada"));

        CodigoQr qr = codigoQrRepository.findByCredencialId(credencial.getId()).orElse(null);
        return construirValidarQrResponse(credencial, qr);
    }

    private ValidarQrResponse construirValidarQrResponse(Credencial credencial, CodigoQr qr) {
        Inscripcion inscripcion = credencial.getInscripcion();
        Usuario usuario = inscripcion.getUsuario();
        
        ResultadoValidacionIngreso validacion = evaluarCondicionesIngreso(credencial, qr);

        return ValidarQrResponse.builder()
                .credencialId(credencial.getId())
                .inscripcionId(inscripcion.getId())
                .fotografiaUrl(usuario.getFotografiaUrl())
                .nombreCompleto(usuario.getNombres() + " " + usuario.getApellidos())
                .documentoIdentidad(usuario.getCi())
                .carrera(usuario.getCarrera() != null ? usuario.getCarrera().getNombre() : "N/A")
                .facultad(usuario.getCarrera() != null ? usuario.getCarrera().getFacultad().getNombre() : "N/A")
                .evento(inscripcion.getEvento().getTitulo())
                .estadoPago(validacion.estadoPago())
                .estadoInscripcion(inscripcion.getEstado().name())
                .estadoQr(qr != null ? qr.getEstadoQr().name() : "N/A")
                .codigoParticipante(inscripcion.getCodigoParticipante())
                .puedeIngresar(validacion.permitido())
                .mensajeValidacion(validacion.mensaje())
                .build();
    }

    @Transactional
    public ControlAccesoResponse autorizarIngreso(AutorizarIngresoRequest request) {
        Credencial credencial = obtenerCredencialVisible(request.getCredencialId());

        CodigoQr qr = codigoQrRepository.findByCredencialId(credencial.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("QR no encontrado para esta credencial"));

        ResultadoValidacionIngreso validacion = evaluarCondicionesIngreso(credencial, qr);
        if (!validacion.permitido()) {
            // Si intenta autorizar algo ya usado, se registra como REINTENTO
            if (qr.getEstadoQr() != CodigoQr.EstadoQr.GENERADO) {
                registrarIntento(credencial, ControlAcceso.EstadoIngreso.REINTENTO,
                        "Intento de autorizar QR ya utilizado/anulado");
            }
            throw new NegocioException(validacion.mensaje());
        }

        Usuario usuarioControl = usuarioAutenticadoService.obtenerUsuario();

        // 1. Registrar ControlAcceso
        ControlAcceso control = ControlAcceso.builder()
                .credencial(credencial)
                .usuarioControl(usuarioControl)
                .fechaHoraIngreso(LocalDateTime.now())
                .estadoIngreso(ControlAcceso.EstadoIngreso.AUTORIZADO)
                .observacion(request.getObservacion())
                .build();
        controlAccesoRepository.save(control);

        // 2. Registrar Asistencia
        asistenciaService.registrarAsistencia(credencial.getInscripcion(), usuarioControl, request.getObservacion());

        // 3. Cambiar QR a UTILIZADO
        qr.setEstadoQr(CodigoQr.EstadoQr.UTILIZADO);
        codigoQrRepository.save(qr);

        return mapearAControlAccesoResponse(control);
    }

    @Transactional
    public ControlAccesoResponse denegarIngreso(AutorizarIngresoRequest request) {
        Credencial credencial = obtenerCredencialVisible(request.getCredencialId());

        ControlAcceso control = registrarIntento(credencial, ControlAcceso.EstadoIngreso.DENEGADO, request.getObservacion());
        return mapearAControlAccesoResponse(control);
    }

    private ControlAcceso registrarIntento(Credencial credencial, ControlAcceso.EstadoIngreso estado, String observacion) {
        Usuario usuarioControl = usuarioAutenticadoService.obtenerUsuario();
        ControlAcceso control = ControlAcceso.builder()
                .credencial(credencial)
                .usuarioControl(usuarioControl)
                .fechaHoraIngreso(LocalDateTime.now())
                .estadoIngreso(estado)
                .observacion(observacion)
                .build();
        return controlAccesoRepository.save(control);
    }

    public List<ControlAccesoResponse> obtenerHistorial(UUID eventoId) {
        validarEventoVisible(eventoId);
        return controlAccesoRepository.findByCredencialInscripcionEventoId(eventoId)
                .stream()
                .map(this::mapearAControlAccesoResponse)
                .collect(Collectors.toList());
    }

    private Credencial obtenerCredencialVisible(UUID credencialId) {
        if (tieneAlcanceGlobal()) {
            return credencialRepository.findById(credencialId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Credencial no encontrada"));
        }

        UUID organizadorId = usuarioAutenticadoService.obtenerUsuario().getId();
        return credencialRepository
                .findByIdAndInscripcionEventoOrganizadorId(credencialId, organizadorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Credencial no encontrada"));
    }

    private void validarEventoVisible(UUID eventoId) {
        if (tieneAlcanceGlobal()) {
            eventoRepository.findById(eventoId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Evento", eventoId));
            return;
        }

        UUID organizadorId = usuarioAutenticadoService.obtenerUsuario().getId();
        eventoRepository.findByIdAndOrganizadorId(eventoId, organizadorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento", eventoId));
    }

    private boolean tieneAlcanceGlobal() {
        return usuarioAutenticadoService.tieneRol("ADMINISTRADOR")
                || usuarioAutenticadoService.tieneRol("PERSONAL_CONTROL");
    }

    private ResultadoValidacionIngreso evaluarCondicionesIngreso(Credencial credencial, CodigoQr qr) {
        Inscripcion inscripcion = credencial.getInscripcion();
        String estadoPago = pagoRepository.findByInscripcionId(inscripcion.getId())
                .map(p -> p.getEstado().name())
                .orElse("SIN_PAGO");

        if (!"ACTIVA".equalsIgnoreCase(credencial.getEstado())) {
            return new ResultadoValidacionIngreso(false, "La credencial no está activa", estadoPago);
        }
        if (inscripcion.getEstado() != EstadoInscripcion.CONFIRMADA) {
            return new ResultadoValidacionIngreso(false, "La inscripción no está confirmada", estadoPago);
        }
        EstadoEvento estadoEvento = inscripcion.getEvento().getEstado();
        if (estadoEvento == EstadoEvento.BORRADOR
                || estadoEvento == EstadoEvento.CANCELADO
                || estadoEvento == EstadoEvento.FINALIZADO) {
            return new ResultadoValidacionIngreso(false, "El evento no admite ingresos en su estado actual", estadoPago);
        }
        if (inscripcion.getEvento().getTipoInscripcion() == TipoInscripcion.PAGO
                && !EstadoPago.VALIDADO.name().equals(estadoPago)) {
            return new ResultadoValidacionIngreso(false, "El pago de la inscripción no está validado", estadoPago);
        }
        if (qr == null) {
            return new ResultadoValidacionIngreso(false, "No existe un código QR para la credencial", estadoPago);
        }
        if (!Boolean.TRUE.equals(qr.getActivo())) {
            return new ResultadoValidacionIngreso(false, "El código QR no está activo", estadoPago);
        }
        if (qr.getEstadoQr() == CodigoQr.EstadoQr.UTILIZADO) {
            return new ResultadoValidacionIngreso(false, "El código QR ya ha sido utilizado", estadoPago);
        }
        if (qr.getEstadoQr() == CodigoQr.EstadoQr.ANULADO) {
            return new ResultadoValidacionIngreso(false, "El código QR ha sido anulado", estadoPago);
        }
        if (qr.getEstadoQr() != CodigoQr.EstadoQr.GENERADO) {
            return new ResultadoValidacionIngreso(false, "El código QR no está disponible para ingreso", estadoPago);
        }

        return new ResultadoValidacionIngreso(true, "Listo para validación visual", estadoPago);
    }

    private record ResultadoValidacionIngreso(boolean permitido, String mensaje, String estadoPago) {
    }

    private ControlAccesoResponse mapearAControlAccesoResponse(ControlAcceso control) {
        Usuario p = control.getCredencial().getInscripcion().getUsuario();
        return ControlAccesoResponse.builder()
                .id(control.getId())
                .nombreParticipante(p.getNombres() + " " + p.getApellidos())
                .evento(control.getCredencial().getInscripcion().getEvento().getTitulo())
                .fechaHoraIngreso(control.getFechaHoraIngreso())
                .estadoIngreso(control.getEstadoIngreso().name())
                .usuarioControl(control.getUsuarioControl().getNombres() + " " + control.getUsuarioControl().getApellidos())
                .observacion(control.getObservacion())
                .build();
    }
}
