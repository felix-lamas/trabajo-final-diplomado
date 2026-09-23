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
                .requiereInscripcion(evento.getRequiereInscripcion())
                .cupoLimitado(evento.getCupoLimitado())
                .emiteCertificado(evento.getEmiteCertificado())
                .tipoCertificado(evento.getTipoCertificado())
                .horasAcademicas(evento.getHorasAcademicas())
                .publicoObjetivo(evento.getPublicoObjetivo())
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
                .direccion(evento.getDireccion())
                .latitud(evento.getLatitud())
                .longitud(evento.getLongitud())
                .radioMetros(evento.getRadioMetros())
                .enlaceVirtual(evento.getEnlaceVirtual())
                .requiereInscripcion(evento.getRequiereInscripcion())
                .cupoLimitado(evento.getCupoLimitado())
                .cupoMaximo(evento.getCupoMaximo())
                .cupoDisponible(evento.getCupoDisponible())
                .estado(evento.getEstado())
                .imagenPortada(evento.getImagenPortada())
                .emiteCertificado(evento.getEmiteCertificado())
                .tipoCertificado(evento.getTipoCertificado())
                .horasAcademicas(evento.getHorasAcademicas())
                .publicoObjetivo(evento.getPublicoObjetivo())
                .telefonoContacto(evento.getTelefonoContacto())
                .emailContacto(evento.getEmailContacto())
                .whatsappContacto(evento.getWhatsappContacto())
                .qrPagoUrl(evento.getQrPagoUrl())
                .instruccionesPago(evento.getInstruccionesPago())
                .motivoRechazo(evento.getMotivoRechazo())
                .motivoCancelacion(evento.getMotivoCancelacion())
                .organizadorId(evento.getOrganizador().getId())
                .organizadorNombre(evento.getOrganizador().getNombres() + " " + evento.getOrganizador().getApellidos())
                .build();
    }
}
