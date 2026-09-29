package bo.uajms.eventos.modulos.usuarios.dtos;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PerfilYCambioContrasenaValidacionTest {

    private static Validator validator;

    @BeforeAll
    static void configurarValidador() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void aceptaPerfilCoherenteConRegistro() {
        ActualizarPerfilRequest request = perfil("Maria Elena", "Flores Rojas", "+591 72900000");

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rechazaCamposObligatoriosVacios() {
        ActualizarPerfilRequest request = perfil(" ", " ", " ");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void rechazaLongitudesYTelefonoQueRegistroNoAcepta() {
        ActualizarPerfilRequest request = perfil("N".repeat(51), "A".repeat(51), "telefono-invalido");

        assertTrue(validator.validate(request).size() >= 3);
    }

    @Test
    void rechazaNuevaContrasenaDebil() {
        CambioContrasenaRequest request = new CambioContrasenaRequest();
        request.setContrasenaActual("Actual9!");
        request.setNuevaContrasena("debil");
        request.setConfirmacion("debil");

        assertFalse(validator.validate(request).isEmpty());
    }

    private ActualizarPerfilRequest perfil(String nombres, String apellidos, String celular) {
        ActualizarPerfilRequest request = new ActualizarPerfilRequest();
        request.setNombres(nombres);
        request.setApellidos(apellidos);
        request.setCelular(celular);
        return request;
    }
}
