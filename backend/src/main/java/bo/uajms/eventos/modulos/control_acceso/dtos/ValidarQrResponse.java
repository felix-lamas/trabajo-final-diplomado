package bo.uajms.eventos.modulos.control_acceso.dtos;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidarQrResponse {
    private UUID credencialId;
    private UUID inscripcionId;
    private String fotografiaUrl;
    private String nombreCompleto;
    private String documentoIdentidad;
    private String carrera;
    private String facultad;
    private String evento;
    private String estadoPago;
    private String estadoInscripcion;
    private String estadoQr;
    private String codigoParticipante;
    private boolean puedeIngresar;
    private String mensajeValidacion;
}
