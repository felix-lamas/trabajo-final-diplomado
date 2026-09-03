package bo.uajms.eventos.modulos.categorias.repositorios;

import bo.uajms.eventos.modulos.categorias.entidades.CategoriaEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoriaEventoRepository extends JpaRepository<CategoriaEvento, UUID> {
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, UUID id);
    List<CategoriaEvento> findByEstado(String estado);
}
