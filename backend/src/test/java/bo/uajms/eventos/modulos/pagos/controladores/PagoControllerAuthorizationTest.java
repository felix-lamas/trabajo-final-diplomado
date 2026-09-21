package bo.uajms.eventos.modulos.pagos.controladores;

import bo.uajms.eventos.modulos.pagos.servicios.PagoService;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PagoControllerAuthorizationTest.Configuracion.class)
class PagoControllerAuthorizationTest {

    @Autowired
    private PagoController pagoController;

    @Autowired
    private PagoService pagoService;

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void estudianteNoPuedeListarTodosLosPagos() {
        autenticar("ESTUDIANTE");

        assertThrows(AccessDeniedException.class, () -> pagoController.listarTodos());
    }

    @Test
    void organizadorNoPuedeListarTodosLosPagos() {
        autenticar("ORGANIZADOR");

        assertThrows(AccessDeniedException.class, () -> pagoController.listarTodos());
    }

    @Test
    void administradorPuedeListarTodosLosPagos() {
        autenticar("ADMINISTRADOR");
        when(pagoService.listarTodos()).thenReturn(List.of());

        assertDoesNotThrow(() -> pagoController.listarTodos());
        verify(pagoService).listarTodos();
    }

    private void autenticar(String rol) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "usuario@example.test",
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_" + rol))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableMethodSecurity
    static class Configuracion {

        @Bean
        PagoService pagoService() {
            return mock(PagoService.class);
        }

        @Bean
        PagoController pagoController(PagoService pagoService) {
            return new PagoController(pagoService);
        }
    }
}
