package bo.uajms.eventos.core.controladores;

import bo.uajms.eventos.core.seguridad.JwtAuthenticationFilter;
import bo.uajms.eventos.core.seguridad.JwtService;
import bo.uajms.eventos.core.seguridad.SecurityConfig;
import bo.uajms.eventos.core.seguridad.SecurityErrorResponseWriter;
import bo.uajms.eventos.modulos.usuarios.servicios.SesionUsuarioServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SaludController.class, properties = "app.cors.allowed-origins=http://localhost:4200")
@ContextConfiguration(classes = {SaludController.class, SecurityConfig.class,
        JwtAuthenticationFilter.class, SecurityErrorResponseWriter.class})
class SaludControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean JwtService jwtService;
    @MockBean UserDetailsService userDetailsService;
    @MockBean SesionUsuarioServicio sesionUsuarioServicio;

    @Test
    void getSaludSinAutenticacionDevuelveJsonUp() throws Exception {
        mockMvc.perform(get("/api/v1/salud"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.estado").value("UP"));
    }

    @Test
    void headSaludSinAutenticacionAlcanzaControlador() throws Exception {
        // MockMvc does not emulate the servlet container's suppression of a HEAD body.
        // SaludHttpIntegrationTest verifies the actual HTTP response on embedded Tomcat.
        mockMvc.perform(head("/api/v1/salud"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(handler().handlerType(SaludController.class));
    }

    @Test
    void otrasRutasYMetodosSiguenRequiriendoAutenticacion() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/perfil")).andExpect(status().isUnauthorized());
        mockMvc.perform(head("/api/v1/usuarios/perfil")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/salud")).andExpect(status().isUnauthorized());
        mockMvc.perform(head("/api/v1/salud/otra-ruta")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/eventos/revision")).andExpect(status().isUnauthorized());
        mockMvc.perform(head("/api/v1/eventos/revision")).andExpect(status().isUnauthorized());
    }
}
