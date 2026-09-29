package bo.uajms.eventos.core.excepciones;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class ManejadorGlobalExcepciones {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> manejarRecursoNoEncontrado(RecursoNoEncontradoException ex, WebRequest request) {
        ErrorRespuesta error = ErrorRespuesta.builder()
                .codigo(ex.getCodigo())
                .mensaje(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .ruta(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ErrorRespuesta> manejarNegocioException(NegocioException ex, WebRequest request) {
        ErrorRespuesta error = ErrorRespuesta.builder()
                .codigo(ex.getCodigo())
                .mensaje(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .ruta(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SeguridadException.class)
    public ResponseEntity<ErrorRespuesta> manejarSeguridadException(SeguridadException ex, WebRequest request) {
        ErrorRespuesta error = ErrorRespuesta.builder()
                .codigo(ex.getCodigo())
                .mensaje(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .ruta(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorRespuesta> manejarAuthenticationException(AuthenticationException ex, WebRequest request) {
        ErrorRespuesta error = ErrorRespuesta.builder()
                .codigo(CodigosError.AUTH_INVALID_CREDENTIALS)
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
                .codigo(CodigosError.VALIDATION_ERROR)
                .mensaje("Datos de entrada invalidos")
                .detalles(detalles)
                .timestamp(LocalDateTime.now())
                .ruta(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorRespuesta> manejarAccesoDenegado(AccessDeniedException ex, WebRequest request) {
        ErrorRespuesta error = ErrorRespuesta.builder()
                .codigo(CodigosError.ACCESS_DENIED)
                .mensaje("No tiene permisos para realizar esta operacion")
                .timestamp(LocalDateTime.now())
                .ruta(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorRespuesta> manejarConflicto(ConflictoException ex, WebRequest request) {
        ErrorRespuesta error = ErrorRespuesta.builder()
                .codigo(ex.getCodigo()).mensaje(ex.getMessage()).timestamp(LocalDateTime.now())
                .ruta(request.getDescription(false)).build();
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRespuesta> manejarIntegridad(DataIntegrityViolationException ex, WebRequest request) {
        log.warn("Conflicto de integridad de datos en {}", request.getDescription(false));
        ErrorRespuesta error = ErrorRespuesta.builder()
                .codigo(CodigosError.CONFLICT)
                .mensaje("La operacion entra en conflicto con el estado actual")
                .timestamp(LocalDateTime.now()).ruta(request.getDescription(false)).build();
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> manejarCuerpoInvalido(HttpMessageNotReadableException ex, WebRequest request) {
        ErrorRespuesta error = ErrorRespuesta.builder()
                .codigo(CodigosError.VALIDATION_ERROR)
                .mensaje("El cuerpo de la solicitud no es valido")
                .timestamp(LocalDateTime.now()).ruta(request.getDescription(false)).build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ServicioNoDisponibleException.class)
    public ResponseEntity<ErrorRespuesta> manejarServicioNoDisponible(
            ServicioNoDisponibleException ex, WebRequest request) {
        ErrorRespuesta error = ErrorRespuesta.builder()
                .codigo(ex.getCodigo())
                .mensaje(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .ruta(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> manejarExcepcionGlobal(Exception ex, WebRequest request) {
        log.error("Error inesperado procesando {}", request.getDescription(false), ex);
        ErrorRespuesta error = ErrorRespuesta.builder()
                .codigo(CodigosError.INTERNAL_ERROR)
                .mensaje("Ha ocurrido un error inesperado en el servidor")
                .timestamp(LocalDateTime.now())
                .ruta(request.getDescription(false))
                .build();
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Aquí se agregarán manejadores específicos para excepciones de negocio
}
