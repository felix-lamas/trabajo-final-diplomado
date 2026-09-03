package bo.uajms.eventos.modulos.certificados.dtos;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificadoResponse {
    private UUID id;
    private String nombreCompleto;
    private String ci;
    private String evento;
    private Integer cargaHoraria;
    private String codigoCertificado;
    private LocalDateTime fechaEmision;
    private String urlVerificacion;
    private String estado;
    private String archivoPdfUrl;
}
