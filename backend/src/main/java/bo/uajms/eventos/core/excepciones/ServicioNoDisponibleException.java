package bo.uajms.eventos.core.excepciones;

public class ServicioNoDisponibleException extends RuntimeException {
    private final String codigo;

    public ServicioNoDisponibleException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
