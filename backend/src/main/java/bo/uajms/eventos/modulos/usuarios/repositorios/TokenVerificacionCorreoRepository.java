package bo.uajms.eventos.modulos.usuarios.repositorios;

import bo.uajms.eventos.modulos.usuarios.entidades.TokenVerificacionCorreo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenVerificacionCorreoRepository extends JpaRepository<TokenVerificacionCorreo, UUID> {
    boolean existsByTokenHash(String tokenHash);

    List<TokenVerificacionCorreo> findByUsuarioIdAndUtilizadoFalse(UUID usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TokenVerificacionCorreo t join fetch t.usuario where t.tokenHash = :tokenHash")
    Optional<TokenVerificacionCorreo> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);
}
