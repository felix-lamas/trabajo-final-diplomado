package bo.uajms.eventos.modulos.usuarios.servicios;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.mail.MailException;
import bo.uajms.eventos.core.excepciones.CodigosError;
import bo.uajms.eventos.core.excepciones.ServicioNoDisponibleException;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import jakarta.mail.internet.InternetAddress;

@Service
@RequiredArgsConstructor
@Slf4j
public class CorreoServicio {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.mail.from:no-reply@uajms.edu.bo}")
    private String remitente;

    @Value("${app.mail.from-name:Vidia}")
    private String nombreRemitente;

    public void enviarRecuperacionContrasena(String destinatario, String enlace) {
        enviar(destinatario, "Recuperacion de contrasena - Plataforma Eventos UAJMS", """
                Recibimos una solicitud para restablecer tu contrasena.

                Ingresa al siguiente enlace para crear una nueva contrasena:
                %s

                El enlace expira en 30 minutos y solo puede utilizarse una vez.
                Si no solicitaste este cambio, ignora este mensaje.
                """.formatted(enlace));
    }

    public void enviarVerificacionCorreo(String destinatario, String enlace) {
        enviar(destinatario, "Verifica tu correo - Vidia", """
                Confirma tu correo para activar tu cuenta en Vidia.

                Abre el siguiente enlace:
                %s

                El enlace expira en 24 horas y solo puede utilizarse una vez.
                Si no creaste esta cuenta, ignora este mensaje.
                """.formatted(enlace));
    }

    private void enviar(String destinatario, String asunto, String contenido) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.error("Servicio SMTP no configurado; no fue posible enviar el correo solicitado");
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
            log.info("Correo transaccional enviado correctamente");
        } catch (MailException ex) {
            log.error("Fallo seguro al enviar correo mediante SMTP: {}", ex.getClass().getSimpleName());
            throw correoNoDisponible();
        }
    }

    private ServicioNoDisponibleException correoNoDisponible() {
        return new ServicioNoDisponibleException(CodigosError.MAIL_SERVICE_UNAVAILABLE,
                "El servicio de correo no esta disponible temporalmente");
    }
}
