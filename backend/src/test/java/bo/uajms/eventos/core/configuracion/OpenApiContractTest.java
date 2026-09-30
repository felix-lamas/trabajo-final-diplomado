package bo.uajms.eventos.core.configuracion;

import bo.uajms.eventos.modulos.asistencias.controladores.AsistenciaController;
import bo.uajms.eventos.modulos.asistencias.controladores.QrAsistenciaController;
import bo.uajms.eventos.modulos.categorias.controladores.CategoriaEventoController;
import bo.uajms.eventos.modulos.certificados.controladores.CertificadoController;
import bo.uajms.eventos.modulos.eventos.controladores.EventoController;
import bo.uajms.eventos.modulos.inscripciones.controladores.InscripcionController;
import bo.uajms.eventos.modulos.pagos.controladores.PagoController;
import bo.uajms.eventos.modulos.reportes.controladores.DashboardController;
import bo.uajms.eventos.modulos.sesiones.controladores.SesionEventoController;
import bo.uajms.eventos.modulos.usuarios.controladores.AutenticacionControlador;
import bo.uajms.eventos.modulos.usuarios.controladores.UsuarioControlador;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenApiContractTest {

    private static final List<Class<?>> CONTROLLERS = List.of(
            AutenticacionControlador.class,
            UsuarioControlador.class,
            CategoriaEventoController.class,
            EventoController.class,
            InscripcionController.class,
            PagoController.class,
            SesionEventoController.class,
            AsistenciaController.class,
            QrAsistenciaController.class,
            CertificadoController.class,
            DashboardController.class
    );

    @Test
    void contratoCanonicoContiene78OperacionesDocumentadas() {
        int operaciones = 0;
        for (Class<?> controller : CONTROLLERS) {
            for (Method method : controller.getDeclaredMethods()) {
                RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
                if (mapping == null || method.isAnnotationPresent(Hidden.class)) {
                    continue;
                }
                Operation operation = method.getAnnotation(Operation.class);
                assertNotNull(operation, () -> "Falta @Operation en " + controller.getSimpleName() + "." + method.getName());
                assertFalse(operation.summary().isBlank(), () -> "Falta summary en " + method.getName());
                assertFalse(operation.description().isBlank(), () -> "Falta description en " + method.getName());

                for (String base : rutas(controller.getAnnotation(RequestMapping.class))) {
                    for (String path : rutas(mapping)) {
                        String ruta = normalizar(base + path);
                        if (ruta.startsWith("/api/v1/")) {
                            operaciones++;
                        } else {
                            assertTrue(controller == DashboardController.class,
                                    () -> "Alias legacy no autorizado: " + ruta);
                        }
                    }
                }
            }
        }
        assertEquals(78, operaciones);
    }

    @Test
    void autenticacionNoExponeAliasesLegacy() {
        assertEquals(List.of("/api/v1/auth"),
                Arrays.asList(AutenticacionControlador.class.getAnnotation(RequestMapping.class).value()));
        assertTrue(Arrays.stream(AutenticacionControlador.class.getDeclaredMethods())
                .noneMatch(method -> method.getName().equals("resetearContrasena")));
    }

    @Test
    void logoutEsProtegidoYVerificacionEsPublica() throws Exception {
        Method logout = Arrays.stream(AutenticacionControlador.class.getDeclaredMethods())
                .filter(method -> method.getName().equals("logout"))
                .findFirst().orElseThrow();
        assertNotNull(logout.getAnnotation(SecurityRequirement.class));
        assertTrue(logout.getAnnotation(SecurityRequirements.class) == null);

        Method verificar = Arrays.stream(AutenticacionControlador.class.getDeclaredMethods())
                .filter(method -> method.getName().equals("verificarCorreo"))
                .findFirst().orElseThrow();
        assertNotNull(verificar.getAnnotation(SecurityRequirements.class));
    }

    @Test
    void verificacionLegacySeConservaOcultaPorCompatibilidad() throws Exception {
        Method legacy = CertificadoController.class.getMethod("verificarPublicoLegacy", String.class);
        assertNotNull(legacy.getAnnotation(Hidden.class));
    }

    private List<String> rutas(RequestMapping mapping) {
        if (mapping == null) {
            return List.of("");
        }
        String[] rutas = mapping.path().length == 0 ? mapping.value() : mapping.path();
        return rutas.length == 0 ? List.of("") : Arrays.asList(rutas);
    }

    private String normalizar(String ruta) {
        return ruta.replaceAll("/{2,}", "/");
    }
}
