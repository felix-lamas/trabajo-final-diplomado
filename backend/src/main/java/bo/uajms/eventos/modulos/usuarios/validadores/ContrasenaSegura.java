package bo.uajms.eventos.modulos.usuarios.validadores;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ContrasenaSeguraValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ContrasenaSegura {
    String message() default "La contrasena debe tener minimo 8 caracteres, una mayuscula, una minuscula, un numero y un caracter especial";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
