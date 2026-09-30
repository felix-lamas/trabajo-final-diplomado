package bo.uajms.eventos.modulos.asistencias.infraestructura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QrAsistenciaTemporalRepository extends JpaRepository<QrAsistenciaTemporal, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM QrAsistenciaTemporal q WHERE q.tokenHash = :tokenHash")
    Optional<QrAsistenciaTemporal> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);
    Optional<QrAsistenciaTemporal> findFirstBySesionEventoIdAndActivoTrueOrderByEmitidoEnDesc(UUID sesionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM QrAsistenciaTemporal q WHERE q.sesionEvento.id = :sesionId AND q.activo = true")
    List<QrAsistenciaTemporal> findActivosBySesionIdForUpdate(@Param("sesionId") UUID sesionId);
}
