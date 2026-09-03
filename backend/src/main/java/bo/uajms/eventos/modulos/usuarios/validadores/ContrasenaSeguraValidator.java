package bo.uajms.eventos.modulos.usuarios.validadores;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ContrasenaSeguraValidator implements ConstraintValidator<ContrasenaSegura, String> {

    private static final String PATRON_CONTRASENA =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return value.matches(PATRON_CONTRASENA);
    }
}
