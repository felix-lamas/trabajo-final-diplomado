package bo.uajms.eventos.modulos.usuarios.servicios;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CorreoServicio {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.mail.from:no-reply@uajms.edu.bo}")
    private String remitente;

    public void enviarRecuperacionContrasena(String destinatario, String enlace) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.info("Enlace de recuperacion para {}: {}", destinatario, enlace);
            return;
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject("Recuperacion de contrasena - Plataforma Eventos UAJMS");
        mensaje.setText("""
                Recibimos una solicitud para restablecer tu contrasena.

                Ingresa al siguiente enlace para crear una nueva contrasena:
                %s

                El enlace expira en 30 minutos y solo puede utilizarse una vez.
                Si no solicitaste este cambio, ignora este mensaje.
                """.formatted(enlace));

        mailSender.send(mensaje);
    }
}
