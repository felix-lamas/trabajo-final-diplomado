package bo.uajms.eventos.core.seguridad;

import bo.uajms.eventos.core.excepciones.CodigosError;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService, writer);
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
}
