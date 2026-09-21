package bo.uajms.eventos.modulos.eventos.repositorios;

import bo.uajms.eventos.modulos.eventos.entidades.EstadoEvento;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventoRepository extends JpaRepository<Evento, UUID> {
    List<Evento> findByEstado(EstadoEvento estado);
    List<Evento> findByOrganizadorId(UUID organizadorId);
    List<Evento> findByOrganizadorIdOrEstado(UUID organizadorId, EstadoEvento estado);
    List<Evento> findByCategoriaId(UUID categoriaId);
    List<Evento> findByCategoriaIdAndEstado(UUID categoriaId, EstadoEvento estado);
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
}
