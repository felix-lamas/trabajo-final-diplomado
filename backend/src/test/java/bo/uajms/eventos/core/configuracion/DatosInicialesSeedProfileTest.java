package bo.uajms.eventos.core.configuracion;

import bo.uajms.eventos.modulos.categorias.entidades.CategoriaEvento;
import bo.uajms.eventos.modulos.categorias.repositorios.CategoriaEventoRepository;
import bo.uajms.eventos.modulos.eventos.entidades.EstadoEvento;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.eventos.entidades.Modalidad;
import bo.uajms.eventos.modulos.eventos.entidades.TipoInscripcion;
import bo.uajms.eventos.modulos.eventos.repositorios.EventoRepository;
import bo.uajms.eventos.modulos.usuarios.entidades.Permiso;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    @Mock private EventoRepository eventoRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private DatosInicialesSeed seed;
    private Rol rolAdministrador;
    private Rol rolOrganizador;
    private Rol rolUsuario;
    private String demoPassword;
    private String contrasenaCodificada;

    @BeforeEach
    void configurarSeed() {
        seed = new DatosInicialesSeed(rolRepository, permisoRepository, usuarioRepository,
                usuarioRolRepository, rolPermisoRepository, categoriaEventoRepository, eventoRepository,
                passwordEncoder);
        demoPassword = UUID.randomUUID().toString();
        contrasenaCodificada = UUID.randomUUID().toString();
        ReflectionTestUtils.setField(seed, "demoPassword", demoPassword);

        rolAdministrador = Rol.builder().nombre("ADMINISTRADOR").build();
        ReflectionTestUtils.setField(rolAdministrador, "id", UUID.randomUUID());
        rolOrganizador = Rol.builder().nombre("ORGANIZADOR").build();
        ReflectionTestUtils.setField(rolOrganizador, "id", UUID.randomUUID());
        rolUsuario = Rol.builder().nombre("USUARIO").build();
        ReflectionTestUtils.setField(rolUsuario, "id", UUID.randomUUID());
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

    @Test
    void debePrepararOrganizadoresDemoAprobadoYPendienteConRolesCoherentes() throws Exception {
        when(rolRepository.findByNombre("ADMINISTRADOR")).thenReturn(Optional.of(rolAdministrador));
        when(rolRepository.findByNombre("ORGANIZADOR")).thenReturn(Optional.of(rolOrganizador));
        when(rolRepository.findByNombre("USUARIO")).thenReturn(Optional.of(rolUsuario));
        when(permisoRepository.findByNombre(anyString())).thenAnswer(invocacion -> {
            Permiso permiso = Permiso.builder().nombre(invocacion.getArgument(0)).build();
            ReflectionTestUtils.setField(permiso, "id", UUID.randomUUID());
            return Optional.of(permiso);
        });
        when(rolPermisoRepository.findByRolId(any(UUID.class))).thenReturn(List.of());
        when(categoriaEventoRepository.existsByNombreNormalizado(anyString())).thenReturn(true);
        when(categoriaEventoRepository.findAll()).thenReturn(categoriasDemo());
        when(eventoRepository.findAll()).thenReturn(List.of());
        when(usuarioRepository.findByCorreoElectronico(anyString())).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> {
            Usuario usuario = invocacion.getArgument(0);
            if (usuario.getId() == null) {
                ReflectionTestUtils.setField(usuario, "id", UUID.randomUUID());
            }
            return usuario;
        });
        when(usuarioRolRepository.findByUsuarioId(any(UUID.class))).thenReturn(List.of());

        seed.run();

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository, times(7)).save(usuarioCaptor.capture());
        Map<String, Usuario> usuarios = usuarioCaptor.getAllValues().stream()
                .collect(Collectors.toMap(Usuario::getCorreoElectronico, Function.identity(), (primero, ultimo) -> ultimo));

        assertEquals(4, usuarios.size());
        Usuario administrador = usuarios.get("admin@demo.local");
        Usuario organizadorAprobado = usuarios.get("organizador1@demo.local");
        Usuario organizadorPendiente = usuarios.get("organizador2@demo.local");
        Usuario participante = usuarios.get("usuario@demo.local");

        assertNotNull(administrador);
        assertEquals(Usuario.EstadoSolicitudOrganizador.APROBADA,
                organizadorAprobado.getEstadoSolicitudOrganizador());
        assertNotNull(organizadorAprobado.getFechaSolicitudOrganizador());
        assertNotNull(organizadorAprobado.getFechaResolucionOrganizador());
        assertSame(administrador, organizadorAprobado.getSolicitudResueltaPor());

        assertEquals(Usuario.EstadoSolicitudOrganizador.PENDIENTE,
                organizadorPendiente.getEstadoSolicitudOrganizador());
        assertNotNull(organizadorPendiente.getFechaSolicitudOrganizador());
        assertNull(organizadorPendiente.getFechaResolucionOrganizador());
        assertNull(organizadorPendiente.getSolicitudResueltaPor());

        assertEquals(Usuario.EstadoSolicitudOrganizador.NINGUNA,
                participante.getEstadoSolicitudOrganizador());
        assertNull(participante.getFechaSolicitudOrganizador());

        ArgumentCaptor<UsuarioRol> rolCaptor = ArgumentCaptor.forClass(UsuarioRol.class);
        verify(usuarioRolRepository, times(5)).save(rolCaptor.capture());
        Map<String, Set<String>> rolesPorCorreo = rolCaptor.getAllValues().stream()
                .collect(Collectors.groupingBy(
                        relacion -> relacion.getUsuario().getCorreoElectronico(),
                        Collectors.mapping(relacion -> relacion.getRol().getNombre(), Collectors.toSet())));

        assertEquals(Set.of("ADMINISTRADOR"), rolesPorCorreo.get("admin@demo.local"));
        assertEquals(Set.of("USUARIO", "ORGANIZADOR"), rolesPorCorreo.get("organizador1@demo.local"));
        assertEquals(Set.of("USUARIO"), rolesPorCorreo.get("organizador2@demo.local"));
        assertEquals(Set.of("USUARIO"), rolesPorCorreo.get("usuario@demo.local"));

        ArgumentCaptor<Evento> eventoCaptor = ArgumentCaptor.forClass(Evento.class);
        verify(eventoRepository, times(8)).save(eventoCaptor.capture());
        List<Evento> eventos = eventoCaptor.getAllValues();
        assertEquals(3, eventos.stream().filter(evento -> evento.getEstado() == EstadoEvento.PUBLICADO).count());
        assertEquals(3, eventos.stream().filter(evento -> evento.getEstado() == EstadoEvento.EN_REVISION).count());
        assertEquals(1, eventos.stream().filter(evento -> evento.getEstado() == EstadoEvento.BORRADOR).count());
        assertEquals(1, eventos.stream().filter(evento -> evento.getEstado() == EstadoEvento.RECHAZADO).count());
        assertTrue(eventos.stream().allMatch(evento -> evento.getOrganizador() == organizadorAprobado));
        assertTrue(eventos.stream().anyMatch(evento -> evento.getTipoInscripcion() == TipoInscripcion.PAGO
                && evento.getCosto().signum() > 0 && evento.getInstruccionesPago() != null));
        assertTrue(eventos.stream().anyMatch(evento -> evento.getTipoInscripcion() == TipoInscripcion.GRATUITO));
        assertTrue(eventos.stream().allMatch(evento -> Boolean.TRUE.equals(evento.getRequiereInscripcion())));
        assertTrue(eventos.stream().anyMatch(evento -> Boolean.TRUE.equals(evento.getEmiteCertificado())
                && evento.getHorasAcademicas() != null));
        assertTrue(eventos.stream().anyMatch(evento -> Boolean.FALSE.equals(evento.getCupoLimitado())));
        assertTrue(eventos.stream().allMatch(evento -> evento.getFechaInicio().isBefore(evento.getFechaFin())));
        assertTrue(eventos.stream().allMatch(evento -> evento.getFechaInicio().isAfter(java.time.LocalDate.now())));
        assertTrue(eventos.stream().filter(evento -> evento.getEstado() == EstadoEvento.PUBLICADO)
                .allMatch(evento -> evento.getResueltoPor() == administrador && evento.getFechaResolucion() != null));
        assertTrue(eventos.stream().filter(evento -> evento.getEstado() == EstadoEvento.EN_REVISION)
                .allMatch(evento -> evento.getResueltoPor() == null && evento.getFechaResolucion() == null));
        assertTrue(eventos.stream().filter(evento -> evento.getModalidad() == Modalidad.PRESENCIAL)
                .allMatch(evento -> evento.getUbicacion() != null && evento.getEnlaceVirtual() == null));
        assertTrue(eventos.stream().filter(evento -> evento.getModalidad() == Modalidad.VIRTUAL)
                .allMatch(evento -> evento.getEnlaceVirtual() != null && evento.getUbicacion() == null));
        assertEquals(Set.of(
                        "Congreso de Innovación Tecnológica UAJMS",
                        "Taller de Desarrollo Web",
                        "Jornada de Emprendimiento Universitario",
                        "Seminario de Inteligencia Artificial",
                        "Curso de Gestión de Proyectos",
                        "Conferencia de Innovación y Tecnología",
                        "Jornada Cultural en Preparacion",
                        "Encuentro Deportivo por Corregir"),
                eventos.stream().map(Evento::getTitulo).collect(Collectors.toSet()));
    }

    @Test
    void noDebeDuplicarNiModificarEventosDemoExistentes() {
        Usuario organizador = usuarioExistente();
        Usuario administrador = usuarioExistente();
        List<Evento> existentes = List.of(
                eventoExistente("Congreso de Innovación Tecnológica UAJMS"),
                eventoExistente("Taller de Desarrollo Web"),
                eventoExistente("Jornada de Emprendimiento Universitario"),
                eventoExistente("Seminario de Inteligencia Artificial"),
                eventoExistente("Curso de Gestión de Proyectos"),
                eventoExistente("Conferencia de Innovación y Tecnología"),
                eventoExistente("Jornada Cultural en Preparacion"),
                eventoExistente("Encuentro Deportivo por Corregir"));
        when(categoriaEventoRepository.findAll()).thenReturn(categoriasDemo());
        when(eventoRepository.findAll()).thenReturn(existentes);

        ReflectionTestUtils.invokeMethod(seed, "cargarEventosDemo", organizador, administrador);

        verify(eventoRepository, never()).save(any(Evento.class));
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
                "RU-DEMO-ADMIN", "Administrador", "Demo", Usuario.TipoUsuario.INTERNO, new Rol[]{rolAdministrador});
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

    private List<CategoriaEvento> categoriasDemo() {
        return List.of(
                categoria("Conferencia"),
                categoria("Taller"),
                categoria("Curso"),
                categoria("Seminario"),
                categoria("Cultural"),
                categoria("Deportivo"));
    }

    private CategoriaEvento categoria(String nombre) {
        CategoriaEvento categoria = CategoriaEvento.builder().nombre(nombre).estado("ACTIVO").build();
        ReflectionTestUtils.setField(categoria, "id", UUID.randomUUID());
        return categoria;
    }

    private Evento eventoExistente(String titulo) {
        Evento evento = Evento.builder().titulo(titulo).build();
        ReflectionTestUtils.setField(evento, "id", UUID.randomUUID());
        return evento;
    }
    @Test
    void seedAgregaUsuarioAlOrganizadorExistenteSinEliminarOtrosRoles() {
        Usuario existente = usuarioExistente();
        when(usuarioRepository.findByCorreoElectronico(CORREO_DEMO)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(existente)).thenReturn(existente);
        when(usuarioRolRepository.findByUsuarioId(existente.getId())).thenReturn(List.of(
                UsuarioRol.builder().usuario(existente).rol(rolOrganizador).build(),
                UsuarioRol.builder().usuario(existente).rol(rolAdministrador).build()));
        ReflectionTestUtils.invokeMethod(seed, "crearUsuarioDemo", CORREO_DEMO, "DEMO-ADMIN",
                "RU-DEMO-ADMIN", "Administrador", "Demo", Usuario.TipoUsuario.INTERNO,
                new Rol[]{rolUsuario, rolOrganizador});
        ArgumentCaptor<UsuarioRol> nueva = ArgumentCaptor.forClass(UsuarioRol.class);
        verify(usuarioRolRepository).save(nueva.capture());
        assertSame(rolUsuario, nueva.getValue().getRol());
        assertSame(existente, nueva.getValue().getUsuario());
        verify(usuarioRolRepository, never()).deleteByUsuarioId(any());
    }

}
