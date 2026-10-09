package bo.uajms.eventos.modulos.usuarios.servicios;

import bo.uajms.eventos.core.excepciones.*;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.usuarios.dtos.*;
import bo.uajms.eventos.modulos.usuarios.entidades.*;
import bo.uajms.eventos.modulos.usuarios.repositorios.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdministradorServicio {
    private final InvitacionAdministradorRepository invitaciones;
    private final UsuarioRepository usuarios;
    private final UsuarioRolRepository rolesUsuario;
    private final RolRepository roles;
    private final UsuarioAutenticadoService autenticado;
    private final PasswordEncoder encoder;
    private final CorreoServicio correo;
    private final SesionUsuarioServicio sesiones;
    private static final SecureRandom RANDOM = new SecureRandom();
    @Value("${app.auth.admin-invitation-expiration-hours:24}") private int horas = 24;
    @Value("${app.auth.admin-invitation-resend-minutes:5}") private int minutosReenvio = 5;
    @Value("${app.frontend.admin-activation-url:}") private String activationUrl;
    @Value("${app.frontend.verify-email-url:http://localhost:4200/auth/verificar-correo}") private String verifyEmailUrl;

    @Transactional(readOnly=true)
    public List<AdministradorResponse> listar() {
        exigirAdministrador();
        return usuarios.findAdministradores().stream().map(u -> new AdministradorResponse(u.getId(),
            u.getNombres(), u.getApellidos(), u.getCorreoElectronico(), u.isActivo(), u.getFechaCreacion())).toList();
    }

    @Transactional(readOnly=true)
    public List<InvitacionAdministradorResponse> listarInvitaciones() {
        exigirAdministrador();
        return invitaciones.findAllByOrderByFechaCreacionDesc().stream().map(this::respuesta).toList();
    }

    @Transactional
    public InvitacionAdministradorResponse invitar(InvitarAdministradorRequest r) {
        bloquearRol(); // Serializes invitations, acceptance and administrator deactivation across instances.
        exigirAdministrador();
        Usuario invitante = autenticado.obtenerUsuario();
        limitarInvitaciones(invitante);
        expirarPendientes();
        String email = r.getCorreo().trim().toLowerCase(Locale.ROOT);
        if (email.equalsIgnoreCase(invitante.getCorreoElectronico()))
            throw new NegocioException("No puede invitarse a si mismo.");
        validarDatosDisponibles(email, r.getCi());
        for (var anterior : invitaciones.findByEstadoAndCorreo(InvitacionAdministrador.Estado.PENDIENTE, email)) {
            if (estado(anterior) == InvitacionAdministrador.Estado.PENDIENTE)
                throw new NegocioException("Ya existe una invitacion pendiente para ese correo.");
            anterior.setEstado(InvitacionAdministrador.Estado.EXPIRADA);
        }
        invitaciones.flush();
        if (invitaciones.existsByEstadoAndCi(InvitacionAdministrador.Estado.PENDIENTE, r.getCi()))
            throw new NegocioException("Ya existe una invitacion pendiente para ese CI.");
        var i = new InvitacionAdministrador();
        i.setCorreo(email); i.setNombres(r.getNombres().trim()); i.setApellidos(r.getApellidos().trim());
        i.setCi(r.getCi()); i.setCelular(r.getCelular()); i.setInvitante(invitante);
        renovarYEnviar(i);
        return respuesta(invitaciones.save(i));
    }

    @Transactional
    public InvitacionAdministradorResponse reenviar(UUID id) {
        bloquearRol(); exigirAdministrador();
        expirarPendientes();
        limitarInvitaciones(autenticado.obtenerUsuario());
        var i = obtener(id);
        if (i.getEstado() != InvitacionAdministrador.Estado.PENDIENTE && i.getEstado() != InvitacionAdministrador.Estado.EXPIRADA)
            throw new NegocioException("La invitacion no permite reenvio.");
        if (i.getFechaUltimoEnvio().plusMinutes(minutosReenvio).isAfter(LocalDateTime.now()))
            throw new NegocioException("Espere antes de reenviar la invitacion.");
        validarDatosDisponibles(i.getCorreo(), i.getCi());
        if (invitaciones.findByEstadoAndCorreo(InvitacionAdministrador.Estado.PENDIENTE, i.getCorreo()).stream()
            .anyMatch(otra -> !otra.getId().equals(id) && estado(otra) == InvitacionAdministrador.Estado.PENDIENTE))
            throw new NegocioException("Ya existe otra invitacion pendiente para ese correo.");
        renovarYEnviar(i); // Replaces hash: the former link immediately becomes invalid.
        return respuesta(invitaciones.save(i));
    }

    @Transactional
    public InvitacionAdministradorResponse revocar(UUID id) {
        bloquearRol(); exigirAdministrador();
        var i = obtener(id);
        if (estado(i) != InvitacionAdministrador.Estado.PENDIENTE)
            throw new NegocioException("Solo se puede revocar una invitacion pendiente.");
        i.setEstado(InvitacionAdministrador.Estado.REVOCADA);
        return respuesta(invitaciones.save(i));
    }

    @Transactional(readOnly=true)
    public Map<String, String> consultar(String token) {
        var i = invitaciones.findByTokenHash(hash(token)).orElseThrow(this::invalida);
        return Map.of("estado", estado(i).name()); // No PII, UUID, email or token is public.
    }

    @Transactional
    public void aceptar(AceptarInvitacionAdministradorRequest r) {
        String tokenHash = hash(r.getToken());
        // Invalid public requests must not acquire the global administrative write lock.
        if (!invitaciones.existsByTokenHashAndEstadoAndFechaExpiracionAfter(tokenHash,
                InvitacionAdministrador.Estado.PENDIENTE, LocalDateTime.now())) throw invalida();
        Rol rol = bloquearRol();
        var i = invitaciones.findByTokenHashForUpdate(tokenHash).orElseThrow(this::invalida);
        if (estado(i) != InvitacionAdministrador.Estado.PENDIENTE) throw invalida();
        if (!r.getContrasena().equals(r.getConfirmacionContrasena()))
            throw new NegocioException("Las contrasenas no coinciden.");
        if (!i.getCi().equals(r.getCi())) throw new NegocioException("Los datos no coinciden con la invitacion.");
        // Recheck uniqueness at acceptance: never promote or overwrite an existing account.
        validarDatosDisponibles(i.getCorreo(), i.getCi());
        Usuario u = Usuario.builder().correoElectronico(i.getCorreo()).ci(i.getCi())
            .nombres(r.getNombres().trim()).apellidos(r.getApellidos().trim()).celular(r.getCelular())
            .tipoUsuario(Usuario.TipoUsuario.EXTERNO).correoVerificado(true).activo(true)
            .contrasena(encoder.encode(r.getContrasena())).build();
        usuarios.saveAndFlush(u);
        rolesUsuario.save(UsuarioRol.builder().usuario(u).rol(rol).build());
        i.setUsuario(u); i.setEstado(InvitacionAdministrador.Estado.ACEPTADA);
        i.setFechaAceptacion(LocalDateTime.now()); invitaciones.save(i);
    }

    @Transactional
    public void desactivar(UUID id) {
        bloquearRol(); exigirAdministrador();
        Usuario u = usuarios.findByIdForUpdate(id).orElseThrow(() -> new RecursoNoEncontradoException("Administrador", id));
        if (!rolesUsuario.existsByUsuarioIdAndRolNombre(id, "ADMINISTRADOR"))
            throw new RecursoNoEncontradoException("Administrador", id);
        if (!u.isActivo()) throw new NegocioException("El administrador ya esta inactivo.");
        if (usuarios.findAdministradores().stream().filter(Usuario::isActivo).count() <= 1)
            throw new NegocioException("No se puede desactivar al último administrador activo.");
        if ("admin@demo.local".equalsIgnoreCase(u.getCorreoElectronico()))
            throw new NegocioException("La cuenta demo esta protegida durante esta etapa.");
        u.setActivo(false); usuarios.save(u); sesiones.revocarSesionesActivas(id);
    }

    private void expirarPendientes() {
        invitaciones.findByEstado(InvitacionAdministrador.Estado.PENDIENTE).stream()
            .filter(i -> estado(i) == InvitacionAdministrador.Estado.EXPIRADA)
            .forEach(i -> i.setEstado(InvitacionAdministrador.Estado.EXPIRADA));
        invitaciones.flush();
    }
    private void limitarInvitaciones(Usuario invitante) {
        if (invitaciones.countByInvitanteIdAndFechaUltimoEnvioAfter(invitante.getId(), LocalDateTime.now().minusHours(1)) >= 10)
            throw new NegocioException("Se alcanzo el limite de invitaciones. Intente mas tarde.");
    }
    private void exigirAdministrador() {
        if (!autenticado.tieneRol("ADMINISTRADOR")) throw new SeguridadException("Se requiere ADMINISTRADOR.");
        if (!autenticado.obtenerUsuario().isActivo()) throw new SeguridadException("La cuenta esta inactiva.");
    }
    private Rol bloquearRol() {
        return roles.findByNombreForUpdate("ADMINISTRADOR").orElseThrow(() -> new NegocioException("Rol administrativo no disponible."));
    }
    private InvitacionAdministrador obtener(UUID id) {
        return invitaciones.findByIdForUpdate(id).orElseThrow(() -> new RecursoNoEncontradoException("Invitacion", id));
    }
    private void validarDatosDisponibles(String email, String ci) {
        if (usuarios.existsByCorreoElectronicoIgnoreCase(email))
            throw new NegocioException("La dirección de correo ya está asociada a una cuenta.");
        if (usuarios.existsByCi(ci)) throw new NegocioException("El CI ya esta asociado a una cuenta.");
    }
    private void renovarYEnviar(InvitacionAdministrador i) {
        if (horas < 1 || minutosReenvio < 1) throw new IllegalStateException("Configuracion de invitacion invalida");
        byte[] bytes = new byte[32]; RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        i.setTokenHash(hash(token)); i.setEstado(InvitacionAdministrador.Estado.PENDIENTE);
        i.setFechaUltimoEnvio(LocalDateTime.now()); i.setFechaExpiracion(i.getFechaUltimoEnvio().plusHours(horas));
        String base = activationUrl == null || activationUrl.isBlank()
            ? UriComponentsBuilder.fromUriString(verifyEmailUrl).replacePath("/auth/activar-administrador").replaceQuery(null).build().toUriString()
            : activationUrl;
        String enlace = UriComponentsBuilder.fromUriString(base).queryParam("token", token).build().toUriString();
        invitaciones.saveAndFlush(i);
        correo.enviarInvitacionAdministrador(i.getCorreo(), enlace, i.getFechaExpiracion());
    }
    private InvitacionAdministrador.Estado estado(InvitacionAdministrador i) {
        if (i.getEstado() == InvitacionAdministrador.Estado.PENDIENTE && !LocalDateTime.now().isBefore(i.getFechaExpiracion()))
            return InvitacionAdministrador.Estado.EXPIRADA;
        return i.getEstado();
    }
    private InvitacionAdministradorResponse respuesta(InvitacionAdministrador i) {
        return new InvitacionAdministradorResponse(i.getId(), i.getCorreo(), i.getNombres(), i.getApellidos(),
            estado(i).name(), i.getFechaCreacion(), i.getFechaExpiracion(), i.getFechaAceptacion());
    }
    private NegocioException invalida() { return new NegocioException("ADMIN_INVITATION_INVALID", "La invitacion no es valida o ya no esta disponible."); }
    private String hash(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) throw invalida();
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException("SHA-256 no disponible", e); }
    }
}
