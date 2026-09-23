package bo.uajms.eventos.modulos.pagos.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ArchivoSeguroServicioTest {

    @TempDir Path temporal;
    private ArchivoSeguroServicio service;

    @BeforeEach
    void setUp() {
        service = new ArchivoSeguroServicio();
        ReflectionTestUtils.setField(service, "baseDir", temporal.toString());
    }

    @Test
    void rechazaArchivoVacio() {
        MockMultipartFile archivo = new MockMultipartFile("archivo", "vacio.pdf", "application/pdf", new byte[0]);
        assertThrows(NegocioException.class, () -> service.guardarComprobante(archivo, "comprobantes"));
    }

    @Test
    void rechazaArchivoMayorACincoMegabytes() {
        byte[] contenido = new byte[5 * 1024 * 1024 + 1];
        MockMultipartFile archivo = new MockMultipartFile("archivo", "grande.pdf", "application/pdf", contenido);
        assertThrows(NegocioException.class, () -> service.guardarComprobante(archivo, "comprobantes"));
    }

    @Test
    void rechazaMimeDeclaradoNoPermitido() {
        MockMultipartFile archivo = new MockMultipartFile("archivo", "script.js", "application/javascript", "alert(1)".getBytes());
        assertThrows(NegocioException.class, () -> service.guardarComprobante(archivo, "comprobantes"));
    }

    @Test
    void rechazaExtensionQueNoCoincideConContenidoReal() {
        MockMultipartFile archivo = new MockMultipartFile("archivo", "falso.pdf", "application/pdf", "texto plano".getBytes());
        assertThrows(NegocioException.class, () -> service.guardarComprobante(archivo, "comprobantes"));
        assertFalse(Files.exists(temporal.resolve("comprobantes")));
    }

    @Test
    void rechazaMimeDeclaradoQueNoCoincideConContenidoReal() throws Exception {
        BufferedImage imagen = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(imagen, "png", bytes);
        MockMultipartFile archivo = new MockMultipartFile("archivo", "comprobante.png", "image/jpeg", bytes.toByteArray());

        assertThrows(NegocioException.class, () -> service.guardarComprobante(archivo, "comprobantes"));
        assertFalse(Files.exists(temporal.resolve("comprobantes")));
    }

    @Test
    void almacenaImagenValidaConNombreSeguroYReferenciaInterna() throws Exception {
        BufferedImage imagen = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(imagen, "png", bytes);
        MockMultipartFile archivo = new MockMultipartFile("archivo", "comprobante.png", "image/png", bytes.toByteArray());

        ArchivoSeguroServicio.ArchivoGuardado guardado = service.guardarComprobante(archivo, "comprobantes");

        assertTrue(guardado.rutaInterna().startsWith("comprobantes/"));
        assertNotEquals("comprobante.png", guardado.nombreArchivo());
        assertEquals("image/png", guardado.tipoContenido());
        assertTrue(Files.exists(temporal.resolve(guardado.rutaInterna())));
        assertFalse(guardado.rutaInterna().contains(temporal.toString()));
    }
}
