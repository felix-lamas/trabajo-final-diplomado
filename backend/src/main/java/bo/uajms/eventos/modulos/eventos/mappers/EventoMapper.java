package bo.uajms.eventos.modulos.eventos.mappers;

import bo.uajms.eventos.modulos.eventos.dtos.EventoDetalleResponse;
import bo.uajms.eventos.modulos.eventos.dtos.EventoResponse;
import bo.uajms.eventos.modulos.eventos.entidades.Evento;
import org.springframework.stereotype.Component;

@Component
public class EventoMapper {

    public EventoResponse toResponse(Evento evento) {
        if (evento == null) return null;
        
        return EventoResponse.builder()
                .id(evento.getId())
                .titulo(evento.getTitulo())
                .descripcion(evento.getDescripcion())
                .objetivos(evento.getObjetivos())
                .categoriaId(evento.getCategoria().getId())
                .categoriaNombre(evento.getCategoria().getNombre())
                .modalidad(evento.getModalidad())
                .tipoInscripcion(evento.getTipoInscripcion())
                .fechaInicio(evento.getFechaInicio())
                .fechaFin(evento.getFechaFin())
                .horaInicio(evento.getHoraInicio())
                .horaFin(evento.getHoraFin())
                .costo(evento.getCosto())
                .cupoMaximo(evento.getCupoMaximo())
                .cupoDisponible(evento.getCupoDisponible())
                .estado(evento.getEstado())
                .imagenPortada(evento.getImagenPortada())
                .organizadorId(evento.getOrganizador().getId())
                .organizadorNombre(evento.getOrganizador().getNombres() + " " + evento.getOrganizador().getApellidos())
                .build();
    }

    public EventoDetalleResponse toDetalleResponse(Evento evento) {
        if (evento == null) return null;

        return EventoDetalleResponse.builder()
                .id(evento.getId())
                .titulo(evento.getTitulo())
                .descripcion(evento.getDescripcion())
                .objetivos(evento.getObjetivos())
                .categoriaId(evento.getCategoria().getId())
                .categoriaNombre(evento.getCategoria().getNombre())
                .modalidad(evento.getModalidad())
                .tipoInscripcion(evento.getTipoInscripcion())
                .costo(evento.getCosto())
                .fechaInicio(evento.getFechaInicio())
                .fechaFin(evento.getFechaFin())
                .horaInicio(evento.getHoraInicio())
                .horaFin(evento.getHoraFin())
                .ubicacion(evento.getUbicacion())
                .enlaceVirtual(evento.getEnlaceVirtual())
                .cupoMaximo(evento.getCupoMaximo())
                .cupoDisponible(evento.getCupoDisponible())
                .estado(evento.getEstado())
                .imagenPortada(evento.getImagenPortada())
                .organizadorId(evento.getOrganizador().getId())
                .organizadorNombre(evento.getOrganizador().getNombres() + " " + evento.getOrganizador().getApellidos())
                .build();
    }
}
