package bo.uajms.eventos.modulos.asistencias.entidades;

import bo.uajms.eventos.comun.EntidadBase;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.sesiones.entidades.SesionEvento;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "asistencias", uniqueConstraints = {
        @UniqueConstraint(name = "uk_asistencia_inscripcion_sesion", columnNames = {"inscripcion_id", "sesion_evento_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE asistencias SET fecha_eliminacion = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("fecha_eliminacion IS NULL")
public class Asistencia extends EntidadBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inscripcion_id", nullable = false)
    private Inscripcion inscripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_evento_id", nullable = false)
    private SesionEvento sesionEvento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por_id")
    private Usuario registradoPor;

    @Column(name = "fecha_hora_registro", nullable = false)
    private LocalDateTime fechaHoraRegistro;

    @Column(name = "distancia_metros", precision = 10, scale = 2)
    private BigDecimal distanciaMetros;

    @Column(name = "precision_gps_metros", precision = 10, scale = 2)
    private BigDecimal precisionGpsMetros;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado_validacion", nullable = false, length = 20)
    @Builder.Default
    private ResultadoValidacion resultadoValidacion = ResultadoValidacion.VALIDADA;

    @Column(columnDefinition = "TEXT")
    private String observacion;

    public enum ResultadoValidacion {
        VALIDADA,
        RECHAZADA
    }
}
