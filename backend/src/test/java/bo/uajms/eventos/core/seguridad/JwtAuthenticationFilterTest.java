package bo.uajms.eventos.core.seguridad;

import bo.uajms.eventos.core.excepciones.CodigosError;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import bo.uajms.eventos.modulos.usuarios.servicios.SesionUsuarioServicio;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

class JwtAuthenticationFilterTest {

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void jwtMalformadoDevuelve401UniformeYNoContinuaLaCadena() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        SecurityErrorResponseWriter writer = new SecurityErrorResponseWriter(mapper);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService, writer,
                mock(SesionUsuarioServicio.class));
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/usuarios/perfil");
        request.addHeader("Authorization", "Bearer token-no-valido");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extraerNombreUsuario("token-no-valido"))
                .thenThrow(new MalformedJwtException("detalle tecnico interno"));

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertEquals(CodigosError.AUTH_INVALID_TOKEN,
                mapper.readTree(response.getContentAsByteArray()).get("codigo").asText());
        verify(chain, never()).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(org.mockito.ArgumentMatchers.anyString());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void jwtConSesionValidaAutenticaYPermitePeticion(boolean cuentaDual) throws Exception {
        JwtService jwtService = mock(JwtService.class);
        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        SesionUsuarioServicio sesiones = mock(SesionUsuarioServicio.class);
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService,
                new SecurityErrorResponseWriter(mapper), sesiones);
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = request();
        MockHttpServletResponse response = new MockHttpServletResponse();
        UUID sesionId = UUID.randomUUID();
        UserDetails details = User.withUsername("usuario@example.test")
                .password("hash").authorities(cuentaDual
                        ? new String[]{"ROLE_USUARIO", "ROLE_ORGANIZADOR"}
                        : new String[]{"ROLE_USUARIO"}).build();
        when(jwtService.extraerNombreUsuario("jwt-prueba")).thenReturn("usuario@example.test");
        when(jwtService.extraerIdSesion("jwt-prueba")).thenReturn(sesionId);
        when(userDetailsService.loadUserByUsername("usuario@example.test")).thenReturn(details);
        when(jwtService.esTokenValido("jwt-prueba", details)).thenReturn(true);
        when(sesiones.esSesionActiva(sesionId, "usuario@example.test")).thenReturn(true);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertEquals(sesionId, request.getAttribute(JwtAuthenticationFilter.SESION_ID_ATTRIBUTE));
        assertEquals(new java.util.HashSet<>(details.getAuthorities()),
                new java.util.HashSet<>(SecurityContextHolder.getContext().getAuthentication().getAuthorities()));
    }

    @ParameterizedTest(name = "JWT con sesion {0} devuelve 401")
    @ValueSource(strings = {"revocada", "inexistente"})
    void jwtConSesionNoActivaDevuelve401(String estadoSesion) throws Exception {
        JwtService jwtService = mock(JwtService.class);
        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        SesionUsuarioServicio sesiones = mock(SesionUsuarioServicio.class);
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService,
                new SecurityErrorResponseWriter(mapper), sesiones);
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = request();
        MockHttpServletResponse response = new MockHttpServletResponse();
        UUID sesionId = UUID.randomUUID();
        UserDetails details = User.withUsername("usuario@example.test")
                .password("hash").authorities("ROLE_USUARIO").build();
        when(jwtService.extraerNombreUsuario("jwt-prueba")).thenReturn("usuario@example.test");
        when(jwtService.extraerIdSesion("jwt-prueba")).thenReturn(sesionId);
        when(userDetailsService.loadUserByUsername("usuario@example.test")).thenReturn(details);
        when(jwtService.esTokenValido("jwt-prueba", details)).thenReturn(true);
        when(sesiones.esSesionActiva(sesionId, "usuario@example.test")).thenReturn(false);

        filter.doFilter(request, response, chain);

        org.junit.jupiter.api.Assertions.assertNotNull(estadoSesion);
        assertEquals(401, response.getStatus());
        assertEquals(CodigosError.AUTH_INVALID_SESSION,
                mapper.readTree(response.getContentAsByteArray()).get("codigo").asText());
        verify(chain, never()).doFilter(request, response);
    }

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/usuarios/perfil");
        request.addHeader("Authorization", "Bearer jwt-prueba");
        return request;
    }
}
