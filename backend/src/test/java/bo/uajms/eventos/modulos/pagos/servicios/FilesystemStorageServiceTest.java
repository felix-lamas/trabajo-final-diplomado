package bo.uajms.eventos.modulos.pagos.servicios;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FilesystemStorageServiceTest {
    @TempDir Path temporaryDirectory;

    @Test
    void guardaDescargaCompruebaYEliminaUnObjetoPorClaveOpaca() throws Exception {
        FilesystemStorageService service = new FilesystemStorageService(temporaryDirectory.toString());
        String key = "comprobantes/" + UUID.randomUUID() + "/" + UUID.randomUUID() + ".pdf";
        byte[] bytes = "%PDF-1.4 test".getBytes();

        service.guardar(key, bytes, "application/pdf");

        assertTrue(service.existe(key));
        Resource resource = service.descargar(key).orElseThrow();
        assertArrayEquals(bytes, resource.getContentAsByteArray());
        service.eliminar(key);
        assertFalse(service.existe(key));
        assertTrue(service.descargar(key).isEmpty());
    }

    @Test
    void rechazaClavesQueNoRespetanLaEstructuraDeComprobantes() {
        FilesystemStorageService service = new FilesystemStorageService(temporaryDirectory.toString());

        assertThrows(IllegalArgumentException.class, () -> service.guardar("../escape.pdf", new byte[1], "application/pdf"));
    }
}
