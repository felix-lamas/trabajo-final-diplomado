package bo.uajms.eventos.modulos.certificados.controladores;

import bo.uajms.eventos.modulos.certificados.dtos.CertificadoResponse;
import bo.uajms.eventos.modulos.certificados.dtos.VerificacionCertificadoResponse;
import bo.uajms.eventos.modulos.certificados.servicios.CertificadoService;
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
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CertificadoControllerAuthorizationTest.Configuracion.class)
class CertificadoControllerAuthorizationTest {

    @Autowired CertificadoController controller;
    @Autowired CertificadoService service;

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void usuarioNoPuedeGenerarCertificado() {
        autenticar("USUARIO");
        assertThrows(AccessDeniedException.class, () -> controller.generar(UUID.randomUUID()));
    }

    @Test
    void organizadorPuedeGenerarCertificado() {
        autenticar("ORGANIZADOR");
        UUID id = UUID.randomUUID();
        when(service.generarCertificado(id)).thenReturn(CertificadoResponse.builder().build());
        assertDoesNotThrow(() -> controller.generar(id));
        verify(service).generarCertificado(id);
    }

    @Test
    void usuarioNoPuedeListarCertificadosPorEvento() {
        autenticar("USUARIO");
        assertThrows(AccessDeniedException.class,
                () -> controller.certificadosPorEvento(UUID.randomUUID()));
    }

    @Test
    void administradorPuedeListarCertificadosPorEvento() {
        autenticar("ADMINISTRADOR");
        UUID eventoId = UUID.randomUUID();
        when(service.listarPorEvento(eventoId)).thenReturn(List.of());
        assertDoesNotThrow(() -> controller.certificadosPorEvento(eventoId));
    }

    @Test
    void verificacionPublicaNoRequiereAutenticacion() {
        when(service.verificarCertificadoPublico("CODIGO"))
                .thenReturn(VerificacionCertificadoResponse.builder().valido(true).build());
        assertTrue(controller.verificarPublico("CODIGO").getBody().isValido());
    }

    @Test
    void descargaAutorizadaDevuelvePdfComoAdjunto() {
        autenticar("USUARIO");
        UUID id = UUID.randomUUID();
        when(service.descargarPdf(id)).thenReturn(new byte[]{1, 2, 3});

        var respuesta = controller.descargar(id);

        assertEquals(MediaType.APPLICATION_PDF, respuesta.getHeaders().getContentType());
        assertTrue(respuesta.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).startsWith("attachment;"));
        assertArrayEquals(new byte[]{1, 2, 3}, respuesta.getBody());
    }

    private void autenticar(String rol) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "usuario@example.test", "N/A", List.of(new SimpleGrantedAuthority("ROLE_" + rol))));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableMethodSecurity
    static class Configuracion {
        @Bean CertificadoService certificadoService() { return mock(CertificadoService.class); }
        @Bean CertificadoController certificadoController(CertificadoService service) {
            return new CertificadoController(service);
        }
    }
}
