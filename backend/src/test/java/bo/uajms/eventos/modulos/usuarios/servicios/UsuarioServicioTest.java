package bo.uajms.eventos.modulos.usuarios.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.usuarios.dtos.ActualizarPerfilRequest;
import bo.uajms.eventos.modulos.usuarios.dtos.CambioContrasenaRequest;
import bo.uajms.eventos.modulos.usuarios.entidades.Rol;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.entidades.UsuarioRol;
import bo.uajms.eventos.modulos.usuarios.mappers.UsuarioMapper;
import bo.uajms.eventos.modulos.usuarios.repositorios.RolRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UsuarioServicioTest {

    private UsuarioRepository usuarioRepository;
    private UsuarioRolRepository usuarioRolRepository;
    private RolRepository rolRepository;
    private PasswordEncoder passwordEncoder;
    private UsuarioAutenticadoService usuarioAutenticadoService;
    private UsuarioServicio servicio;
    private SesionUsuarioServicio sesionUsuarioServicio;
    private Usuario usuario;
    private Usuario administrador;

    @BeforeEach
    void configurar() {
        usuarioRepository = mock(UsuarioRepository.class);
        usuarioRolRepository = mock(UsuarioRolRepository.class);
        rolRepository = mock(RolRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        usuarioAutenticadoService = mock(UsuarioAutenticadoService.class);
        sesionUsuarioServicio = mock(SesionUsuarioServicio.class);
        servicio = new UsuarioServicio(usuarioRepository, usuarioRolRepository, rolRepository,
                new UsuarioMapper(), passwordEncoder, usuarioAutenticadoService, sesionUsuarioServicio);
        usuario = usuario("usuario@ejemplo.test");
        administrador = usuario("admin@ejemplo.test");
    }

    @Test
    void consultaExclusivamenteElPerfilAutenticado() {
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(usuario);
        when(usuarioRolRepository.findByUsuarioId(usuario.getId()))
                .thenReturn(List.of(relacion(usuario, "USUARIO")));

        assertEquals(usuario.getId(), servicio.obtenerPerfilActual().getId());
        verify(usuarioRepository, never()).findById(any());
    }

    @Test
    void actualizaSoloDatosEditablesDelPerfilPropio() {
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(usuario);
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioRolRepository.findByUsuarioId(usuario.getId()))
                .thenReturn(List.of(relacion(usuario, "USUARIO")));
        ActualizarPerfilRequest request = new ActualizarPerfilRequest();
        String ciOriginal = usuario.getCi();
        Usuario.TipoUsuario tipoOriginal = usuario.getTipoUsuario();
        request.setNombres("  Nuevo  ");
        request.setApellidos("  Nombre  ");
        request.setCelular("  71111111  ");

        servicio.actualizarPerfil(request);

        assertEquals("Nuevo", usuario.getNombres());
        assertEquals("Nombre", usuario.getApellidos());
        assertEquals("71111111", usuario.getCelular());
        assertEquals("usuario@ejemplo.test", usuario.getCorreoElectronico());
        assertEquals(ciOriginal, usuario.getCi());
        assertEquals(tipoOriginal, usuario.getTipoUsuario());
        verify(usuarioRolRepository, never()).save(any());
    }

    @Test
    void actualizacionPerfilNoExponeCamposProtegidosNiOtroUsuario() {
        for (String campo : List.of("id", "usuarioId", "correoElectronico", "ci", "ru", "roles",
                "contrasena", "estadoSolicitudOrganizador", "correoVerificado")) {
            assertThrows(NoSuchFieldException.class,
                    () -> ActualizarPerfilRequest.class.getDeclaredField(campo));
        }
    }

    @Test
    void cambiaContrasenaDelUsuarioAutenticado() {
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(usuario);
        when(usuarioRepository.findByIdForUpdate(usuario.getId())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Actual9!", "hash-actual")).thenReturn(true);
        when(passwordEncoder.encode("NuevaClave9!")).thenReturn("hash-nuevo");
        usuario.setContrasena("hash-actual");

        servicio.cambiarContrasena(cambio("Actual9!", "NuevaClave9!", "NuevaClave9!"));

        assertEquals("hash-nuevo", usuario.getContrasena());
        verify(usuarioRepository).save(usuario);
        verify(sesionUsuarioServicio).revocarSesionesActivas(usuario.getId());
    }

    @Test
    void rechazaContrasenaActualIncorrectaSinPersistir() {
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(usuario);
        when(usuarioRepository.findByIdForUpdate(usuario.getId())).thenReturn(Optional.of(usuario));
        usuario.setContrasena("hash-actual");
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThrows(NegocioException.class,
                () -> servicio.cambiarContrasena(cambio("Incorrecta9!", "NuevaClave9!", "NuevaClave9!")));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rechazaConfirmacionDiferenteSinPersistirNiRevocar() {
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(usuario);
        when(usuarioRepository.findByIdForUpdate(usuario.getId())).thenReturn(Optional.of(usuario));
        usuario.setContrasena("hash-actual");
        when(passwordEncoder.matches("Actual9!", "hash-actual")).thenReturn(true);

        NegocioException error = assertThrows(NegocioException.class,
                () -> servicio.cambiarContrasena(cambio("Actual9!", "NuevaClave9!", "OtraClave9!")));

        assertEquals("PASSWORD_INVALID", error.getCodigo());
        verify(usuarioRepository, never()).save(any());
        verify(sesionUsuarioServicio, never()).revocarSesionesActivas(any());
    }

    @Test
    void rechazaReutilizarContrasenaActual() {
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(usuario);
        when(usuarioRepository.findByIdForUpdate(usuario.getId())).thenReturn(Optional.of(usuario));
        usuario.setContrasena("hash-actual");
        when(passwordEncoder.matches("Actual9!", "hash-actual")).thenReturn(true);

        NegocioException error = assertThrows(NegocioException.class,
                () -> servicio.cambiarContrasena(cambio("Actual9!", "Actual9!", "Actual9!")));

        assertEquals("PASSWORD_INVALID", error.getCodigo());
        verify(usuarioRepository, never()).save(any());
        verify(sesionUsuarioServicio, never()).revocarSesionesActivas(any());
    }

    @Test
    void cambioDeContrasenaNoPermiteElegirOtroUsuario() {
        assertThrows(NoSuchFieldException.class,
                () -> CambioContrasenaRequest.class.getDeclaredField("usuarioId"));
    }

    @Test
    void usuarioSolicitaSerOrganizador() {
        autenticarUsuario();
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        var response = servicio.solicitarSerOrganizador();

        assertEquals("PENDIENTE", response.getEstado());
        assertNotNull(usuario.getFechaSolicitudOrganizador());
    }

    @Test
    void organizadorNoPuedeSolicitarNuevamente() {
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(usuario);
        when(usuarioRolRepository.existsByUsuarioIdAndRolNombre(usuario.getId(), "ORGANIZADOR"))
                .thenReturn(true);

        assertThrows(NegocioException.class, () -> servicio.solicitarSerOrganizador());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void administradorListaSolicitudesPendientes() {
        usuario.setEstadoSolicitudOrganizador(Usuario.EstadoSolicitudOrganizador.PENDIENTE);
        when(usuarioRepository.findByEstadoSolicitudOrganizadorOrderByFechaSolicitudOrganizadorAsc(
                Usuario.EstadoSolicitudOrganizador.PENDIENTE)).thenReturn(List.of(usuario));

        assertEquals(1, servicio.listarSolicitudesOrganizador(
                Usuario.EstadoSolicitudOrganizador.PENDIENTE).size());
    }

    @Test
    void administradorApruebaYReemplazaRolUsuarioPorOrganizador() {
        prepararSolicitudPendiente();
        Rol rolOrganizador = rol("ORGANIZADOR");
        when(rolRepository.findByNombre("ORGANIZADOR")).thenReturn(Optional.of(rolOrganizador));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        var response = servicio.aprobarSolicitudOrganizador(usuario.getId());

        assertEquals("APROBADA", response.getEstado());
        verify(usuarioRolRepository).deleteByUsuarioId(usuario.getId());
        verify(usuarioRolRepository).save(argThat(relacion ->
                relacion.getRol().getNombre().equals("ORGANIZADOR")));
    }

    @Test
    void administradorRechazaYUsuarioPermaneceConSuRol() {
        prepararSolicitudPendiente();
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        var response = servicio.rechazarSolicitudOrganizador(usuario.getId(), "Falta informacion");

        assertEquals("RECHAZADA", response.getEstado());
        assertEquals("Falta informacion", response.getMotivoRechazo());
        verify(usuarioRolRepository, never()).deleteByUsuarioId(any());
        verify(usuarioRolRepository, never()).save(any());
    }

    @Test
    void administradorNoPuedeAprobarseASiMismo() {
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(administrador);

        assertThrows(NegocioException.class,
                () -> servicio.aprobarSolicitudOrganizador(administrador.getId()));
        verify(usuarioRolRepository, never()).save(any());
    }

    private void autenticarUsuario() {
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(usuario);
        when(usuarioRolRepository.existsByUsuarioIdAndRolNombre(usuario.getId(), "USUARIO"))
                .thenReturn(true);
    }

    private void prepararSolicitudPendiente() {
        usuario.setEstadoSolicitudOrganizador(Usuario.EstadoSolicitudOrganizador.PENDIENTE);
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(administrador);
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(usuarioRolRepository.existsByUsuarioIdAndRolNombre(usuario.getId(), "USUARIO"))
                .thenReturn(true);
    }

    private CambioContrasenaRequest cambio(String actual, String nueva, String confirmacion) {
        CambioContrasenaRequest request = new CambioContrasenaRequest();
        request.setContrasenaActual(actual);
        request.setNuevaContrasena(nueva);
        request.setConfirmacion(confirmacion);
        return request;
    }

    private Usuario usuario(String correo) {
        Usuario resultado = Usuario.builder()
                .correoElectronico(correo).contrasena("hash")
                .nombres("Nombre").apellidos("Apellido").ci(UUID.randomUUID().toString())
                .tipoUsuario(Usuario.TipoUsuario.EXTERNO).build();
        ReflectionTestUtils.setField(resultado, "id", UUID.randomUUID());
        return resultado;
    }

    private UsuarioRol relacion(Usuario propietario, String nombreRol) {
        return UsuarioRol.builder().usuario(propietario).rol(rol(nombreRol)).build();
    }

    private Rol rol(String nombre) {
        Rol rol = Rol.builder().nombre(nombre).build();
        ReflectionTestUtils.setField(rol, "id", UUID.randomUUID());
        return rol;
    }
}
