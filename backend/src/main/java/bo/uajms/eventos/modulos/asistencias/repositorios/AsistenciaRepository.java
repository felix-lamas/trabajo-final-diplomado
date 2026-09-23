package bo.uajms.eventos.modulos.asistencias.repositorios;

import bo.uajms.eventos.modulos.asistencias.entidades.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT COUNT(DISTINCT a.sesionEvento.id) FROM Asistencia a " +
            "WHERE a.inscripcion.id = :inscripcionId " +
            "AND a.sesionEvento.evento.id = :eventoId " +
            "AND a.sesionEvento.requiereAsistencia = true " +
            "AND a.resultadoValidacion = bo.uajms.eventos.modulos.asistencias.entidades.Asistencia.ResultadoValidacion.VALIDADA")
    long countSesionesRequeridasAsistidas(@Param("inscripcionId") UUID inscripcionId,
                                           @Param("eventoId") UUID eventoId);
}
