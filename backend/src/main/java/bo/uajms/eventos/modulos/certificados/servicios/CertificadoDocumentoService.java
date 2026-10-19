package bo.uajms.eventos.modulos.certificados.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import bo.uajms.eventos.modulos.certificados.entidades.Certificado;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class CertificadoDocumentoService {

    private static final Logger log = LoggerFactory.getLogger(CertificadoDocumentoService.class);
    private static final String INSTITUCION = "Universidad Autónoma Juan Misael Saracho";
    private static final Color NAVY = new DeviceRgb(18, 42, 78);
    private static final Color RED = new DeviceRgb(166, 42, 55);
    private static final Color GRAY = new DeviceRgb(82, 91, 104);
    private static final float PAGE_WIDTH = PageSize.A4.rotate().getWidth();

    @Value("${app.certificados.facultad:}") private String facultad = "";
    @Value("${app.certificados.logotipo-path:}") private String logotipoPath = "";
    @Value("${app.certificados.autoridad-1-nombre:}") private String autoridad1Nombre = "";
    @Value("${app.certificados.autoridad-1-cargo:Autoridad institucional}") private String autoridad1Cargo = "Autoridad institucional";
    @Value("${app.certificados.autoridad-1-firma-path:}") private String autoridad1FirmaPath = "";
    @Value("${app.certificados.autoridad-2-nombre:}") private String autoridad2Nombre = "";
    @Value("${app.certificados.autoridad-2-cargo:Representante del evento}") private String autoridad2Cargo = "Representante del evento";
    @Value("${app.certificados.autoridad-2-firma-path:}") private String autoridad2FirmaPath = "";

    public byte[] generarPdf(Certificado certificado) {
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            PdfDocument pdf = new PdfDocument(new PdfWriter(salida));
            pdf.setDefaultPageSize(PageSize.A4.rotate());
            pdf.addNewPage(PageSize.A4.rotate());
            try (Document documento = new Document(pdf, PageSize.A4.rotate())) {
                documento.setMargins(0, 0, 0, 0);
                PdfCanvas canvas = new PdfCanvas(pdf.getFirstPage());
                PdfFont serif = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
                PdfFont serifBold = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
                PdfFont sans = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                PdfFont sansBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

                dibujarMarco(canvas);
                dibujarImagenOpcional(canvas, logotipoPath, 48, 490, 56, 56);
                dibujarImagenOpcional(canvas, logotipoPath, 737, 490, 56, 56);
                centrado(canvas, serifBold, 18, NAVY, INSTITUCION, 530);
                centrado(canvas, sansBold, 8, RED, "TARIJA · BOLIVIA", 513);
                if (noVacio(facultad)) centrado(canvas, sans, 9, GRAY, facultad, 498);

                centrado(canvas, serifBold, 31, NAVY, "CERTIFICADO", 454);
                centrado(canvas, sansBold, 9, RED, subtitulo(certificado), 434);
                centrado(canvas, serif, 11, GRAY,
                        "La Universidad Autónoma Juan Misael Saracho confiere el presente certificado a:", 407);

                Usuario usuario = certificado == null ? null : certificado.getUsuario();
                String nombre = ((usuario == null ? "" : valor(usuario.getNombres())) + " " +
                        (usuario == null ? "" : valor(usuario.getApellidos()))).trim();
                if (nombre.isBlank()) nombre = "Participante";
                List<String> nombreLineas = ajustarLineas(nombre, serifBold, 25, 680, 2);
                float nombreY = nombreLineas.size() == 1 ? 371 : 381;
                for (String linea : nombreLineas) {
                    centrado(canvas, serifBold, nombreLineas.size() == 1 ? 25 : 21, NAVY, linea, nombreY);
                    nombreY -= 25;
                }

                String identificacion = identificacion(usuario);
                if (!identificacion.isBlank()) centrado(canvas, sans, 8, GRAY, identificacion, nombreY - 1);
                centrado(canvas, serif, 10, GRAY, "En reconocimiento a su participación en:", nombreY - 24);

                String evento = certificado == null || certificado.getEvento() == null
                        ? "Evento académico" : valor(certificado.getEvento().getTitulo());
                List<String> eventoLineas = ajustarLineas(evento.isBlank() ? "Evento académico" : evento,
                        serifBold, 15, 650, 3);
                float eventoY = nombreY - 49;
                for (String linea : eventoLineas) {
                    centrado(canvas, serifBold, eventoLineas.size() == 3 ? 12 : 14, NAVY, linea, eventoY);
                    eventoY -= 17;
                }

                String detalle = detalle(certificado);
                if (!detalle.isBlank()) centrado(canvas, sans, 8, GRAY, truncar(detalle, 120), eventoY - 4);
                String fecha = certificado == null || certificado.getFechaEmision() == null ? "" :
                        certificado.getFechaEmision().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "BO")));
                centrado(canvas, sans, 8, GRAY, "Emitido el " + fecha, 152);

                dibujarAutoridad(canvas, 238, autoridad1Nombre, autoridad1Cargo, autoridad1FirmaPath, sans, sansBold);
                dibujarAutoridad(canvas, 524, autoridad2Nombre, autoridad2Cargo, autoridad2FirmaPath, sans, sansBold);

                if (certificado != null && noVacio(certificado.getUrlVerificacion())) {
                    canvas.addImageFittedIntoRectangle(
                            ImageDataFactory.create(generarQrVerificacion(certificado.getUrlVerificacion())),
                            new Rectangle(740, 51, 62, 62), false);
                }
                centradoEn(canvas, sansBold, 7, NAVY,
                        certificado == null ? "" : valor(certificado.getCodigoCertificado()), 771, 41);
                centradoEn(canvas, sans, 6, GRAY, "VERIFICACIÓN PÚBLICA", 771, 32);
            }
            pdf.close();
            return salida.toByteArray();
        } catch (Exception ex) {
            log.error("No fue posible generar el PDF del certificado", ex);
            throw new NegocioException("No fue posible generar el PDF del certificado");
        }
    }

    private void dibujarMarco(PdfCanvas canvas) {
        canvas.saveState()
                .setStrokeColor(NAVY).setLineWidth(2).rectangle(22, 22, 798, 551).stroke()
                .setStrokeColor(RED).setLineWidth(.8f).rectangle(29, 29, 784, 537).stroke()
                .setFillColor(NAVY).rectangle(22, 22, 8, 38).fill().rectangle(812, 535, 8, 38).fill()
                .setFillColor(RED).rectangle(30, 22, 40, 5).fill().rectangle(772, 568, 40, 5).fill()
                .restoreState();
    }

    private void dibujarImagenOpcional(PdfCanvas canvas, String ruta, float x, float y, float w, float h) {
        if (!noVacio(ruta)) return;
        try {
            Path path = Path.of(ruta);
            if (!Files.isRegularFile(path)) {
                log.warn("No se encontró el recurso gráfico opcional del certificado: {}", ruta);
                return;
            }
            canvas.addImageFittedIntoRectangle(ImageDataFactory.create(path.toString()), new Rectangle(x, y, w, h), true);
        } catch (Exception ex) {
            log.warn("No se pudo cargar el recurso gráfico opcional del certificado: {}", ruta);
        }
    }

    private void dibujarAutoridad(PdfCanvas canvas, float x, String nombre, String cargo, String firma,
                                 PdfFont sans, PdfFont sansBold) {
        dibujarImagenOpcional(canvas, firma, x - 48, 119, 96, 30);
        canvas.saveState().setStrokeColor(GRAY).setLineWidth(.6f).moveTo(x - 75, 118).lineTo(x + 75, 118)
                .stroke().restoreState();
        if (noVacio(nombre)) centradoEn(canvas, sansBold, 7, NAVY, nombre, x, 105);
        if (noVacio(cargo)) centradoEn(canvas, sans, 7, GRAY, cargo, x, noVacio(nombre) ? 94 : 104);
    }

    private void centrado(PdfCanvas canvas, PdfFont font, float size, Color color, String text, float y) {
        centradoEn(canvas, font, size, color, text, PAGE_WIDTH / 2, y);
    }

    private void centradoEn(PdfCanvas canvas, PdfFont font, float size, Color color, String text, float centro, float y) {
        String limpio = valor(text).replace('\n', ' ').replace("\r", "");
        float x = Math.max(38, centro - font.getWidth(limpio, size) / 2);
        canvas.saveState().beginText().setFontAndSize(font, size).setFillColor(color).moveText(x, y)
                .showText(limpio).endText().restoreState();
    }

    private List<String> ajustarLineas(String text, PdfFont font, float size, float anchoMaximo, int maxLineas) {
        List<String> lineas = new ArrayList<>();
        StringBuilder linea = new StringBuilder();
        for (String palabra : text.trim().split("\\s+")) {
            if (palabra.isBlank()) continue;
            String candidata = linea.isEmpty() ? palabra : linea + " " + palabra;
            if (!linea.isEmpty() && font.getWidth(candidata, size) > anchoMaximo) {
                lineas.add(linea.toString());
                linea.setLength(0);
                linea.append(palabra);
            } else {
                if (!linea.isEmpty()) linea.append(' ');
                linea.append(palabra);
            }
        }
        if (!linea.isEmpty()) lineas.add(linea.toString());
        if (lineas.size() > maxLineas) {
            lineas = new ArrayList<>(lineas.subList(0, maxLineas));
            String ultima = lineas.get(maxLineas - 1);
            while (!ultima.isEmpty() && font.getWidth(ultima + "…", size) > anchoMaximo)
                ultima = ultima.substring(0, ultima.length() - 1);
            lineas.set(maxLineas - 1, ultima + "…");
        }
        return lineas;
    }

    private String subtitulo(Certificado certificado) {
        return certificado != null && certificado.getTipoCertificado() == Certificado.TipoCertificado.CURRICULAR
                ? "RECONOCIMIENTO DE PARTICIPACIÓN ACADÉMICA" : "CONSTANCIA DE PARTICIPACIÓN";
    }

    private String identificacion(Usuario usuario) {
        if (usuario == null) return "";
        String ru = noVacio(usuario.getRu()) ? "RU " + usuario.getRu() : "";
        String ci = noVacio(usuario.getCi()) ? "CI " + usuario.getCi() : "";
        return ru.isBlank() ? ci : ci.isBlank() ? ru : ru + "   ·   " + ci;
    }

    private String detalle(Certificado certificado) {
        if (certificado == null) return "";
        StringBuilder detalle = new StringBuilder();
        if (certificado.getHorasAcademicas() != null) detalle.append(certificado.getHorasAcademicas()).append(" horas académicas");
        if (certificado.getEvento() != null && certificado.getEvento().getOrganizador() != null) {
            Usuario organizador = certificado.getEvento().getOrganizador();
            String nombre = (valor(organizador.getNombres()) + " " + valor(organizador.getApellidos())).trim();
            if (!nombre.isBlank()) {
                if (!detalle.isEmpty()) detalle.append("   ·   ");
                detalle.append("Organizado por ").append(nombre);
            }
        }
        return detalle.toString();
    }

    private boolean noVacio(String text) { return text != null && !text.isBlank(); }
    private String valor(String text) { return text == null ? "" : text.trim(); }
    private String truncar(String text, int maximo) { return text.length() <= maximo ? text : text.substring(0, maximo - 1) + "…"; }

    byte[] generarQrVerificacion(String urlVerificacion) {
        if (!noVacio(urlVerificacion)) throw new NegocioException("El certificado no tiene URL de verificación");
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            var matriz = new QRCodeWriter().encode(urlVerificacion, BarcodeFormat.QR_CODE, 300, 300);
            ImageIO.write(MatrixToImageWriter.toBufferedImage(matriz), "png", salida);
            return salida.toByteArray();
        } catch (Exception ex) {
            throw new NegocioException("No fue posible generar el QR de verificación");
        }
    }
}
