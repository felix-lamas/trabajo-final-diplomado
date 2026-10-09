package bo.uajms.eventos.modulos.usuarios.repositorios;

import bo.uajms.eventos.modulos.usuarios.entidades.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SolicitudOrganizadorHistorialRepository extends JpaRepository<SolicitudOrganizadorHistorial, UUID> {
    List<SolicitudOrganizadorHistorial> findByEstado(Usuario.EstadoSolicitudOrganizador estado);
}
