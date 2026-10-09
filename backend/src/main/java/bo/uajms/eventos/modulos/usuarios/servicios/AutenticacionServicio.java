package bo.uajms.eventos.modulos.usuarios.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.CodigosError;
import bo.uajms.eventos.core.excepciones.ServicioNoDisponibleException;
import bo.uajms.eventos.core.seguridad.JwtService;
import bo.uajms.eventos.core.seguridad.RolSistema;
import bo.uajms.eventos.modulos.usuarios.dtos.LoginRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.LoginResponse;
import bo.uajms.eventos.modulos.usuarios.dtos.RegistroResponse;
import bo.uajms.eventos.modulos.usuarios.dtos.ReenviarVerificacionRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.VerificarCorreoRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.RecuperarContrasenaRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.RegistroUsuarioRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.ResetContrasenaRequest;
import bo.uajms.eventos.modulos.usuarios.entidades.Rol;
import bo.uajms.eventos.modulos.usuarios.entidades.TokenRecuperacion;
import bo.uajms.eventos.modulos.usuarios.entidades.TokenVerificacionCorreo;
import bo.uajms.eventos.modulos.usuarios.entidades.SesionUsuario;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.entidades.UsuarioRol;
import bo.uajms.eventos.modulos.usuarios.mappers.UsuarioMapper;
import bo.uajms.eventos.modulos.usuarios.repositorios.RolRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.TokenRecuperacionRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.TokenVerificacionCorreoRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AutenticacionServicio {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final TokenRecuperacionRepository tokenRecuperacionRepository;
    private final TokenVerificacionCorreoRepository tokenVerificacionCorreoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UsuarioMapper usuarioMapper;
    private final CorreoServicio correoServicio;
    private final SesionUsuarioServicio sesionUsuarioServicio;

    @Value("${app.frontend.reset-password-url:http://localhost:4200/auth/restablecer-contrasena}")
    private String resetPasswordUrl;

    @Value("${app.frontend.verify-email-url:http://localhost:4200/auth/verificar-correo}")
    private String verifyEmailUrl;

    @Transactional
    public RegistroResponse registrar(RegistroUsuarioRequest request) {
        validarRegistro(request);
        String correoNormalizado = normalizarCorreo(request.getCorreoElectronico());

        if (usuarioRepository.existsByCorreoElectronicoIgnoreCase(correoNormalizado)) {
            throw new NegocioException("El correo electronico ya esta registrado");
        }
        if (usuarioRepository.existsByCi(request.getCi())) {
            throw new NegocioException("El CI ya esta registrado");
        }
        if (request.getRu() != null && !request.getRu().isBlank()
                && usuarioRepository.existsByRu(request.getRu().trim())) {
            throw new NegocioException("El RU ya esta registrado");
        }

        Usuario usuario = usuarioMapper.deRegistroRequest(request);
        usuario.setCorreoElectronico(correoNormalizado);
        usuario.setContrasena(passwordEncoder.encode(request.getContrasena()));
        usuario.setCorreoVerificado(false);

        String nombreRol = "USUARIO";

        Rol rol = rolRepository.findByNombre(nombreRol)
                .orElseThrow(() -> new NegocioException("Rol de registro no encontrado: " + nombreRol));

        usuarioRepository.save(usuario);

        UsuarioRol usuarioRol = UsuarioRol.builder()
                .usuario(usuario)
                .rol(rol)
                .build();
        usuarioRolRepository.save(usuarioRol);

        generarYEnviarTokenVerificacion(usuario);

        return RegistroResponse.builder()
                .correoElectronico(usuario.getCorreoElectronico())
                .correoVerificado(false)
                .mensaje("Registro recibido. Verifique su correo antes de iniciar sesion.")
                .build();
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String correoNormalizado = normalizarCorreo(request.getCorreoElectronico());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        correoNormalizado,
                        request.getContrasena()
                )
        );

        Usuario usuario = usuarioRepository.findByCorreoElectronicoIgnoreCaseForUpdate(correoNormalizado)
                .orElseThrow();

        if (!usuario.isActivo()) {
            throw new org.springframework.security.authentication.DisabledException("Cuenta inactiva");
        }
        if (!usuario.isCorreoVerificado()) {
            throw new NegocioException(CodigosError.EMAIL_NOT_VERIFIED,
                    "Debe verificar su correo antes de iniciar sesion");
        }

        List<String> roles = usuarioRolRepository.findByUsuarioId(usuario.getId())
                .stream()
                .map(ur -> ur.getRol().getNombre())
                .filter(RolSistema::esOficial)
                .toList();

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getCorreoElectronico());
        SesionUsuario sesion = sesionUsuarioServicio.crearSesionUnica(usuario);
        String jwtToken = jwtService.generarToken(userDetails, sesion.getId());

        return LoginResponse.builder()
                .token(jwtToken)
                .usuario(usuarioMapper.aPerfilResponse(usuario, roles))
                .build();
    }

    @Transactional
    public void solicitarRecuperacion(RecuperarContrasenaRequest request) {
        usuarioRepository.findByCorreoElectronicoIgnoreCase(normalizarCorreo(request.getCorreoElectronico()))
                .ifPresent(usuario -> {
                    try {
                        generarYEnviarTokenRecuperacion(usuario);
                    } catch (ServicioNoDisponibleException ignored) {
                        // Conserva la respuesta no enumerativa y no persiste un token que no fue entregado.
                    }
                });
    }

    @Transactional
    public void verificarCorreo(VerificarCorreoRequest request) {
        TokenVerificacionCorreo token = tokenVerificacionCorreoRepository
                .findByTokenHashForUpdate(hashToken(request.getToken()))
                .orElseThrow(() -> new NegocioException(
                        CodigosError.EMAIL_VERIFICATION_TOKEN_INVALID, "Token de verificacion invalido"));

        if (token.isUtilizado()) {
            throw new NegocioException(CodigosError.EMAIL_VERIFICATION_TOKEN_USED,
                    "Token de verificacion ya utilizado");
        }
        if (token.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new NegocioException(CodigosError.EMAIL_VERIFICATION_TOKEN_EXPIRED,
                    "Token de verificacion expirado");
        }

        token.setUtilizado(true);
        token.getUsuario().setCorreoVerificado(true);
        tokenVerificacionCorreoRepository.save(token);
        usuarioRepository.save(token.getUsuario());
    }

    @Transactional
    public void reenviarVerificacion(ReenviarVerificacionRequest request) {
        usuarioRepository.findByCorreoElectronicoIgnoreCaseForUpdate(
                        normalizarCorreo(request.getCorreoElectronico()))
                .filter(usuario -> !usuario.isCorreoVerificado())
                .ifPresent(usuario -> {
                    try {
                        generarYEnviarTokenVerificacion(usuario);
                    } catch (ServicioNoDisponibleException ignored) {
                        // La respuesta permanece no enumerativa. CorreoServicio ya registra un error sin secretos.
                    }
                });
    }

    @Transactional
    public void logout(java.util.UUID sesionId, String correoUsuario) {
        sesionUsuarioServicio.revocarSesion(sesionId, correoUsuario);
    }

    @Transactional
    public void restablecerContrasena(ResetContrasenaRequest request) {
        if (!request.getNuevaContrasena().equals(request.getConfirmacion())) {
            throw new NegocioException("Las contrasenas no coinciden");
        }

        TokenRecuperacion token = tokenRecuperacionRepository.findByToken(hashToken(request.getToken()))
                .orElseThrow(() -> new NegocioException(CodigosError.PASSWORD_RESET_TOKEN_INVALID, "Token invalido"));

        if (token.isUtilizado())
            throw new NegocioException(CodigosError.PASSWORD_RESET_TOKEN_USED, "Token ya utilizado");
        if (token.getFechaExpiracion().isBefore(LocalDateTime.now()))
            throw new NegocioException(CodigosError.PASSWORD_RESET_TOKEN_EXPIRED, "Token expirado");

        Usuario usuario = usuarioRepository.findByIdForUpdate(token.getUsuario().getId())
                .orElseThrow(() -> new NegocioException("Usuario no encontrado"));
        usuario.setContrasena(passwordEncoder.encode(request.getNuevaContrasena()));
        usuarioRepository.save(usuario);
        sesionUsuarioServicio.revocarSesionesActivas(usuario.getId());

        token.setUtilizado(true);
        tokenRecuperacionRepository.save(token);
    }

    private void generarYEnviarTokenRecuperacion(Usuario usuario) {
        String token = generarTokenUnico();
        TokenRecuperacion tokenEntity = TokenRecuperacion.builder()
                .token(hashToken(token))
                .usuario(usuario)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(30))
                .utilizado(false)
                .build();

        String enlace = UriComponentsBuilder.fromUriString(resetPasswordUrl)
                .queryParam("token", token)
                .build()
                .toUriString();

        correoServicio.enviarRecuperacionContrasena(usuario.getCorreoElectronico(), enlace);
        tokenRecuperacionRepository.findByUsuarioIdAndUtilizadoFalse(usuario.getId())
                .forEach(tokenAnterior -> tokenAnterior.setUtilizado(true));
        tokenRecuperacionRepository.save(tokenEntity);
    }

    private void generarYEnviarTokenVerificacion(Usuario usuario) {
        String token = generarTokenVerificacionUnico();
        TokenVerificacionCorreo tokenEntity = TokenVerificacionCorreo.builder()
                .tokenHash(hashToken(token))
                .usuario(usuario)
                .fechaExpiracion(LocalDateTime.now().plusHours(24))
                .utilizado(false)
                .build();

        String enlace = UriComponentsBuilder.fromUriString(verifyEmailUrl)
                .queryParam("token", token)
                .build()
                .toUriString();
        correoServicio.enviarVerificacionCorreo(usuario.getCorreoElectronico(), enlace);

        tokenVerificacionCorreoRepository.findByUsuarioIdAndUtilizadoFalse(usuario.getId())
                .forEach(tokenAnterior -> tokenAnterior.setUtilizado(true));
        tokenVerificacionCorreoRepository.save(tokenEntity);
    }

    private String generarTokenVerificacionUnico() {
        String token;
        do {
            byte[] bytes = new byte[32];
            SECURE_RANDOM.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } while (tokenVerificacionCorreoRepository.existsByTokenHash(hashToken(token)));
        return token;
    }

    private String generarTokenUnico() {
        String token;
        do {
            byte[] bytes = new byte[32];
            SECURE_RANDOM.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } while (tokenRecuperacionRepository.existsByToken(hashToken(token)));
        return token;
    }

    private String hashToken(String token) {
        if (token == null || token.isBlank()) {
            throw new NegocioException(CodigosError.PASSWORD_RESET_TOKEN_INVALID, "Token invalido");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }

    private void validarRegistro(RegistroUsuarioRequest request) {
        if (!request.getContrasena().equals(request.getConfirmacionContrasena())) {
            throw new NegocioException("La contrasena y su confirmacion no coinciden");
        }

        boolean tieneRu = request.getRu() != null && !request.getRu().isBlank();
        if (request.getTipoUsuario() == Usuario.TipoUsuario.INTERNO && !tieneRu) {
            throw new NegocioException("El RU es obligatorio para usuarios UAJMS");
        }
        if (request.getTipoUsuario() == Usuario.TipoUsuario.EXTERNO && tieneRu) {
            throw new NegocioException("El RU solo corresponde a usuarios UAJMS");
        }
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }
}
