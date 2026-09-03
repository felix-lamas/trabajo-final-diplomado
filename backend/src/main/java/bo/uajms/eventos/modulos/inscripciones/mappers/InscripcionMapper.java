package bo.uajms.eventos.modulos.inscripciones.mappers;

import bo.uajms.eventos.modulos.inscripciones.dtos.DetalleInscripcionResponse;
import bo.uajms.eventos.modulos.inscripciones.dtos.InscripcionResponse;
import bo.uajms.eventos.modulos.inscripciones.entidades.Inscripcion;
import org.springframework.stereotype.Component;

@Component
public class InscripcionMapper {

    public InscripcionResponse toResponse(Inscripcion inscripcion) {
        if (inscripcion == null) return null;
        
        return InscripcionResponse.builder()
                .id(inscripcion.getId())
                .eventoId(inscripcion.getEvento().getId())
                .eventoTitulo(inscripcion.getEvento().getTitulo())
                .fechaInscripcion(inscripcion.getFechaInscripcion())
                .estado(inscripcion.getEstado())
                .build();
    }

    public DetalleInscripcionResponse toDetalleResponse(Inscripcion inscripcion) {
        if (inscripcion == null) return null;

        return DetalleInscripcionResponse.builder()
                .id(inscripcion.getId())
                .usuarioId(inscripcion.getUsuario().getId())
                .usuarioNombre(inscripcion.getUsuario().getNombres() + " " + inscripcion.getUsuario().getApellidos())
                .eventoId(inscripcion.getEvento().getId())
                .eventoTitulo(inscripcion.getEvento().getTitulo())
                .fechaInscripcion(inscripcion.getFechaInscripcion())
                .estado(inscripcion.getEstado())
                .observacion(inscripcion.getObservacion())
                .modalidalEvento(inscripcion.getEvento().getModalidad().toString())
                .ubicacionEvento(inscripcion.getEvento().getUbicacion())
                .build();
    }
}
