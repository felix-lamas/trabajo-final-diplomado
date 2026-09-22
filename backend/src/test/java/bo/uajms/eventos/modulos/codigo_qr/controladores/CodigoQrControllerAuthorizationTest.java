package bo.uajms.eventos.modulos.codigo_qr.controladores;

import bo.uajms.eventos.modulos.codigo_qr.entidades.CodigoQr;
import bo.uajms.eventos.modulos.codigo_qr.mappers.CodigoQrMapper;
import bo.uajms.eventos.modulos.codigo_qr.servicios.CodigoQrService;
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
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CodigoQrControllerAuthorizationTest.Configuracion.class)
class CodigoQrControllerAuthorizationTest {

    @Autowired private CodigoQrController codigoQrController;
    @Autowired private CodigoQrService codigoQrService;

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void usuarioNoAdministrativoNoPuedeValidarQr() {
        autenticar("ESTUDIANTE");

        assertThrows(AccessDeniedException.class,
                () -> codigoQrController.validar("00000000-0000-0000-0000-000000000001"));
    }

    @Test
    void organizadorPuedeInvocarValidacionQr() {
        autenticar("ORGANIZADOR");
        String contenido = "00000000-0000-0000-0000-000000000001";
        when(codigoQrService.validarContenidoVisible(contenido)).thenReturn(CodigoQr.builder().build());

        assertDoesNotThrow(() -> codigoQrController.validar(contenido));
    }

    private void autenticar(String rol) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "usuario@example.test",
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_" + rol))
        ));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableMethodSecurity
    static class Configuracion {

        @Bean
        CodigoQrService codigoQrService() {
            return mock(CodigoQrService.class);
        }

        @Bean
        CodigoQrMapper codigoQrMapper() {
            return mock(CodigoQrMapper.class);
        }

        @Bean
        CodigoQrController codigoQrController(CodigoQrService codigoQrService, CodigoQrMapper codigoQrMapper) {
            return new CodigoQrController(codigoQrService, codigoQrMapper);
        }
    }
}
