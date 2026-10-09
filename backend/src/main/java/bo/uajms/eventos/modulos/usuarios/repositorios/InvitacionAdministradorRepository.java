package bo.uajms.eventos.modulos.usuarios.repositorios;

import bo.uajms.eventos.modulos.usuarios.entidades.InvitacionAdministrador;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface InvitacionAdministradorRepository extends JpaRepository<InvitacionAdministrador, UUID> {
    List<InvitacionAdministrador> findAllByOrderByFechaCreacionDesc();
    Optional<InvitacionAdministrador> findByTokenHash(String hash);
    boolean existsByTokenHashAndEstadoAndFechaExpiracionAfter(String hash, InvitacionAdministrador.Estado estado, java.time.LocalDateTime desde);
    List<InvitacionAdministrador> findByEstado(InvitacionAdministrador.Estado estado);
    long countByInvitanteIdAndFechaUltimoEnvioAfter(UUID invitanteId, java.time.LocalDateTime desde);
    List<InvitacionAdministrador> findByEstadoAndCorreo(InvitacionAdministrador.Estado estado, String correo);
    boolean existsByEstadoAndCi(InvitacionAdministrador.Estado estado, String ci);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InvitacionAdministrador i where i.id = :id")
    Optional<InvitacionAdministrador> findByIdForUpdate(@Param("id") UUID id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InvitacionAdministrador i where i.tokenHash = :hash")
    Optional<InvitacionAdministrador> findByTokenHashForUpdate(@Param("hash") String hash);
}
