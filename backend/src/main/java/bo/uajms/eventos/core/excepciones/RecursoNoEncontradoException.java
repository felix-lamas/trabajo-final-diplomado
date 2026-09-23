package bo.uajms.eventos.core.excepciones;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RecursoNoEncontradoException extends RuntimeException {
    private final String codigo;

    public RecursoNoEncontradoException(String mensaje) {
        this(CodigosError.RESOURCE_NOT_FOUND, mensaje);
    }

    public RecursoNoEncontradoException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public RecursoNoEncontradoException(String recurso, Object id) {
        this(CodigosError.RESOURCE_NOT_FOUND, recurso + " no encontrado: " + id);
    }

    public String getCodigo() {
        return codigo;
    }
}
