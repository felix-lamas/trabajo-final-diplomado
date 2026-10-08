package bo.uajms.eventos.modulos.usuarios.servicios;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CorreoServicioTest {

    @Test
    void enviaVerificacionMedianteProveedorAbstractoConEnlaceSinAlterar() {
        ProveedorCorreo proveedor = mock(ProveedorCorreo.class);
        CorreoServicio servicio = new CorreoServicio(proveedor);

        servicio.enviarVerificacionCorreo("ana@example.test", "https://vidia.test/verificar?token=token-falso-test");

        ArgumentCaptor<String> contenido = ArgumentCaptor.forClass(String.class);
        verify(proveedor).enviar(org.mockito.ArgumentMatchers.eq("ana@example.test"),
                org.mockito.ArgumentMatchers.eq("Verifica tu correo - Vidia"), contenido.capture());
        assertTrue(contenido.getValue().contains("https://vidia.test/verificar?token=token-falso-test"));
    }

    @Test
    void enviaRecuperacionConEnlaceProporcionado() {
        ProveedorCorreo proveedor = mock(ProveedorCorreo.class);
        CorreoServicio servicio = new CorreoServicio(proveedor);

        servicio.enviarRecuperacionContrasena("ana@example.test", "https://vidia.test/reset?token=falso-test");

        ArgumentCaptor<String> contenido = ArgumentCaptor.forClass(String.class);
        verify(proveedor).enviar(org.mockito.ArgumentMatchers.eq("ana@example.test"),
                org.mockito.ArgumentMatchers.eq("Recuperacion de contrasena - Plataforma Eventos UAJMS"),
                contenido.capture());
        assertTrue(contenido.getValue().contains("https://vidia.test/reset?token=falso-test"));
    }
}
