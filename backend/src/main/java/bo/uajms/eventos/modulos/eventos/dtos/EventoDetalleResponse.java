package bo.uajms.eventos.modulos.eventos.dtos;

import bo.uajms.eventos.modulos.eventos.entidades.EstadoEvento;
import bo.uajms.eventos.modulos.eventos.entidades.Modalidad;
import bo.uajms.eventos.modulos.eventos.entidades.TipoInscripcion;
import bo.uajms.eventos.modulos.eventos.entidades.PublicoObjetivo;
import bo.uajms.eventos.modulos.eventos.entidades.TipoCertificadoEvento;
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
    private String direccion;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private Integer radioMetros;
    private String enlaceVirtual;
    private Boolean requiereInscripcion;
    private Boolean cupoLimitado;
    private Integer cupoMaximo;
    private Integer cupoDisponible;
    private EstadoEvento estado;
    private String imagenPortada;
    private Boolean emiteCertificado;
    private TipoCertificadoEvento tipoCertificado;
    private Integer horasAcademicas;
    private PublicoObjetivo publicoObjetivo;
    private String telefonoContacto;
    private String emailContacto;
    private String whatsappContacto;
    private String qrPagoUrl;
    private String instruccionesPago;
    private String motivoRechazo;
    private String motivoCancelacion;
    private UUID organizadorId;
    private String organizadorNombre;
}
