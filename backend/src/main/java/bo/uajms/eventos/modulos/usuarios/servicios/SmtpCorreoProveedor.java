package bo.uajms.eventos.modulos.usuarios.servicios;

import bo.uajms.eventos.core.excepciones.CodigosError;
import bo.uajms.eventos.core.excepciones.ServicioNoDisponibleException;
import jakarta.mail.internet.InternetAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@ConditionalOnProperty(name = "app.mail.provider", havingValue = "smtp")
public class SmtpCorreoProveedor implements ProveedorCorreo {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String remitente;
    private final String nombreRemitente;

    public SmtpCorreoProveedor(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            org.springframework.core.env.Environment environment) {
        this.mailSenderProvider = mailSenderProvider;
        this.remitente = environment.getProperty("app.mail.from", "no-reply@uajms.edu.bo");
        this.nombreRemitente = environment.getProperty("app.mail.from-name", "Vidia");
    }

    @Override
    public void enviar(String destinatario, String asunto, String contenido) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.error("Proveedor SMTP local no disponible");
            throw correoNoDisponible();
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();
        try {
            mensaje.setFrom(new InternetAddress(remitente, nombreRemitente, StandardCharsets.UTF_8.name())
                    .toUnicodeString());
        } catch (UnsupportedEncodingException ex) {
            log.error("No fue posible preparar la identidad del remitente de correo");
            throw correoNoDisponible();
        }
        mensaje.setTo(destinatario);
        mensaje.setSubject(asunto);
        mensaje.setText(contenido);

        try {
            mailSender.send(mensaje);
            log.info("Correo transaccional enviado por SMTP local");
        } catch (MailException ex) {
            log.error("Fallo seguro al enviar correo mediante SMTP local: {}", ex.getClass().getSimpleName());
            throw correoNoDisponible();
        }
    }

    private ServicioNoDisponibleException correoNoDisponible() {
        return new ServicioNoDisponibleException(CodigosError.MAIL_SERVICE_UNAVAILABLE,
                "El servicio de correo no esta disponible temporalmente");
    }
}
