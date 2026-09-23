package bo.uajms.eventos.modulos.pagos.entidades;

import bo.uajms.eventos.comun.EntidadBase;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE pagos SET fecha_eliminacion = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("fecha_eliminacion IS NULL")
public class Pago extends EntidadBase {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inscripcion_id", nullable = false, unique = true)
    private Inscripcion inscripcion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_pago", nullable = false)
    @Builder.Default
    private LocalDateTime fechaPago = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPago estado;

    @Column(columnDefinition = "TEXT")
    private String observacion;

    @Column(name = "comprobante_url", length = 500)
    private String comprobanteUrl;

    @Column(name = "comprobante_nombre_archivo", length = 255)
    private String comprobanteNombreArchivo;

    @Column(name = "comprobante_tipo_contenido", length = 100)
    private String comprobanteTipoContenido;

    @Column(name = "fecha_carga_comprobante")
    private LocalDateTime fechaCargaComprobante;

    @Column(name = "intentos_comprobante", nullable = false)
    @Builder.Default
    private Integer intentosComprobante = 0;

    @Column(name = "motivo_rechazo", length = 1000)
    private String motivoRechazo;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resuelto_por_id")
    private Usuario resueltoPor;

    public String getNumeroReferencia() {
        return getId() != null ? getId().toString() : null;
    }
}
