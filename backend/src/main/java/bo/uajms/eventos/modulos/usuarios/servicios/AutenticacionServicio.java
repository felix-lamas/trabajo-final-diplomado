package bo.uajms.eventos.modulos.usuarios.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.CodigosError;
import bo.uajms.eventos.core.seguridad.JwtService;
import bo.uajms.eventos.core.seguridad.RolSistema;
import bo.uajms.eventos.modulos.usuarios.dtos.LoginRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.LoginResponse;
import bo.uajms.eventos.modulos.usuarios.dtos.RecuperarContrasenaRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.RegistroUsuarioRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.ResetContrasenaRequest;
import bo.uajms.eventos.modulos.usuarios.entidades.Rol;
import bo.uajms.eventos.modulos.usuarios.entidades.TokenRecuperacion;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.entidades.UsuarioRol;
import bo.uajms.eventos.modulos.usuarios.mappers.UsuarioMapper;
import bo.uajms.eventos.modulos.usuarios.repositorios.RolRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.TokenRecuperacionRepository;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UsuarioMapper usuarioMapper;
    private final CorreoServicio correoServicio;

    @Value("${app.frontend.reset-password-url:http://localhost:4200/auth/restablecer-contrasena}")
    private String resetPasswordUrl;

    @Transactional
    public LoginResponse registrar(RegistroUsuarioRequest request) {
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

        String nombreRol = "USUARIO";

        Rol rol = rolRepository.findByNombre(nombreRol)
                .orElseThrow(() -> new NegocioException("Rol de registro no encontrado: " + nombreRol));

        usuarioRepository.save(usuario);

        UsuarioRol usuarioRol = UsuarioRol.builder()
                .usuario(usuario)
                .rol(rol)
                .build();
        usuarioRolRepository.save(usuarioRol);

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getCorreoElectronico());
        String jwtToken = jwtService.generarToken(userDetails);

        return LoginResponse.builder()
                .token(jwtToken)
                .usuario(usuarioMapper.aPerfilResponse(usuario, List.of(rol.getNombre())))
                .build();
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String correoNormalizado = normalizarCorreo(request.getCorreoElectronico());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        correoNormalizado,
                        request.getContrasena()
                )
        );

        Usuario usuario = usuarioRepository.findByCorreoElectronicoIgnoreCase(correoNormalizado)
                .orElseThrow();

        List<String> roles = usuarioRolRepository.findByUsuarioId(usuario.getId())
                .stream()
                .map(ur -> ur.getRol().getNombre())
                .filter(RolSistema::esOficial)
                .toList();

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getCorreoElectronico());
        String jwtToken = jwtService.generarToken(userDetails);

        return LoginResponse.builder()
                .token(jwtToken)
                .usuario(usuarioMapper.aPerfilResponse(usuario, roles))
                .build();
    }

    @Transactional
    public void solicitarRecuperacion(RecuperarContrasenaRequest request) {
        usuarioRepository.findByCorreoElectronicoIgnoreCase(normalizarCorreo(request.getCorreoElectronico()))
                .ifPresent(this::generarYEnviarTokenRecuperacion);
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

        Usuario usuario = token.getUsuario();
        usuario.setContrasena(passwordEncoder.encode(request.getNuevaContrasena()));
        usuarioRepository.save(usuario);

        token.setUtilizado(true);
        tokenRecuperacionRepository.save(token);
    }

    @Transactional
    public void resetearContrasena(ResetContrasenaRequest request) {
        restablecerContrasena(request);
    }

    private void generarYEnviarTokenRecuperacion(Usuario usuario) {
        tokenRecuperacionRepository.findByUsuarioIdAndUtilizadoFalse(usuario.getId())
                .forEach(tokenAnterior -> tokenAnterior.setUtilizado(true));

        String token = generarTokenUnico();
        TokenRecuperacion tokenEntity = TokenRecuperacion.builder()
                .token(hashToken(token))
                .usuario(usuario)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(30))
                .utilizado(false)
                .build();

        tokenRecuperacionRepository.save(tokenEntity);

        String enlace = UriComponentsBuilder.fromUriString(resetPasswordUrl)
                .queryParam("token", token)
                .build()
                .toUriString();

        correoServicio.enviarRecuperacionContrasena(usuario.getCorreoElectronico(), enlace);
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
