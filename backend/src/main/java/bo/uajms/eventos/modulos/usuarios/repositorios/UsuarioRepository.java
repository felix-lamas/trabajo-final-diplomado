package bo.uajms.eventos.modulos.usuarios.repositorios;

import bo.uajms.eventos.modulos.usuarios.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByCorreoElectronico(String correoElectronico);
    Optional<Usuario> findByCorreoElectronicoIgnoreCase(String correoElectronico);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where lower(u.correoElectronico) = lower(:correo)")
    Optional<Usuario> findByCorreoElectronicoIgnoreCaseForUpdate(@Param("correo") String correo);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.id = :id")
    Optional<Usuario> findByIdForUpdate(@Param("id") UUID id);
    boolean existsByCorreoElectronico(String correoElectronico);
    boolean existsByCorreoElectronicoIgnoreCase(String correoElectronico);
    boolean existsByCi(String ci);
    boolean existsByRu(String ru);
    List<Usuario> findByEstadoSolicitudOrganizadorOrderByFechaSolicitudOrganizadorAsc(
            Usuario.EstadoSolicitudOrganizador estado);
}
