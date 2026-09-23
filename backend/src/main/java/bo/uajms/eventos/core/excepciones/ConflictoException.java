package bo.uajms.eventos.core.excepciones;

public class ConflictoException extends NegocioException {
    public ConflictoException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
