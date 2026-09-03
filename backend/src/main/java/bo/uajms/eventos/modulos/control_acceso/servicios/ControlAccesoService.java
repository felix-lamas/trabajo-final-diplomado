package bo.uajms.eventos.modulos.control_acceso.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.modulos.asistencias.servicios.AsistenciaService;
import bo.uajms.eventos.modulos.codigo_qr.entidades.CodigoQr;
import bo.uajms.eventos.modulos.codigo_qr.repositorios.CodigoQrRepository;
import bo.uajms.eventos.modulos.control_acceso.dtos.*;
import bo.uajms.eventos.modulos.control_acceso.entidades.ControlAcceso;
import bo.uajms.eventos.modulos.control_acceso.repositorios.ControlAccesoRepository;
import bo.uajms.eventos.modulos.credenciales.entidades.Credencial;
import bo.uajms.eventos.modulos.credenciales.repositorios.CredencialRepository;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.pagos.entidades.Pago;
import bo.uajms.eventos.modulos.pagos.repositorios.PagoRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UsuarioRepository usuarioRepository;
    private final AsistenciaService asistenciaService;
    private final PagoRepository pagoRepository;

    public ValidarQrResponse validarQr(String tokenQr) {
        CodigoQr codigoQr = codigoQrRepository.findByContenido(tokenQr)
                .orElseThrow(() -> new RecursoNoEncontradoException("Código QR no encontrado"));

        return construirValidarQrResponse(codigoQr.getCredencial(), codigoQr);
    }

    public ValidarQrResponse buscarPorCodigoParticipante(String codigo) {
        Inscripcion inscripcion = inscripcionRepository.findByCodigoParticipante(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción no encontrada"));
        
        Credencial credencial = credencialRepository.findByInscripcionId(inscripcion.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Credencial no encontrada"));

        CodigoQr qr = codigoQrRepository.findByCredencialId(credencial.getId()).orElse(null);
        return construirValidarQrResponse(credencial, qr);
    }

    public ValidarQrResponse buscarPorDocumentoIdentidad(String ci, UUID eventoId) {
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
        
        // Obtener estado de pago
        String estadoPago = pagoRepository.findByInscripcionId(inscripcion.getId())
                .map(p -> p.getEstado().name())
                .orElse("SIN_PAGO");

        boolean qrValido = qr != null && qr.getEstadoQr() == CodigoQr.EstadoQr.GENERADO;
        String mensaje = "Listo para validación visual";
        
        if (qr != null) {
            if (qr.getEstadoQr() == CodigoQr.EstadoQr.UTILIZADO) {
                mensaje = "ERROR: El código QR ya ha sido utilizado.";
                qrValido = false;
            } else if (qr.getEstadoQr() == CodigoQr.EstadoQr.ANULADO) {
                mensaje = "ERROR: El código QR ha sido anulado.";
                qrValido = false;
            }
        }

        return ValidarQrResponse.builder()
                .credencialId(credencial.getId())
                .inscripcionId(inscripcion.getId())
                .fotografiaUrl(usuario.getFotografiaUrl())
                .nombreCompleto(usuario.getNombres() + " " + usuario.getApellidos())
                .documentoIdentidad(usuario.getCi())
                .carrera(usuario.getCarrera() != null ? usuario.getCarrera().getNombre() : "N/A")
                .facultad(usuario.getCarrera() != null ? usuario.getCarrera().getFacultad().getNombre() : "N/A")
                .evento(inscripcion.getEvento().getTitulo())
                .estadoPago(estadoPago)
                .estadoInscripcion(inscripcion.getEstado().name())
                .estadoQr(qr != null ? qr.getEstadoQr().name() : "N/A")
                .codigoParticipante(inscripcion.getCodigoParticipante())
                .puedeIngresar(qrValido)
                .mensajeValidacion(mensaje)
                .build();
    }

    @Transactional
    public ControlAccesoResponse autorizarIngreso(AutorizarIngresoRequest request) {
        Credencial credencial = credencialRepository.findById(request.getCredencialId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Credencial no encontrada"));

        CodigoQr qr = codigoQrRepository.findByCredencialId(credencial.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("QR no encontrado para esta credencial"));

        if (qr.getEstadoQr() != CodigoQr.EstadoQr.GENERADO) {
            // Si intenta autorizar algo ya usado, se registra como REINTENTO
            registrarIntento(credencial, ControlAcceso.EstadoIngreso.REINTENTO, "Intento de autorizar QR ya utilizado/anulado");
            throw new NegocioException("No se puede autorizar un QR que no esté en estado GENERADO");
        }

        Usuario usuarioControl = obtenerUsuarioActual();

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
        Credencial credencial = credencialRepository.findById(request.getCredencialId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Credencial no encontrada"));

        ControlAcceso control = registrarIntento(credencial, ControlAcceso.EstadoIngreso.DENEGADO, request.getObservacion());
        return mapearAControlAccesoResponse(control);
    }

    private ControlAcceso registrarIntento(Credencial credencial, ControlAcceso.EstadoIngreso estado, String observacion) {
        Usuario usuarioControl = obtenerUsuarioActual();
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
        return controlAccesoRepository.findByCredencialInscripcionEventoId(eventoId)
                .stream()
                .map(this::mapearAControlAccesoResponse)
                .collect(Collectors.toList());
    }

    private Usuario obtenerUsuarioActual() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByCorreoElectronico(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario actual no encontrado"));
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
