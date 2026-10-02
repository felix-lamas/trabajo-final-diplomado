package bo.uajms.eventos.modulos.pagos.servicios;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.core.io.Resource;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** Prueba manual opt-in: RUN_SUPABASE_STORAGE_SMOKE=true. Nunca imprime credenciales. */
class SupabaseStorageSmokeTest {
    @Test
    @EnabledIfEnvironmentVariable(named = "RUN_SUPABASE_STORAGE_SMOKE", matches = "true")
    void realizaRoundTripRealDeUnObjetoTemporalYLoElimina() throws Exception {
        String endpoint = required("SUPABASE_STORAGE_ENDPOINT");
        String region = required("SUPABASE_STORAGE_REGION");
        String bucket = required("SUPABASE_STORAGE_BUCKET");
        String accessKey = required("SUPABASE_STORAGE_ACCESS_KEY");
        String secretKey = required("SUPABASE_STORAGE_SECRET_KEY");
        String key = "comprobantes/" + UUID.randomUUID() + "/" + UUID.randomUUID() + ".pdf";
        byte[] payload = "%PDF-1.4 Vidia storage smoke test".getBytes(java.nio.charset.StandardCharsets.US_ASCII);

        SupabaseStorageProperties properties = new SupabaseStorageProperties();
        properties.setEndpoint(endpoint);
        properties.setRegion(region);
        properties.setBucket(bucket);
        properties.setAccessKey(accessKey);
        properties.setSecretKey(secretKey);
        properties.setConnectionTimeoutMs(5000);
        properties.setApiTimeoutMs(20000);
        S3Client client = new SupabaseStorageConfiguration().supabaseS3Client(properties);
        SupabaseS3StorageService storage = new SupabaseS3StorageService(client, properties);
        try (client) {
            try {
                storage.guardar(key, payload, "application/pdf");
                assertTrue(storage.existe(key));
                Resource downloaded = storage.descargar(key).orElseThrow();
                assertArrayEquals(payload, downloaded.getContentAsByteArray());
            } catch (RuntimeException exception) {
                throw new AssertionError("La prueba real de Storage falló; no se muestran detalles de conexión ni credenciales");
            } finally {
                try {
                    storage.eliminar(key);
                    assertFalse(storage.existe(key));
                } catch (RuntimeException exception) {
                    throw new AssertionError("No se pudo limpiar el objeto temporal de prueba");
                }
            }
        }
    }

    private String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new AssertionError("Falta variable requerida: " + name);
        return value;
    }
}
