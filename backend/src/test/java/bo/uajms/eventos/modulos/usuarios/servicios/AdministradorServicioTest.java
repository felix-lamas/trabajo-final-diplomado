package bo.uajms.eventos.modulos.usuarios.servicios;

import bo.uajms.eventos.core.excepciones.*;
import bo.uajms.eventos.core.seguridad.UsuarioAutenticadoService;
import bo.uajms.eventos.modulos.usuarios.dtos.*;
import bo.uajms.eventos.modulos.usuarios.entidades.*;
import bo.uajms.eventos.modulos.usuarios.repositorios.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdministradorServicioTest {
    InvitacionAdministradorRepository invitaciones; UsuarioRepository usuarios; UsuarioRolRepository usuarioRoles;
    RolRepository roles; UsuarioAutenticadoService autenticado; PasswordEncoder encoder;
    CorreoServicio correo; SesionUsuarioServicio sesiones; AdministradorServicio servicio; Usuario admin; Rol rol;
    final String token="A".repeat(43);
    @BeforeEach void setup() {
        invitaciones=mock(InvitacionAdministradorRepository.class);usuarios=mock(UsuarioRepository.class);
        usuarioRoles=mock(UsuarioRolRepository.class);roles=mock(RolRepository.class);autenticado=mock(UsuarioAutenticadoService.class);
        encoder=mock(PasswordEncoder.class);correo=mock(CorreoServicio.class);sesiones=mock(SesionUsuarioServicio.class);
        servicio=new AdministradorServicio(invitaciones,usuarios,usuarioRoles,roles,autenticado,encoder,correo,sesiones);
        admin=Usuario.builder().correoElectronico("admin@example.test").build();admin.setId(UUID.randomUUID());
        rol=Rol.builder().nombre("ADMINISTRADOR").build();
        when(autenticado.tieneRol("ADMINISTRADOR")).thenReturn(true);when(autenticado.obtenerUsuario()).thenReturn(admin);
        when(roles.findByNombreForUpdate("ADMINISTRADOR")).thenReturn(Optional.of(rol));
        when(invitaciones.save(any())).thenAnswer(a->a.getArgument(0));
        when(encoder.encode(any())).thenReturn("bcrypt-hash");
        ReflectionTestUtils.setField(servicio,"activationUrl","https://example.test/auth/activar-administrador");
    }
    InvitarAdministradorRequest solicitud(){var r=new InvitarAdministradorRequest();r.setCorreo("nuevo@example.test");r.setCi("E2E-NEW");r.setNombres("Nuevo");r.setApellidos("Admin");r.setCelular("00000000");return r;}
    InvitacionAdministrador invitacion(){var i=new InvitacionAdministrador();i.setId(UUID.randomUUID());i.setCorreo("nuevo@example.test");i.setCi("E2E-NEW");i.setNombres("Nuevo");i.setApellidos("Admin");i.setCelular("00000000");i.setFechaExpiracion(LocalDateTime.now().plusHours(1));i.setFechaUltimoEnvio(LocalDateTime.now().minusMinutes(10));when(invitaciones.existsByTokenHashAndEstadoAndFechaExpiracionAfter(any(),any(),any())).thenReturn(true);when(invitaciones.findByTokenHashForUpdate(any())).thenReturn(Optional.of(i));when(invitaciones.findByTokenHash(any())).thenReturn(Optional.of(i));when(invitaciones.findByIdForUpdate(i.getId())).thenReturn(Optional.of(i));return i;}
    AceptarInvitacionAdministradorRequest aceptar(){var r=new AceptarInvitacionAdministradorRequest();r.setToken(token);r.setCi("E2E-NEW");r.setNombres("Nuevo");r.setApellidos("Admin");r.setCelular("00000000");r.setContrasena("LocalTest9!");r.setConfirmacionContrasena("LocalTest9!");return r;}
    @Test void invitaGuardaHashYEnviaCorreoSinDevolverToken(){var response=servicio.invitar(solicitud());var cap=ArgumentCaptor.forClass(InvitacionAdministrador.class);verify(invitaciones).save(cap.capture());var i=cap.getValue();assertEquals("PENDIENTE",response.estado());assertEquals(64,i.getTokenHash().length());verify(correo).enviarInvitacionAdministrador(eq("nuevo@example.test"),contains("/auth/activar-administrador?token="),eq(i.getFechaExpiracion()));assertFalse(response.toString().contains(i.getTokenHash()));}
    @Test void correoRegistradoNoSeConvierte(){when(usuarios.existsByCorreoElectronicoIgnoreCase(any())).thenReturn(true);assertThrows(NegocioException.class,()->servicio.invitar(solicitud()));verify(usuarios,never()).save(any());verify(usuarioRoles,never()).save(any());}
    @Test void ciRegistradoSeRechaza(){when(usuarios.existsByCi(any())).thenReturn(true);assertThrows(NegocioException.class,()->servicio.invitar(solicitud()));}
    @Test void invitacionPendienteDuplicada(){var i=invitacion();when(invitaciones.findByEstadoAndCorreo(any(),any())).thenReturn(List.of(i));assertThrows(NegocioException.class,()->servicio.invitar(solicitud()));}
    @Test void invitanteNoPuedeInvitarse(){var r=solicitud();r.setCorreo("ADMIN@example.test");assertThrows(NegocioException.class,()->servicio.invitar(r));}
    @Test void limitaInvitacionesPorAdministrador(){when(invitaciones.countByInvitanteIdAndFechaUltimoEnvioAfter(any(),any())).thenReturn(10L);assertThrows(NegocioException.class,()->servicio.invitar(solicitud()));verifyNoInteractions(correo);}
    @ParameterizedTest @ValueSource(strings={"USUARIO","ORGANIZADOR","USUARIO+ORGANIZADOR"}) void noAdministradorNoInvita(String rol){when(autenticado.tieneRol("ADMINISTRADOR")).thenReturn(false);assertThrows(SeguridadException.class,()->servicio.invitar(solicitud()));verifyNoInteractions(correo);}
    @Test void consultaPublicaSoloEstado(){invitacion();assertEquals(Map.of("estado","PENDIENTE"),servicio.consultar(token));}
    @Test void tokenDesconocidoRechazado(){assertThrows(NegocioException.class,()->servicio.consultar(token));}
    @ParameterizedTest @ValueSource(strings={"", "abc", "../../token"}) void formatoInvalidoRechazado(String t){assertThrows(NegocioException.class,()->servicio.consultar(t));verify(invitaciones,never()).findByTokenHash(any());}
    @Test void aceptarSoloAdministradorVerificadoActivo(){var i=invitacion();servicio.aceptar(aceptar());var cap=ArgumentCaptor.forClass(Usuario.class);verify(usuarios).saveAndFlush(cap.capture());var u=cap.getValue();assertTrue(u.isActivo());assertTrue(u.isCorreoVerificado());assertEquals("bcrypt-hash",u.getContrasena());var rc=ArgumentCaptor.forClass(UsuarioRol.class);verify(usuarioRoles,times(1)).save(rc.capture());assertEquals("ADMINISTRADOR",rc.getValue().getRol().getNombre());assertEquals(InvitacionAdministrador.Estado.ACEPTADA,i.getEstado());assertNotNull(i.getFechaAceptacion());assertSame(u,i.getUsuario());}
    @ParameterizedTest @EnumSource(value=InvitacionAdministrador.Estado.class,names={"ACEPTADA","REVOCADA","EXPIRADA"}) void estadosNoAceptables(InvitacionAdministrador.Estado estado){var i=invitacion();i.setEstado(estado);assertEquals(estado.name(),servicio.consultar(token).get("estado"));assertThrows(NegocioException.class,()->servicio.aceptar(aceptar()));verify(usuarios,never()).saveAndFlush(any());}
    @Test void fechaExpiradaRechazaAceptacion(){var i=invitacion();i.setFechaExpiracion(LocalDateTime.now().minusSeconds(1));assertEquals("EXPIRADA",servicio.consultar(token).get("estado"));assertThrows(NegocioException.class,()->servicio.aceptar(aceptar()));}
    @Test void tokenSoloUnUso(){invitacion();servicio.aceptar(aceptar());assertThrows(NegocioException.class,()->servicio.aceptar(aceptar()));verify(usuarioRoles,times(1)).save(any());}
    @Test void revalidaCorreoAlAceptar(){invitacion();when(usuarios.existsByCorreoElectronicoIgnoreCase(any())).thenReturn(true);assertThrows(NegocioException.class,()->servicio.aceptar(aceptar()));verify(usuarioRoles,never()).save(any());}
    @Test void confirmacionContrasenaDistinta(){invitacion();var r=aceptar();r.setConfirmacionContrasena("otra");assertThrows(NegocioException.class,()->servicio.aceptar(r));}
    @Test void ciDebeCoincidir(){invitacion();var r=aceptar();r.setCi("OTRO-CI");assertThrows(NegocioException.class,()->servicio.aceptar(r));}
    @Test void revocaPendiente(){var i=invitacion();servicio.revocar(i.getId());assertEquals(InvitacionAdministrador.Estado.REVOCADA,i.getEstado());assertThrows(NegocioException.class,()->servicio.aceptar(aceptar()));}
    @Test void noRevocaAceptada(){var i=invitacion();i.setEstado(InvitacionAdministrador.Estado.ACEPTADA);assertThrows(NegocioException.class,()->servicio.revocar(i.getId()));}
    @Test void reenvioRotaHash(){var i=invitacion();i.setTokenHash("viejo");servicio.reenviar(i.getId());assertNotEquals("viejo",i.getTokenHash());assertTrue(i.getFechaExpiracion().isAfter(LocalDateTime.now().plusHours(23)));}
    @Test void reenvioTieneEspera(){var i=invitacion();i.setFechaUltimoEnvio(LocalDateTime.now());assertThrows(NegocioException.class,()->servicio.reenviar(i.getId()));verifyNoInteractions(correo);}
    @Test void noReenviaRevocada(){var i=invitacion();i.setEstado(InvitacionAdministrador.Estado.REVOCADA);assertThrows(NegocioException.class,()->servicio.reenviar(i.getId()));}
    @Test void recursoInexistenteNoManipulado(){assertThrows(RecursoNoEncontradoException.class,()->servicio.revocar(UUID.randomUUID()));}
    @Test void protegeUltimoAdministrador(){when(usuarios.findByIdForUpdate(admin.getId())).thenReturn(Optional.of(admin));when(usuarioRoles.existsByUsuarioIdAndRolNombre(any(),eq("ADMINISTRADOR"))).thenReturn(true);when(usuarios.findAdministradores()).thenReturn(List.of(admin));var ex=assertThrows(NegocioException.class,()->servicio.desactivar(admin.getId()));assertEquals("No se puede desactivar al último administrador activo.",ex.getMessage());assertTrue(admin.isActivo());verifyNoInteractions(sesiones);}
    @Test void secretosNoSeSerializanNiAparecenEnToString() throws Exception {
        var r=aceptar();var mapper=new com.fasterxml.jackson.databind.ObjectMapper();
        assertFalse(r.toString().contains(token));assertFalse(r.toString().contains(r.getContrasena()));
        String json=mapper.writeValueAsString(r);assertFalse(json.contains("token"));assertFalse(json.contains("contrasena"));
        var consulta=new ConsultarInvitacionAdministradorRequest();consulta.setToken(token);
        assertFalse(consulta.toString().contains(token));
        assertThrows(com.fasterxml.jackson.databind.exc.InvalidDefinitionException.class, () -> mapper.writeValueAsString(consulta));
        assertEquals(token,mapper.readValue("{\"token\":\""+token+"\"}",ConsultarInvitacionAdministradorRequest.class).getToken());
    }
    @Test void variosAdminsPermitenDesactivarYRevocarSesiones(){when(usuarios.findByIdForUpdate(admin.getId())).thenReturn(Optional.of(admin));when(usuarioRoles.existsByUsuarioIdAndRolNombre(any(),any())).thenReturn(true);when(usuarios.findAdministradores()).thenReturn(List.of(admin,Usuario.builder().build()));servicio.desactivar(admin.getId());assertFalse(admin.isActivo());verify(sesiones).revocarSesionesActivas(admin.getId());}
    @Test void cuentaDemoProtegida(){admin.setCorreoElectronico("admin@demo.local");when(usuarios.findByIdForUpdate(admin.getId())).thenReturn(Optional.of(admin));when(usuarioRoles.existsByUsuarioIdAndRolNombre(any(),any())).thenReturn(true);when(usuarios.findAdministradores()).thenReturn(List.of(admin,Usuario.builder().build()));assertThrows(NegocioException.class,()->servicio.desactivar(admin.getId()));assertTrue(admin.isActivo());}
    @Test void noDesactivaParticipantePorUuid(){when(usuarios.findByIdForUpdate(admin.getId())).thenReturn(Optional.of(admin));assertThrows(RecursoNoEncontradoException.class,()->servicio.desactivar(admin.getId()));assertTrue(admin.isActivo());}
}
