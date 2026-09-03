package bo.uajms.eventos.modulos.encuestas.repositorios;

import bo.uajms.eventos.modulos.encuestas.entidades.PreguntaEncuesta;
import bo.uajms.eventos.modulos.encuestas.entidades.TipoPreguntaEncuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PreguntaEncuestaRepository extends JpaRepository<PreguntaEncuesta, UUID> {
    Optional<PreguntaEncuesta> findFirstByTipoAndActivaTrueOrderByOrdenAsc(TipoPreguntaEncuesta tipo);
    List<PreguntaEncuesta> findByActivaTrueOrderByOrdenAsc();
}
