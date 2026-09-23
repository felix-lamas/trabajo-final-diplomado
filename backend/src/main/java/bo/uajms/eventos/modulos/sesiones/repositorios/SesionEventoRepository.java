package bo.uajms.eventos.modulos.sesiones.repositorios;

import bo.uajms.eventos.modulos.sesiones.entidades.SesionEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SesionEventoRepository extends JpaRepository<SesionEvento, UUID> {
    List<SesionEvento> findByEventoIdOrderByFechaAscHoraInicioAsc(UUID eventoId);
    Optional<SesionEvento> findByIdAndEventoOrganizadorId(UUID id, UUID organizadorId);
    long countByEventoIdAndRequiereAsistenciaTrueAndActivaTrue(UUID eventoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SesionEvento s WHERE s.id = :id")
    Optional<SesionEvento> findByIdForUpdate(@Param("id") UUID id);
}
