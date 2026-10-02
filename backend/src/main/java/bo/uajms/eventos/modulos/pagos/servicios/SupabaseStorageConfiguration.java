package bo.uajms.eventos.modulos.pagos.servicios;

import bo.uajms.eventos.core.excepciones.ServicioNoDisponibleException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(SupabaseStorageProperties.class)
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "supabase")
public class SupabaseStorageConfiguration {

    @Bean(destroyMethod = "close")
    S3Client supabaseS3Client(SupabaseStorageProperties properties) {
        if (blank(properties.getEndpoint()) || blank(properties.getRegion()) || blank(properties.getBucket())
                || blank(properties.getAccessKey()) || blank(properties.getSecretKey())) {
            throw new ServicioNoDisponibleException("STORAGE_CONFIGURATION_MISSING",
                    "El almacenamiento persistente no esta configurado correctamente");
        }
        try {
            URI endpoint = URI.create(properties.getEndpoint());
            if (!"https".equalsIgnoreCase(endpoint.getScheme()) || endpoint.getHost() == null) {
                throw new IllegalArgumentException("Storage endpoint must use HTTPS");
            }
            return S3Client.builder()
                    .endpointOverride(endpoint)
                    .region(Region.of(properties.getRegion()))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())))
                    .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                    .httpClientBuilder(UrlConnectionHttpClient.builder()
                            .connectionTimeout(Duration.ofMillis(properties.getConnectionTimeoutMs()))
                            .socketTimeout(Duration.ofMillis(properties.getApiTimeoutMs())))
                    .overrideConfiguration(ClientOverrideConfiguration.builder()
                            .apiCallTimeout(Duration.ofMillis(properties.getApiTimeoutMs()))
                            .apiCallAttemptTimeout(Duration.ofMillis(properties.getApiTimeoutMs()))
                            .build())
                    .build();
        } catch (IllegalArgumentException exception) {
            throw new ServicioNoDisponibleException("STORAGE_CONFIGURATION_INVALID",
                    "La configuracion del almacenamiento persistente no es valida");
        }
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }
}
