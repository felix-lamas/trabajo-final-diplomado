package bo.uajms.eventos.modulos.usuarios.repositorios;

import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByCorreoElectronico(String correoElectronico);
    Optional<Usuario> findByCorreoElectronicoIgnoreCase(String correoElectronico);
    boolean existsByCorreoElectronico(String correoElectronico);
    boolean existsByCorreoElectronicoIgnoreCase(String correoElectronico);
    boolean existsByCi(String ci);
    boolean existsByRu(String ru);
    List<Usuario> findByEstadoSolicitudOrganizadorOrderByFechaSolicitudOrganizadorAsc(
            Usuario.EstadoSolicitudOrganizador estado);
}
