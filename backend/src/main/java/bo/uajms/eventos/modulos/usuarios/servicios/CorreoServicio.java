package bo.uajms.eventos.modulos.usuarios.servicios;

import org.springframework.stereotype.Service;

@Service
public class CorreoServicio {

    private final ProveedorCorreo proveedorCorreo;

    public CorreoServicio(ProveedorCorreo proveedorCorreo) {
        this.proveedorCorreo = proveedorCorreo;
    }

    public void enviarRecuperacionContrasena(String destinatario, String enlace) {
        enviar(destinatario, "Recuperacion de contrasena - Plataforma Eventos UAJMS", """
                Recibimos una solicitud para restablecer tu contrasena.

                Ingresa al siguiente enlace para crear una nueva contrasena:
                %s

                El enlace expira en 30 minutos y solo puede utilizarse una vez.
                Si no solicitaste este cambio, ignora este mensaje.
                """.formatted(enlace));
    }

    public void enviarVerificacionCorreo(String destinatario, String enlace) {
        enviar(destinatario, "Verifica tu correo - Vidia", """
                Confirma tu correo para activar tu cuenta en Vidia.

                Abre el siguiente enlace:
                %s

                El enlace expira en 24 horas y solo puede utilizarse una vez.
                Si no creaste esta cuenta, ignora este mensaje.
                """.formatted(enlace));
    }

    public void enviarInvitacionAdministrador(String destinatario, String enlace, java.time.LocalDateTime expiracion) {
        enviar(destinatario, "Invitacion administrativa - Vidia", """
                Has recibido una invitacion para administrar Vidia.
                Confirma tus datos y establece tu propia contrasena mediante este enlace:
                %s
                Expira el %s (hora del servidor). Solo puede utilizarse una vez.
                Si no esperabas esta invitacion, ignora el mensaje o contacta al administrador.
                """.formatted(enlace, expiracion));
    }

    private void enviar(String destinatario, String asunto, String contenido) {
        proveedorCorreo.enviar(destinatario, asunto, contenido);
    }
}
