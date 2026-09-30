package bo.uajms.eventos.modulos.asistencias.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.CodigosError;
import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.asistencias.dtos.QrAsistenciaResponse;
import bo.uajms.eventos.modulos.asistencias.infraestructura.*;
import bo.uajms.eventos.modulos.eventos.entidades.EstadoEvento;
import bo.uajms.eventos.modulos.sesiones.entidades.SesionEvento;
import bo.uajms.eventos.modulos.sesiones.repositorios.SesionEventoRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QrAsistenciaService {
    private static final Duration DURACION = Duration.ofMinutes(2);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final QrAsistenciaTemporalRepository qrRepository;
    private final SesionEventoRepository sesionRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final Clock clock;

    @Transactional
    public QrAsistenciaResponse generar(UUID sesionId) {
        exigirGestor();
        SesionEvento sesion = sesionRepository.findByIdForUpdate(sesionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesion", sesionId));
        validarOwnership(sesion);
        LocalDateTime ahora = LocalDateTime.now(clock);
        LocalDateTime fin = LocalDateTime.of(sesion.getFecha(), sesion.getHoraFin());
        if (!Boolean.TRUE.equals(sesion.getActiva()) || !Boolean.TRUE.equals(sesion.getRequiereAsistencia())
                || sesion.getEvento().getEstado() != EstadoEvento.PUBLICADO || !ahora.isBefore(fin))
            throw new NegocioException("La sesion no admite emision de QR de asistencia");

        List<QrAsistenciaTemporal> activos = qrRepository.findActivosBySesionIdForUpdate(sesionId);
        activos.forEach(qr -> { qr.setActivo(false); qr.setRevocadoEn(ahora); });
        if (!activos.isEmpty()) qrRepository.saveAll(activos);

        String token = generarToken();
        LocalDateTime expiracion = ahora.plus(DURACION);
        if (expiracion.isAfter(fin)) expiracion = fin;
        Usuario generador = usuarioAutenticadoService.obtenerUsuario();
        QrAsistenciaTemporal qr = QrAsistenciaTemporal.builder().sesionEvento(sesion).tokenHash(hash(token))
                .emitidoEn(ahora).expiraEn(expiracion).generadoPor(generador).activo(true).build();
        qr = qrRepository.save(qr);
        return mapear(qr, token);
    }

    @Transactional(readOnly = true)
    public QrAsistenciaResponse obtenerActivo(UUID sesionId) {
        exigirGestor();
        SesionEvento sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesion", sesionId));
        validarOwnership(sesion);
        QrAsistenciaTemporal qr = qrRepository.findFirstBySesionEventoIdAndActivoTrueOrderByEmitidoEnDesc(sesionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("QR de asistencia", sesionId));
        validarVigencia(qr, LocalDateTime.now(clock));
        return mapear(qr, null);
    }

    @Transactional
    public QrAsistenciaTemporal resolverToken(String token) {
        if (token == null || token.isBlank()) throw new NegocioException(CodigosError.QR_INVALID, "QR invalido");
        return qrRepository.findByTokenHashForUpdate(hash(token))
                .orElseThrow(() -> new NegocioException(CodigosError.QR_INVALID, "QR invalido"));
    }

    public void validarVigencia(QrAsistenciaTemporal qr, LocalDateTime ahora) {
        if (!Boolean.TRUE.equals(qr.getActivo()) || qr.getRevocadoEn() != null)
            throw new NegocioException(CodigosError.QR_REVOKED, "QR revocado");
        if (!ahora.isBefore(qr.getExpiraEn()))
            throw new NegocioException(CodigosError.QR_EXPIRED, "QR expirado");
        if (ahora.isBefore(qr.getEmitidoEn()))
            throw new NegocioException(CodigosError.QR_INVALID, "QR invalido");
    }

    String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    private String generarToken() {
        byte[] bytes = new byte[32]; RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void exigirGestor() {
        if (!usuarioAutenticadoService.tieneRol("ADMINISTRADOR")
                && !usuarioAutenticadoService.tieneRol("ORGANIZADOR"))
            throw new AccessDeniedException("El rol no permite generar QR de asistencia");
    }

    private void validarOwnership(SesionEvento sesion) {
        if (usuarioAutenticadoService.tieneRol("ADMINISTRADOR")) return;
        UUID usuarioId = usuarioAutenticadoService.obtenerUsuario().getId();
        if (!sesion.getEvento().getOrganizador().getId().equals(usuarioId))
            throw new RecursoNoEncontradoException("Sesion", sesion.getId());
    }

    private QrAsistenciaResponse mapear(QrAsistenciaTemporal qr, String token) {
        return QrAsistenciaResponse.builder().id(qr.getId()).sesionId(qr.getSesionEvento().getId()).token(token)
                .emitidoEn(qr.getEmitidoEn()).expiraEn(qr.getExpiraEn()).activo(qr.getActivo())
                .revocadoEn(qr.getRevocadoEn()).build();
    }
}
