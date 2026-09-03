package bo.uajms.eventos.modulos.facultades.repositorios;

import bo.uajms.eventos.modulos.facultades.entidades.Facultad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FacultadRepository extends JpaRepository<Facultad, UUID> {
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, UUID id);
    Optional<Facultad> findByNombreIgnoreCase(String nombre);
}
