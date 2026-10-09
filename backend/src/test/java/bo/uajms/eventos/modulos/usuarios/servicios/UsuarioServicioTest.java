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
    private bo.uajms.eventos.modulos.usuarios.repositorios.SolicitudOrganizadorHistorialRepository historialRepository;
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
        historialRepository = mock(bo.uajms.eventos.modulos.usuarios.repositorios.SolicitudOrganizadorHistorialRepository.class);
        servicio = new UsuarioServicio(usuarioRepository, usuarioRolRepository, rolRepository,
                new UsuarioMapper(), passwordEncoder, usuarioAutenticadoService, sesionUsuarioServicio, historialRepository);
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

        var response = servicio.solicitarSerOrganizador(solicitudValida());

        assertEquals("PENDIENTE", response.getEstado());
        assertNotNull(usuario.getFechaSolicitudOrganizador());
        verify(usuarioRepository).findByIdForUpdate(usuario.getId());
    }

    @Test
    void solicitudPendienteNoPuedeDuplicarse() {
        usuario.setEstadoSolicitudOrganizador(Usuario.EstadoSolicitudOrganizador.PENDIENTE);
        autenticarUsuario();

        assertThrows(NegocioException.class, () -> servicio.solicitarSerOrganizador(solicitudValida()));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void solicitudRechazadaPuedePresentarseNuevamenteSinEspera() {
        usuario.setEstadoSolicitudOrganizador(Usuario.EstadoSolicitudOrganizador.RECHAZADA);
        usuario.setMotivoRechazoOrganizador("Informacion incompleta");
        autenticarUsuario();
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        var response = servicio.solicitarSerOrganizador(solicitudValida());

        assertEquals("PENDIENTE", response.getEstado());
        assertNull(response.getMotivoRechazo());
        assertNull(response.getFechaResolucion());
    }

    @Test
    void organizadorNoPuedeSolicitarNuevamente() {
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(usuario);
        when(usuarioRepository.findByIdForUpdate(usuario.getId())).thenReturn(Optional.of(usuario));
        when(usuarioRolRepository.existsByUsuarioIdAndRolNombre(usuario.getId(), "USUARIO"))
                .thenReturn(true);
        when(usuarioRolRepository.existsByUsuarioIdAndRolNombre(usuario.getId(), "ORGANIZADOR"))
                .thenReturn(true);

        assertThrows(NegocioException.class, () -> servicio.solicitarSerOrganizador(solicitudValida()));
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
    void administradorApruebaYConservaRolUsuarioAgregandoOrganizador() {
        prepararSolicitudPendiente();
        Rol rolOrganizador = rol("ORGANIZADOR");
        when(rolRepository.findByNombre("ORGANIZADOR")).thenReturn(Optional.of(rolOrganizador));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        var response = servicio.aprobarSolicitudOrganizador(usuario.getId());

        assertEquals("APROBADA", response.getEstado());
        verify(usuarioRolRepository, never()).deleteByUsuarioId(any());
        verify(usuarioRolRepository).save(argThat(relacion ->
                relacion.getRol().getNombre().equals("ORGANIZADOR")));
        verify(usuarioRepository).findByIdForUpdate(usuario.getId());
    }

    @Test
    void aprobarSolicitudInexistenteFalla() {
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(administrador);
        when(usuarioRepository.findByIdForUpdate(usuario.getId())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> servicio.aprobarSolicitudOrganizador(usuario.getId()));
        verify(usuarioRolRepository, never()).save(any());
    }

    @Test
    void aprobarSolicitudNoPendienteFalla() {
        usuario.setEstadoSolicitudOrganizador(Usuario.EstadoSolicitudOrganizador.RECHAZADA);
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(administrador);
        when(usuarioRepository.findByIdForUpdate(usuario.getId())).thenReturn(Optional.of(usuario));

        assertThrows(NegocioException.class, () -> servicio.aprobarSolicitudOrganizador(usuario.getId()));
        verify(usuarioRolRepository, never()).save(any());
    }

    @Test
    void dosAprobacionesSoloPermitenUnaTransicion() {
        prepararSolicitudPendiente();
        when(rolRepository.findByNombre("ORGANIZADOR")).thenReturn(Optional.of(rol("ORGANIZADOR")));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        servicio.aprobarSolicitudOrganizador(usuario.getId());

        assertThrows(NegocioException.class, () -> servicio.aprobarSolicitudOrganizador(usuario.getId()));
        verify(usuarioRolRepository, times(1)).save(any(UsuarioRol.class));
        verify(usuarioRolRepository, never()).deleteByUsuarioId(any());
    }

    @Test
    void aprobarYRechazarSoloPermitenLaPrimeraTransicion() {
        prepararSolicitudPendiente();
        when(rolRepository.findByNombre("ORGANIZADOR")).thenReturn(Optional.of(rol("ORGANIZADOR")));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        servicio.aprobarSolicitudOrganizador(usuario.getId());

        assertThrows(NegocioException.class,
                () -> servicio.rechazarSolicitudOrganizador(usuario.getId(), "Rechazo tardio"));
        assertEquals(Usuario.EstadoSolicitudOrganizador.APROBADA, usuario.getEstadoSolicitudOrganizador());
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
        when(usuarioRepository.findByIdForUpdate(usuario.getId())).thenReturn(Optional.of(usuario));
        when(usuarioRolRepository.existsByUsuarioIdAndRolNombre(usuario.getId(), "USUARIO"))
                .thenReturn(true);
    }

    private void prepararSolicitudPendiente() {
        usuario.setEstadoSolicitudOrganizador(Usuario.EstadoSolicitudOrganizador.PENDIENTE);
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(administrador);
        when(usuarioRepository.findByIdForUpdate(usuario.getId())).thenReturn(Optional.of(usuario));
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
    @Test
    void aprobacionConservaIdentidadRolesYResolutor() {
        prepararSolicitudPendiente();
        UUID idOriginal = usuario.getId();
        UsuarioRol usuarioOriginal = relacion(usuario, "USUARIO");
        var asignaciones = new java.util.ArrayList<UsuarioRol>(List.of(usuarioOriginal));
        when(usuarioRolRepository.findByUsuarioId(idOriginal)).thenReturn(asignaciones);
        when(rolRepository.findByNombre("ORGANIZADOR")).thenReturn(Optional.of(rol("ORGANIZADOR")));
        when(usuarioRolRepository.save(any(UsuarioRol.class))).thenAnswer(invocacion -> {
            UsuarioRol nueva = invocacion.getArgument(0);
            asignaciones.add(nueva);
            return nueva;
        });
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        var respuesta = servicio.aprobarSolicitudOrganizador(idOriginal);
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(usuario);
        var perfil = servicio.obtenerPerfilActual();

        assertEquals(idOriginal, perfil.getId());
        assertEquals(List.of("USUARIO", "ORGANIZADOR"), perfil.getRoles());
        assertSame(usuarioOriginal, asignaciones.getFirst());
        assertEquals(administrador.getId(), respuesta.getResueltaPorId());
        assertNotNull(respuesta.getFechaResolucion());
        assertEquals("APROBADA", perfil.getEstadoSolicitudOrganizador());
        verify(usuarioRolRepository, never()).deleteByUsuarioId(any());
        verify(usuarioRepository, never()).delete(any());
        verifyNoInteractions(sesionUsuarioServicio);
    }

    @Test
    void solicitudPendienteConOrganizadorExistenteSeResuelveSinDuplicarlo() {
        prepararSolicitudPendiente();
        UsuarioRol rolUsuario = relacion(usuario, "USUARIO");
        UsuarioRol rolOrganizador = relacion(usuario, "ORGANIZADOR");
        when(usuarioRolRepository.findByUsuarioId(usuario.getId())).thenReturn(List.of(rolUsuario, rolOrganizador));
        when(rolRepository.findByNombre("ORGANIZADOR")).thenReturn(Optional.of(rolOrganizador.getRol()));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        assertEquals("APROBADA", servicio.aprobarSolicitudOrganizador(usuario.getId()).getEstado());
        verify(usuarioRolRepository, never()).save(any());
        verify(usuarioRolRepository, never()).deleteByUsuarioId(any());
    }

    @Test
    void solicitudDeAdministradorNoSeApruebaNiSeEliminanSusRoles() {
        prepararSolicitudPendiente();
        when(usuarioRolRepository.existsByUsuarioIdAndRolNombre(usuario.getId(), "ADMINISTRADOR")).thenReturn(true);
        assertThrows(NegocioException.class, () -> servicio.aprobarSolicitudOrganizador(usuario.getId()));
        verify(usuarioRolRepository, never()).save(any());
        verify(usuarioRolRepository, never()).deleteByUsuarioId(any());
    }

    private bo.uajms.eventos.modulos.usuarios.dtos.SolicitarOrganizadorRequest solicitudValida() {
        var request = new bo.uajms.eventos.modulos.usuarios.dtos.SolicitarOrganizadorRequest();
        request.setMotivoSolicitud("Deseo organizar eventos universitarios educativos");
        request.setTiposEventos(List.of(bo.uajms.eventos.modulos.usuarios.entidades.TipoEventoSolicitud.CURSOS_TALLERES));
        return request;
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"vacio", "corto", "largo", "espacios", "sinTipos", "nulos", "duplicados", "adicional"})
    void validaCamposAntesDePersistir(String caso) {
        var request = solicitudValida();
        switch (caso) {
            case "vacio" -> request.setMotivoSolicitud("");
            case "corto" -> request.setMotivoSolicitud("Quiero organizar");
            case "largo" -> request.setMotivoSolicitud("a".repeat(1001));
            case "espacios" -> request.setMotivoSolicitud(" ".repeat(30));
            case "sinTipos" -> request.setTiposEventos(List.of());
            case "nulos" -> request.setTiposEventos(java.util.Arrays.asList((bo.uajms.eventos.modulos.usuarios.entidades.TipoEventoSolicitud)null));
            case "duplicados" -> request.setTiposEventos(List.of(request.getTiposEventos().getFirst(), request.getTiposEventos().getFirst()));
            case "adicional" -> request.setInformacionAdicional("a".repeat(1001));
        }
        assertThrows(NegocioException.class, () -> servicio.solicitarSerOrganizador(request));
        verify(usuarioRepository, never()).save(any());
    }

    @Test void solicitudConservaMotivoTiposEInformacionOpcional() {
        autenticarUsuario(); when(usuarioRepository.save(usuario)).thenReturn(usuario);
        var request = solicitudValida(); request.setInformacionAdicional("Experiencia previa");
        var response = servicio.solicitarSerOrganizador(request);
        assertEquals(request.getMotivoSolicitud(), response.getMotivoSolicitud());
        assertEquals(request.getTiposEventos(), response.getTiposEventos());
        assertEquals("Experiencia previa", response.getInformacionAdicional());
    }

    @Test void reenvioArchivaRechazoAnteriorSinInventarMotivoHistorico() {
        usuario.setEstadoSolicitudOrganizador(Usuario.EstadoSolicitudOrganizador.RECHAZADA);
        usuario.setMotivoRechazoOrganizador("Completa la propuesta");
        autenticarUsuario(); when(usuarioRepository.save(usuario)).thenReturn(usuario);
        servicio.solicitarSerOrganizador(solicitudValida());
        var captor = org.mockito.ArgumentCaptor.forClass(bo.uajms.eventos.modulos.usuarios.entidades.SolicitudOrganizadorHistorial.class);
        verify(historialRepository).save(captor.capture());
        assertEquals(usuario.getId(), captor.getValue().getUsuarioId());
        assertEquals("Completa la propuesta", captor.getValue().getDetalle().getMotivoRechazo());
        assertNull(captor.getValue().getDetalle().getMotivoSolicitud());
        assertEquals(List.of(), captor.getValue().getDetalle().getTiposEventos());
    }

    @Test void estadoPropioAutorizaReenvioSegunRolesYEstadoActuales() {
        when(usuarioAutenticadoService.obtenerUsuario()).thenReturn(usuario);
        when(usuarioRolRepository.existsByUsuarioIdAndRolNombre(usuario.getId(), "USUARIO")).thenReturn(true);
        assertTrue(servicio.obtenerMiSolicitudOrganizador().isPuedeSolicitar());
        usuario.setEstadoSolicitudOrganizador(Usuario.EstadoSolicitudOrganizador.PENDIENTE);
        assertFalse(servicio.obtenerMiSolicitudOrganizador().isPuedeSolicitar());
        usuario.setEstadoSolicitudOrganizador(Usuario.EstadoSolicitudOrganizador.RECHAZADA);
        assertTrue(servicio.obtenerMiSolicitudOrganizador().isPuedeSolicitar());
        when(usuarioRolRepository.existsByUsuarioIdAndRolNombre(usuario.getId(), "ADMINISTRADOR")).thenReturn(true);
        assertFalse(servicio.obtenerMiSolicitudOrganizador().isPuedeSolicitar());
    }

    @Test void cuentaAdministrativaNoPuedeSolicitarInclusoConRolUsuarioInconsistente() {
        autenticarUsuario();
        when(usuarioRolRepository.existsByUsuarioIdAndRolNombre(usuario.getId(), "ADMINISTRADOR")).thenReturn(true);
        assertThrows(NegocioException.class, () -> servicio.solicitarSerOrganizador(solicitudValida()));
        verify(usuarioRepository, never()).save(any());
    }

    @Test void categoriasArbitrariasNoSonParteDelContrato() {
        assertThrows(com.fasterxml.jackson.core.JsonProcessingException.class, () ->
            new com.fasterxml.jackson.databind.ObjectMapper().readValue("{\"motivoSolicitud\":\"Motivo universitario educativo completo\",\"tiposEventos\":[\"ARBITRARIA\"]}",
                bo.uajms.eventos.modulos.usuarios.dtos.SolicitarOrganizadorRequest.class));
    }

}
