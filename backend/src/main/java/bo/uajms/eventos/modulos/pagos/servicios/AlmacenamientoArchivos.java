package bo.uajms.eventos.modulos.pagos.servicios;

import org.springframework.core.io.Resource;

import java.util.Optional;
import java.util.regex.Pattern;

/** Puerto de archivos privados; autorización y ownership pertenecen a los servicios de dominio. */
public interface AlmacenamientoArchivos {
    Pattern CLAVE_COMPROBANTE = Pattern.compile("^comprobantes/[0-9a-fA-F-]{36}/[0-9a-fA-F-]{36}\\.(pdf|png|jpg|jpeg)$");
    Pattern CLAVE_QR_EVENTO = Pattern.compile("^eventos/[0-9a-fA-F-]{36}/qr-pago/[0-9a-fA-F-]{36}\\.(png|jpg|jpeg)$");

    static boolean esClaveQrPago(String clave) {
        return clave != null && CLAVE_QR_EVENTO.matcher(clave).matches();
    }

    static boolean esClavePermitida(String clave) {
        return clave != null && (CLAVE_COMPROBANTE.matcher(clave).matches() || esClaveQrPago(clave));
    }

    void guardar(String clave, byte[] contenido, String tipoContenido);
    Optional<Resource> descargar(String clave);
    void eliminar(String clave);
    boolean existe(String clave);
}
