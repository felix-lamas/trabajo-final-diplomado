package bo.uajms.eventos.core.excepciones;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> manejarRecursoNoEncontrado(RecursoNoEncontradoException ex, WebRequest request) {
        ErrorRespuesta error = ErrorRespuesta.builder()
                .codigo("RECURSO_NO_ENCONTRADO")
                .mensaje(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .ruta(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ErrorRespuesta> manejarNegocioException(NegocioException ex, WebRequest request) {
        ErrorRespuesta error = ErrorRespuesta.builder()
                .codigo("ERROR_NEGOCIO")
                .mensaje(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .ruta(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SeguridadException.class)
    public ResponseEntity<ErrorRespuesta> manejarSeguridadException(SeguridadException ex, WebRequest request) {
        ErrorRespuesta error = ErrorRespuesta.builder()
                .codigo("ERROR_SEGURIDAD")
                .mensaje(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .ruta(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorRespuesta> manejarAuthenticationException(AuthenticationException ex, WebRequest request) {
        ErrorRespuesta error = ErrorRespuesta.builder()
                .codigo("CREDENCIALES_INVALIDAS")
                .mensaje("Correo electronico o contrasena incorrectos")
                .timestamp(LocalDateTime.now())
                .ruta(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> manejarValidacion(MethodArgumentNotValidException ex, WebRequest request) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        ErrorRespuesta error = ErrorRespuesta.builder()
                .codigo("VALIDACION_ERROR")
                .mensaje("Datos de entrada invalidos")
                .detalles(detalles)
                .timestamp(LocalDateTime.now())
                .ruta(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> manejarExcepcionGlobal(Exception ex, WebRequest request) {
        ErrorRespuesta error = ErrorRespuesta.builder()
                .codigo("ERROR_INTERNO")
                .mensaje("Ha ocurrido un error inesperado en el servidor")
                .detalles(java.util.List.of(ex.getMessage()))
                .timestamp(LocalDateTime.now())
                .ruta(request.getDescription(false))
                .build();
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Aquí se agregarán manejadores específicos para excepciones de negocio
}
