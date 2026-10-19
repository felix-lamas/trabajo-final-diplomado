package bo.uajms.eventos.modulos.certificados.servicios;

import bo.uajms.eventos.modulos.certificados.entidades.Certificado;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CertificadoDocumentoServiceTest {

    private final CertificadoDocumentoService service = new CertificadoDocumentoService();

    @Test
    void pdfContieneDatosObligatoriosYQr() throws Exception {
        Certificado certificado = certificado();
        byte[] bytes = service.generarPdf(certificado);
        assertTrue(bytes.length > 1000);
        try (PdfDocument pdf = new PdfDocument(new PdfReader(new ByteArrayInputStream(bytes)))) {
            assertEquals(1, pdf.getNumberOfPages());
            assertEquals(PageSize.A4.rotate().getWidth(), pdf.getPage(1).getPageSize().getWidth(), .1);
            assertEquals(PageSize.A4.rotate().getHeight(), pdf.getPage(1).getPageSize().getHeight(), .1);
            String texto = PdfTextExtractor.getTextFromPage(pdf.getPage(1));
            assertTrue(texto.contains("Universidad Autónoma Juan Misael Saracho"));
            assertTrue(texto.contains("RECONOCIMIENTO DE PARTICIPACIÓN ACADÉMICA"));
            assertTrue(texto.contains("Ana Perez"));
            assertTrue(texto.contains("RU 20260001"));
            assertTrue(texto.contains("CI 1234567"));
            assertTrue(texto.contains("Jornadas Academicas"));
            assertTrue(texto.contains("40 horas académicas"));
            assertTrue(texto.contains("UAJMS-ABC123"));
            assertFalse(pdf.getPage(1).getResources().getResource(PdfName.XObject).isEmpty());
        }
    }

    @Test
    void pdfAdmiteNombresYEventosLargosConTildesYDatosOpcionalesAusentes() throws Exception {
        Certificado certificado = certificado();
        certificado.getUsuario().setNombres("Ángela María del Carmen");
        certificado.getUsuario().setApellidos("Pérez Quiñones de la Cruz");
        certificado.getUsuario().setCi(null);
        certificado.getUsuario().setRu(null);
        certificado.getEvento().setTitulo("Seminario internacional de innovación, tecnología y gestión universitaria con enfoque regional");
        byte[] bytes = service.generarPdf(certificado);
        try (PdfDocument pdf = new PdfDocument(new PdfReader(new ByteArrayInputStream(bytes)))) {
            String texto = PdfTextExtractor.getTextFromPage(pdf.getPage(1));
            assertTrue(texto.contains("Ángela María del Carmen Pérez Quiñones de la Cruz"));
            assertTrue(texto.contains("Seminario internacional de innovación"));
            assertFalse(texto.contains("CI "));
            assertFalse(texto.contains("RU "));
        }
    }

    @Test
    void qrDelCertificadoApuntaAUrlPublicaGuardada() throws Exception {
        String url = "https://eventos.example.test/verificar-certificado/UAJMS-ABC123";
        byte[] png = service.generarQrVerificacion(url);
        var imagen = ImageIO.read(new ByteArrayInputStream(png));
        var bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(imagen)));
        assertEquals(url, new MultiFormatReader().decode(bitmap).getText());
        assertFalse(url.toLowerCase().contains("jwt"));
        assertFalse(url.toLowerCase().contains("password"));
    }

    @Test
    void recursosGraficosAusentesNoImpidenGenerarYGuardaEjemploFicticio() throws Exception {
        ReflectionTestUtils.setField(service, "logotipoPath", "target/recurso-institucional-inexistente.png");
        ReflectionTestUtils.setField(service, "autoridad1FirmaPath", "target/firma-inexistente.png");
        Certificado ejemplo = certificado();
        ejemplo.getUsuario().setNombres("PERSONA FICTICIA");
        ejemplo.getUsuario().setApellidos("EJEMPLO DE PRUEBA");
        ejemplo.getUsuario().setCi("CI-PRUEBA-000");
        ejemplo.getUsuario().setRu("RU-PRUEBA-000");
        ejemplo.getEvento().setTitulo("Evento demostrativo de certificados — DATOS DE PRUEBA");
        Path salida = Path.of("target", "certificado-ejemplo-prueba.pdf");
        Files.createDirectories(salida.getParent());
        Files.write(salida, service.generarPdf(ejemplo));
        assertTrue(Files.size(salida) > 1000);
        try (PdfDocument pdf = new PdfDocument(new PdfReader(salida.toString()))) {
            String texto = PdfTextExtractor.getTextFromPage(pdf.getPage(1));
            assertTrue(texto.contains("PERSONA FICTICIA EJEMPLO DE PRUEBA"));
            assertTrue(texto.contains("DATOS DE PRUEBA"));
        }
    }

    private Certificado certificado() {
        Usuario usuario = Usuario.builder().nombres("Ana").apellidos("Perez").ru("20260001").ci("1234567").build();
        Evento evento = Evento.builder().titulo("Jornadas Academicas").build();
        return Certificado.builder().usuario(usuario).evento(evento).codigoCertificado("UAJMS-ABC123")
                .tipoCertificado(Certificado.TipoCertificado.CURRICULAR).horasAcademicas(40)
                .fechaEmision(LocalDateTime.of(2026, 9, 23, 12, 0))
                .urlVerificacion("https://eventos.example.test/verificar-certificado/UAJMS-ABC123")
                .estado(Certificado.EstadoCertificado.GENERADO).build();
    }
}
