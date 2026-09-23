package bo.uajms.eventos.modulos.certificados.entidades;

import bo.uajms.eventos.comun.EntidadBase;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "certificados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE certificados SET fecha_eliminacion = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("fecha_eliminacion IS NULL")
public class Certificado extends EntidadBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inscripcion_id", nullable = false, unique = true)
    private Inscripcion inscripcion;

    @Column(name = "codigo_certificado", nullable = false, unique = true, length = 50)
    private String codigoCertificado;

    @Column(name = "fecha_emision", nullable = false)
    @Builder.Default
    private LocalDateTime fechaEmision = LocalDateTime.now();

    @Column(name = "url_verificacion", length = 255)
    private String urlVerificacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoCertificado estado = EstadoCertificado.GENERADO;

    @Column(name = "archivo_pdf_url", length = 255)
    private String archivoPdfUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_certificado", nullable = false, length = 20)
    @Builder.Default
    private TipoCertificado tipoCertificado = TipoCertificado.NO_CURRICULAR;

    @Column(name = "horas_academicas")
    private Integer horasAcademicas;

    @Column(name = "porcentaje_asistencia", precision = 5, scale = 2)
    private BigDecimal porcentajeAsistencia;

    public enum EstadoCertificado {
        GENERADO, DESCARGADO, ANULADO
    }

    public enum TipoCertificado {
        CURRICULAR, NO_CURRICULAR
    }
}
