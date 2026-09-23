package bo.uajms.eventos.core.seguridad;

import bo.uajms.eventos.core.excepciones.CodigosError;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class SecurityErrorResponseWriterTest {
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final SecurityErrorResponseWriter writer = new SecurityErrorResponseWriter(mapper);

    @Test
    void faltaDeJwtSeRepresentaComo401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/usuarios/perfil");
        MockHttpServletResponse response = new MockHttpServletResponse();
        writer.escribir(request, response, 401, CodigosError.AUTH_REQUIRED, "Se requiere autenticacion");
        assertEquals(401, response.getStatus());
        assertEquals(CodigosError.AUTH_REQUIRED, mapper.readTree(response.getContentAsByteArray()).get("codigo").asText());
    }

    @Test
    void jwtInvalidoSeRepresentaComo401SinDetalleTecnico() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/usuarios/perfil");
        MockHttpServletResponse response = new MockHttpServletResponse();
        writer.escribir(request, response, 401, CodigosError.AUTH_INVALID_TOKEN, "El token de autenticacion no es valido");
        var json = mapper.readTree(response.getContentAsByteArray());
        assertEquals(401, response.getStatus());
        assertEquals(CodigosError.AUTH_INVALID_TOKEN, json.get("codigo").asText());
        assertFalse(json.has("detalles") && !json.get("detalles").isNull());
    }
}
