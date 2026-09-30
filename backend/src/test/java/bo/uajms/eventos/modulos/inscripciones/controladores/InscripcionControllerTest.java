package bo.uajms.eventos.modulos.inscripciones.controladores;

import bo.uajms.eventos.core.excepciones.ManejadorGlobalExcepciones;
import bo.uajms.eventos.core.seguridad.JwtAuthenticationFilter;
import bo.uajms.eventos.modulos.inscripciones.dtos.ComprobanteInscripcionResponse;
import bo.uajms.eventos.modulos.inscripciones.dtos.DetalleInscripcionResponse;
import bo.uajms.eventos.modulos.inscripciones.entidades.EstadoInscripcion;
import bo.uajms.eventos.modulos.inscripciones.servicios.InscripcionService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InscripcionController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class))
@Import({ManejadorGlobalExcepciones.class, InscripcionControllerTest.TestSecurityConfiguration.class})
@ContextConfiguration(classes = {
        InscripcionController.class,
        ManejadorGlobalExcepciones.class,
        InscripcionControllerTest.TestSecurityConfiguration.class
})
class InscripcionControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfiguration {
        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            return http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .exceptionHandling(errors -> errors.authenticationEntryPoint(
                            (request, response, exception) -> response.sendError(401)))
                    .build();
        }
    }

    @jakarta.annotation.Resource MockMvc mockMvc;
    @MockBean InscripcionService service;
    private final UUID id = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private final UUID eventoId = UUID.fromString("30000000-0000-0000-0000-000000000002");

    @Test
    void crearSinAutenticacionResponde401() throws Exception {
        mockMvc.perform(post("/api/v1/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventoId\":\"" + eventoId + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void administradorNoUsaFlujoPersonal() throws Exception {
        mockMvc.perform(post("/api/v1/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventoId\":\"" + eventoId + "\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/inscripciones/mis-inscripciones"))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/v1/inscripciones/{id}/cancelar", id))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(roles = "USUARIO")
    void usuarioCreaSoloConEventoId() throws Exception {
        when(service.inscribir(any())).thenReturn(DetalleInscripcionResponse.builder()
                .id(id).eventoId(eventoId).estado(EstadoInscripcion.CONFIRMADA).build());

        mockMvc.perform(post("/api/v1/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventoId\":\"" + eventoId + "\",\"usuarioId\":\""
                                + UUID.randomUUID() + "\",\"estado\":\"CANCELADA\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));
    }

    @Test
    @WithMockUser(roles = "USUARIO")
    void usuarioObtieneComprobantePropio() throws Exception {
        when(service.obtenerComprobantePropio(id)).thenReturn(ComprobanteInscripcionResponse.builder()
                .inscripcionId(id).eventoId(eventoId).codigoInscripcion("INS-ABC")
                .estadoInscripcion(EstadoInscripcion.CONFIRMADA).estadoPago("NO_APLICA")
                .monto(BigDecimal.ZERO).codigoVerificacion("INS-ABC").build());

        mockMvc.perform(get("/api/v1/inscripciones/{id}/comprobante", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoInscripcion").value("INS-ABC"))
                .andExpect(jsonPath("$.estadoPago").value("NO_APLICA"));
    }
}
