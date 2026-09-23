package bo.uajms.eventos.modulos.asistencias.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class QrAsistenciaResponse {
    private UUID id;
    private UUID sesionId;
    private String token;
    private LocalDateTime emitidoEn;
    private LocalDateTime expiraEn;
    private Boolean activo;
    private LocalDateTime revocadoEn;
}
