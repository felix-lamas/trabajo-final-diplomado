package bo.uajms.eventos.modulos.certificados.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.CodigosError;
import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.asistencias.repositorios.AsistenciaRepository;
import bo.uajms.eventos.modulos.certificados.dtos.CertificadoResponse;
import bo.uajms.eventos.modulos.certificados.dtos.VerificacionCertificadoResponse;
import bo.uajms.eventos.modulos.certificados.entidades.Certificado;
import bo.uajms.eventos.modulos.certificados.repositorios.CertificadoRepository;
import bo.uajms.eventos.modulos.eventos.entidades.EstadoEvento;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.eventos.entidades.TipoCertificadoEvento;
import bo.uajms.eventos.modulos.eventos.entidades.TipoInscripcion;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.inscripciones.entidades.EstadoInscripcion;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.inscripciones.repositorios.InscripcionRepository;
import bo.uajms.eventos.modulos.pagos.entidades.EstadoPago;
import bo.uajms.eventos.modulos.pagos.repositorios.PagoRepository;
import bo.uajms.eventos.modulos.sesiones.repositorios.SesionEventoRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CertificadoService {

    private static final String INSTITUCION = "Universidad Autónoma Juan Misael Saracho - UAJMS";

    private final CertificadoRepository certificadoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final EventoRepository eventoRepository;
    private final SesionEventoRepository sesionEventoRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final PagoRepository pagoRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final CertificadoDocumentoService documentoService;
    private final Clock clock;

    @Value("${app.certificados.verificacion-base-url:http://localhost:4200/verificar-certificado}")
    private String verificacionBaseUrl = "http://localhost:4200/verificar-certificado";

    @Transactional
    public CertificadoResponse generarCertificado(UUID inscripcionId) {
        Inscripcion inscripcion = obtenerInscripcionGestionable(inscripcionId);
        var existente = certificadoRepository.findByInscripcionId(inscripcionId);
        if (existente.isPresent()) return mapear(existente.get());

        Evento evento = inscripcion.getEvento();
        validarCondicionesGenerales(inscripcion, evento);
        validarPago(inscripcion, evento);
        CalculoAsistencia calculo = calcularAsistencia(inscripcion, evento);
        Certificado.TipoCertificado tipo = resolverTipo(evento);
        validarAsistenciaYHoras(evento, tipo, calculo);

        String codigo = generarCodigoUnico();
        Certificado certificado = Certificado.builder()
                .usuario(inscripcion.getUsuario()).evento(evento).inscripcion(inscripcion)
                .codigoCertificado(codigo).fechaEmision(LocalDateTime.now(clock))
                .urlVerificacion(construirUrlVerificacion(codigo))
                .estado(Certificado.EstadoCertificado.GENERADO).tipoCertificado(tipo)
                .horasAcademicas(tipo == Certificado.TipoCertificado.CURRICULAR
                        ? evento.getHorasAcademicas() : null)
                .porcentajeAsistencia(calculo.porcentaje()).build();

        certificado = certificadoRepository.save(certificado);
        if (certificado.getId() != null) {
            certificado.setArchivoPdfUrl("/api/v1/certificados/" + certificado.getId() + "/descargar");
            certificado = certificadoRepository.save(certificado);
        }
        return mapear(certificado);
    }

    @Transactional(readOnly = true)
    public CertificadoResponse obtenerPorId(UUID id) {
        return mapear(obtenerVisible(id));
    }

    @Transactional
    public byte[] descargarPdf(UUID id) {
        Certificado certificado = obtenerVisible(id);
        byte[] pdf = documentoService.generarPdf(certificado);
        if (certificado.getEstado() == Certificado.EstadoCertificado.GENERADO) {
            certificado.setEstado(Certificado.EstadoCertificado.DESCARGADO);
            certificadoRepository.save(certificado);
        }
        return pdf;
    }

    @Transactional(readOnly = true)
    public List<CertificadoResponse> listarMisCertificados() {
        exigirRol("USUARIO");
        UUID usuarioId = usuarioAutenticadoService.obtenerUsuario().getId();
        return certificadoRepository.findByUsuarioId(usuarioId).stream().map(this::mapear).toList();
    }

    @Transactional(readOnly = true)
    public List<CertificadoResponse> listarPorEvento(UUID eventoId) {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) {
            eventoRepository.findById(eventoId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Evento", eventoId));
            return certificadoRepository.findByEventoId(eventoId).stream().map(this::mapear).toList();
        }
        if (usuarioAutenticadoService.tieneRol("ORGANIZADOR")) {
            UUID organizadorId = usuarioAutenticadoService.obtenerUsuario().getId();
            eventoRepository.findByIdAndOrganizadorId(eventoId, organizadorId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Evento", eventoId));
            return certificadoRepository.findByEventoIdAndEventoOrganizadorId(eventoId, organizadorId)
                    .stream().map(this::mapear).toList();
        }
        throw new AccessDeniedException("El rol no permite consultar certificados por evento");
    }

    @Transactional(readOnly = true)
    public VerificacionCertificadoResponse verificarCertificadoPublico(String codigo) {
        if (codigo == null || codigo.isBlank()) return noRegistrado(codigo);
        return certificadoRepository.findByCodigoCertificado(codigo.trim())
                .map(this::mapearVerificacionPublica)
                .orElseGet(() -> noRegistrado(codigo.trim()));
    }

    private Inscripcion obtenerInscripcionGestionable(UUID id) {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) {
            return inscripcionRepository.findByIdForUpdate(id)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Inscripcion", id));
        }
        if (usuarioAutenticadoService.tieneRol("ORGANIZADOR")) {
            UUID organizadorId = usuarioAutenticadoService.obtenerUsuario().getId();
            return inscripcionRepository.findByIdAndEventoOrganizadorForUpdate(id, organizadorId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Inscripcion", id));
        }
        throw new AccessDeniedException("El rol no permite generar certificados");
    }

    private Certificado obtenerVisible(UUID id) {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) {
            return certificadoRepository.findById(id)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Certificado", id));
        }
        UUID usuarioId = usuarioAutenticadoService.obtenerUsuario().getId();
        if (usuarioAutenticadoService.tieneRol("ORGANIZADOR")) {
            return certificadoRepository.findByIdAndEventoOrganizadorId(id, usuarioId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Certificado", id));
        }
        if (usuarioAutenticadoService.tieneRol("USUARIO")) {
            return certificadoRepository.findByIdAndUsuarioId(id, usuarioId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Certificado", id));
        }
        throw new AccessDeniedException("El rol no permite consultar certificados");
    }

    private void validarCondicionesGenerales(Inscripcion inscripcion, Evento evento) {
        if (evento.getEstado() != EstadoEvento.FINALIZADO)
            throw new NegocioException(CodigosError.CERTIFICATE_NOT_AVAILABLE,
                    "El certificado solo puede emitirse para un evento FINALIZADO");
        if (!Boolean.TRUE.equals(evento.getEmiteCertificado()) || evento.getTipoCertificado() == null)
            throw new NegocioException("El evento no esta configurado para emitir certificados");
        if (inscripcion.getEstado() != EstadoInscripcion.CONFIRMADA)
            throw new NegocioException(CodigosError.CERTIFICATE_NOT_AVAILABLE,
                    "La inscripcion debe estar CONFIRMADA");
        if (inscripcion.getUsuario() == null || inscripcion.getEvento() == null)
            throw new NegocioException("La inscripcion no tiene trazabilidad completa");
    }

    private void validarPago(Inscripcion inscripcion, Evento evento) {
        if (evento.getTipoInscripcion() == TipoInscripcion.GRATUITO) return;
        var pago = pagoRepository.findByInscripcionId(inscripcion.getId())
                .orElseThrow(() -> new NegocioException("El evento pagado requiere un pago aprobado"));
        if (pago.getEstado() != EstadoPago.APROBADO)
            throw new NegocioException("El evento pagado requiere un pago APROBADO");
    }

    private CalculoAsistencia calcularAsistencia(Inscripcion inscripcion, Evento evento) {
        long requeridas = sesionEventoRepository.countByEventoIdAndRequiereAsistenciaTrue(evento.getId());
        long asistidas = asistenciaRepository.countSesionesRequeridasAsistidas(inscripcion.getId(), evento.getId());
        if (asistidas > requeridas) throw new NegocioException("Los datos de asistencia son inconsistentes");
        if (requeridas == 0 || asistidas == 0)
            throw new NegocioException(CodigosError.CERTIFICATE_NOT_AVAILABLE,
                    "Debe existir asistencia registrada en una sesion requerida");
        BigDecimal porcentaje = BigDecimal.valueOf(asistidas).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(requeridas), 2, RoundingMode.HALF_UP);
        return new CalculoAsistencia(requeridas, asistidas, porcentaje);
    }

    private void validarAsistenciaYHoras(Evento evento, Certificado.TipoCertificado tipo,
                                          CalculoAsistencia calculo) {
        if (tipo == Certificado.TipoCertificado.CURRICULAR) {
            if (evento.getHorasAcademicas() == null || evento.getHorasAcademicas() <= 0)
                throw new NegocioException("El certificado curricular requiere horas academicas mayores a cero");
            if (calculo.asistidas() * 100 < calculo.requeridas() * 80)
                throw new NegocioException("No se alcanza el 80% de asistencia requerido");
            return;
        }
        // El certificado no curricular exige participacion real, no un porcentaje minimo adicional.
    }

    private Certificado.TipoCertificado resolverTipo(Evento evento) {
        return evento.getTipoCertificado() == TipoCertificadoEvento.CURRICULAR
                ? Certificado.TipoCertificado.CURRICULAR : Certificado.TipoCertificado.NO_CURRICULAR;
    }

    private String generarCodigoUnico() {
        for (int intento = 0; intento < 3; intento++) {
            String codigo = "UAJMS-" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
            if (!certificadoRepository.existsByCodigoCertificado(codigo)) return codigo;
        }
        throw new NegocioException("No fue posible generar un codigo unico de certificado");
    }

    private String construirUrlVerificacion(String codigo) {
        String base = verificacionBaseUrl.endsWith("/")
                ? verificacionBaseUrl.substring(0, verificacionBaseUrl.length() - 1)
                : verificacionBaseUrl;
        return base + "/" + codigo;
    }

    private VerificacionCertificadoResponse mapearVerificacionPublica(Certificado certificado) {
        boolean valido = certificado.getEstado() != Certificado.EstadoCertificado.ANULADO;
        Usuario usuario = certificado.getUsuario();
        return VerificacionCertificadoResponse.builder().valido(valido)
                .mensaje(valido ? "Certificado valido emitido por la UAJMS" : "Certificado anulado")
                .institucion(INSTITUCION).nombreCompleto(usuario.getNombres() + " " + usuario.getApellidos())
                .evento(certificado.getEvento().getTitulo()).tipoCertificado(certificado.getTipoCertificado().name())
                .horasAcademicas(certificado.getHorasAcademicas())
                .porcentajeAsistencia(certificado.getPorcentajeAsistencia()).fechaEmision(certificado.getFechaEmision())
                .codigoCertificado(certificado.getCodigoCertificado()).estado(certificado.getEstado().name()).build();
    }

    private VerificacionCertificadoResponse noRegistrado(String codigo) {
        return VerificacionCertificadoResponse.builder().valido(false).mensaje("Certificado no registrado")
                .institucion(INSTITUCION).codigoCertificado(codigo).estado("NO_REGISTRADO").build();
    }

    private CertificadoResponse mapear(Certificado certificado) {
        Usuario usuario = certificado.getUsuario();
        return CertificadoResponse.builder().id(certificado.getId())
                .nombreCompleto(usuario.getNombres() + " " + usuario.getApellidos()).ru(usuario.getRu()).ci(usuario.getCi())
                .evento(certificado.getEvento().getTitulo()).cargaHoraria(certificado.getEvento().getHorasAcademicas())
                .tipoCertificado(certificado.getTipoCertificado().name())
                .horasAcademicas(certificado.getHorasAcademicas())
                .porcentajeAsistencia(certificado.getPorcentajeAsistencia())
                .codigoCertificado(certificado.getCodigoCertificado()).fechaEmision(certificado.getFechaEmision())
                .urlVerificacion(certificado.getUrlVerificacion()).estado(certificado.getEstado().name())
                .archivoPdfUrl(certificado.getArchivoPdfUrl()).build();
    }

    private void exigirRol(String rol) {
        if (!usuarioAutenticadoService.tieneRol(rol)) throw new AccessDeniedException("Se requiere rol " + rol);
    }

    private record CalculoAsistencia(long requeridas, long asistidas, BigDecimal porcentaje) {}
}
