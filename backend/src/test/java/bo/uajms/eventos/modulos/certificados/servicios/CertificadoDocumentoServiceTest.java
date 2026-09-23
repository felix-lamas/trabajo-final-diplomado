package bo.uajms.eventos.modulos.certificados.servicios;

import bo.uajms.eventos.modulos.certificados.entidades.Certificado;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CertificadoDocumentoServiceTest {

    private final CertificadoDocumentoService service = new CertificadoDocumentoService();

    @Test
    void pdfContieneDatosObligatorios() throws Exception {
        Certificado certificado = certificado();
        byte[] bytes = service.generarPdf(certificado);

        assertTrue(bytes.length > 1000);
        try (PdfDocument pdf = new PdfDocument(new PdfReader(new ByteArrayInputStream(bytes)))) {
            String texto = PdfTextExtractor.getTextFromPage(pdf.getPage(1));
            assertTrue(texto.contains("Universidad Autonoma Juan Misael Saracho"));
            assertTrue(texto.contains("Ana Perez"));
            assertTrue(texto.contains("RU: 20260001"));
            assertTrue(texto.contains("Jornadas Academicas"));
            assertTrue(texto.contains("CURRICULAR"));
            assertTrue(texto.contains("40"));
            assertTrue(texto.contains("UAJMS-ABC123"));
        }
    }

    @Test
    void qrDelCertificadoApuntaAVerificacionPublica() throws Exception {
        String url = "https://eventos.example.test/api/v1/certificados/verificar/UAJMS-ABC123";
        byte[] png = service.generarQrVerificacion(url);
        var imagen = ImageIO.read(new ByteArrayInputStream(png));
        var bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(imagen)));
        assertEquals(url, new MultiFormatReader().decode(bitmap).getText());
    }

    private Certificado certificado() {
        Usuario usuario = Usuario.builder().nombres("Ana").apellidos("Perez").ru("20260001").ci("1234567").build();
        Evento evento = Evento.builder().titulo("Jornadas Academicas").build();
        return Certificado.builder().usuario(usuario).evento(evento).codigoCertificado("UAJMS-ABC123")
                .tipoCertificado(Certificado.TipoCertificado.CURRICULAR).horasAcademicas(40)
                .fechaEmision(LocalDateTime.of(2026, 9, 23, 12, 0))
                .urlVerificacion("/api/v1/certificados/verificar/UAJMS-ABC123")
                .estado(Certificado.EstadoCertificado.GENERADO).build();
    }
}
