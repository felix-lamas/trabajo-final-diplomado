package bo.uajms.eventos.modulos.eventos.repositorios;

import bo.uajms.eventos.modulos.eventos.entidades.EstadoEvento;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import bo.uajms.eventos.modulos.eventos.entidades.Modalidad;
import bo.uajms.eventos.modulos.eventos.entidades.TipoInscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventoRepository extends JpaRepository<Evento, UUID> {
    List<Evento> findByEstado(EstadoEvento estado);
    List<Evento> findByOrganizadorId(UUID organizadorId);
    long countByOrganizadorId(UUID organizadorId);
    long countByOrganizadorIdAndEstado(UUID organizadorId, EstadoEvento estado);
    List<Evento> findByOrganizadorIdOrEstado(UUID organizadorId, EstadoEvento estado);
    List<Evento> findByCategoriaId(UUID categoriaId);
    List<Evento> findByCategoriaIdAndEstado(UUID categoriaId, EstadoEvento estado);
    List<Evento> findByCategoriaIdAndOrganizadorId(UUID categoriaId, UUID organizadorId);
    Optional<Evento> findByIdAndEstado(UUID id, EstadoEvento estado);
    Optional<Evento> findByIdAndOrganizadorId(UUID id, UUID organizadorId);

    @Query("""
            SELECT e FROM Evento e
            WHERE e.categoria.id = :categoriaId
              AND (e.organizador.id = :organizadorId OR e.estado = :estado)
            """)
    List<Evento> findVisiblesPorCategoriaParaOrganizador(
            @Param("categoriaId") UUID categoriaId,
            @Param("organizadorId") UUID organizadorId,
            @Param("estado") EstadoEvento estado
    );

    @Query("""
            SELECT e FROM Evento e
            WHERE e.estado = :estado
              AND (:texto IS NULL OR LOWER(e.titulo) LIKE LOWER(CONCAT('%', :texto, '%'))
                   OR LOWER(e.descripcion) LIKE LOWER(CONCAT('%', :texto, '%')))
              AND (:categoriaId IS NULL OR e.categoria.id = :categoriaId)
              AND (:tipo IS NULL OR e.tipoInscripcion = :tipo)
              AND (:modalidad IS NULL OR e.modalidad = :modalidad)
            """)
    List<Evento> buscarPublicados(
            @Param("estado") EstadoEvento estado,
            @Param("texto") String texto,
            @Param("categoriaId") UUID categoriaId,
            @Param("tipo") TipoInscripcion tipo,
            @Param("modalidad") Modalidad modalidad
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Evento e WHERE e.id = :id")
    Optional<Evento> findByIdForUpdate(@Param("id") UUID id);
}
