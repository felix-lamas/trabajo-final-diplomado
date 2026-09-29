package bo.uajms.eventos.modulos.usuarios.servicios;

import bo.uajms.eventos.core.excepciones.ServicioNoDisponibleException;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CorreoServicioTest {

    @Test
    void tokenNoApareceEnLogsCuandoSmtpNoEstaDisponible() {
        @SuppressWarnings("unchecked")
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);
        CorreoServicio servicio = new CorreoServicio(provider);
        ReflectionTestUtils.setField(servicio, "remitente", "no-reply@example.test");
        String secreto = "token-plano-super-secreto";

        Logger logger = (Logger) LoggerFactory.getLogger(CorreoServicio.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            assertThrows(ServicioNoDisponibleException.class, () -> servicio.enviarVerificacionCorreo(
                    "usuario@example.test", "https://app.example.test/verificar?token=" + secreto));
        } finally {
            logger.detachAppender(appender);
        }

        String logs = appender.list.stream().map(ILoggingEvent::getFormattedMessage)
                .reduce("", (left, right) -> left + right);
        assertFalse(logs.contains(secreto));
        assertFalse(logs.contains("https://app.example.test"));
        assertFalse(logs.contains("usuario@example.test"));
    }
}
