package bo.uajms.eventos.modulos.pagos.servicios;

import bo.uajms.eventos.core.excepciones.ServicioNoDisponibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/** Almacenamiento local para desarrollo y pruebas; produccion fuerza la implementacion externa. */
@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
public class FilesystemStorageService implements AlmacenamientoArchivos {
    private final Path raiz;

    public FilesystemStorageService(@Value("${app.uploads.base-dir:uploads}") String baseDir) {
        this.raiz = Path.of(baseDir).toAbsolutePath().normalize();
    }

    @Override
    public void guardar(String clave, byte[] contenido, String tipoContenido) {
        try {
            Path destino = resolver(clave);
            Files.createDirectories(destino.getParent());
            Files.write(destino, contenido);
        } catch (IOException exception) {
            throw noDisponible();
        }
    }

    @Override
    public Optional<Resource> descargar(String clave) {
        Path archivo = resolver(clave);
        if (!Files.isRegularFile(archivo) || !Files.isReadable(archivo)) return Optional.empty();
        return Optional.of(new org.springframework.core.io.FileSystemResource(archivo));
    }

    @Override
    public void eliminar(String clave) {
        try {
            Files.deleteIfExists(resolver(clave));
        } catch (IOException exception) {
            throw noDisponible();
        }
    }

    @Override
    public boolean existe(String clave) {
        Path archivo = resolver(clave);
        return Files.isRegularFile(archivo) && Files.isReadable(archivo);
    }

    private Path resolver(String clave) {
        if (!AlmacenamientoArchivos.esClavePermitida(clave)) {
            throw new IllegalArgumentException("Clave de almacenamiento invalida");
        }
        Path archivo = raiz.resolve(clave).normalize();
        if (!archivo.startsWith(raiz)) throw new IllegalArgumentException("Clave de almacenamiento invalida");
        return archivo;
    }

    private ServicioNoDisponibleException noDisponible() {
        return new ServicioNoDisponibleException("STORAGE_UNAVAILABLE",
                "El almacenamiento de archivos no esta disponible temporalmente");
    }
}
