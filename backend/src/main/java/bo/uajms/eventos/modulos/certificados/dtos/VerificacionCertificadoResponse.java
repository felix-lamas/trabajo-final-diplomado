package bo.uajms.eventos.modulos.certificados.dtos;

import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificacionCertificadoResponse {
    private boolean valido;
    private String mensaje;
    private String nombreCompleto;
    private String ci;
    private String evento;
    private Integer cargaHoraria;
    private String tipoCertificado;
    private Integer horasAcademicas;
    private BigDecimal porcentajeAsistencia;
    private LocalDateTime fechaEmision;
    private String codigoCertificado;
    private String estado;
}
