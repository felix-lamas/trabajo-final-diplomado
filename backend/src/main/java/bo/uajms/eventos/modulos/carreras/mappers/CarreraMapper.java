package bo.uajms.eventos.modulos.carreras.mappers;

import bo.uajms.eventos.modulos.carreras.dtos.CarreraResponse;
import bo.uajms.eventos.modulos.carreras.entidades.Carrera;
import org.springframework.stereotype.Component;

@Component
public class CarreraMapper {

    public CarreraResponse toResponse(Carrera carrera) {
        if (carrera == null) return null;
        
        return CarreraResponse.builder()
                .id(carrera.getId())
                .nombre(carrera.getNombre())
                .descripcion(carrera.getDescripcion())
                .estado(carrera.getEstado())
                .facultadId(carrera.getFacultad() != null ? carrera.getFacultad().getId() : null)
                .facultadNombre(carrera.getFacultad() != null ? carrera.getFacultad().getNombre() : null)
                .build();
    }
}
