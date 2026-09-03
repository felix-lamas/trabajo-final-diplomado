package bo.uajms.eventos.modulos.control_acceso.repositorios;

import bo.uajms.eventos.modulos.control_acceso.entidades.ControlAcceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ControlAccesoRepository extends JpaRepository<ControlAcceso, UUID> {
    List<ControlAcceso> findByCredencialIdOrderByFechaHoraIngresoDesc(UUID credencialId);
    List<ControlAcceso> findByCredencialInscripcionEventoId(UUID eventoId);
}
