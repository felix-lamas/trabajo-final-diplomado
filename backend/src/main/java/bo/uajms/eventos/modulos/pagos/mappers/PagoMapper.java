package bo.uajms.eventos.modulos.pagos.mappers;

import bo.uajms.eventos.modulos.pagos.dtos.ComprobantePagoResponse;
import bo.uajms.eventos.modulos.pagos.dtos.PagoResponse;
import bo.uajms.eventos.modulos.pagos.entidades.Pago;
import org.springframework.stereotype.Component;

@Component
public class PagoMapper {

    public PagoResponse toResponse(Pago pago) {
        if (pago == null) return null;

        return PagoResponse.builder()
                .id(pago.getId())
                .inscripcionId(pago.getInscripcion().getId())
                .eventoTitulo(pago.getInscripcion().getEvento().getTitulo())
                .usuarioNombre(pago.getInscripcion().getUsuario().getNombres() + " " + pago.getInscripcion().getUsuario().getApellidos())
                .monto(pago.getMonto())
                .fechaPago(pago.getFechaPago())
                .estado(pago.getEstado())
                .observacion(pago.getObservacion())
                .comprobante(toComprobanteResponse(pago))
                .build();
    }

    public ComprobantePagoResponse toComprobanteResponse(Pago pago) {
        if (pago.getComprobanteUrl() == null) return null;

        return ComprobantePagoResponse.builder()
                .id(pago.getId())
                .urlArchivo(pago.getComprobanteUrl())
                .nombreArchivo(pago.getComprobanteNombreArchivo())
                .tipoContenido(pago.getComprobanteTipoContenido())
                .build();
    }
}
