package bo.uajms.eventos.modulos.usuarios.repositorios;

import bo.uajms.eventos.modulos.usuarios.entidades.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface RolRepository extends JpaRepository<Rol, UUID> {
    Optional<Rol> findByNombre(String nombre);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select r from Rol r where r.nombre = :nombre")
    Optional<Rol> findByNombreForUpdate(@org.springframework.data.repository.query.Param("nombre") String nombre);
}
