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
@Table(name = "certificados", uniqueConstraints = {
        @UniqueConstraint(name = "uk_certificado_inscripcion", columnNames = "inscripcion_id"),
        @UniqueConstraint(name = "uk_certificado_codigo", columnNames = "codigo_certificado")
}, indexes = {
        @Index(name = "idx_certificado_usuario", columnList = "usuario_id"),
        @Index(name = "idx_certificado_evento", columnList = "evento_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE certificados SET fecha_eliminacion = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("fecha_eliminacion IS NULL")
public class Certificado extends EntidadBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, updatable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false, updatable = false)
    private Evento evento;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inscripcion_id", nullable = false, unique = true, updatable = false)
    private Inscripcion inscripcion;

    @Column(name = "codigo_certificado", nullable = false, unique = true, length = 50, updatable = false)
    private String codigoCertificado;

    @Column(name = "fecha_emision", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaEmision = LocalDateTime.now();

    @Column(name = "url_verificacion", nullable = false, length = 255, updatable = false)
    private String urlVerificacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoCertificado estado = EstadoCertificado.GENERADO;

    @Column(name = "archivo_pdf_url", length = 255)
    private String archivoPdfUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_certificado", nullable = false, length = 20, updatable = false)
    @Builder.Default
    private TipoCertificado tipoCertificado = TipoCertificado.NO_CURRICULAR;

    @Column(name = "horas_academicas", updatable = false)
    private Integer horasAcademicas;

    @Column(name = "porcentaje_asistencia", nullable = false, precision = 5, scale = 2, updatable = false)
    private BigDecimal porcentajeAsistencia;

    public enum EstadoCertificado {
        GENERADO, DESCARGADO, ANULADO
    }

    public enum TipoCertificado {
        CURRICULAR, NO_CURRICULAR
    }
}
