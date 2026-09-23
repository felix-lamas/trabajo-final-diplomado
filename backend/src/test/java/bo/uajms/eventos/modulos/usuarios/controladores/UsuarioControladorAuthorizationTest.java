package bo.uajms.eventos.modulos.usuarios.controladores;

import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import bo.uajms.eventos.modulos.usuarios.servicios.UsuarioServicio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = UsuarioControladorAuthorizationTest.Configuracion.class)
class UsuarioControladorAuthorizationTest {

    @Autowired private UsuarioControlador controlador;
    @Autowired private UsuarioServicio servicio;

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void usuarioNoPuedeConsultarPerfilDeOtroUsuario() {
        autenticar("USUARIO");
        assertThrows(AccessDeniedException.class, () -> controlador.buscarPorId(UUID.randomUUID()));
    }

    @Test
    void usuarioNoPuedeConsultarSolicitudesAdministrativas() {
        autenticar("USUARIO");
        assertThrows(AccessDeniedException.class, () -> controlador.listarSolicitudesOrganizador(
                Usuario.EstadoSolicitudOrganizador.PENDIENTE));
    }

    @Test
    void usuarioNoPuedeAprobarSolicitudes() {
        autenticar("USUARIO");
        assertThrows(AccessDeniedException.class,
                () -> controlador.aprobarSolicitudOrganizador(UUID.randomUUID()));
    }

    @Test
    void administradorPuedeConsultarYAprobarSolicitudes() {
        autenticar("ADMINISTRADOR");
        when(servicio.listarSolicitudesOrganizador(Usuario.EstadoSolicitudOrganizador.PENDIENTE))
                .thenReturn(List.of());

        assertDoesNotThrow(() -> controlador.listarSolicitudesOrganizador(
                Usuario.EstadoSolicitudOrganizador.PENDIENTE));
    }

    @Test
    void usuarioPuedeCrearSuSolicitudYOrganizadorNoPuedeRepetirla() {
        autenticar("USUARIO");
        assertDoesNotThrow(() -> controlador.solicitarSerOrganizador());

        autenticar("ORGANIZADOR");
        assertThrows(AccessDeniedException.class, () -> controlador.solicitarSerOrganizador());
    }

    private void autenticar(String rol) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "usuario@ejemplo.test", "N/A",
                        List.of(new SimpleGrantedAuthority("ROLE_" + rol))));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableMethodSecurity
    static class Configuracion {
        @Bean UsuarioServicio usuarioServicio() {
            return mock(UsuarioServicio.class);
        }

        @Bean UsuarioControlador usuarioControlador(UsuarioServicio servicio) {
            return new UsuarioControlador(servicio);
        }
    }
}
