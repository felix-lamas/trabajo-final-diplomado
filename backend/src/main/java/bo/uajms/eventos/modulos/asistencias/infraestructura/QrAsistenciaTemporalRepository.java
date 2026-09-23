package bo.uajms.eventos.modulos.asistencias.infraestructura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QrAsistenciaTemporalRepository extends JpaRepository<QrAsistenciaTemporal, UUID> {
    Optional<QrAsistenciaTemporal> findByTokenHash(String tokenHash);
    Optional<QrAsistenciaTemporal> findFirstBySesionEventoIdAndActivoTrueOrderByEmitidoEnDesc(UUID sesionId);
    List<QrAsistenciaTemporal> findBySesionEventoIdAndActivoTrue(UUID sesionId);
}
