package bo.uajms.eventos.modulos.certificados.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.modulos.certificados.entidades.Certificado;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class CertificadoDocumentoService {

    private static final String INSTITUCION = "Universidad Autonoma Juan Misael Saracho";

    public byte[] generarPdf(Certificado certificado) {
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(salida);
            PdfDocument pdf = new PdfDocument(writer);
            try (Document documento = new Document(pdf)) {
                Usuario usuario = certificado.getUsuario();
                documento.add(new Paragraph(INSTITUCION).setBold().setFontSize(18));
                documento.add(new Paragraph("CERTIFICADO").setBold().setFontSize(16));
                documento.add(new Paragraph("Participante: " + usuario.getNombres() + " " + usuario.getApellidos()));
                documento.add(new Paragraph(identificadorParticipante(usuario)));
                documento.add(new Paragraph("Evento: " + certificado.getEvento().getTitulo()));
                documento.add(new Paragraph("Tipo: " + certificado.getTipoCertificado().name()));
                if (certificado.getHorasAcademicas() != null) {
                    documento.add(new Paragraph("Horas academicas: " + certificado.getHorasAcademicas()));
                }
                documento.add(new Paragraph("Fecha de emision: " + certificado.getFechaEmision()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
                documento.add(new Paragraph("Codigo de verificacion: " + certificado.getCodigoCertificado()));
                documento.add(new Paragraph("Verificacion publica: " + certificado.getUrlVerificacion()));
                documento.add(new Image(ImageDataFactory.create(generarQrVerificacion(certificado.getUrlVerificacion())))
                        .setWidth(140).setHeight(140));
            }
            return salida.toByteArray();
        } catch (Exception ex) {
            throw new NegocioException("No fue posible generar el PDF del certificado");
        }
    }

    byte[] generarQrVerificacion(String urlVerificacion) {
        if (urlVerificacion == null || urlVerificacion.isBlank()) {
            throw new NegocioException("El certificado no tiene URL de verificacion");
        }
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            var matriz = new QRCodeWriter().encode(urlVerificacion, BarcodeFormat.QR_CODE, 300, 300);
            ImageIO.write(MatrixToImageWriter.toBufferedImage(matriz), "png", salida);
            return salida.toByteArray();
        } catch (Exception ex) {
            throw new NegocioException("No fue posible generar el QR de verificacion");
        }
    }

    private String identificadorParticipante(Usuario usuario) {
        if (usuario.getRu() != null && !usuario.getRu().isBlank()) {
            return "RU: " + usuario.getRu();
        }
        return "CI: " + usuario.getCi();
    }
}
