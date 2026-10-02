package bo.uajms.eventos.modulos.usuarios.servicios;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorreoConfiguracionTest {

    @Test
    void applicationDeclaraVariablesSMTPYRemitenteSinClaveVersionada() throws Exception {
        var config = new YamlPropertySourceLoader().load("application",
                new ClassPathResource("application.yml")).get(0);

        assertEquals("${SPRING_MAIL_HOST:}", config.getProperty("spring.mail.host"));
        assertEquals("${SPRING_MAIL_PORT:587}", config.getProperty("spring.mail.port").toString());
        assertEquals("${SPRING_MAIL_USERNAME:}", config.getProperty("spring.mail.username"));
        assertEquals("${SPRING_MAIL_PASSWORD:}", config.getProperty("spring.mail.password"));
        assertEquals("${MAIL_FROM:appvidia@gmail.com}", config.getProperty("app.mail.from"));
        assertEquals("${MAIL_FROM_NAME:Vidia}", config.getProperty("app.mail.from-name"));
    }

    @Test
    void springMailSenderSeConfiguraConBrevoStarttlsYTimeouts() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(MailSenderAutoConfiguration.class))
                .withPropertyValues(
                        "spring.mail.host=smtp-relay.brevo.com",
                        "spring.mail.port=587",
                        "spring.mail.username=bc2be3001@smtp-brevo.com",
                        "spring.mail.properties[mail.smtp.auth]=true",
                        "spring.mail.properties[mail.smtp.starttls.enable]=true",
                        "spring.mail.properties[mail.smtp.connectiontimeout]=5000",
                        "spring.mail.properties[mail.smtp.timeout]=5000",
                        "spring.mail.properties[mail.smtp.writetimeout]=5000")
                .run(context -> {
                    assertTrue(context.containsBean("mailSender"));
                    JavaMailSenderImpl sender = context.getBean(JavaMailSenderImpl.class);
                    assertEquals("smtp-relay.brevo.com", sender.getHost());
                    assertEquals(587, sender.getPort());
                    assertEquals("bc2be3001@smtp-brevo.com", sender.getUsername());
                    Properties properties = sender.getJavaMailProperties();
                    assertEquals("true", properties.getProperty("mail.smtp.auth"));
                    assertEquals("true", properties.getProperty("mail.smtp.starttls.enable"));
                    assertEquals("5000", properties.getProperty("mail.smtp.connectiontimeout"));
                    assertEquals("5000", properties.getProperty("mail.smtp.timeout"));
                    assertEquals("5000", properties.getProperty("mail.smtp.writetimeout"));
                });
    }
}
