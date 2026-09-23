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
    List<Certificado> findByEventoId(UUID eventoId);
    List<Certificado> findByEventoIdAndEventoOrganizadorId(UUID eventoId, UUID organizadorId);
    List<Certificado> findByEventoOrganizadorId(UUID organizadorId);
    long countByEventoOrganizadorId(UUID organizadorId);
    Optional<Certificado> findByIdAndUsuarioId(UUID id, UUID usuarioId);
    Optional<Certificado> findByIdAndEventoOrganizadorId(UUID id, UUID organizadorId);
    boolean existsByInscripcionId(UUID inscripcionId);
    boolean existsByCodigoCertificado(String codigoCertificado);
}
