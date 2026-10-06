package bo.uajms.eventos.core.controladores;

import bo.uajms.eventos.core.seguridad.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SaludController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class))
@Import(SaludControllerTest.TestSecurityConfiguration.class)
@ContextConfiguration(classes = {SaludController.class, SaludControllerTest.TestSecurityConfiguration.class})
class SaludControllerTest {

    @TestConfiguration
    static class TestSecurityConfiguration {
        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            return http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(HttpMethod.GET, "/api/v1/salud").permitAll()
                            .anyRequest().authenticated())
                    .exceptionHandling(errors -> errors.authenticationEntryPoint(
                            (request, response, exception) -> response.sendError(401)))
                    .build();
        }
    }

    @Autowired
    MockMvc mockMvc;

    @Test
    void getSaludSinAutenticacionDevuelveJsonUp() throws Exception {
        mockMvc.perform(get("/api/v1/salud"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.estado").value("UP"));
    }

    @Test
    void otrasRutasYMetodosSiguenRequiriendoAutenticacion() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/perfil"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/salud"))
                .andExpect(status().isUnauthorized());
    }
}
