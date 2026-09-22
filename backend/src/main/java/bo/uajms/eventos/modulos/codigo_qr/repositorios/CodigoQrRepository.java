package bo.uajms.eventos.modulos.codigo_qr.repositorios;

import bo.uajms.eventos.modulos.codigo_qr.entidades.CodigoQr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CodigoQrRepository extends JpaRepository<CodigoQr, UUID> {
    Optional<CodigoQr> findByCredencialId(UUID credencialId);
    Optional<CodigoQr> findByCredencialIdAndCredencialUsuarioId(UUID credencialId, UUID usuarioId);
    Optional<CodigoQr> findByCredencialIdAndCredencialEventoOrganizadorId(UUID credencialId, UUID organizadorId);
    Optional<CodigoQr> findByContenido(String contenido);
    Optional<CodigoQr> findByContenidoAndCredencialInscripcionEventoOrganizadorId(String contenido, UUID organizadorId);
    long countByEstadoQrAndCredencialEventoOrganizadorId(CodigoQr.EstadoQr estadoQr, UUID organizadorId);
}
