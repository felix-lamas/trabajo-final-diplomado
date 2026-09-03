package bo.uajms.eventos.modulos.eventos.dtos;

import bo.uajms.eventos.modulos.eventos.entidades.Modalidad;
import bo.uajms.eventos.modulos.eventos.entidades.TipoInscripcion;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class CrearEventoRequest {
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200)
    private String titulo;

    private String descripcion;
    private String objetivos;

    @NotNull(message = "La categoría es obligatoria")
    private UUID categoriaId;

    @NotNull(message = "La modalidad es obligatoria")
    private Modalidad modalidad;

    @NotNull(message = "El tipo de inscripción es obligatorio")
    private TipoInscripcion tipoInscripcion;

    @NotNull(message = "El costo es obligatorio")
    @DecimalMin(value = "0.0", message = "El costo no puede ser negativo")
    private BigDecimal costo;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    private LocalTime horaInicio;
    private LocalTime horaFin;

    private String ubicacion;
    private String enlaceVirtual;

    @NotNull(message = "El cupo máximo es obligatorio")
    @Min(value = 1, message = "El cupo mínimo debe ser 1")
    private Integer cupoMaximo;

    private String imagenPortada;
}
