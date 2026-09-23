package bo.uajms.eventos.modulos.usuarios.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.seguridad.JwtService;
import bo.uajms.eventos.modulos.usuarios.dtos.LoginRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.LoginResponse;
import bo.uajms.eventos.modulos.usuarios.dtos.RegistroUsuarioRequest;
import bo.uajms.eventos.modulos.usuarios.entidades.Rol;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.entidades.UsuarioRol;
import bo.uajms.eventos.modulos.usuarios.mappers.UsuarioMapper;
import bo.uajms.eventos.modulos.usuarios.repositorios.RolRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.TokenRecuperacionRepository;
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

    @BeforeEach
    void configurar() {
        usuarioRepository = mock(UsuarioRepository.class);
        rolRepository = mock(RolRepository.class);
        usuarioRolRepository = mock(UsuarioRolRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        authenticationManager = mock(AuthenticationManager.class);
        userDetailsService = mock(UserDetailsService.class);
        rolUsuario = rol("USUARIO");

        servicio = new AutenticacionServicio(
                usuarioRepository,
                rolRepository,
                usuarioRolRepository,
                mock(TokenRecuperacionRepository.class),
                passwordEncoder,
                jwtService,
                authenticationManager,
                userDetailsService,
                new UsuarioMapper(),
                mock(CorreoServicio.class)
        );
    }

    @Test
    void registroUajmsCreaUsuarioConRuYRolUsuario() {
        RegistroUsuarioRequest request = registroInterno();
        prepararRegistroExitoso();

        LoginResponse response = servicio.registrar(request);

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());
        assertEquals("RU-12345", usuarioCaptor.getValue().getRu());
        assertEquals("usuario@ejemplo.test", usuarioCaptor.getValue().getCorreoElectronico());
        assertEquals("hash", usuarioCaptor.getValue().getContrasena());
        ArgumentCaptor<UsuarioRol> rolCaptor = ArgumentCaptor.forClass(UsuarioRol.class);
        verify(usuarioRolRepository).save(rolCaptor.capture());
        assertEquals("USUARIO", rolCaptor.getValue().getRol().getNombre());
        assertEquals(List.of("USUARIO"), response.getUsuario().getRoles());
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
        when(usuarioRepository.findByCorreoElectronicoIgnoreCase("usuario@ejemplo.test"))
                .thenReturn(Optional.of(usuario));
        when(usuarioRolRepository.findByUsuarioId(usuario.getId()))
                .thenReturn(List.of(UsuarioRol.builder().usuario(usuario).rol(rol).build()));
        UserDetails details = User.withUsername(usuario.getCorreoElectronico())
                .password("hash").authorities("ROLE_" + nombreRol).build();
        when(userDetailsService.loadUserByUsername("usuario@ejemplo.test")).thenReturn(details);
        when(jwtService.generarToken(details)).thenReturn("jwt-demo");

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
        verify(jwtService, never()).generarToken(any(UserDetails.class));
    }

    private void prepararRegistroExitoso() {
        when(rolRepository.findByNombre("USUARIO")).thenReturn(Optional.of(rolUsuario));
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(usuarioRepository.save(any())).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            ReflectionTestUtils.setField(usuario, "id", UUID.randomUUID());
            return usuario;
        });
        UserDetails details = User.withUsername("usuario@ejemplo.test")
                .password("hash").authorities("ROLE_USUARIO").build();
        when(userDetailsService.loadUserByUsername("usuario@ejemplo.test")).thenReturn(details);
        when(jwtService.generarToken(details)).thenReturn("jwt-demo");
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
                .tipoUsuario(Usuario.TipoUsuario.INTERNO).build();
        ReflectionTestUtils.setField(usuario, "id", UUID.randomUUID());
        return usuario;
    }

    private Rol rol(String nombre) {
        Rol rol = Rol.builder().nombre(nombre).build();
        ReflectionTestUtils.setField(rol, "id", UUID.randomUUID());
        return rol;
    }
}
