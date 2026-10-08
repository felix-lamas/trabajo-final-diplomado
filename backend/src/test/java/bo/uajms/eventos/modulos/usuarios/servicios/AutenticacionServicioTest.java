package bo.uajms.eventos.modulos.usuarios.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.excepciones.ServicioNoDisponibleException;
import bo.uajms.eventos.core.seguridad.JwtService;
import bo.uajms.eventos.modulos.usuarios.dtos.LoginRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.LoginResponse;
import bo.uajms.eventos.modulos.usuarios.dtos.RegistroResponse;
import bo.uajms.eventos.modulos.usuarios.dtos.RegistroUsuarioRequest;
import bo.uajms.eventos.modulos.usuarios.entidades.Rol;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.entidades.UsuarioRol;
import bo.uajms.eventos.modulos.usuarios.entidades.TokenVerificacionCorreo;
import bo.uajms.eventos.modulos.usuarios.entidades.SesionUsuario;
import bo.uajms.eventos.modulos.usuarios.mappers.UsuarioMapper;
import bo.uajms.eventos.modulos.usuarios.repositorios.RolRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.TokenRecuperacionRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.TokenVerificacionCorreoRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AutenticacionServicioTest {

    private UsuarioRepository usuarioRepository;
    private RolRepository rolRepository;
    private UsuarioRolRepository usuarioRolRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthenticationManager authenticationManager;
    private UserDetailsService userDetailsService;
    private AutenticacionServicio servicio;
    private Rol rolUsuario;
    private TokenVerificacionCorreoRepository tokenVerificacionCorreoRepository;
    private SesionUsuarioServicio sesionUsuarioServicio;
    private CorreoServicio correoServicio;

    @BeforeEach
    void configurar() {
        usuarioRepository = mock(UsuarioRepository.class);
        rolRepository = mock(RolRepository.class);
        usuarioRolRepository = mock(UsuarioRolRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        authenticationManager = mock(AuthenticationManager.class);
        userDetailsService = mock(UserDetailsService.class);
        tokenVerificacionCorreoRepository = mock(TokenVerificacionCorreoRepository.class);
        sesionUsuarioServicio = mock(SesionUsuarioServicio.class);
        correoServicio = mock(CorreoServicio.class);
        rolUsuario = rol("USUARIO");

        servicio = new AutenticacionServicio(
                usuarioRepository,
                rolRepository,
                usuarioRolRepository,
                mock(TokenRecuperacionRepository.class),
                tokenVerificacionCorreoRepository,
                passwordEncoder,
                jwtService,
                authenticationManager,
                userDetailsService,
                new UsuarioMapper(),
                correoServicio,
                sesionUsuarioServicio
        );
        ReflectionTestUtils.setField(servicio, "verifyEmailUrl", "https://app.example.test/verificar");
    }

    @Test
    void registroUajmsCreaUsuarioConRuYRolUsuario() {
        RegistroUsuarioRequest request = registroInterno();
        prepararRegistroExitoso();

        RegistroResponse response = servicio.registrar(request);

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());
        assertEquals("RU-12345", usuarioCaptor.getValue().getRu());
        assertEquals("usuario@ejemplo.test", usuarioCaptor.getValue().getCorreoElectronico());
        assertEquals("hash", usuarioCaptor.getValue().getContrasena());
        ArgumentCaptor<UsuarioRol> rolCaptor = ArgumentCaptor.forClass(UsuarioRol.class);
        verify(usuarioRolRepository).save(rolCaptor.capture());
        assertEquals("USUARIO", rolCaptor.getValue().getRol().getNombre());
        assertFalse(response.isCorreoVerificado());
        verify(jwtService, never()).generarToken(any(), any());
    }

    @Test
    void registroDejaCorreoSinVerificarYPersisteHashDeTokenConExpiracion() {
        prepararRegistroExitoso();
        ArgumentCaptor<TokenVerificacionCorreo> tokenCaptor =
                ArgumentCaptor.forClass(TokenVerificacionCorreo.class);
        ArgumentCaptor<String> enlaceCaptor = ArgumentCaptor.forClass(String.class);
        LocalDateTime inicio = LocalDateTime.now();

        RegistroResponse response = servicio.registrar(registroInterno());

        LocalDateTime fin = LocalDateTime.now();
        verify(tokenVerificacionCorreoRepository).save(tokenCaptor.capture());
        verify(correoServicio).enviarVerificacionCorreo(eq("usuario@ejemplo.test"), enlaceCaptor.capture());
        TokenVerificacionCorreo token = tokenCaptor.getValue();
        String plano = enlaceCaptor.getValue().substring(enlaceCaptor.getValue().indexOf("token=") + 6);
        assertFalse(response.isCorreoVerificado());
        assertFalse(token.isUtilizado());
        assertEquals(hash(plano), token.getTokenHash());
        assertNotEquals(plano, token.getTokenHash());
        assertFalse(token.getFechaExpiracion().isBefore(inicio.plusHours(24)));
        assertFalse(token.getFechaExpiracion().isAfter(fin.plusHours(24)));
    }

    @Test
    void falloDelProveedorEnRegistroSePropagaComoServicioNoDisponible() {
        prepararRegistroExitoso();
        doThrow(new ServicioNoDisponibleException("MAIL_SERVICE_UNAVAILABLE", "correo no disponible"))
                .when(correoServicio).enviarVerificacionCorreo(eq("usuario@ejemplo.test"), anyString());

        ServicioNoDisponibleException exception = assertThrows(ServicioNoDisponibleException.class,
                () -> servicio.registrar(registroInterno()));

        assertEquals("MAIL_SERVICE_UNAVAILABLE", exception.getCodigo());
        verify(tokenVerificacionCorreoRepository, never()).save(any());
    }

    @Test
    void registroExternoNoExigeRu() {
        RegistroUsuarioRequest request = registroInterno();
        request.setTipoUsuario(Usuario.TipoUsuario.EXTERNO);
        request.setRu(null);
        prepararRegistroExitoso();

        assertDoesNotThrow(() -> servicio.registrar(request));
    }

    @Test
    void contratoDeRegistroNoPermiteSeleccionarAdministrador() {
        assertThrows(NoSuchFieldException.class,
                () -> RegistroUsuarioRequest.class.getDeclaredField("rol"));
    }

    @Test
    void contratoDeRegistroNoPermiteSeleccionarOrganizador() {
        assertThrows(NoSuchFieldException.class,
                () -> RegistroUsuarioRequest.class.getDeclaredField("roles"));
    }

    @Test
    void rechazaCorreoDuplicadoSinPersistir() {
        RegistroUsuarioRequest request = registroInterno();
        when(usuarioRepository.existsByCorreoElectronicoIgnoreCase("usuario@ejemplo.test")).thenReturn(true);

        assertThrows(NegocioException.class, () -> servicio.registrar(request));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rechazaCiDuplicadoSinPersistir() {
        RegistroUsuarioRequest request = registroInterno();
        when(usuarioRepository.existsByCi(request.getCi())).thenReturn(true);

        assertThrows(NegocioException.class, () -> servicio.registrar(request));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rechazaRuDuplicadoSinPersistir() {
        RegistroUsuarioRequest request = registroInterno();
        when(usuarioRepository.existsByRu(request.getRu())).thenReturn(true);

        assertThrows(NegocioException.class, () -> servicio.registrar(request));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rechazaConfirmacionDeContrasenaDistinta() {
        RegistroUsuarioRequest request = registroInterno();
        request.setConfirmacionContrasena("OtraClave9!");

        assertThrows(NegocioException.class, () -> servicio.registrar(request));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void rechazaRuAusenteParaUsuarioUajms() {
        RegistroUsuarioRequest request = registroInterno();
        request.setRu(null);

        assertThrows(NegocioException.class, () -> servicio.registrar(request));
    }

    @ParameterizedTest
    @ValueSource(strings = {"USUARIO", "ORGANIZADOR", "ADMINISTRADOR"})
    void loginFuncionaParaCadaRolOficial(String nombreRol) {
        Usuario usuario = usuario();
        Rol rol = rol(nombreRol);
        LoginRequest request = new LoginRequest();
        request.setCorreoElectronico(" Usuario@Ejemplo.Test ");
        request.setContrasena("Clave9!Segura");
        when(usuarioRepository.findByCorreoElectronicoIgnoreCaseForUpdate("usuario@ejemplo.test"))
                .thenReturn(Optional.of(usuario));
        when(usuarioRolRepository.findByUsuarioId(usuario.getId()))
                .thenReturn(List.of(UsuarioRol.builder().usuario(usuario).rol(rol).build()));
        UserDetails details = User.withUsername(usuario.getCorreoElectronico())
                .password("hash").authorities("ROLE_" + nombreRol).build();
        when(userDetailsService.loadUserByUsername("usuario@ejemplo.test")).thenReturn(details);
        UUID sesionId = UUID.randomUUID();
        SesionUsuario sesion = SesionUsuario.builder().usuario(usuario).build();
        ReflectionTestUtils.setField(sesion, "id", sesionId);
        when(sesionUsuarioServicio.crearSesionUnica(usuario)).thenReturn(sesion);
        when(jwtService.generarToken(details, sesionId)).thenReturn("jwt-demo");

        LoginResponse response = servicio.login(request);

        assertEquals("jwt-demo", response.getToken());
        assertEquals(List.of(nombreRol), response.getUsuario().getRoles());
    }

    @Test
    void loginRechazaContrasenaIncorrecta() {
        LoginRequest request = new LoginRequest();
        request.setCorreoElectronico("usuario@ejemplo.test");
        request.setContrasena("Incorrecta9!");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("credenciales invalidas"));

        assertThrows(BadCredentialsException.class, () -> servicio.login(request));
        verify(jwtService, never()).generarToken(any(UserDetails.class), any(UUID.class));
    }

    @Test
    void loginRechazaUsuarioNoVerificadoSinCrearSesion() {
        LoginRequest request = new LoginRequest();
        request.setCorreoElectronico("usuario@ejemplo.test");
        request.setContrasena("Clave9!Segura");
        Usuario noVerificado = usuario();
        noVerificado.setCorreoVerificado(false);
        when(usuarioRepository.findByCorreoElectronicoIgnoreCaseForUpdate("usuario@ejemplo.test"))
                .thenReturn(Optional.of(noVerificado));

        NegocioException error = assertThrows(NegocioException.class, () -> servicio.login(request));

        assertEquals("EMAIL_NOT_VERIFIED", error.getCodigo());
        verify(sesionUsuarioServicio, never()).crearSesionUnica(any());
    }

    private void prepararRegistroExitoso() {
        when(rolRepository.findByNombre("USUARIO")).thenReturn(Optional.of(rolUsuario));
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(usuarioRepository.save(any())).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            ReflectionTestUtils.setField(usuario, "id", UUID.randomUUID());
            return usuario;
        });
        when(tokenVerificacionCorreoRepository.existsByTokenHash(anyString())).thenReturn(false);
    }

    private RegistroUsuarioRequest registroInterno() {
        RegistroUsuarioRequest request = new RegistroUsuarioRequest();
        request.setNombres("Usuario");
        request.setApellidos("Prueba");
        request.setCorreoElectronico(" Usuario@Ejemplo.Test ");
        request.setCi("CI-12345");
        request.setRu("RU-12345");
        request.setCelular("70000000");
        request.setContrasena("Clave9!Segura");
        request.setConfirmacionContrasena("Clave9!Segura");
        request.setTipoUsuario(Usuario.TipoUsuario.INTERNO);
        return request;
    }

    private Usuario usuario() {
        Usuario usuario = Usuario.builder()
                .correoElectronico("usuario@ejemplo.test")
                .contrasena("hash")
                .nombres("Usuario").apellidos("Prueba").ci("CI-12345")
                .correoVerificado(true)
                .tipoUsuario(Usuario.TipoUsuario.INTERNO).build();
        ReflectionTestUtils.setField(usuario, "id", UUID.randomUUID());
        return usuario;
    }

    private Rol rol(String nombre) {
        Rol rol = Rol.builder().nombre(nombre).build();
        ReflectionTestUtils.setField(rol, "id", UUID.randomUUID());
        return rol;
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }
}
