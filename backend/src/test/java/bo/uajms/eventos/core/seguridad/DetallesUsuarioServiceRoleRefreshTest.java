package bo.uajms.eventos.core.seguridad;

import bo.uajms.eventos.modulos.usuarios.entidades.Rol;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.entidades.UsuarioRol;
import bo.uajms.eventos.modulos.usuarios.repositorios.RolPermisoRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRepository;
import bo.uajms.eventos.modulos.usuarios.repositorios.UsuarioRolRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DetallesUsuarioServiceRoleRefreshTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void autoridadesRespetanRolesExplicitosSinInferirUsuario(boolean cuentaDual) {
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        UsuarioRolRepository usuarioRoles = mock(UsuarioRolRepository.class);
        RolPermisoRepository permisos = mock(RolPermisoRepository.class);
        DetallesUsuarioService servicio = new DetallesUsuarioService(usuarios, usuarioRoles, permisos);
        Usuario usuario = Usuario.builder()
                .correoElectronico("organizador@ejemplo.test")
                .contrasena("hash")
                .nombres("Organizador")
                .apellidos("Demo")
                .ci("1234567")
                .tipoUsuario(Usuario.TipoUsuario.EXTERNO)
                .build();
        Rol organizador = Rol.builder().nombre("ORGANIZADOR").build();
        ReflectionTestUtils.setField(usuario, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(organizador, "id", UUID.randomUUID());
        when(usuarios.findByCorreoElectronicoIgnoreCase(usuario.getCorreoElectronico()))
                .thenReturn(Optional.of(usuario));
        Rol participante = Rol.builder().nombre("USUARIO").build();
        ReflectionTestUtils.setField(participante, "id", UUID.randomUUID());
        var rolOrganizador = UsuarioRol.builder().usuario(usuario).rol(organizador).build();
        var rolUsuario = UsuarioRol.builder().usuario(usuario).rol(participante).build();
        when(usuarioRoles.findByUsuarioId(usuario.getId()))
                .thenReturn(cuentaDual ? List.of(rolUsuario, rolOrganizador) : List.of(rolOrganizador));
        when(permisos.findByRolId(organizador.getId())).thenReturn(List.of());

        var detalles = servicio.loadUserByUsername(usuario.getCorreoElectronico());

        assertTrue(detalles.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ORGANIZADOR")));
        assertEquals(cuentaDual, detalles.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_USUARIO")));
        assertFalse(detalles.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMINISTRADOR")));
    }
}
