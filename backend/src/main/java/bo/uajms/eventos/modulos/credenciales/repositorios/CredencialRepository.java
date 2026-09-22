package bo.uajms.eventos.modulos.credenciales.repositorios;

import bo.uajms.eventos.modulos.credenciales.entidades.Credencial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CredencialRepository extends JpaRepository<Credencial, UUID> {
    Optional<Credencial> findByInscripcionId(UUID inscripcionId);
    Optional<Credencial> findByIdAndUsuarioId(UUID id, UUID usuarioId);
    Optional<Credencial> findByIdAndEventoOrganizadorId(UUID id, UUID organizadorId);
    List<Credencial> findByUsuarioId(UUID usuarioId);
    boolean existsByInscripcionId(UUID inscripcionId);
}
