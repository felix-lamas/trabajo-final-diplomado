package bo.uajms.eventos.modulos.categorias.controladores;

import bo.uajms.eventos.core.excepciones.ManejadorGlobalExcepciones;
import bo.uajms.eventos.core.excepciones.RecursoNoEncontradoException;
import bo.uajms.eventos.core.excepciones.CodigosError;
import bo.uajms.eventos.core.excepciones.ConflictoException;
import bo.uajms.eventos.core.seguridad.JwtAuthenticationFilter;
import bo.uajms.eventos.modulos.categorias.dtos.CategoriaEventoResponse;
import bo.uajms.eventos.modulos.categorias.servicios.CategoriaEventoService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ContextConfiguration;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CategoriaEventoController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class))
@Import({ManejadorGlobalExcepciones.class, CategoriaEventoControllerTest.TestSecurityConfiguration.class})
@ContextConfiguration(classes = {
        CategoriaEventoController.class,
        ManejadorGlobalExcepciones.class,
        CategoriaEventoControllerTest.TestSecurityConfiguration.class
})
class CategoriaEventoControllerTest {

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
    @MockBean CategoriaEventoService service;
    private final UUID id = UUID.fromString("10000000-0000-0000-0000-000000000001");

    @Test
    void endpointSinAutenticacionResponde401() throws Exception {
        mockMvc.perform(get("/api/v1/categorias-evento"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USUARIO")
    void usuarioPuedeListar() throws Exception {
        when(service.listarTodas()).thenReturn(List.of(respuesta()));
        mockMvc.perform(get("/api/v1/categorias-evento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Conferencia"))
                .andExpect(jsonPath("$[0].nombreNormalizado").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ORGANIZADOR")
    void organizadorPuedeListarActivas() throws Exception {
        when(service.listarActivas()).thenReturn(List.of(respuesta()));
        mockMvc.perform(get("/api/v1/categorias-evento/activas"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USUARIO")
    void obtieneCategoriaPorId() throws Exception {
        when(service.buscarPorId(id)).thenReturn(respuesta());
        mockMvc.perform(get("/api/v1/categorias-evento/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void administradorCreaCategoria() throws Exception {
        when(service.crear(any())).thenReturn(respuesta());
        mockMvc.perform(post("/api/v1/categorias-evento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Conferencia\",\"descripcion\":\"Académica\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Conferencia"));
    }

    @Test
    @WithMockUser(roles = "USUARIO")
    void usuarioNoPuedeCrear() throws Exception {
        mockMvc.perform(post("/api/v1/categorias-evento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Conferencia\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(roles = "ORGANIZADOR")
    void organizadorNoPuedeCrear() throws Exception {
        mockMvc.perform(post("/api/v1/categorias-evento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Conferencia\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(roles = "USUARIO")
    void usuarioNoPuedeActualizar() throws Exception {
        mockMvc.perform(put("/api/v1/categorias-evento/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Taller\",\"estado\":\"ACTIVO\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ORGANIZADOR")
    void organizadorNoPuedeEliminar() throws Exception {
        mockMvc.perform(delete("/api/v1/categorias-evento/{id}", id))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void administradorActualizaCategoria() throws Exception {
        when(service.actualizar(eq(id), any())).thenReturn(respuesta());
        mockMvc.perform(put("/api/v1/categorias-evento/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Conferencia\",\"estado\":\"ACTIVO\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void administradorEliminaCategoria() throws Exception {
        mockMvc.perform(delete("/api/v1/categorias-evento/{id}", id))
                .andExpect(status().isNoContent());
        verify(service).eliminar(id);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void nombreObligatorioUsaContratoEstandarDeError() throws Exception {
        mockMvc.perform(post("/api/v1/categorias-evento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.mensaje").value("Datos de entrada invalidos"))
                .andExpect(jsonPath("$.detalles[0]").exists());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void nombreVacioEsRechazado() throws Exception {
        mockMvc.perform(post("/api/v1/categorias-evento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void estadoInvalidoEsRechazado() throws Exception {
        mockMvc.perform(put("/api/v1/categorias-evento/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Taller\",\"estado\":\"BORRADA\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void nombreDuplicadoDevuelve409ConContratoEstandar() throws Exception {
        when(service.crear(any())).thenThrow(new ConflictoException(CodigosError.CONFLICT, "Nombre duplicado"));
        mockMvc.perform(post("/api/v1/categorias-evento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Conferencia\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICT"))
                .andExpect(jsonPath("$.mensaje").value("Nombre duplicado"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void categoriaReferenciadaDevuelve409() throws Exception {
        doThrow(new ConflictoException(CodigosError.CONFLICT, "Categoría referenciada"))
                .when(service).eliminar(id);
        mockMvc.perform(delete("/api/v1/categorias-evento/{id}", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICT"));
    }

    @Test
    @WithMockUser(roles = "USUARIO")
    void idInexistenteDevuelve404ConContratoEstandar() throws Exception {
        when(service.buscarPorId(id)).thenThrow(new RecursoNoEncontradoException("Categoría de evento", id));
        mockMvc.perform(get("/api/v1/categorias-evento/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.ruta").exists());
    }

    @Test
    void anotacionesRestringenMutacionesSoloAAdministrador() throws Exception {
        for (String metodo : List.of("crear", "actualizar", "eliminar")) {
            Method encontrado = java.util.Arrays.stream(CategoriaEventoController.class.getDeclaredMethods())
                    .filter(candidate -> candidate.getName().equals(metodo)).findFirst().orElseThrow();
            assertEquals("hasRole('ADMINISTRADOR')", encontrado.getAnnotation(PreAuthorize.class).value());
        }
    }

    private CategoriaEventoResponse respuesta() {
        return CategoriaEventoResponse.builder().id(id).nombre("Conferencia")
                .descripcion("Académica").estado("ACTIVO").build();
    }
}
