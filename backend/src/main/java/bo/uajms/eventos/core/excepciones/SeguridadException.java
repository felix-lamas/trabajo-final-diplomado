package bo.uajms.eventos.core.excepciones;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class SeguridadException extends RuntimeException {
    private final String codigo;

    public SeguridadException(String mensaje) {
        this(CodigosError.AUTH_REQUIRED, mensaje);
    }

    public SeguridadException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
