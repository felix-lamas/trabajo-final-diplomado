package bo.uajms.eventos.core.excepciones;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.junit.jupiter.api.Assertions.*;

class ManejadorGlobalExcepcionesTest {
    private final ManejadorGlobalExcepciones handler = new ManejadorGlobalExcepciones();
    private final ServletWebRequest request = new ServletWebRequest(new MockHttpServletRequest("GET", "/api/protegido"));

    @Test
    void accesoDenegadoDevuelve403Estable() {
        var response = handler.manejarAccesoDenegado(new AccessDeniedException("detalle interno"), request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(CodigosError.ACCESS_DENIED, response.getBody().getCodigo());
        assertFalse(response.getBody().getMensaje().contains("detalle interno"));
    }

    @Test
    void conflictoDevuelve409() {
        var response = handler.manejarConflicto(
                new ConflictoException(CodigosError.ATTENDANCE_DUPLICATED, "Duplicada"), request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(CodigosError.ATTENDANCE_DUPLICATED, response.getBody().getCodigo());
    }

    @Test
    void tamanoMultipartExcesivoDevuelve413SinExponerDetalle() {
        var response = handler.manejarArchivoDemasiadoGrande(
                new MaxUploadSizeExceededException(5L * 1024 * 1024), request);
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
        assertEquals(CodigosError.FILE_TOO_LARGE, response.getBody().getCodigo());
        assertFalse(response.getBody().getMensaje().contains("5"));
    }

    @Test
    void proveedorDeCorreoNoDisponibleDevuelve503() {
        var response = handler.manejarServicioNoDisponible(
                new ServicioNoDisponibleException(CodigosError.MAIL_SERVICE_UNAVAILABLE,
                        "El servicio de correo no esta disponible temporalmente"), request);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(CodigosError.MAIL_SERVICE_UNAVAILABLE, response.getBody().getCodigo());
    }

    @Test
    void errorInesperadoNoExponeDetalle() {
        var response = handler.manejarExcepcionGlobal(new RuntimeException("SELECT secreto FROM tabla"), request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(CodigosError.INTERNAL_ERROR, response.getBody().getCodigo());
        assertNull(response.getBody().getDetalles());
        assertFalse(response.getBody().getMensaje().contains("SELECT"));
    }
}
