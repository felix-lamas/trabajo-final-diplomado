package bo.uajms.eventos.core.controladores;

import bo.uajms.eventos.core.seguridad.JwtAuthenticationFilter;
import bo.uajms.eventos.core.seguridad.JwtService;
import bo.uajms.eventos.core.seguridad.SecurityConfig;
import bo.uajms.eventos.core.seguridad.SecurityErrorResponseWriter;
import bo.uajms.eventos.modulos.usuarios.servicios.SesionUsuarioServicio;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** Uses real local HTTP without a database, JWT credentials or external services. */
@SpringBootTest(classes = SaludHttpIntegrationTest.HealthApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.config.import=", "app.cors.allowed-origins=http://localhost:4200"})
class SaludHttpIntegrationTest {
    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class, JpaRepositoriesAutoConfiguration.class,
            FlywayAutoConfiguration.class})
    @Import({SaludController.class, SecurityConfig.class, JwtAuthenticationFilter.class,
            SecurityErrorResponseWriter.class})
    static class HealthApplication {
    }

    @LocalServerPort int port;
    @Autowired ObjectMapper objectMapper;
    @MockBean JwtService jwtService;
    @MockBean UserDetailsService userDetailsService;
    @MockBean SesionUsuarioServicio sesionUsuarioServicio;

    private HttpResponse<byte[]> request(String method, String path) throws Exception {
        try (var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()) {
            var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                    .timeout(Duration.ofSeconds(10))
                    .method(method, HttpRequest.BodyPublishers.noBody()).build();
            return client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        }
    }

    @Test
    void getPublicoDevuelve200YEstadoUp() throws Exception {
        var response = request("GET", "/api/v1/salud");
        assertEquals(200, response.statusCode());
        assertEquals("application/json", response.headers().firstValue("Content-Type").orElseThrow());
        assertEquals("UP", objectMapper.readTree(response.body()).path("estado").asText());
    }

    @Test
    void headPublicoDevuelve200EncabezadosDelGetYCuerpoVacio() throws Exception {
        var get = request("GET", "/api/v1/salud");
        var head = request("HEAD", "/api/v1/salud");
        assertEquals(200, get.statusCode());
        assertEquals(200, head.statusCode());
        assertEquals(0, head.body().length);
        assertFalse(get.body().length == 0);
        assertEquals(get.headers().firstValue("Content-Type"), head.headers().firstValue("Content-Type"));
        assertEquals("nosniff", head.headers().firstValue("X-Content-Type-Options").orElseThrow());
        // If Content-Length is present, it describes GET's representation, not a HEAD body.
        head.headers().firstValueAsLong("Content-Length").ifPresent(length ->
                assertEquals((long) get.body().length, length));
    }

    @Test
    void otrasRutasYMetodosConservanLaProteccion() throws Exception {
        assertEquals(401, request("GET", "/api/v1/usuarios/perfil").statusCode());
        assertEquals(401, request("HEAD", "/api/v1/usuarios/perfil").statusCode());
        assertEquals(401, request("POST", "/api/v1/salud").statusCode());
        assertEquals(401, request("HEAD", "/api/v1/salud/otra-ruta").statusCode());
        assertEquals(401, request("GET", "/api/v1/eventos/revision").statusCode());
        assertEquals(401, request("HEAD", "/api/v1/eventos/revision").statusCode());
    }
}
