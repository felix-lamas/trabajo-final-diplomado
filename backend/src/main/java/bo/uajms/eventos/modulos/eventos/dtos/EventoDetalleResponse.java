package bo.uajms.eventos.modulos.eventos.dtos;

import bo.uajms.eventos.modulos.eventos.entidades.EstadoEvento;
import bo.uajms.eventos.modulos.eventos.entidades.Modalidad;
import bo.uajms.eventos.modulos.eventos.entidades.TipoInscripcion;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class EventoDetalleResponse {
    private UUID id;
    private String titulo;
    private String descripcion;
    private String objetivos;
    private UUID categoriaId;
    private String categoriaNombre;
    private Modalidad modalidad;
    private TipoInscripcion tipoInscripcion;
    private BigDecimal costo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String ubicacion;
    private String enlaceVirtual;
    private Integer cupoMaximo;
    private Integer cupoDisponible;
    private EstadoEvento estado;
    private String imagenPortada;
    private UUID organizadorId;
    private String organizadorNombre;
}
