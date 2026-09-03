package bo.uajms.eventos.core.excepciones;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class SeguridadException extends RuntimeException {
    public SeguridadException(String mensaje) {
        super(mensaje);
    }
}
