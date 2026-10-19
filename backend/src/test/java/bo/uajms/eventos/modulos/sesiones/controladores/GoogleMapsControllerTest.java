package bo.uajms.eventos.modulos.sesiones.controladores;

import bo.uajms.eventos.core.excepciones.ManejadorGlobalExcepciones;
import bo.uajms.eventos.core.seguridad.JwtAuthenticationFilter;
import bo.uajms.eventos.modulos.sesiones.dtos.GoogleMapsCoordinatesResponse;
import bo.uajms.eventos.modulos.sesiones.servicios.GoogleMapsLinkResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GoogleMapsController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class))
@Import({ManejadorGlobalExcepciones.class, GoogleMapsControllerTest.TestSecurityConfiguration.class})
@ContextConfiguration(classes = {
        GoogleMapsController.class,
        ManejadorGlobalExcepciones.class,
        GoogleMapsControllerTest.TestSecurityConfiguration.class
})
class GoogleMapsControllerTest {
    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfiguration {
        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            return http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .build();
        }
    }

    @jakarta.annotation.Resource MockMvc mockMvc;
    @MockBean GoogleMapsLinkResolver resolver;

    @Test
    @WithMockUser(roles = "ORGANIZADOR")
    void organizadorObtieneCoordenadasResueltas() throws Exception {
        when(resolver.resolve(anyString())).thenReturn(new GoogleMapsCoordinatesResponse(
                new BigDecimal("-21.538558"), new BigDecimal("-64.764673")));

        mockMvc.perform(post("/api/v1/maps/coordenadas-compartidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://maps.app.goo.gl/example\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitud").value(-21.538558))
                .andExpect(jsonPath("$.longitud").value(-64.764673));
        verify(resolver).resolve("https://maps.app.goo.gl/example");
    }

    @Test
    @WithMockUser(roles = "USUARIO")
    void usuarioNoPuedeUsarElResolvedor() throws Exception {
        mockMvc.perform(post("/api/v1/maps/coordenadas-compartidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://maps.app.goo.gl/example\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(resolver);
    }

    @Test
    @WithMockUser(roles = "ORGANIZADOR")
    void rechazaEnlaceVacioAntesDeInvocarElResolvedor() throws Exception {
        mockMvc.perform(post("/api/v1/maps/coordenadas-compartidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\" \"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(resolver);
    }
}
