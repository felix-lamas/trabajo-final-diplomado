package bo.uajms.eventos.modulos.pagos.repositorios;

import bo.uajms.eventos.modulos.pagos.entidades.EstadoPago;
import bo.uajms.eventos.modulos.pagos.entidades.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PagoRepository extends JpaRepository<Pago, UUID> {
    
    Optional<Pago> findByInscripcionId(UUID inscripcionId);
    
    List<Pago> findByEstado(EstadoPago estado);
    
    @Query("SELECT p FROM Pago p JOIN p.inscripcion i WHERE i.usuario.id = :usuarioId")
    List<Pago> findByUsuarioId(UUID usuarioId);
}
