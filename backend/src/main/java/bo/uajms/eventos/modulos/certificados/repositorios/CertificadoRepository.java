package bo.uajms.eventos.modulos.certificados.repositorios;

import bo.uajms.eventos.modulos.certificados.entidades.Certificado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CertificadoRepository extends JpaRepository<Certificado, UUID> {
    Optional<Certificado> findByInscripcionId(UUID inscripcionId);
    Optional<Certificado> findByCodigoCertificado(String codigoCertificado);
    List<Certificado> findByUsuarioId(UUID usuarioId);
    boolean existsByInscripcionId(UUID inscripcionId);
}
