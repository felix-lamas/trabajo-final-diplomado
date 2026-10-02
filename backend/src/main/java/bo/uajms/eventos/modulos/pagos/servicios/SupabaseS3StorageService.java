package bo.uajms.eventos.modulos.pagos.servicios;

import bo.uajms.eventos.core.excepciones.ServicioNoDisponibleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Slf4j
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "supabase")
public class SupabaseS3StorageService implements AlmacenamientoArchivos {
    private static final Pattern CLAVE_SEGURA = Pattern.compile(
            "^comprobantes/[0-9a-fA-F-]{36}/[0-9a-fA-F-]{36}\\.(pdf|png|jpg|jpeg)$");

    private final S3Client cliente;
    private final SupabaseStorageProperties properties;

    public SupabaseS3StorageService(S3Client cliente, SupabaseStorageProperties properties) {
        this.cliente = cliente;
        this.properties = properties;
    }

    @Override
    public void guardar(String clave, byte[] contenido, String tipoContenido) {
        validarClave(clave);
        try {
            cliente.putObject(PutObjectRequest.builder().bucket(properties.getBucket()).key(clave)
                    .contentType(tipoContenido).contentLength((long) contenido.length).build(),
                    RequestBody.fromBytes(contenido));
        } catch (S3Exception exception) {
            log.warn("Fallo al guardar objeto en storage; codigo={}, status={}",
                    exception.awsErrorDetails() == null ? "desconocido" : exception.awsErrorDetails().errorCode(),
                    exception.statusCode());
            throw noDisponible();
        } catch (RuntimeException exception) {
            log.warn("Fallo de transporte al guardar objeto en storage");
            throw noDisponible();
        }
    }

    @Override
    public Optional<Resource> descargar(String clave) {
        validarClave(clave);
        try {
            ResponseBytes<GetObjectResponse> respuesta = cliente.getObjectAsBytes(
                    GetObjectRequest.builder().bucket(properties.getBucket()).key(clave).build());
            return Optional.of(new ByteArrayResource(respuesta.asByteArray()));
        } catch (NoSuchKeyException exception) {
            return Optional.empty();
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) return Optional.empty();
            log.warn("Fallo al descargar objeto de storage; codigo={}, status={}",
                    exception.awsErrorDetails() == null ? "desconocido" : exception.awsErrorDetails().errorCode(),
                    exception.statusCode());
            throw noDisponible();
        } catch (RuntimeException exception) {
            log.warn("Fallo de transporte al descargar objeto de storage");
            throw noDisponible();
        }
    }

    @Override
    public void eliminar(String clave) {
        validarClave(clave);
        try {
            cliente.deleteObject(DeleteObjectRequest.builder().bucket(properties.getBucket()).key(clave).build());
        } catch (RuntimeException exception) {
            log.warn("Fallo al eliminar objeto de storage");
            throw noDisponible();
        }
    }

    @Override
    public boolean existe(String clave) {
        validarClave(clave);
        try {
            cliente.headObject(HeadObjectRequest.builder().bucket(properties.getBucket()).key(clave).build());
            return true;
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) return false;
            log.warn("Fallo al verificar objeto de storage; status={}", exception.statusCode());
            throw noDisponible();
        } catch (RuntimeException exception) {
            log.warn("Fallo de transporte al verificar objeto de storage");
            throw noDisponible();
        }
    }

    private void validarClave(String clave) {
        if (clave == null || !CLAVE_SEGURA.matcher(clave).matches()) {
            throw new IllegalArgumentException("Clave de almacenamiento invalida");
        }
    }

    private ServicioNoDisponibleException noDisponible() {
        return new ServicioNoDisponibleException("STORAGE_UNAVAILABLE",
                "El almacenamiento de archivos no esta disponible temporalmente");
    }
}
