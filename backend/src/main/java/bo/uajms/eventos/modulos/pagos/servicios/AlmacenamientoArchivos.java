package bo.uajms.eventos.modulos.pagos.servicios;

import org.springframework.core.io.Resource;

import java.util.Optional;

/** Puerto de almacenamiento para comprobantes; las decisiones de acceso permanecen en PagoService. */
public interface AlmacenamientoArchivos {
    void guardar(String clave, byte[] contenido, String tipoContenido);
    Optional<Resource> descargar(String clave);
    void eliminar(String clave);
    boolean existe(String clave);
}
