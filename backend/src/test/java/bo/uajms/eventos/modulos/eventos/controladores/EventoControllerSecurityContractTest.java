package bo.uajms.eventos.modulos.eventos.controladores;

import bo.uajms.eventos.modulos.eventos.dtos.ActualizarEventoRequest;
import bo.uajms.eventos.modulos.eventos.dtos.CrearEventoRequest;
import bo.uajms.eventos.modulos.eventos.dtos.EventoRechazoRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EventoControllerSecurityContractTest {

    @Test
    void crearEstaReservadoAlOrganizador() throws Exception {
        assertEquals("hasRole('ORGANIZADOR')", regla("crear", CrearEventoRequest.class));
    }

    @Test
    void enviarRevisionEstaReservadoAlOrganizador() throws Exception {
        assertEquals("hasRole('ORGANIZADOR')", regla("enviarARevision", UUID.class));
    }

    @Test
    void publicarEstaReservadoAlAdministrador() throws Exception {
        assertEquals("hasRole('ADMINISTRADOR')", regla("publicar", UUID.class));
    }

    @Test
    void rechazarEstaReservadoAlAdministrador() throws Exception {
        assertEquals("hasRole('ADMINISTRADOR')", regla("rechazar", UUID.class, EventoRechazoRequest.class));
    }

    @Test
    void finalizarEstaReservadoAlAdministrador() throws Exception {
        assertEquals("hasRole('ADMINISTRADOR')", regla("finalizar", UUID.class));
    }

    @Test
    void cargarYEliminarQrExigenRolDeGestion() throws Exception {
        assertEquals("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')",
                regla("subirQrPago", MultipartFile.class, UUID.class));
        assertEquals("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')", regla("eliminarQrPago", UUID.class));
    }

    @Test
    void lecturaQrEsPublicaEnElContratoYRestringidaPorServicioParaNoPublicados() throws Exception {
        var metodo = EventoController.class.getMethod("obtenerQrPago", UUID.class);
        assertNotNull(metodo.getAnnotation(io.swagger.v3.oas.annotations.security.SecurityRequirements.class));
        assertNull(metodo.getAnnotation(PreAuthorize.class));
    }

    @Test
    void cancelarEstaReservadoAlAdministrador() throws Exception {
        assertEquals("hasRole('ADMINISTRADOR')", regla("cancelar", UUID.class,
                bo.uajms.eventos.modulos.eventos.dtos.EventoCancelacionRequest.class));
    }

    @Test
    void dtoCreacionNoPermiteAsignarOrganizadorEstadoNiAuditoria() {
        var nombres = Arrays.stream(CrearEventoRequest.class.getMethods()).map(Method::getName).toList();
        assertFalse(nombres.contains("setOrganizador"));
        assertFalse(nombres.contains("setOrganizadorId"));
        assertFalse(nombres.contains("setEstado"));
        assertFalse(nombres.contains("setResueltoPor"));
    }

    @Test
    void dtoActualizacionNoPermiteCambiarOwnershipNiEstado() {
        var nombres = Arrays.stream(ActualizarEventoRequest.class.getMethods()).map(Method::getName).toList();
        assertFalse(nombres.contains("setOrganizadorId"));
        assertFalse(nombres.contains("setEstado"));
    }

    @Test
    void dtoDeEventoNoAceptaKeysNiUrlsQrDeCliente() {
        var nombres = Arrays.stream(CrearEventoRequest.class.getMethods()).map(Method::getName).toList();
        assertFalse(nombres.contains("setQrPagoUrl"));
        assertFalse(nombres.contains("setQrPagoStorageKey"));
    }

    private String regla(String metodo, Class<?>... tipos) throws Exception {
        return EventoController.class.getMethod(metodo, tipos).getAnnotation(PreAuthorize.class).value();
    }
}
