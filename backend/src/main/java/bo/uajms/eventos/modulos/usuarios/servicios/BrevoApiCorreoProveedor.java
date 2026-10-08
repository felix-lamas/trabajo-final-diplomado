package bo.uajms.eventos.modulos.usuarios.servicios;

import bo.uajms.eventos.core.excepciones.CodigosError;
import bo.uajms.eventos.core.excepciones.ServicioNoDisponibleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
@ConditionalOnProperty(name = "app.mail.provider", havingValue = "brevo-api", matchIfMissing = true)
public class BrevoApiCorreoProveedor implements ProveedorCorreo {

    private static final String URL_API = "https://api.brevo.com";

    private final RestClient restClient;
    private final String apiKey;
    private final Map<String, String> remitente;

    public BrevoApiCorreoProveedor(
            RestClient.Builder restClientBuilder,
            @Value("${BREVO_API_KEY:}") String apiKey,
            @Value("${app.mail.from:}") String correoRemitente,
            @Value("${app.mail.from-name:Vidia}") String nombreRemitente) {
        this.restClient = restClientBuilder.clone()
                .baseUrl(URL_API)
                .build();
        this.apiKey = apiKey;
        this.remitente = Map.of("email", correoRemitente, "name", nombreRemitente);
    }

    @Override
    public void enviar(String destinatario, String asunto, String contenido) {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("Proveedor Brevo API no configurado: falta la clave de API");
            throw correoNoDisponible();
        }

        Map<String, Object> solicitud = Map.of(
                "sender", remitente,
                "to", List.of(Map.of("email", destinatario)),
                "subject", asunto,
                "textContent", contenido);

        try {
            restClient.post()
                    .uri("/v3/smtp/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("api-key", apiKey)
                    .body(solicitud)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Correo transaccional aceptado por Brevo API");
        } catch (RestClientResponseException ex) {
            log.error("Brevo API rechazo el correo transaccional; HTTP {}", ex.getStatusCode().value());
            throw correoNoDisponible();
        } catch (RestClientException ex) {
            log.error("Fallo de transporte al enviar correo mediante Brevo API: {}",
                    ex.getClass().getSimpleName());
            throw correoNoDisponible();
        }
    }

    private ServicioNoDisponibleException correoNoDisponible() {
        return new ServicioNoDisponibleException(CodigosError.MAIL_SERVICE_UNAVAILABLE,
                "El servicio de correo no esta disponible temporalmente");
    }
}
