package bo.uajms.eventos.modulos.pagos.servicios;

import bo.uajms.eventos.core.excepciones.ServicioNoDisponibleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SupabaseS3StorageServiceTest {
    private S3Client client;
    private SupabaseStorageProperties properties;
    private SupabaseS3StorageService service;
    private String key;

    @BeforeEach
    void setUp() {
        client = mock(S3Client.class);
        properties = new SupabaseStorageProperties();
        properties.setBucket("vidia-comprobantes");
        service = new SupabaseS3StorageService(client, properties);
        key = "comprobantes/" + UUID.randomUUID() + "/" + UUID.randomUUID() + ".pdf";
    }

    @Test
    void uploadDownloadExistsAndDeleteDelegateToS3WithPrivateBucketKey() {
        byte[] payload = "%PDF-1.4".getBytes();
        when(client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenReturn(ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), payload));
        when(client.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder().build());

        service.guardar(key, payload, "application/pdf");
        Resource downloaded = service.descargar(key).orElseThrow();
        assertTrue(service.existe(key));
        service.eliminar(key);

        assertArrayEquals(payload, assertDoesNotThrow(downloaded::getContentAsByteArray));
        verify(client).putObject(argThat((PutObjectRequest request) -> request.bucket().equals("vidia-comprobantes")
                && request.key().equals(key) && request.contentType().equals("application/pdf")), any(RequestBody.class));
        verify(client).getObjectAsBytes(argThat((GetObjectRequest request) -> request.key().equals(key)));
        verify(client).headObject(argThat((HeadObjectRequest request) -> request.key().equals(key)));
        verify(client).deleteObject(argThat((DeleteObjectRequest request) -> request.key().equals(key)));
    }

    @Test
    void falloDeProveedorSeTraduceSinPropagarDetallesDelCliente() {
        when(client.getObjectAsBytes(any(GetObjectRequest.class))).thenThrow(new IllegalStateException("sensitive endpoint detail"));

        ServicioNoDisponibleException error = assertThrows(ServicioNoDisponibleException.class,
                () -> service.descargar(key));

        assertFalse(error.getMessage().contains("sensitive endpoint detail"));
    }

    @Test
    void claveConPathTraversalSeRechazaAntesDeLlamarAlProveedor() {
        assertThrows(IllegalArgumentException.class, () -> service.descargar("comprobantes/../../secret.pdf"));
        verifyNoInteractions(client);
    }
}
