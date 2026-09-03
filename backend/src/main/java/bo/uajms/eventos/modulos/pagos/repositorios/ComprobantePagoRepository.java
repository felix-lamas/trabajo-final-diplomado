package bo.uajms.eventos.modulos.pagos.repositorios;

import bo.uajms.eventos.modulos.pagos.entidades.ComprobantePago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ComprobantePagoRepository extends JpaRepository<ComprobantePago, UUID> {
}
