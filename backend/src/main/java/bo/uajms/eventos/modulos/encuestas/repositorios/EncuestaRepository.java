package bo.uajms.eventos.modulos.encuestas.repositorios;

import bo.uajms.eventos.modulos.encuestas.entidades.Encuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EncuestaRepository extends JpaRepository<Encuesta, UUID> {
    boolean existsByEventoIdAndUsuarioId(UUID eventoId, UUID usuarioId);
    long countByEventoId(UUID eventoId);
    List<Encuesta> findByEventoId(UUID eventoId);
    Optional<Encuesta> findByEventoIdAndUsuarioId(UUID eventoId, UUID usuarioId);
}
