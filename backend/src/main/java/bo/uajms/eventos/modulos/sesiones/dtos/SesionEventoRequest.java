package bo.uajms.eventos.modulos.sesiones.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class SesionEventoRequest {
    @NotBlank @Size(max = 150) private String nombre;
    private String descripcion;
    @NotNull private LocalDate fecha;
    @NotNull private LocalTime horaInicio;
    @NotNull private LocalTime horaFin;
    @NotNull private Boolean requiereAsistencia;
    @DecimalMin("-90.0") @DecimalMax("90.0") private BigDecimal latitud;
    @DecimalMin("-180.0") @DecimalMax("180.0") private BigDecimal longitud;
    @Min(1) @Max(500) private Integer radioMetros = 100;
    @NotNull private Boolean activa = true;
}
