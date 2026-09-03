package bo.uajms.eventos.modulos.codigo_qr.mappers;

import bo.uajms.eventos.modulos.codigo_qr.dtos.CodigoQrResponse;
import bo.uajms.eventos.modulos.codigo_qr.entidades.CodigoQr;
import org.springframework.stereotype.Component;

@Component
public class CodigoQrMapper {

    public CodigoQrResponse toResponse(CodigoQr codigoQr) {
        if (codigoQr == null) return null;

        return CodigoQrResponse.builder()
                .id(codigoQr.getId())
                .credencialId(codigoQr.getCredencial().getId())
                .contenido(codigoQr.getContenido())
                .fechaGeneracion(codigoQr.getFechaGeneracion())
                .activo(codigoQr.getActivo())
                .build();
    }
}
