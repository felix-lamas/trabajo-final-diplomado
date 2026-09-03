package bo.uajms.eventos.modulos.usuarios.repositorios;

import bo.uajms.eventos.modulos.usuarios.entidades.TokenRecuperacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, UUID> {
    Optional<TokenRecuperacion> findByToken(String token);
    boolean existsByToken(String token);
    List<TokenRecuperacion> findByUsuarioIdAndUtilizadoFalse(UUID usuarioId);
}
