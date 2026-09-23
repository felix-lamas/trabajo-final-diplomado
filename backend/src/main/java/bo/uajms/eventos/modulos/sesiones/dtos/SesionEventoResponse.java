package bo.uajms.eventos.modulos.sesiones.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data @Builder
public class SesionEventoResponse {
    private UUID id;
    private UUID eventoId;
    private String nombre;
    private String descripcion;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Boolean requiereAsistencia;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private Integer radioMetros;
    private Boolean activa;
    private Boolean historica;
}
