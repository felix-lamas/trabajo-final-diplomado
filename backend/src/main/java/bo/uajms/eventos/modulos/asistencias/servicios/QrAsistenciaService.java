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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;

@Service
public class QrAsistenciaService {
    private static final Duration DURACION = Duration.ofMinutes(2);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final QrAsistenciaTemporalRepository qrRepository;
    private final SesionEventoRepository sesionRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final Clock clock;
    @Value("${app.jwt.secreto}") private String jwtSecret;

    public QrAsistenciaService(QrAsistenciaTemporalRepository qrRepository,
            SesionEventoRepository sesionRepository,
            UsuarioAutenticadoService usuarioAutenticadoService, Clock clock) {
        this.qrRepository = qrRepository;
        this.sesionRepository = sesionRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.clock = clock;
    }

    @Transactional
    public QrAsistenciaResponse generar(UUID sesionId) {
        exigirGestor();
        SesionEvento sesion = sesionRepository.findByIdForUpdate(sesionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesion", sesionId));
        validarOwnership(sesion);
        sesion.setControlAsistenciaActivo(true);
        return emitir(sesion, LocalDateTime.now(clock), usuarioAutenticadoService.obtenerUsuario());
    }

    @Transactional
    public void detener(UUID sesionId) {
        exigirGestor();
        SesionEvento sesion = sesionRepository.findByIdForUpdate(sesionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesion", sesionId));
        validarOwnership(sesion);
        LocalDateTime ahora = LocalDateTime.now(clock);
        sesion.setControlAsistenciaActivo(false);
        revocarActivos(sesionId, ahora);
    }

    private QrAsistenciaResponse emitir(SesionEvento sesion, LocalDateTime ahora, Usuario generador) {
        UUID sesionId = sesion.getId();
        LocalDateTime fin = LocalDateTime.of(sesion.getFecha(), sesion.getHoraFin());
        if (!Boolean.TRUE.equals(sesion.getActiva()) || !Boolean.TRUE.equals(sesion.getRequiereAsistencia())
                || sesion.getEvento().getEstado() != EstadoEvento.PUBLICADO || !ahora.isBefore(fin))
            throw new NegocioException("La sesion no admite emision de QR de asistencia");

        revocarActivos(sesionId, ahora);

        String token = generarToken();
        LocalDateTime expiracion = ahora.plus(DURACION);
        if (expiracion.isAfter(fin)) expiracion = fin;
        QrAsistenciaTemporal qr = QrAsistenciaTemporal.builder().sesionEvento(sesion).tokenHash(hash(token)).tokenCifrado(cifrar(token))
                .emitidoEn(ahora).expiraEn(expiracion).generadoPor(generador).activo(true).build();
        qr = qrRepository.save(qr);
        return mapear(qr, token);
    }

    private void revocarActivos(UUID sesionId, LocalDateTime ahora) {
        List<QrAsistenciaTemporal> activos = qrRepository.findActivosBySesionIdForUpdate(sesionId);
        activos.forEach(qr -> { qr.setActivo(false); qr.setRevocadoEn(ahora); });
        if (!activos.isEmpty()) qrRepository.saveAll(activos);
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
        return mapear(qr, descifrar(qr.getTokenCifrado()));
    }

    @Transactional
    public QrAsistenciaTemporal resolverToken(String token) {
        if (token == null || token.isBlank()) throw new NegocioException(CodigosError.QR_INVALID, "QR invalido");
        return qrRepository.findByTokenHash(hash(token))
                .orElseThrow(() -> new NegocioException(CodigosError.QR_INVALID, "QR invalido"));
    }

    @Transactional
    public void consumirYRotar(QrAsistenciaTemporal qr, LocalDateTime ahora) {
        SesionEvento sesion = sesionRepository.findByIdForUpdate(qr.getSesionEvento().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesion", qr.getSesionEvento().getId()));
        QrAsistenciaTemporal bloqueado = qrRepository.findByTokenHashForUpdate(qr.getTokenHash())
                .orElseThrow(() -> new NegocioException(CodigosError.QR_INVALID, "QR invalido"));
        validarVigencia(bloqueado, ahora);
        if (!Boolean.TRUE.equals(sesion.getControlAsistenciaActivo()))
            throw new NegocioException(CodigosError.QR_REVOKED, "Control de asistencia detenido");
        bloqueado.setActivo(false); bloqueado.setRevocadoEn(ahora);
        qrRepository.save(bloqueado);
        emitir(sesion, ahora, bloqueado.getGeneradoPor());
    }

    @Transactional
    public void renovarExpirados() {
        for (UUID id : sesionRepository.findIdsConControlAsistenciaActivo()) renovarExpirado(id);
    }

    @Transactional
    public void renovarExpirado(UUID id) {
        SesionEvento sesion = sesionRepository.findByIdForUpdate(id).orElse(null);
        if (sesion == null || !Boolean.TRUE.equals(sesion.getControlAsistenciaActivo())) return;
        LocalDateTime ahora = LocalDateTime.now(clock);
        LocalDateTime inicio = LocalDateTime.of(sesion.getFecha(), sesion.getHoraInicio());
        LocalDateTime fin = LocalDateTime.of(sesion.getFecha(), sesion.getHoraFin());
        if (!Boolean.TRUE.equals(sesion.getActiva()) || !Boolean.TRUE.equals(sesion.getRequiereAsistencia())
                || sesion.getEvento().getEstado() != EstadoEvento.PUBLICADO || !ahora.isBefore(fin)) {
            sesion.setControlAsistenciaActivo(false);
            revocarActivos(id, ahora);
            return;
        }
        List<QrAsistenciaTemporal> activos = qrRepository.findActivosBySesionIdForUpdate(id);
        boolean vigente = activos.stream().anyMatch(q -> ahora.isBefore(q.getExpiraEn()));
        if (!vigente && !ahora.isBefore(inicio)) {
            Usuario generador = activos.isEmpty() ? sesion.getEvento().getOrganizador() : activos.get(0).getGeneradoPor();
            emitir(sesion, ahora, generador);
        }
    }

    private String cifrar(String token) {
        try {
            byte[] key = MessageDigest.getInstance("SHA-256").digest(jwtSecret.getBytes(StandardCharsets.UTF_8));
            byte[] iv = new byte[12]; RANDOM.nextBytes(iv);
            var cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, new javax.crypto.spec.SecretKeySpec(key, "AES"), new javax.crypto.spec.GCMParameterSpec(128, iv));
            byte[] data = cipher.doFinal(token.getBytes(StandardCharsets.UTF_8));
            byte[] packed = new byte[iv.length + data.length]; System.arraycopy(iv,0,packed,0,iv.length); System.arraycopy(data,0,packed,iv.length,data.length);
            return Base64.getEncoder().encodeToString(packed);
        } catch (GeneralSecurityException e) { throw new IllegalStateException("No se pudo cifrar el QR", e); }
    }

    private String descifrar(String encrypted) {
        if (encrypted == null) return null;
        try {
            byte[] packed = Base64.getDecoder().decode(encrypted), iv = Arrays.copyOfRange(packed,0,12), data = Arrays.copyOfRange(packed,12,packed.length);
            byte[] key = MessageDigest.getInstance("SHA-256").digest(jwtSecret.getBytes(StandardCharsets.UTF_8));
            var cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, new javax.crypto.spec.SecretKeySpec(key,"AES"), new javax.crypto.spec.GCMParameterSpec(128,iv));
            return new String(cipher.doFinal(data), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) { throw new IllegalStateException("No se pudo recuperar el QR", e); }
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
