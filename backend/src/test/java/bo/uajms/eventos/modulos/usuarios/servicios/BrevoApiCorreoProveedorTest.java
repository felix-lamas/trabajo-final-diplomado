package bo.uajms.eventos.modulos.usuarios.servicios;

import bo.uajms.eventos.core.excepciones.ServicioNoDisponibleException;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BrevoApiCorreoProveedorTest {

    @Test
    void enviaCorreoTransaccionalPorHttpsConContratoBrevo() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("api-key", "clave-simulada"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "sender":{"email":"no-reply@example.test","name":"Vidia"},
                          "to":[{"email":"destino@example.test"}],
                          "subject":"Verifica tu correo",
                          "textContent":"Abre el enlace de verificacion"
                        }
                        """))
                .andRespond(withSuccess("{\"messageId\":\"simulado\"}", MediaType.APPLICATION_JSON));

        BrevoApiCorreoProveedor proveedor = new BrevoApiCorreoProveedor(
                builder, "clave-simulada", "no-reply@example.test", "Vidia");

        proveedor.enviar("destino@example.test", "Verifica tu correo", "Abre el enlace de verificacion");

        server.verify();
    }

    @Test
    void transformaRespuestaNoExitosaEnServicioNoDisponible() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE));
        BrevoApiCorreoProveedor proveedor = new BrevoApiCorreoProveedor(
                builder, "clave-simulada", "no-reply@example.test", "Vidia");

        ServicioNoDisponibleException exception = assertThrows(ServicioNoDisponibleException.class,
                () -> proveedor.enviar("destino@example.test", "Asunto", "Contenido"));

        assertEquals("MAIL_SERVICE_UNAVAILABLE", exception.getCodigo());
        server.verify();
    }

    @Test
    void noRegistraApiKeyDestinatarioNiTokenCuandoBrevoFalla() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE));
        BrevoApiCorreoProveedor proveedor = new BrevoApiCorreoProveedor(
                builder, "api-key-simulada", "no-reply@example.test", "Vidia");
        Logger logger = (Logger) LoggerFactory.getLogger(BrevoApiCorreoProveedor.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            assertThrows(ServicioNoDisponibleException.class, () -> proveedor.enviar(
                    "destino@example.test", "Asunto", "https://app.example.test/?token=token-simulado"));
        } finally {
            logger.detachAppender(appender);
        }

        String logs = appender.list.stream().map(ILoggingEvent::getFormattedMessage)
                .reduce("", (left, right) -> left + right);
        assertFalse(logs.contains("api-key-simulada"));
        assertFalse(logs.contains("destino@example.test"));
        assertFalse(logs.contains("token-simulado"));
        assertFalse(logs.contains("app.example.test"));
        server.verify();
    }

    @Test
    void claveAusenteFallaSinEnviarSolicitud() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        BrevoApiCorreoProveedor proveedor = new BrevoApiCorreoProveedor(
                builder, "", "no-reply@example.test", "Vidia");

        ServicioNoDisponibleException exception = assertThrows(ServicioNoDisponibleException.class,
                () -> proveedor.enviar("destino@example.test", "Asunto", "Contenido"));

        assertEquals("MAIL_SERVICE_UNAVAILABLE", exception.getCodigo());
        server.verify();
    }
}
