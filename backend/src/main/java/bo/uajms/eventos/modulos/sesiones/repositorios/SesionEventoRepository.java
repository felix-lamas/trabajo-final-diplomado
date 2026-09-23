package bo.uajms.eventos.modulos.sesiones.repositorios;

import bo.uajms.eventos.modulos.sesiones.entidades.SesionEvento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SesionEventoRepository extends JpaRepository<SesionEvento, UUID> {
    List<SesionEvento> findByEventoIdOrderByFechaAscHoraInicioAsc(UUID eventoId);
    Optional<SesionEvento> findByIdAndEventoOrganizadorId(UUID id, UUID organizadorId);
    long countByEventoIdAndRequiereAsistenciaTrueAndActivaTrue(UUID eventoId);
}
