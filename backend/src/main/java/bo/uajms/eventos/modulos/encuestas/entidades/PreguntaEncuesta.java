package bo.uajms.eventos.modulos.encuestas.entidades;

import bo.uajms.eventos.comun.EntidadBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "preguntas_encuesta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE preguntas_encuesta SET fecha_eliminacion = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("fecha_eliminacion IS NULL")
public class PreguntaEncuesta extends EntidadBase {

    @Column(nullable = false, length = 200)
    private String texto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoPreguntaEncuesta tipo;

    @Column(nullable = false)
    private boolean obligatoria;

    @Column(nullable = false)
    private Integer orden;

    @Column(nullable = false)
    private boolean activa;
}
