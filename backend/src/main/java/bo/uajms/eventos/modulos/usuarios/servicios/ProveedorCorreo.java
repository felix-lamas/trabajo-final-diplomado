package bo.uajms.eventos.modulos.usuarios.servicios;

public interface ProveedorCorreo {

    void enviar(String destinatario, String asunto, String contenido);
}
