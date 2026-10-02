package bo.uajms.eventos.modulos.pagos.servicios;

import bo.uajms.eventos.core.excepciones.NegocioException;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
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
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class ArchivoSeguroServicio {

    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of("jpg", "jpeg", "png", "pdf");
    private static final Set<String> EXTENSIONES_BLOQUEADAS = Set.of("exe", "bat", "js", "jar", "dll", "zip");
    private static final Set<String> MIME_PERMITIDOS = Set.of("image/jpeg", "image/png", "application/pdf");
    private static final long TAMANO_MAXIMO_BYTES = 5 * 1024 * 1024L;
    private static final int MAX_ANCHO = 400;
    private static final int MAX_ALTO = 400;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Tika TIKA = new Tika();
    private static final Pattern CLAVE_STORAGE = Pattern.compile(
            "^comprobantes/[0-9a-fA-F-]{36}/[0-9a-fA-F-]{36}\\.(pdf|png|jpg|jpeg)$");
    private static final Pattern RUTA_LOCAL_LEGACY = Pattern.compile(
            "^comprobantes/[A-Za-z0-9_-]{24}\\.(pdf|png|jpg|jpeg)$");

    @Value("${app.uploads.base-dir:uploads}")
    private String baseDir;

    public ArchivoGuardado guardarComprobante(MultipartFile archivo, String subDirectorio) {
        try {
            ArchivoProcesado procesado = procesarComprobante(archivo);
            Path destino = prepararDestino(subDirectorio, procesado.extension());
            Files.createDirectories(destino.getParent());
            Files.write(destino, procesado.contenido());

            return new ArchivoGuardado(
                    Path.of(baseDir).toAbsolutePath().normalize().relativize(destino).toString().replace('\\', '/'),
                    destino.getFileName().toString(),
                    procesado.tipoContenido()
            );
        } catch (IOException e) {
            throw new NegocioException("No fue posible procesar el archivo");
        }
    }

    public ArchivoProcesado procesarComprobante(MultipartFile archivo) {
        validarArchivo(archivo);
        try {
            String extension = extensionNormalizada(archivo.getOriginalFilename());
            byte[] contenido = archivo.getBytes();
            String mimeDetectado = TIKA.detect(contenido, archivo.getOriginalFilename());
            validarContenidoReal(extension, archivo.getContentType(), mimeDetectado);

            if (esImagen(extension, mimeDetectado)) {
                contenido = optimizarImagen(contenido, extension);
                mimeDetectado = TIKA.detect(contenido, archivo.getOriginalFilename());
            }
            return new ArchivoProcesado(contenido, extension, mimeDetectado);
        } catch (IOException e) {
            throw new NegocioException("No fue posible procesar el archivo");
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

    public Resource cargarArchivo(String rutaInterna) {
        if (rutaInterna == null || rutaInterna.isBlank()) {
            throw new NegocioException("El archivo solicitado no esta disponible");
        }
        Path raiz = Path.of(baseDir).toAbsolutePath().normalize();
        Path archivo = raiz.resolve(rutaInterna).normalize();
        if (!archivo.startsWith(raiz) || !Files.isRegularFile(archivo) || !Files.isReadable(archivo)) {
            throw new NegocioException("El archivo solicitado no esta disponible");
        }
        return new FileSystemResource(archivo);
    }

    public void eliminarArchivoLegacy(String rutaInterna) throws IOException {
        if (rutaInterna == null || !RUTA_LOCAL_LEGACY.matcher(rutaInterna).matches()) return;
        Path raiz = Path.of(baseDir).toAbsolutePath().normalize();
        Path archivo = raiz.resolve(rutaInterna).normalize();
        if (archivo.startsWith(raiz)) Files.deleteIfExists(archivo);
    }

    public static boolean esClaveStorage(String referencia) {
        return referencia != null && CLAVE_STORAGE.matcher(referencia).matches();
    }

    private void validarContenidoReal(String extension, String mimeDeclarado, String mimeDetectado) {
        if (!MIME_PERMITIDOS.contains(mimeDetectado)) {
            throw new NegocioException("El contenido real del archivo no esta permitido");
        }
        if (mimeDeclarado == null || !mimeDetectado.equalsIgnoreCase(mimeDeclarado)) {
            throw new NegocioException("El tipo MIME declarado no coincide con el contenido real del archivo");
        }
        boolean coherente = ("pdf".equals(extension) && "application/pdf".equals(mimeDetectado))
                || (("jpg".equals(extension) || "jpeg".equals(extension)) && "image/jpeg".equals(mimeDetectado))
                || ("png".equals(extension) && "image/png".equals(mimeDetectado));
        if (!coherente) throw new NegocioException("La extension no coincide con el contenido real del archivo");
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
        Path raiz = Path.of(baseDir).toAbsolutePath().normalize();
        Path directorio = raiz.resolve(subDirectorio).normalize();
        if (!directorio.startsWith(raiz)) throw new NegocioException("Directorio de almacenamiento invalido");
        return directorio.resolve(nombreSeguro).normalize();
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

    public record ArchivoGuardado(String rutaInterna, String nombreArchivo, String tipoContenido) {}
    public record ArchivoProcesado(byte[] contenido, String extension, String tipoContenido) {}
}
