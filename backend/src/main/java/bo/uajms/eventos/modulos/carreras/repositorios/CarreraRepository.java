package bo.uajms.eventos.modulos.carreras.repositorios;

import bo.uajms.eventos.modulos.carreras.entidades.Carrera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CarreraRepository extends JpaRepository<Carrera, UUID> {
    List<Carrera> findByFacultadId(UUID facultadId);
    boolean existsByNombreIgnoreCaseAndFacultadId(String nombre, UUID facultadId);
    boolean existsByNombreIgnoreCaseAndFacultadIdAndIdNot(String nombre, UUID facultadId, UUID id);
}
