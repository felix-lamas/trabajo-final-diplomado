package bo.uajms.eventos.modulos.inscripciones.repositorios;

import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, UUID> {
    boolean existsByUsuarioIdAndEventoId(UUID usuarioId, UUID eventoId);
    List<Inscripcion> findByUsuarioId(UUID usuarioId);
    List<Inscripcion> findByEventoId(UUID eventoId);
    Optional<Inscripcion> findByUsuarioIdAndEventoId(UUID usuarioId, UUID eventoId);
    Optional<Inscripcion> findByCodigoParticipante(String codigoParticipante);
    Optional<Inscripcion> findByUsuarioCiAndEventoId(String ci, UUID eventoId);
    Optional<Inscripcion> findByUsuarioCi(String ci);
}
