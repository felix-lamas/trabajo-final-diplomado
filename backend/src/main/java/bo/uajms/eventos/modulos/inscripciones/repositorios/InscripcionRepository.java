package bo.uajms.eventos.modulos.inscripciones.repositorios;

import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import bo.uajms.eventos.modulos.inscripciones.entidades.EstadoInscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, UUID> {
    boolean existsByUsuarioIdAndEventoId(UUID usuarioId, UUID eventoId);
    List<Inscripcion> findByUsuarioId(UUID usuarioId);
    List<Inscripcion> findByEventoId(UUID eventoId);
    Optional<Inscripcion> findByIdAndUsuarioId(UUID id, UUID usuarioId);
    Optional<Inscripcion> findByIdAndEventoOrganizadorId(UUID id, UUID organizadorId);
    Optional<Inscripcion> findByUsuarioIdAndEventoId(UUID usuarioId, UUID eventoId);
    Optional<Inscripcion> findByUsuarioIdAndEventoIdAndEstado(UUID usuarioId, UUID eventoId, EstadoInscripcion estado);
    Optional<Inscripcion> findByCodigoParticipante(String codigoParticipante);
    Optional<Inscripcion> findByCodigoParticipanteAndEventoOrganizadorId(String codigoParticipante, UUID organizadorId);
    Optional<Inscripcion> findByUsuarioCiAndEventoId(String ci, UUID eventoId);
    Optional<Inscripcion> findByUsuarioCi(String ci);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inscripcion i WHERE i.id = :id AND i.usuario.id = :usuarioId")
    Optional<Inscripcion> findByIdAndUsuarioForUpdate(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId);
}
