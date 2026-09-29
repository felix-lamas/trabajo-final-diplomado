package bo.uajms.eventos.modulos.eventos.controladores;

import bo.uajms.eventos.core.excepciones.*;
import bo.uajms.eventos.core.seguridad.JwtAuthenticationFilter;
import bo.uajms.eventos.modulos.eventos.dtos.EventoDetalleResponse;
import bo.uajms.eventos.modulos.eventos.servicios.EventoService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EventoController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@Import({ManejadorGlobalExcepciones.class, EventoControllerTest.TestSecurityConfiguration.class})
@ContextConfiguration(classes = {EventoController.class, ManejadorGlobalExcepciones.class,
        EventoControllerTest.TestSecurityConfiguration.class})
class EventoControllerTest {
    @TestConfiguration @EnableMethodSecurity
    static class TestSecurityConfiguration {
        @Bean SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            return http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .exceptionHandling(errors -> errors.authenticationEntryPoint((req, res, ex) -> res.sendError(401))).build();
        }
    }

    @jakarta.annotation.Resource MockMvc mockMvc;
    @MockBean EventoService service;
    private final UUID id = UUID.fromString("10000000-0000-0000-0000-000000000001");

    @Test void endpointProtegidoSinJwtResponde401() throws Exception {
        mockMvc.perform(get("/api/v1/eventos")).andExpect(status().isUnauthorized());
    }

    @Test @WithMockUser(roles = "USUARIO") void usuarioNoCreaEvento() throws Exception {
        mockMvc.perform(post("/api/v1/eventos").contentType(MediaType.APPLICATION_JSON).content(jsonValido()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test @WithMockUser(roles = "ORGANIZADOR") void organizadorCreaEvento() throws Exception {
        when(service.crear(any())).thenReturn(EventoDetalleResponse.builder().id(id).build());
        mockMvc.perform(post("/api/v1/eventos").contentType(MediaType.APPLICATION_JSON).content(jsonValido()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test @WithMockUser(roles = "ADMINISTRADOR") void administradorCreaEvento() throws Exception {
        when(service.crear(any())).thenReturn(EventoDetalleResponse.builder().id(id).build());
        mockMvc.perform(post("/api/v1/eventos").contentType(MediaType.APPLICATION_JSON).content(jsonValido()))
                .andExpect(status().isCreated());
    }

    @Test @WithMockUser(roles = "ORGANIZADOR") void organizadorNoPublica() throws Exception {
        mockMvc.perform(patch("/api/v1/eventos/{id}/publicar", id)).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test @WithMockUser(roles = "ORGANIZADOR") void organizadorNoCancela() throws Exception {
        mockMvc.perform(patch("/api/v1/eventos/{id}/cancelar", id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"motivo\":\"Fuerza mayor\"}")).andExpect(status().isForbidden());
    }

    @Test @WithMockUser(roles = "ADMINISTRADOR") void administradorPublica() throws Exception {
        mockMvc.perform(patch("/api/v1/eventos/{id}/publicar", id)).andExpect(status().isOk());
        verify(service).publicar(id);
    }

    @Test @WithMockUser(roles = "ADMINISTRADOR") void requestInvalidoUsaErrorEstandar() throws Exception {
        mockMvc.perform(post("/api/v1/eventos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.detalles[0]").exists());
    }

    @Test @WithMockUser(roles = "ADMINISTRADOR") void transicionInvalidaDevuelve409() throws Exception {
        doThrow(new ConflictoException(CodigosError.EVENT_INVALID_STATE, "Estado invalido")).when(service).publicar(id);
        mockMvc.perform(patch("/api/v1/eventos/{id}/publicar", id)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("EVENT_INVALID_STATE"));
    }

    @Test @WithMockUser(roles = "USUARIO") void eventoNoVisibleDevuelve404() throws Exception {
        when(service.buscarPorId(id)).thenThrow(new RecursoNoEncontradoException("Evento", id));
        mockMvc.perform(get("/api/v1/eventos/{id}", id)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RESOURCE_NOT_FOUND"));
    }

    private String jsonValido() {
        return """
                {"titulo":"Evento","descripcion":"Descripcion","objetivos":"Objetivos",
                "categoriaId":"20000000-0000-0000-0000-000000000001","modalidad":"VIRTUAL",
                "tipoInscripcion":"GRATUITO","costo":0,"fechaInicio":"2026-10-10","fechaFin":"2026-10-10",
                "horaInicio":"08:00:00","horaFin":"10:00:00","enlaceVirtual":"https://meet.example.test/x",
                "requiereInscripcion":true,"cupoLimitado":false,"emiteCertificado":false,"publicoObjetivo":"AMBOS"}
                """;
    }
}
