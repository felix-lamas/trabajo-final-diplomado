package bo.uajms.eventos.modulos.eventos.dtos;

import bo.uajms.eventos.modulos.eventos.entidades.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public abstract class EventoDatosRequest {
    @NotBlank @Size(max = 200) private String titulo;
    private String descripcion;
    private String objetivos;
    @NotNull private UUID categoriaId;
    @NotNull private Modalidad modalidad;
    @NotNull private TipoInscripcion tipoInscripcion;
    @DecimalMin("0.0") private BigDecimal costo;
    @NotNull private LocalDate fechaInicio;
    @NotNull private LocalDate fechaFin;
    @NotNull private LocalTime horaInicio;
    @NotNull private LocalTime horaFin;
    @Size(max = 255) private String ubicacion;
    @Size(max = 500) private String direccion;
    @DecimalMin("-90.0") @DecimalMax("90.0") private BigDecimal latitud;
    @DecimalMin("-180.0") @DecimalMax("180.0") private BigDecimal longitud;
    @Min(1) @Max(500) private Integer radioMetros;
    @Size(max = 500) private String enlaceVirtual;
    @NotNull private Boolean requiereInscripcion;
    @NotNull private Boolean cupoLimitado;
    @Min(1) private Integer cupoMaximo;
    @NotNull private Boolean emiteCertificado;
    private TipoCertificadoEvento tipoCertificado;
    @Min(1) private Integer horasAcademicas;
    @NotNull private PublicoObjetivo publicoObjetivo;
    @Pattern(regexp = "^[0-9+() -]{7,20}$") private String telefonoContacto;
    @Email @Size(max = 100) private String emailContacto;
    @Pattern(regexp = "^[0-9+() -]{7,20}$") private String whatsappContacto;
    @Size(max = 500) private String imagenPortada;
    @Size(max = 2000) private String instruccionesPago;
}
