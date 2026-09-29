package bo.uajms.eventos.modulos.usuarios.repositorios;

import bo.uajms.eventos.modulos.usuarios.entidades.SesionUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SesionUsuarioRepository extends JpaRepository<SesionUsuario, UUID> {
    List<SesionUsuario> findByUsuarioIdAndFechaRevocacionIsNull(UUID usuarioId);
}
