package bo.uajms.eventos.modulos.categorias.entidades;

import bo.uajms.eventos.comun.EntidadBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.Locale;

@Entity
@Table(name = "categorias_evento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE categorias_evento SET fecha_eliminacion = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("fecha_eliminacion IS NULL")
public class CategoriaEvento extends EntidadBase {

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "nombre_normalizado", nullable = false, unique = true, length = 100)
    private String nombreNormalizado;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    @Builder.Default
    private String estado = "ACTIVO";

    @PrePersist
    @PreUpdate
    void normalizarNombre() {
        if (nombre != null) {
            nombre = nombre.trim();
            nombreNormalizado = nombre.toLowerCase(Locale.ROOT);
        }
    }
}
