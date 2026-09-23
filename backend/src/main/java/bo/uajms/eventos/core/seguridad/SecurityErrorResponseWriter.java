package bo.uajms.eventos.core.seguridad;

import bo.uajms.eventos.core.excepciones.ErrorRespuesta;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SecurityErrorResponseWriter {
    private final ObjectMapper objectMapper;

    public void escribir(HttpServletRequest request, HttpServletResponse response, int estado,
                         String codigo, String mensaje) throws IOException {
        if (response.isCommitted()) return;
        response.setStatus(estado);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ErrorRespuesta error = ErrorRespuesta.builder()
                .codigo(codigo).mensaje(mensaje).timestamp(LocalDateTime.now())
                .ruta(request.getRequestURI()).build();
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
