package bo.uajms.eventos.core.configuracion;

import bo.uajms.eventos.modulos.categorias.repositorios.CategoriaEventoRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Rol;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.entidades.UsuarioRol;
import bo.uajms.eventos.modulos.usuarios.repositorios.PermisoRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.RolPermisoRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.RolRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatosInicialesSeedProfileTest {

    private static final String CORREO_DEMO = "admin@demo.local";

    @Mock private RolRepository rolRepository;
    @Mock private PermisoRepository permisoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private UsuarioRolRepository usuarioRolRepository;
    @Mock private RolPermisoRepository rolPermisoRepository;
    @Mock private CategoriaEventoRepository categoriaEventoRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private DatosInicialesSeed seed;
    private Rol rolAdministrador;
    private String demoPassword;
    private String contrasenaCodificada;

    @BeforeEach
    void configurarSeed() {
        seed = new DatosInicialesSeed(rolRepository, permisoRepository, usuarioRepository,
                usuarioRolRepository, rolPermisoRepository, categoriaEventoRepository, passwordEncoder);
        demoPassword = UUID.randomUUID().toString();
        contrasenaCodificada = UUID.randomUUID().toString();
        ReflectionTestUtils.setField(seed, "demoPassword", demoPassword);

        rolAdministrador = Rol.builder().nombre("ADMINISTRADOR").build();
        ReflectionTestUtils.setField(rolAdministrador, "id", UUID.randomUUID());
        lenient().when(passwordEncoder.encode(demoPassword)).thenReturn(contrasenaCodificada);
    }

    @Test
    void debeActivarseExclusivamenteConElPerfilDemo() {
        Profile profile = DatosInicialesSeed.class.getAnnotation(Profile.class);

        assertNotNull(profile, "El seed demo debe estar restringido por un perfil Spring");
        assertArrayEquals(new String[]{"demo"}, profile.value());
    }

    @Test
    void noDebeRegistrarseEnPerfilesNormales() {
        assertFalse(esCandidatoConPerfil("dev"));
        assertFalse(esCandidatoConPerfil("prod"));
    }

    @Test
    void debeRegistrarseCuandoElPerfilDemoSeActivaExplicitamente() {
        assertTrue(esCandidatoConPerfil("demo"));
    }

    @Test
    void debeCrearUsuarioInexistenteConContrasenaCodificada() {
        when(usuarioRepository.findByCorreoElectronico(CORREO_DEMO)).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> {
            Usuario usuario = invocacion.getArgument(0);
            ReflectionTestUtils.setField(usuario, "id", UUID.randomUUID());
            return usuario;
        });
        when(usuarioRolRepository.findByUsuarioId(any(UUID.class))).thenReturn(List.of());

        crearAdministradorDemo();

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());
        assertEquals(CORREO_DEMO, usuarioCaptor.getValue().getCorreoElectronico());
        assertEquals(contrasenaCodificada, usuarioCaptor.getValue().getContrasena());
        assertFalse(demoPassword.equals(usuarioCaptor.getValue().getContrasena()));
    }

    @Test
    void debeActualizarSoloLaContrasenaDelUsuarioExistente() {
        Usuario existente = usuarioExistente();
        UUID idOriginal = existente.getId();
        String nombreOriginal = existente.getNombres();
        when(usuarioRepository.findByCorreoElectronico(CORREO_DEMO)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(existente)).thenReturn(existente);
        when(usuarioRolRepository.findByUsuarioId(idOriginal))
                .thenReturn(List.of(UsuarioRol.builder().usuario(existente).rol(rolAdministrador).build()));

        crearAdministradorDemo();

        assertEquals(idOriginal, existente.getId());
        assertEquals(nombreOriginal, existente.getNombres());
        assertEquals(contrasenaCodificada, existente.getContrasena());
        verify(usuarioRepository).save(existente);
    }

    @Test
    void debeAsignarElRolEsperadoCuandoFalta() {
        Usuario existente = usuarioExistente();
        when(usuarioRepository.findByCorreoElectronico(CORREO_DEMO)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(existente)).thenReturn(existente);
        when(usuarioRolRepository.findByUsuarioId(existente.getId())).thenReturn(List.of());

        crearAdministradorDemo();

        ArgumentCaptor<UsuarioRol> relacionCaptor = ArgumentCaptor.forClass(UsuarioRol.class);
        verify(usuarioRolRepository).save(relacionCaptor.capture());
        assertSame(existente, relacionCaptor.getValue().getUsuario());
        assertSame(rolAdministrador, relacionCaptor.getValue().getRol());
    }

    @Test
    void noDebeDuplicarUsuarioNiRolEnEjecucionesRepetidas() {
        Usuario existente = usuarioExistente();
        UsuarioRol relacionExistente = UsuarioRol.builder().usuario(existente).rol(rolAdministrador).build();
        when(usuarioRepository.findByCorreoElectronico(CORREO_DEMO)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(existente)).thenReturn(existente);
        when(usuarioRolRepository.findByUsuarioId(existente.getId())).thenReturn(List.of(relacionExistente));

        crearAdministradorDemo();
        crearAdministradorDemo();

        verify(usuarioRepository, times(2)).findByCorreoElectronico(CORREO_DEMO);
        verify(usuarioRepository, times(2)).save(existente);
        verify(usuarioRolRepository, never()).save(any(UsuarioRol.class));
    }

    private boolean esCandidatoConPerfil(String perfil) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.setActiveProfiles(perfil);

        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(true, environment);

        return scanner.findCandidateComponents("bo.uajms.eventos.core.configuracion")
                .stream()
                .anyMatch(bean -> DatosInicialesSeed.class.getName().equals(bean.getBeanClassName()));
    }

    private void crearAdministradorDemo() {
        ReflectionTestUtils.invokeMethod(seed, "crearUsuarioDemo", CORREO_DEMO, "DEMO-ADMIN",
                "RU-DEMO-ADMIN", "Administrador", "Demo", Usuario.TipoUsuario.INTERNO, rolAdministrador);
    }

    private Usuario usuarioExistente() {
        Usuario usuario = Usuario.builder()
                .correoElectronico(CORREO_DEMO)
                .contrasena(UUID.randomUUID().toString())
                .nombres("Administrador")
                .apellidos("Demo")
                .ci("DEMO-ADMIN")
                .ru("RU-DEMO-ADMIN")
                .celular("70000000")
                .tipoUsuario(Usuario.TipoUsuario.INTERNO)
                .build();
        ReflectionTestUtils.setField(usuario, "id", UUID.randomUUID());
        return usuario;
    }
}
