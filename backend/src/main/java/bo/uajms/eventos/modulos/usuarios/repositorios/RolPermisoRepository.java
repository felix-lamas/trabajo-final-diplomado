package bo.uajms.eventos.modulos.usuarios.repositorios;

import bo.uajms.eventos.modulos.usuarios.entidades.RolPermiso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface RolPermisoRepository extends JpaRepository<RolPermiso, UUID> {
    List<RolPermiso> findByRolId(UUID rolId);
}
