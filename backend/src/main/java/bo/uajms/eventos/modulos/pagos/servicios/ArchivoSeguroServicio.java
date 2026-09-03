package bo.uajms.eventos.modulos.pagos.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;

@Service
@Slf4j
public class ArchivoSeguroServicio {

    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of("jpg", "jpeg", "png", "pdf");
    private static final Set<String> EXTENSIONES_BLOQUEADAS = Set.of("exe", "bat", "js", "jar", "dll", "zip");
    private static final Set<String> MIME_PERMITIDOS = Set.of("image/jpeg", "image/png", "application/pdf");
    private static final long TAMANO_MAXIMO_BYTES = 5 * 1024 * 1024L;
    private static final int MAX_ANCHO = 400;
    private static final int MAX_ALTO = 400;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Tika TIKA = new Tika();

    @Value("${app.uploads.base-dir:uploads}")
    private String baseDir;

    public ArchivoGuardado guardarComprobante(MultipartFile archivo, String subDirectorio) {
        validarArchivo(archivo);

        try {
            String extension = extensionNormalizada(archivo.getOriginalFilename());
            byte[] contenido = archivo.getBytes();
            String mimeDetectado = TIKA.detect(contenido, archivo.getOriginalFilename());

            if (esImagen(extension, mimeDetectado)) {
                contenido = optimizarImagen(contenido, extension);
                mimeDetectado = TIKA.detect(contenido, archivo.getOriginalFilename());
            }

            Path destino = prepararDestino(subDirectorio, extension);
            Files.createDirectories(destino.getParent());
            Files.write(destino, contenido);

            return new ArchivoGuardado(
                    "/" + destino.toString().replace('\\', '/'),
                    destino.getFileName().toString(),
                    mimeDetectado
            );
        } catch (IOException e) {
            throw new NegocioException("No fue posible procesar el archivo: " + e.getMessage());
        }
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new NegocioException("El archivo es obligatorio");
        }

        if (archivo.getSize() > TAMANO_MAXIMO_BYTES) {
            throw new NegocioException("El archivo supera el tamano maximo permitido de 5 MB");
        }

        String extension = extensionNormalizada(archivo.getOriginalFilename());
        if (extension == null) {
            throw new NegocioException("El archivo no tiene extension valida");
        }

        if (EXTENSIONES_BLOQUEADAS.contains(extension)) {
            throw new NegocioException("Tipo de archivo bloqueado");
        }

        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new NegocioException("Solo se permiten archivos jpg, jpeg, png y pdf");
        }

        String mime = archivo.getContentType() == null ? "" : archivo.getContentType().toLowerCase();
        if (!MIME_PERMITIDOS.contains(mime)) {
            throw new NegocioException("Tipo MIME no permitido");
        }
    }

    private boolean esImagen(String extension, String mime) {
        return ("jpg".equals(extension) || "jpeg".equals(extension) || "png".equals(extension))
                && (mime.startsWith("image/"));
    }

    private byte[] optimizarImagen(byte[] contenido, String extension) throws IOException {
        BufferedImage original = ImageIO.read(new ByteArrayInputStream(contenido));
        if (original == null) {
            throw new NegocioException("La imagen no pudo ser leida");
        }

        int ancho = original.getWidth();
        int alto = original.getHeight();
        double ratio = Math.min((double) MAX_ANCHO / ancho, (double) MAX_ALTO / alto);
        ratio = Math.min(ratio, 1.0);

        int nuevoAncho = Math.max(1, (int) Math.round(ancho * ratio));
        int nuevoAlto = Math.max(1, (int) Math.round(alto * ratio));

        BufferedImage redimensionada = new BufferedImage(nuevoAncho, nuevoAlto,
                "png".equals(extension) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = redimensionada.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.drawImage(original, 0, 0, nuevoAncho, nuevoAlto, null);
        g2d.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(redimensionada, "png".equals(extension) ? "png" : "jpg", out);
        return out.toByteArray();
    }

    private Path prepararDestino(String subDirectorio, String extension) {
        String nombreSeguro = generarNombreSeguro() + "." + extension;
        return Path.of(baseDir, subDirectorio, nombreSeguro).normalize();
    }

    private String generarNombreSeguro() {
        byte[] bytes = new byte[18];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String extensionNormalizada(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return null;
        }
        int lastDot = originalFilename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == originalFilename.length() - 1) {
            return null;
        }
        return originalFilename.substring(lastDot + 1).toLowerCase();
    }

    public record ArchivoGuardado(String urlArchivo, String nombreArchivo, String tipoContenido) {}
}
