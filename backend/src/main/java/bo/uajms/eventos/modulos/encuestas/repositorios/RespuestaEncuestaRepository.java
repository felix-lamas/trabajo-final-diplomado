package bo.uajms.eventos.modulos.encuestas.repositorios;

import bo.uajms.eventos.modulos.encuestas.entidades.RespuestaEncuesta;
import bo.uajms.eventos.modulos.encuestas.entidades.TipoPreguntaEncuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RespuestaEncuestaRepository extends JpaRepository<RespuestaEncuesta, UUID> {

    @Query("""
            select avg(r.calificacion)
            from RespuestaEncuesta r
            where r.encuesta.evento.id = :eventoId
              and r.pregunta.tipo = :tipo
              and r.calificacion is not null
            """)
    Double promedioCalificacionPorEvento(@Param("eventoId") UUID eventoId, @Param("tipo") TipoPreguntaEncuesta tipo);

    @Query("""
            select r
            from RespuestaEncuesta r
            where r.encuesta.evento.id = :eventoId
              and r.pregunta.tipo = :tipo
              and r.comentario is not null
              and trim(r.comentario) <> ''
            order by r.fechaCreacion desc
            """)
    List<RespuestaEncuesta> comentariosPorEvento(@Param("eventoId") UUID eventoId, @Param("tipo") TipoPreguntaEncuesta tipo);
}
