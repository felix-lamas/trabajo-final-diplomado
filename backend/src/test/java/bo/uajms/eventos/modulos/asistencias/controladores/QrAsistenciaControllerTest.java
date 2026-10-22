package bo.uajms.eventos.modulos.asistencias.controladores;

import bo.uajms.eventos.modulos.asistencias.dtos.QrAsistenciaResponse;
import bo.uajms.eventos.modulos.asistencias.servicios.QrAsistenciaService;
import bo.uajms.eventos.core.excepciones.ManejadorGlobalExcepciones;
import bo.uajms.eventos.core.seguridad.JwtAuthenticationFilter;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = QrAsistenciaController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class))
@Import({ManejadorGlobalExcepciones.class, QrAsistenciaControllerTest.TestSecurityConfiguration.class})
@ContextConfiguration(classes = {QrAsistenciaController.class, ManejadorGlobalExcepciones.class,
        QrAsistenciaControllerTest.TestSecurityConfiguration.class})
class QrAsistenciaControllerTest {
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

    @Resource MockMvc mockMvc;
    @MockBean QrAsistenciaService service;

    @Test
    @WithMockUser(roles = "ORGANIZADOR")
    void endpointRealDevuelveQrActualYNoPermiteAlmacenarLaRespuesta() throws Exception {
        UUID sesionId = UUID.randomUUID();
        UUID qrId = UUID.randomUUID();
        when(service.obtenerActivo(sesionId)).thenReturn(QrAsistenciaResponse.builder()
                .id(qrId).sesionId(sesionId).token("token-de-prueba").activo(true).build());

        mockMvc.perform(get("/api/v1/sesiones/{sesionId}/qr", sesionId).param("_", "123"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")))
                .andExpect(jsonPath("$.id").value(qrId.toString()))
                .andExpect(jsonPath("$.token").value("token-de-prueba"));
    }
}
