package bo.uajms.eventos.modulos.eventos.repositorios;

import bo.uajms.eventos.modulos.eventos.entidades.EstadoEvento;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventoRepository extends JpaRepository<Evento, UUID> {
    List<Evento> findByEstado(EstadoEvento estado);
    List<Evento> findByOrganizadorId(UUID organizadorId);
    List<Evento> findByCategoriaId(UUID categoriaId);
}
