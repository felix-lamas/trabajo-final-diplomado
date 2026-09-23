package bo.uajms.eventos.modulos.asistencias.repositorios;

import bo.uajms.eventos.modulos.asistencias.entidades.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, UUID> {
    List<Asistencia> findByInscripcionId(UUID inscripcionId);
    List<Asistencia> findByInscripcionUsuarioId(UUID usuarioId);
    List<Asistencia> findBySesionEventoEventoId(UUID eventoId);
    boolean existsByInscripcionIdAndSesionEventoId(UUID inscripcionId, UUID sesionEventoId);
    boolean existsByInscripcionIdAndFechaEliminacionIsNull(UUID inscripcionId);
    long countBySesionEventoEventoOrganizadorId(UUID organizadorId);
}
