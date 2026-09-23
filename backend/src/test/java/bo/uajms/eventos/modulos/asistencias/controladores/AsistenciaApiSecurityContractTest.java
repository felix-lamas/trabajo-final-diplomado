package bo.uajms.eventos.modulos.asistencias.controladores;

import bo.uajms.eventos.modulos.asistencias.dtos.RegistrarAsistenciaRequest;
import bo.uajms.eventos.modulos.sesiones.controladores.SesionEventoController;
import bo.uajms.eventos.modulos.sesiones.dtos.SesionEventoRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AsistenciaApiSecurityContractTest {
    @Test void registrarAsistenciaExigeUsuario() throws Exception {
        assertEquals("hasRole('USUARIO')", AsistenciaController.class.getMethod("registrar", RegistrarAsistenciaRequest.class).getAnnotation(PreAuthorize.class).value());
    }
    @Test void listarEventoNoSeExponeAUsuario() throws Exception {
        assertEquals("hasAnyRole('ADMINISTRADOR', 'ORGANIZADOR')", AsistenciaController.class.getMethod("listarPorEvento", UUID.class).getAnnotation(PreAuthorize.class).value());
    }
    @Test void qrSoloPermiteAdministradorUOrganizador() {
        assertEquals("hasAnyRole('ADMINISTRADOR','ORGANIZADOR')", QrAsistenciaController.class.getAnnotation(PreAuthorize.class).value());
    }
    @Test void crearSesionNoPermiteUsuario() throws Exception {
        assertEquals("hasAnyRole('ADMINISTRADOR','ORGANIZADOR')", SesionEventoController.class.getMethod("crear", UUID.class, SesionEventoRequest.class).getAnnotation(PreAuthorize.class).value());
    }
    @Test void solicitudAsistenciaNoAceptaUsuarioIdNiDistancia() {
        var setters=Arrays.stream(RegistrarAsistenciaRequest.class.getMethods()).map(java.lang.reflect.Method::getName).toList();
        assertFalse(setters.contains("setUsuarioId")); assertFalse(setters.contains("setInscripcionId")); assertFalse(setters.contains("setDistancia"));
    }
    @Test void apiNoRecuperaRutaAntiguaCodigosQr() {
        String ruta=QrAsistenciaController.class.getAnnotation(RequestMapping.class).value()[0];
        assertEquals("/api/v1/sesiones/{sesionId}/qr",ruta); assertFalse(ruta.contains("codigos-qr"));
    }
}
