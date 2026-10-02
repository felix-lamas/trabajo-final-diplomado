package bo.uajms.eventos.modulos.usuarios.servicios;

import bo.uajms.eventos.core.excepciones.ServicioNoDisponibleException;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.MailSendException;
import org.springframework.test.util.ReflectionTestUtils;

import org.springframework.mail.SimpleMailMessage;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;

class CorreoServicioTest {

    @Test
    void enviaVerificacionConRemitenteDeMarcaYEnlaceSinAlterar() {
        JavaMailSender sender = mock(JavaMailSender.class);
        CorreoServicio servicio = servicioConSender(sender);
        ArgumentCaptor<SimpleMailMessage> mensaje = ArgumentCaptor.forClass(SimpleMailMessage.class);

        servicio.enviarVerificacionCorreo("ana@example.test", "https://vidia.test/verificar?token=token-falso-test");

        verify(sender).send(mensaje.capture());
        assertEquals("Vidia <appvidia@gmail.com>", mensaje.getValue().getFrom());
        assertEquals("ana@example.test", mensaje.getValue().getTo()[0]);
        assertTrue(mensaje.getValue().getText().contains("https://vidia.test/verificar?token=token-falso-test"));
    }

    @Test
    void enviaRecuperacionConEnlaceProporcionado() {
        JavaMailSender sender = mock(JavaMailSender.class);
        CorreoServicio servicio = servicioConSender(sender);
        ArgumentCaptor<SimpleMailMessage> mensaje = ArgumentCaptor.forClass(SimpleMailMessage.class);

        servicio.enviarRecuperacionContrasena("ana@example.test", "https://vidia.test/reset?token=falso-test");

        verify(sender).send(mensaje.capture());
        assertEquals("Vidia <appvidia@gmail.com>", mensaje.getValue().getFrom());
        assertTrue(mensaje.getValue().getText().contains("https://vidia.test/reset?token=falso-test"));
    }

    @Test
    void noRegistraDetallesDeMailExceptionNiEnlaceCuandoSmtpFalla() {
        JavaMailSender sender = mock(JavaMailSender.class);
        doThrow(new MailSendException("detalle-tecnico-falso-de-prueba"))
                .when(sender).send(any(SimpleMailMessage.class));
        CorreoServicio servicio = servicioConSender(sender);

        Logger logger = (Logger) LoggerFactory.getLogger(CorreoServicio.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            assertThrows(ServicioNoDisponibleException.class, () -> servicio.enviarRecuperacionContrasena(
                    "ana@example.test", "https://vidia.test/reset?token=token-falso-test"));
        } finally {
            logger.detachAppender(appender);
        }

        String logs = appender.list.stream().map(ILoggingEvent::getFormattedMessage)
                .reduce("", (left, right) -> left + right);
        assertFalse(logs.contains("detalle-tecnico-falso-de-prueba"));
        assertFalse(logs.contains("token-falso-test"));
        assertFalse(logs.contains("ana@example.test"));
    }

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

    private CorreoServicio servicioConSender(JavaMailSender sender) {
        @SuppressWarnings("unchecked")
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(sender);
        CorreoServicio servicio = new CorreoServicio(provider);
        ReflectionTestUtils.setField(servicio, "remitente", "appvidia@gmail.com");
        ReflectionTestUtils.setField(servicio, "nombreRemitente", "Vidia");
        return servicio;
    }
}
