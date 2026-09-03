package bo.uajms.eventos.modulos.facultades.mappers;

import bo.uajms.eventos.modulos.facultades.dtos.FacultadResponse;
import bo.uajms.eventos.modulos.facultades.entidades.Facultad;
import org.springframework.stereotype.Component;

@Component
public class FacultadMapper {

    public FacultadResponse toResponse(Facultad facultad) {
        if (facultad == null) return null;
        
        return FacultadResponse.builder()
                .id(facultad.getId())
                .nombre(facultad.getNombre())
                .descripcion(facultad.getDescripcion())
                .estado(facultad.getEstado())
                .cantidadCarreras(facultad.getCarreras() != null ? (long) facultad.getCarreras().size() : 0L)
                .build();
    }
}
