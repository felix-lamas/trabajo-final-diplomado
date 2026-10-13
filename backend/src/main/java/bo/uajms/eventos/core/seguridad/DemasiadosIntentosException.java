package bo.uajms.eventos.core.seguridad;

public class DemasiadosIntentosException extends RuntimeException {
    private final long segundos;
    public DemasiadosIntentosException(long segundos) {
        super("Demasiados intentos. Espere antes de volver a intentar.");
        this.segundos = segundos;
    }
    public long getSegundos() { return segundos; }
}
